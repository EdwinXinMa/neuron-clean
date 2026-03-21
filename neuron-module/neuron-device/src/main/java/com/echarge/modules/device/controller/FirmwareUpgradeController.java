package com.echarge.modules.device.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.echarge.common.api.vo.Result;
import com.echarge.common.exception.NeuronBootException;
import com.echarge.modules.device.entity.FirmwareUpgradeTask;
import com.echarge.modules.device.entity.FirmwareVersion;
import com.echarge.modules.device.entity.NcDevice;
import com.echarge.modules.device.service.IFirmwareUpgradeTaskService;
import com.echarge.modules.device.service.IFirmwareVersionService;
import com.echarge.modules.device.service.INcDeviceService;
import com.echarge.modules.device.websocket.OtaWebSocket;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

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
        if (!"RELEASED".equals(firmware.getStatus())) {
            throw new NeuronBootException("固件状态不是已发布，无法升级");
        }

        // 校验设备
        NcDevice device = ncDeviceService.getOne(
                new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, deviceSn)
        );
        if (device == null) {
            throw new NeuronBootException("设备不存在: " + deviceSn);
        }
        if (!"ONLINE".equals(device.getOnlineStatus())) {
            throw new NeuronBootException("设备不在线，无法升级");
        }

        // 创建任务
        FirmwareUpgradeTask task = new FirmwareUpgradeTask();
        task.setFirmwareId(firmwareId);
        task.setDeviceSn(deviceSn);
        task.setStatus("PENDING");
        task.setProgress(0);
        task.setStartTime(new Date());
        upgradeTaskService.save(task);

        String taskId = task.getId();

        // 异步模拟升级流程
        new Thread(() -> simulateUpgrade(taskId, deviceSn)).start();

        return Result.OK("升级任务已创建", taskId);
    }

    @Operation(summary = "查询任务详情")
    @GetMapping("/task/{taskId}")
    public Result<FirmwareUpgradeTask> task(@PathVariable String taskId) {
        FirmwareUpgradeTask task = upgradeTaskService.getById(taskId);
        if (task == null) {
            return Result.error("任务不存在");
        }
        return Result.OK(task);
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
        return Result.OK(page);
    }

    private void simulateUpgrade(String taskId, String deviceSn) {
        try {
            // DOWNLOADING 阶段
            sleep(1000);
            updateAndPush(taskId, deviceSn, "DOWNLOADING", 0, "开始下载固件");
            for (int p = 20; p <= 100; p += 20) {
                sleep(500);
                updateAndPush(taskId, deviceSn, "DOWNLOADING", p, "下载中");
            }

            // INSTALLING 阶段
            updateAndPush(taskId, deviceSn, "INSTALLING", 0, "开始安装固件");
            for (int p = 25; p <= 100; p += 25) {
                sleep(500);
                updateAndPush(taskId, deviceSn, "INSTALLING", p, "安装中");
            }

            // COMPLETED
            FirmwareUpgradeTask task = upgradeTaskService.getById(taskId);
            if (task != null) {
                task.setStatus("COMPLETED");
                task.setProgress(100);
                task.setFinishTime(new Date());
                upgradeTaskService.updateById(task);
            }
            pushMessage(taskId, deviceSn, "COMPLETED", 100, "升级完成");

        } catch (Exception e) {
            log.error("OTA升级模拟异常, taskId={}", taskId, e);
            FirmwareUpgradeTask task = upgradeTaskService.getById(taskId);
            if (task != null) {
                task.setStatus("FAILED");
                task.setErrorMsg(e.getMessage());
                task.setFinishTime(new Date());
                upgradeTaskService.updateById(task);
            }
            pushMessage(taskId, deviceSn, "FAILED", 0, "升级失败: " + e.getMessage());
        }
    }

    private void updateAndPush(String taskId, String deviceSn, String status, int progress, String message) {
        FirmwareUpgradeTask task = upgradeTaskService.getById(taskId);
        if (task != null) {
            task.setStatus(status);
            task.setProgress(progress);
            upgradeTaskService.updateById(task);
        }
        pushMessage(taskId, deviceSn, status, progress, message);
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

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
