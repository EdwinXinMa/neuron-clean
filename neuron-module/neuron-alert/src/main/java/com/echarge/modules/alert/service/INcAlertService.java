package com.echarge.modules.alert.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.echarge.modules.alert.entity.NcAlert;

public interface INcAlertService extends IService<NcAlert> {

    /**
     * 触发告警（StatusNotification 故障时调用）
     */
    void triggerAlert(String deviceSn, Integer connectorId, String errorCode, String vendorErrorCode, String description);

    /**
     * 自动恢复告警（StatusNotification 恢复正常时调用）
     */
    void resolveAlertAuto(String deviceSn, Integer connectorId);

    /**
     * 手动处理告警（运维人员操作）
     */
    void resolveAlertManual(String alertId, String resolveBy, String remark);

    /**
     * 查询未处理告警数量
     */
    long countActive();
}
