package com.echarge.modules.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.echarge.common.constant.BizConstant;
import com.echarge.common.exception.NeuronBootException;
import com.echarge.modules.device.entity.FirmwareLatest;
import com.echarge.modules.device.entity.FirmwareVersion;
import com.echarge.modules.device.mapper.FirmwareLatestMapper;
import com.echarge.modules.device.mapper.FirmwareVersionMapper;
import com.echarge.modules.device.service.IFirmwareVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author Edwin
 */
@Service
public class FirmwareVersionServiceImpl extends ServiceImpl<FirmwareVersionMapper, FirmwareVersion>
        implements IFirmwareVersionService {

    @Autowired
    private FirmwareLatestMapper firmwareLatestMapper;

    /** {@inheritDoc} */
    @Override
    public void release(String id) {
        FirmwareVersion fw = getById(id);
        if (fw == null) {
            throw new NeuronBootException("固件不存在");
        }
        if (!BizConstant.FIRMWARE_DRAFT.equals(fw.getStatus())) {
            throw new NeuronBootException("只有草稿状态的固件才能发布");
        }
        fw.setStatus(BizConstant.FIRMWARE_RELEASED);
        updateById(fw);

        // 更新最新版本记录
        updateLatest(fw);
    }

    /** {@inheritDoc} */
    @Override
    public void deprecate(String id) {
        FirmwareVersion fw = getById(id);
        if (fw == null) {
            throw new NeuronBootException("固件不存在");
        }
        if (!BizConstant.FIRMWARE_RELEASED.equals(fw.getStatus())) {
            throw new NeuronBootException("只有已发布状态的固件才能废弃");
        }

        // 最新发布版本不能废弃
        FirmwareLatest latest = getLatest(fw.getDeviceType());
        if (latest != null && fw.getId().equals(latest.getLatestFirmwareId())) {
            throw new NeuronBootException("最新发布版本不能废弃，请先发布新版本");
        }

        fw.setStatus(BizConstant.FIRMWARE_DEPRECATED);
        updateById(fw);
    }

    /** {@inheritDoc} */
    @Override
    public void checkUploadVersion(String version, String deviceType) {
        // 1. 查重：同版本号 + 同设备类型不能重复（不论状态）
        long count = count(new LambdaQueryWrapper<FirmwareVersion>()
                .eq(FirmwareVersion::getVersion, version)
                .eq(FirmwareVersion::getDeviceType, deviceType));
        if (count > 0) {
            throw new NeuronBootException("版本 " + version + " 已存在，不能重复上传");
        }

        // 2. 版本号不能倒退（跟所有已存在的版本比较，包括草稿）
        List<FirmwareVersion> allVersions = list(new LambdaQueryWrapper<FirmwareVersion>()
                .eq(FirmwareVersion::getDeviceType, deviceType)
                .select(FirmwareVersion::getVersion));
        for (FirmwareVersion existing : allVersions) {
            if (compareVersion(version, existing.getVersion()) <= 0) {
                throw new NeuronBootException("版本号不能小于或等于已有版本 " + existing.getVersion());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public FirmwareLatest getLatest(String deviceType) {
        return firmwareLatestMapper.selectOne(
                new LambdaQueryWrapper<FirmwareLatest>()
                        .eq(FirmwareLatest::getDeviceType, deviceType));
    }

    /**
     * 发布时更新最新版本记录
     */
    private void updateLatest(FirmwareVersion fw) {
        FirmwareLatest latest = getLatest(fw.getDeviceType());
        String logEntry = fw.getVersion() + " - " + fw.getReleaseNotes();

        if (latest == null) {
            latest = new FirmwareLatest()
                    .setDeviceType(fw.getDeviceType())
                    .setLatestVersion(fw.getVersion())
                    .setLatestFirmwareId(fw.getId())
                    .setReleaseNotes(fw.getReleaseNotes())
                    .setLatestUploadTime(new Date())
                    .setVersionLog(logEntry);
            firmwareLatestMapper.insert(latest);
        } else {
            latest.setPreviousVersion(latest.getLatestVersion())
                    .setLatestVersion(fw.getVersion())
                    .setLatestFirmwareId(fw.getId())
                    .setReleaseNotes(fw.getReleaseNotes())
                    .setLatestUploadTime(new Date());
            // 追加版本日志
            String existingLog = latest.getVersionLog();
            latest.setVersionLog(existingLog != null ? logEntry + "\n" + existingLog : logEntry);
            firmwareLatestMapper.updateById(latest);
        }
    }

    /**
     * 语义化版本比较（支持 v 前缀和数字段）
     * @return 正数: v1 > v2, 0: 相等, 负数: v1 < v2
     */
    public static int compareVersion(String v1, String v2) {
        String[] parts1 = normalizeVersion(v1).split("\\.");
        String[] parts2 = normalizeVersion(v2).split("\\.");
        int maxLen = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < maxLen; i++) {
            int n1 = i < parts1.length ? parseSegment(parts1[i]) : 0;
            int n2 = i < parts2.length ? parseSegment(parts2[i]) : 0;
            if (n1 != n2) {
                return n1 - n2;
            }
        }
        return 0;
    }

    private static String normalizeVersion(String v) {
        if (v == null) {
            return "0";
        }
        // 去掉 v/V 前缀
        String s = v.trim();
        if (s.startsWith("v") || s.startsWith("V")) {
            s = s.substring(1);
        }
        return s;
    }

    private static int parseSegment(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
