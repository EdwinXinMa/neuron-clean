package com.echarge.modules.alert.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.echarge.modules.alert.entity.NcAlert;
import com.echarge.modules.alert.mapper.NcAlertMapper;
import com.echarge.modules.alert.service.INcAlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * 告警 Service 实现（v3.0 精简版：只读，只插入，无状态流转）
 * @author Edwin
 */
@Slf4j
@Service
public class NcAlertServiceImpl extends ServiceImpl<NcAlertMapper, NcAlert> implements INcAlertService {

    /**
     * OCPP 错误码 → 告警级别映射
     * CRITICAL: 需立即感知（接地故障、过温）
     * IMPORTANT: 当日关注（过流、过压、欠压、内部错误）
     * NORMAL: 记录跟踪（锁枪失败、其他错误）
     */
    private static final Map<String, String> ERROR_LEVEL_MAP = Map.of(
            "GroundFailure",        "CRITICAL",
            "HighTemperature",      "CRITICAL",
            "OverCurrentFailure",   "IMPORTANT",
            "OverVoltage",          "IMPORTANT",
            "UnderVoltage",         "IMPORTANT",
            "PowerSwitchFailure",   "IMPORTANT",
            "InternalError",        "IMPORTANT",
            "ConnectorLockFailure", "NORMAL",
            "OtherError",           "NORMAL"
    );

    @Override
    public void recordAlert(String deviceSn, Integer connectorId, String errorCode, String vendorErrorCode, String description) {
        NcAlert alert = new NcAlert();
        alert.setDeviceSn(deviceSn);
        alert.setConnectorId(connectorId);
        alert.setAlertLevel(ERROR_LEVEL_MAP.getOrDefault(errorCode, "NORMAL"));
        alert.setErrorCode(errorCode);
        alert.setVendorErrorCode(vendorErrorCode);
        alert.setDescription(description);
        alert.setAlertTime(new Date());
        alert.setCreateTime(new Date());
        this.save(alert);
        log.info("[Alert] Recorded: deviceSn={}, connectorId={}, level={}, errorCode={}",
                deviceSn, connectorId, alert.getAlertLevel(), errorCode);
    }

    @Override
    public long countRecentCritical() {
        // 最近 24 小时的 CRITICAL + IMPORTANT 告警数量（导航角标用）
        Date since = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L);
        return this.count(new LambdaQueryWrapper<NcAlert>()
                .in(NcAlert::getAlertLevel, "CRITICAL", "IMPORTANT")
                .ge(NcAlert::getAlertTime, since));
    }
}
