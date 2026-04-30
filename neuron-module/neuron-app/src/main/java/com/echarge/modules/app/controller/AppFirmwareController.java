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
import com.echarge.modules.device.websocket.AppWebSocket;
import com.echarge.modules.device.entity.FirmwareLatest;
import com.echarge.modules.device.entity.FirmwareUpgradeTask;
import com.echarge.modules.device.entity.FirmwareVersion;
import com.echarge.modules.device.entity.NcDevice;
import com.echarge.modules.device.service.IFirmwareUpgradeTaskService;
import com.echarge.modules.device.service.IFirmwareVersionService;
import com.echarge.modules.device.service.INcDeviceService;
import com.echarge.modules.device.service.impl.FirmwareVersionServiceImpl;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @Autowired
    private IFirmwareVersionService firmwareVersionService;

    /**
     * 检查固件更新（免登录）
     */
    @GetMapping("/check")
    @Operation(summary = "检查固件更新（免登录）")
    public AppResult<?> checkUpdate(@RequestParam String currentVersion) {
        if (currentVersion == null || currentVersion.isBlank()) {
            return AppResult.error("currentVersion 不能为空");
        }

        FirmwareLatest latest = firmwareVersionService.getLatest(BizConstant.TYPE_N3_LITE);

        Map<String, Object> data = new LinkedHashMap<>();
        if (latest == null || latest.getLatestVersion() == null) {
            data.put("needUpdate", false);
            data.put("currentVersion", currentVersion);
            data.put("latestVersion", null);
            return AppResult.ok(data);
        }

        boolean needUpdate = FirmwareVersionServiceImpl.compareVersion(
                latest.getLatestVersion(), currentVersion) > 0;
        data.put("needUpdate", needUpdate);
        data.put("currentVersion", currentVersion);
        data.put("latestVersion", latest.getLatestVersion());
        if (needUpdate) {
            data.put("releaseNotes", latest.getReleaseNotes());
            data.put("latestUploadTime", latest.getLatestUploadTime());
        }
        return AppResult.ok(data);
    }

    /**
     * 下载最新固件（免登录，本地模式专用）
     */
    @GetMapping("/download/latest")
    @Operation(summary = "下载最新固件（免登录）")
    public AppResult<?> downloadLatest() {
        FirmwareLatest latest = firmwareVersionService.getLatest(BizConstant.TYPE_N3_LITE);
        if (latest == null || latest.getLatestFirmwareId() == null) {
            return AppResult.error(404, "暂无已发布的固件版本");
        }

        FirmwareVersion fw = firmwareVersionService.getById(latest.getLatestFirmwareId());
        if (fw == null || fw.getFileUrl() == null) {
            return AppResult.error(404, "固件文件不存在");
        }

        try {
            String bucketName = MinioUtil.getBucketName();
            String objectName = MinioUtil.extractObjectName(fw.getFileUrl(), bucketName);
            String downloadUrl = MinioUtil.getObjectUrl(bucketName, objectName, 3600);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("downloadUrl", downloadUrl);
            data.put("version", fw.getVersion());
            data.put("fileSize", fw.getFileSize());
            data.put("fileName", fw.getFileName());
            return AppResult.ok(data);
        } catch (Exception e) {
            log.error("[AppFirmware] 生成下载链接失败", e);
            return AppResult.error("生成下载链接失败: " + e.getMessage());
        }
    }

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

        // 从文件名解析版本号（格式：N3Lite-X.Y.Z.bin 或 N3Lite-X.Y.Z_XXXXX.bin）
        String originalFilename = file.getOriginalFilename();
        String fwVersion = parseFirmwareVersion(originalFilename);
        if (fwVersion == null) {
            return AppResult.error("文件名格式不正确，应为 N3Lite-X.Y.Z.bin 或 N3Lite-X.Y.Z_XXXXX.bin");
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

            // 自动重命名为 N3Lite-{version}_{yyyyMMdd}.bin
            String dateStr = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            String standardName = "N3Lite-" + fwVersion + "_" + dateStr + ".bin";
            String objectName = "firmware/app/" + user.getId() + "/" + standardName;
            fileUrl = MinioUtil.uploadWithName(file, objectName);
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
        wsMsg.put("message_en", "Upgrade command sent, waiting for device response");
        wsMsg.put("message_tw", "升級指令已下發，等待裝置回應");
        AppWebSocket.publish("ota:" + taskId, wsMsg);

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

    /** 从文件名解析版本号，如 N3Lite-1.2.3.bin → 1.2.3，N3Lite-1.2.3_abc.bin → 1.2.3 */
    private static final Pattern FIRMWARE_NAME_PATTERN = Pattern.compile("^N3Lite-(\\d+\\.\\d+\\.\\d+)[_.]");

    private String parseFirmwareVersion(String filename) {
        if (filename == null || filename.isBlank()) {
            return null;
        }
        Matcher matcher = FIRMWARE_NAME_PATTERN.matcher(filename);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
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
