package com.echarge.modules.device.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.echarge.common.api.vo.Result;
import com.echarge.common.constant.BizConstant;
import com.echarge.common.exception.NeuronBootException;
import com.echarge.common.util.MinioUtil;
import com.echarge.modules.device.entity.FirmwareUpgradeTask;
import com.echarge.modules.device.entity.FirmwareVersion;
import com.echarge.modules.device.entity.NcDevice;
import com.echarge.modules.device.entity.NcOpLog;
import com.echarge.modules.device.service.IFirmwareUpgradeTaskService;
import com.echarge.modules.device.service.IFirmwareVersionService;
import com.echarge.modules.device.service.INcDeviceService;
import com.echarge.modules.device.service.INcOpLogService;
import com.echarge.modules.device.websocket.OtaWebSocket;
import com.echarge.common.ocpp.OcppCommandSender;
import com.echarge.common.system.vo.LoginUser;
import org.apache.shiro.SecurityUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * @author Edwin
 */
@Slf4j
@Tag(name = "固件升级")
@RestController
@RequestMapping("/firmware/upgrade")
public class FirmwareUpgradeController {

    @Autowired
    private IFirmwareUpgradeTaskService upgradeTaskService;

    @Autowired
    private IFirmwareVersionService firmwareVersionService;

    @Autowired
    private INcDeviceService ncDeviceService;

    @Autowired
    private OcppCommandSender ocppCommandSender;

    @Autowired
    private INcOpLogService opLogService;

    @Operation(summary = "发起升级")
    @PostMapping("/start")
    public Result<String> start(@RequestBody JSONObject params) {
        String firmwareId = params.getString("firmwareId");
        String deviceSn = params.getString("deviceSn");

        if (StringUtils.isBlank(firmwareId) || StringUtils.isBlank(deviceSn)) {
            return Result.error("firmwareId 和 deviceSn 不能为空");
        }

        // 校验固件
        FirmwareVersion firmware = firmwareVersionService.getById(firmwareId);
        if (firmware == null) {
            throw new NeuronBootException("固件不存在");
        }
        if (!BizConstant.FIRMWARE_RELEASED.equals(firmware.getStatus())) {
            throw new NeuronBootException("固件状态不是已发布，无法升级");
        }

        // 校验设备
        NcDevice device = ncDeviceService.getOne(
                new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, deviceSn)
        );
        if (device == null) {
            throw new NeuronBootException("设备不存在: " + deviceSn);
        }
        if (!BizConstant.DEVICE_ONLINE.equals(device.getOnlineStatus())) {
            throw new NeuronBootException("设备不在线，无法升级");
        }

        // 校验 OCPP 会话
        if (!ocppCommandSender.isDeviceConnected(deviceSn)) {
            throw new NeuronBootException("设备 OCPP 连接不存在，无法下发升级指令");
        }

        // 生成固件下载 URL（MinIO presigned URL，1小时有效）
        String downloadUrl;
        try {
            String bucketName = MinioUtil.getBucketName();
            String objectName = MinioUtil.extractObjectName(firmware.getFileUrl(), bucketName);
            downloadUrl = MinioUtil.getObjectUrl(bucketName, objectName, 3600);
        } catch (Exception e) {
            log.error("生成固件下载链接失败", e);
            throw new NeuronBootException("生成固件下载链接失败: " + e.getMessage());
        }

        // 创建任务
        FirmwareUpgradeTask task = new FirmwareUpgradeTask();
        task.setFirmwareId(firmwareId);
        task.setDeviceSn(deviceSn);
        task.setStatus(BizConstant.TASK_PENDING);
        task.setProgress(0);
        task.setStartTime(new Date());
        upgradeTaskService.save(task);

        String taskId = task.getId();

        // 获取当前操作人
        String opUser = "system";
        try {
            LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (loginUser != null) {
                opUser = loginUser.getUsername();
            }
        } catch (Exception ignored) {}

        String opContent = device.getFirmwareVersion() + " → " + firmware.getVersion();

        // 通过 OCPP 1.6 下发 UpdateFirmware 命令
        try {
            sendUpdateFirmware(deviceSn, downloadUrl, taskId);
        } catch (Exception e) {
            log.error("下发 UpdateFirmware 失败, taskId={}", taskId, e);
            task.setStatus(BizConstant.TASK_FAILED);
            task.setErrorMsg("下发指令失败: " + e.getMessage());
            task.setFinishTime(new Date());
            upgradeTaskService.updateById(task);

            // 记录失败日志
            saveOpLog(deviceSn, opUser, NcOpLog.OTA_UPGRADE, opContent, NcOpLog.FAIL, "下发指令失败: " + e.getMessage());

            throw new NeuronBootException("下发升级指令失败: " + e.getMessage());
        }

        // 记录发起日志（结果待设备回报后由 DeviceEventHandler 更新）
        saveOpLog(deviceSn, opUser, NcOpLog.OTA_UPGRADE, opContent, NcOpLog.SUCCESS, null);

        // 推送初始状态到前端
        pushMessage(taskId, deviceSn, BizConstant.TASK_PENDING, 0, "升级指令已下发，等待设备响应");

        return Result.ok("升级任务已创建", taskId);
    }

    @Operation(summary = "查询任务详情")
    @GetMapping("/task/{taskId}")
    public Result<FirmwareUpgradeTask> task(@PathVariable String taskId) {
        FirmwareUpgradeTask task = upgradeTaskService.getById(taskId);
        if (task == null) {
            return Result.error("任务不存在");
        }
        return Result.ok(task);
    }

    @Operation(summary = "升级历史")
    @GetMapping("/list")
    public Result<IPage<FirmwareUpgradeTask>> list(
            @RequestParam(required = false) String deviceSn,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        LambdaQueryWrapper<FirmwareUpgradeTask> query = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(deviceSn)) {
            query.eq(FirmwareUpgradeTask::getDeviceSn, deviceSn);
        }
        query.orderByDesc(FirmwareUpgradeTask::getCreateTime);

        IPage<FirmwareUpgradeTask> page = upgradeTaskService.page(new Page<>(pageNo, pageSize), query);
        return Result.ok(page);
    }

    /**
     * 通过 OCPP 1.6 发送 UpdateFirmware CALL 到设备
     * OCPP 1.6 UpdateFirmware.req: { location: string, retrieveDate: string, retries?: int, retryInterval?: int }
     */
    private void sendUpdateFirmware(String deviceSn, String downloadUrl, String taskId) {
        String messageId = "ota-" + UUID.randomUUID().toString().substring(0, 8);

        // 构建 OCPP CALL: [2, messageId, "UpdateFirmware", { location, retrieveDate }]
        JsonObject payload = new JsonObject();
        payload.addProperty("location", downloadUrl);
        payload.addProperty("retrieveDate", Instant.now().toString());
        payload.addProperty("retries", 1);
        payload.addProperty("retryInterval", 30);

        JsonArray call = new JsonArray();
        call.add(2);
        call.add(messageId);
        call.add("UpdateFirmware");
        call.add(payload);

        String message = call.toString();
        log.info("[OTA] Sending UpdateFirmware to {}: messageId={}, url={}", deviceSn, messageId, downloadUrl);
        ocppCommandSender.sendCall(deviceSn, message);
    }

    private void saveOpLog(String deviceSn, String opUser, String opType, String opContent, String opResult, String failReason) {
        NcOpLog opLog = new NcOpLog();
        opLog.setDeviceSn(deviceSn);
        opLog.setOpUser(opUser);
        opLog.setOpType(opType);
        opLog.setOpContent(opContent);
        opLog.setOpResult(opResult);
        opLog.setFailReason(failReason);
        opLog.setOpTime(new Date());
        opLog.setCreateTime(new Date());
        opLogService.save(opLog);
    }

    private void pushMessage(String taskId, String deviceSn, String status, int progress, String message) {
        JSONObject msg = new JSONObject();
        msg.put("taskId", taskId);
        msg.put("deviceSn", deviceSn);
        msg.put("status", status);
        msg.put("progress", progress);
        msg.put("message", message);
        OtaWebSocket.sendMessage(deviceSn, msg.toJSONString());
    }
}
