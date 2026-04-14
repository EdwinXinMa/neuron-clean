package com.echarge.modules.app.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echarge.common.constant.BizConstant;
import com.echarge.common.ocpp.OcppCommandSender;
import com.echarge.common.util.MinioUtil;
import com.echarge.modules.app.entity.AppFirmware;
import com.echarge.modules.app.entity.AppUser;
import com.echarge.modules.app.entity.AppUserDevice;
import com.echarge.modules.app.mapper.AppFirmwareMapper;
import com.echarge.modules.app.mapper.AppUserDeviceMapper;
import com.echarge.modules.app.vo.AppResult;
import com.echarge.modules.device.websocket.AppOtaWebSocket;
import com.echarge.modules.device.entity.FirmwareUpgradeTask;
import com.echarge.modules.device.entity.NcDevice;
import com.echarge.modules.device.service.IFirmwareUpgradeTaskService;
import com.echarge.modules.device.service.INcDeviceService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

/**
 * App OTA 固件升级
 * @author Edwin
 */
@Slf4j
@RestController
@RequestMapping("/app/firmware")
@Tag(name = "App OTA 升级")
public class AppFirmwareController {

    @Autowired
    private AppFirmwareMapper appFirmwareMapper;

    @Autowired
    private AppUserDeviceMapper userDeviceMapper;

    @Autowired
    private INcDeviceService deviceService;

    @Autowired
    private IFirmwareUpgradeTaskService upgradeTaskService;

    @Autowired
    private OcppCommandSender ocppCommandSender;

    /**
     * 上传固件并发起升级（一步到位）
     */
    @PostMapping("/ota")
    @Operation(summary = "上传固件并发起 OTA 升级")
    public AppResult<?> ota(@RequestParam("file") MultipartFile file,
                            @RequestParam String deviceSn,
                            HttpServletRequest request) {

        AppUser user = (AppUser) request.getAttribute("appUser");

        // 校验参数
        if (file == null || file.isEmpty()) {
            return AppResult.error("固件文件不能为空");
        }
        if (deviceSn == null || deviceSn.isBlank()) {
            return AppResult.error("设备序列号不能为空");
        }

        // 校验设备归属
        long bound = userDeviceMapper.selectCount(
                new LambdaQueryWrapper<AppUserDevice>()
                        .eq(AppUserDevice::getUserId, user.getId())
                        .eq(AppUserDevice::getDeviceSn, deviceSn));
        if (bound == 0) {
            return AppResult.error(403, "该设备未绑定到当前用户");
        }

        // 校验设备在线
        NcDevice device = deviceService.getOne(
                new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, deviceSn));
        if (device == null || !BizConstant.DEVICE_ONLINE.equals(device.getOnlineStatus())) {
            return AppResult.error("设备不在线，无法升级");
        }

        // 校验 OCPP 连接
        if (!ocppCommandSender.isDeviceConnected(deviceSn)) {
            return AppResult.error("设备 OCPP 连接不存在，无法下发升级指令");
        }

        // 上传固件到 MinIO
        String fileUrl;
        String checksum;
        try {
            // 计算 MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(file.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            checksum = sb.toString();

            // 上传到独立路径
            String bizPath = "firmware/app/" + user.getId();
            fileUrl = MinioUtil.upload(file, bizPath);
        } catch (Exception e) {
            log.error("[AppOTA] 文件上传失败", e);
            return AppResult.error("文件上传失败: " + e.getMessage());
        }

        // 保存固件记录
        AppFirmware firmware = new AppFirmware();
        firmware.setUserId(user.getId());
        firmware.setDeviceSn(deviceSn);
        firmware.setFileUrl(fileUrl);
        firmware.setFileName(file.getOriginalFilename());
        firmware.setFileSize(file.getSize());
        firmware.setChecksum(checksum);
        firmware.setCreateTime(new Date());
        appFirmwareMapper.insert(firmware);

        // 生成 presigned URL
        String downloadUrl;
        try {
            String bucketName = MinioUtil.getBucketName();
            String objectName = MinioUtil.extractObjectName(fileUrl, bucketName);
            downloadUrl = MinioUtil.getObjectUrl(bucketName, objectName, 3600);
        } catch (Exception e) {
            log.error("[AppOTA] 生成下载链接失败", e);
            return AppResult.error("生成固件下载链接失败: " + e.getMessage());
        }

        // 创建升级任务
        FirmwareUpgradeTask task = new FirmwareUpgradeTask();
        task.setFirmwareId(firmware.getId());
        task.setDeviceSn(deviceSn);
        task.setStatus(BizConstant.TASK_PENDING);
        task.setProgress(0);
        task.setStartTime(new Date());
        upgradeTaskService.save(task);

        String taskId = task.getId();

        // OCPP UpdateFirmware 下发
        try {
            sendUpdateFirmware(deviceSn, downloadUrl, taskId);
        } catch (Exception e) {
            log.error("[AppOTA] 下发升级指令失败, taskId={}", taskId, e);
            task.setStatus(BizConstant.TASK_FAILED);
            task.setErrorMsg("下发指令失败: " + e.getMessage());
            task.setFinishTime(new Date());
            upgradeTaskService.updateById(task);
            return AppResult.error("下发升级指令失败: " + e.getMessage());
        }

        // 推送初始状态到 App WebSocket
        JSONObject wsMsg = new JSONObject();
        wsMsg.put("taskId", taskId);
        wsMsg.put("deviceSn", deviceSn);
        wsMsg.put("status", BizConstant.TASK_PENDING);
        wsMsg.put("progress", 0);
        wsMsg.put("message", "升级指令已下发，等待设备响应");
        AppOtaWebSocket.sendMessage(taskId, wsMsg.toJSONString());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("taskId", taskId);
        data.put("deviceSn", deviceSn);
        data.put("status", BizConstant.TASK_PENDING);
        return AppResult.ok("升级任务已创建", data);
    }

    /**
     * 查询升级进度（兜底轮询接口）
     */
    @GetMapping("/progress")
    @Operation(summary = "查询 OTA 升级进度")
    public AppResult<?> progress(@RequestParam String taskId, HttpServletRequest request) {
        AppUser user = (AppUser) request.getAttribute("appUser");

        FirmwareUpgradeTask task = upgradeTaskService.getById(taskId);
        if (task == null) {
            return AppResult.error("任务不存在");
        }

        // 校验设备归属
        long bound = userDeviceMapper.selectCount(
                new LambdaQueryWrapper<AppUserDevice>()
                        .eq(AppUserDevice::getUserId, user.getId())
                        .eq(AppUserDevice::getDeviceSn, task.getDeviceSn()));
        if (bound == 0) {
            return AppResult.error(403, "无权查看该任务");
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("taskId", task.getId());
        data.put("deviceSn", task.getDeviceSn());
        data.put("status", task.getStatus());
        data.put("progress", task.getProgress());
        data.put("message", getStatusMessage(task.getStatus(), task.getErrorMsg()));
        data.put("startTime", task.getStartTime() != null ? String.valueOf(task.getStartTime().getTime() / 1000) : null);
        data.put("finishTime", task.getFinishTime() != null ? String.valueOf(task.getFinishTime().getTime() / 1000) : null);

        return AppResult.ok(data);
    }

    private void sendUpdateFirmware(String deviceSn, String downloadUrl, String taskId) {
        String messageId = "ota-" + UUID.randomUUID().toString().substring(0, 8);

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

        log.info("[AppOTA] Sending UpdateFirmware to {}: messageId={}", deviceSn, messageId);
        ocppCommandSender.sendCall(deviceSn, call.toString());
    }

    private String getStatusMessage(String status, String errorMsg) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case "PENDING" -> "升级指令已下发，等待设备响应";
            case "DOWNLOADING" -> "设备正在下载固件";
            case "INSTALLING" -> "设备正在安装固件";
            case "COMPLETED" -> "固件升级完成";
            case "FAILED" -> errorMsg != null ? errorMsg : "升级失败";
            default -> "";
        };
    }
}
