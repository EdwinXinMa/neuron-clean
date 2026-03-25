package com.echarge.modules.device.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.echarge.common.api.vo.Result;
import com.echarge.common.exception.NeuronBootException;
import com.echarge.common.util.MinioUtil;
import com.echarge.modules.device.entity.FirmwareVersion;
import com.echarge.modules.device.service.IFirmwareVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Tag(name = "固件版本管理")
@RestController
@RequestMapping("/firmware")
public class FirmwareVersionController {

    @Autowired
    private IFirmwareVersionService firmwareVersionService;

    @Operation(summary = "固件分页列表")
    @GetMapping("/list")
    public Result<IPage<FirmwareVersion>> list(
            @RequestParam(required = false) String version,
            @RequestParam(required = false) String deviceType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        LambdaQueryWrapper<FirmwareVersion> query = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(version)) {
            query.like(FirmwareVersion::getVersion, version);
        }
        if (StringUtils.isNotBlank(deviceType)) {
            query.eq(FirmwareVersion::getDeviceType, deviceType);
        }
        if (StringUtils.isNotBlank(status)) {
            query.eq(FirmwareVersion::getStatus, status.toUpperCase());
        }
        query.orderByDesc(FirmwareVersion::getCreateTime);

        IPage<FirmwareVersion> page = firmwareVersionService.page(new Page<>(pageNo, pageSize), query);
        return Result.OK(page);
    }

    @Operation(summary = "上传固件文件")
    @PostMapping("/upload")
    public Result<?> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam String version,
            @RequestParam(defaultValue = "N3_LITE") String deviceType,
            @RequestParam(required = false) String releaseNotes) {

        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            long fileSize = file.getSize();

            // calculate MD5
            byte[] fileBytes = file.getBytes();
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(fileBytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            String checksum = sb.toString();

            // upload to MinIO
            String bizPath = "firmware/" + deviceType + "/" + version;
            String fileUrl = MinioUtil.upload(file, bizPath);

            // save record
            FirmwareVersion fw = new FirmwareVersion();
            fw.setVersion(version);
            fw.setDeviceType(deviceType);
            fw.setFileUrl(fileUrl);
            fw.setFileName(originalFilename);
            fw.setFileSize(fileSize);
            fw.setChecksum(checksum);
            fw.setReleaseNotes(releaseNotes);
            fw.setStatus("DRAFT");
            firmwareVersionService.save(fw);

            return Result.OK("上传成功", fw);
        } catch (Exception e) {
            log.error("固件上传失败", e);
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "发布固件")
    @PutMapping("/release")
    public Result<?> release(@RequestParam String id) {
        try {
            firmwareVersionService.release(id);
            return Result.OK("发布成功");
        } catch (NeuronBootException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "废弃固件")
    @PutMapping("/deprecate")
    public Result<?> deprecate(@RequestParam String id) {
        try {
            firmwareVersionService.deprecate(id);
            return Result.OK("废弃成功");
        } catch (NeuronBootException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "删除固件")
    @DeleteMapping("/delete")
    public Result<?> delete(@RequestParam String id) {
        FirmwareVersion fw = firmwareVersionService.getById(id);
        if (fw == null) {
            return Result.error("固件不存在");
        }
        if (!"DRAFT".equals(fw.getStatus())) {
            return Result.error("只有草稿状态的固件才能删除");
        }
        // remove file from MinIO
        try {
            String fileUrl = fw.getFileUrl();
            String bucketName = MinioUtil.getBucketName();
            String objectName;
            int bucketIdx = fileUrl.indexOf("/" + bucketName + "/");
            if (bucketIdx >= 0) {
                objectName = fileUrl.substring(bucketIdx + bucketName.length() + 2);
            } else {
                objectName = fileUrl.replace(MinioUtil.getMinioUrl() + bucketName + "/", "");
            }
            MinioUtil.removeObject(bucketName, objectName);
        } catch (Exception e) {
            log.warn("删除MinIO文件失败: {}", e.getMessage());
        }
        firmwareVersionService.removeById(id);
        return Result.OK("删除成功");
    }

    @Operation(summary = "获取固件下载链接")
    @GetMapping("/download")
    public Result<Map<String, String>> download(@RequestParam String id) {
        FirmwareVersion fw = firmwareVersionService.getById(id);
        if (fw == null) {
            return Result.error("固件不存在");
        }
        try {
            String fileUrl = fw.getFileUrl();
            String bucketName = MinioUtil.getBucketName();
            String objectName;
            int bucketIdx = fileUrl.indexOf("/" + bucketName + "/");
            if (bucketIdx >= 0) {
                objectName = fileUrl.substring(bucketIdx + bucketName.length() + 2);
            } else {
                objectName = fileUrl.replace(MinioUtil.getMinioUrl() + bucketName + "/", "");
            }
            // 1 hour = 3600 seconds
            String presignedUrl = MinioUtil.getObjectUrl(bucketName, objectName, 3600);

            Map<String, String> result = new LinkedHashMap<>();
            result.put("url", presignedUrl);
            result.put("fileName", fw.getFileName());
            return Result.OK(result);
        } catch (Exception e) {
            log.error("生成下载链接失败", e);
            return Result.error("生成下载链接失败: " + e.getMessage());
        }
    }
}
