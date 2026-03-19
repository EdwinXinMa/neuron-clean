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

@Slf4j
@Service
public class NcAlertServiceImpl extends ServiceImpl<NcAlertMapper, NcAlert> implements INcAlertService {

    /**
     * 错误码 → 告警级别映射
     */
    private static final Map<String, String> ERROR_LEVEL_MAP = Map.of(
            "GroundFailure", "CRITICAL",
            "HighTemperature", "CRITICAL",
            "OverCurrentFailure", "IMPORTANT",
            "OverVoltage", "IMPORTANT",
            "UnderVoltage", "IMPORTANT",
            "PowerSwitchFailure", "IMPORTANT",
            "InternalError", "IMPORTANT",
            "ConnectorLockFailure", "NORMAL",
            "OtherError", "NORMAL"
    );

    @Override
    public void triggerAlert(String deviceSn, Integer connectorId, String errorCode, String vendorErrorCode, String description) {
        // 检查是否已有同设备同枪的未处理告警（避免重复）
        LambdaQueryWrapper<NcAlert> wrapper = new LambdaQueryWrapper<NcAlert>()
                .eq(NcAlert::getDeviceSn, deviceSn)
                .eq(NcAlert::getStatus, "ACTIVE")
                .eq(NcAlert::getErrorCode, errorCode);
        if (connectorId != null) {
            wrapper.eq(NcAlert::getConnectorId, connectorId);
        }
        if (this.count(wrapper) > 0) {
            log.debug("[Alert] Duplicate alert ignored: deviceSn={}, connectorId={}, errorCode={}", deviceSn, connectorId, errorCode);
            return;
        }

        NcAlert alert = new NcAlert();
        alert.setDeviceSn(deviceSn);
        alert.setConnectorId(connectorId);
        alert.setAlertLevel(ERROR_LEVEL_MAP.getOrDefault(errorCode, "NORMAL"));
        alert.setErrorCode(errorCode);
        alert.setVendorErrorCode(vendorErrorCode);
        alert.setDescription(description);
        alert.setStatus("ACTIVE");
        alert.setAlertTime(new Date());
        alert.setCreateTime(new Date());
        this.save(alert);
        log.info("[Alert] New alert: deviceSn={}, connectorId={}, level={}, errorCode={}", deviceSn, connectorId, alert.getAlertLevel(), errorCode);
    }

    @Override
    public void resolveAlertAuto(String deviceSn, Integer connectorId) {
        LambdaQueryWrapper<NcAlert> wrapper = new LambdaQueryWrapper<NcAlert>()
                .eq(NcAlert::getDeviceSn, deviceSn)
                .eq(NcAlert::getStatus, "ACTIVE");
        if (connectorId != null) {
            wrapper.eq(NcAlert::getConnectorId, connectorId);
        }

        NcAlert update = new NcAlert();
        update.setStatus("RESOLVED");
        update.setResolveTime(new Date());
        this.update(update, wrapper);
        log.info("[Alert] Auto resolved: deviceSn={}, connectorId={}", deviceSn, connectorId);
    }

    @Override
    public void resolveAlertManual(String alertId, String resolveBy, String remark) {
        NcAlert alert = this.getById(alertId);
        if (alert == null || !"ACTIVE".equals(alert.getStatus())) {
            return;
        }
        alert.setStatus("RESOLVED");
        alert.setResolveTime(new Date());
        alert.setResolveBy(resolveBy);
        alert.setResolveRemark(remark);
        this.updateById(alert);
        log.info("[Alert] Manual resolved: alertId={}, by={}", alertId, resolveBy);
    }

    @Override
    public long countActive() {
        return this.count(new LambdaQueryWrapper<NcAlert>().eq(NcAlert::getStatus, "ACTIVE"));
    }
}
