package com.echarge.modules.alert.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.echarge.modules.alert.entity.NcAlert;

/**
 * 告警 Service（v3.0 精简版：只读，只插入，无状态流转）
 * @author Edwin
 */
public interface INcAlertService extends IService<NcAlert> {

    /**
     * 记录告警（StatusNotification 故障时调用）
     */
    void recordAlert(String deviceSn, Integer connectorId, String errorCode, String vendorErrorCode, String description);

    /**
     * 查询最近 24h 严重+重要告警数量（导航角标用）
     */
    long countRecentCritical();
}
