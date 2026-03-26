package com.echarge.modules.device.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.echarge.modules.device.entity.NcDlmHistory;

/**
 * @author Edwin
 */
public interface INcDlmHistoryService extends IService<NcDlmHistory> {

    /**
     * 保存 DLM 上报数据到时序表（主表 + 分配表）
     */
    void saveDlmReport(String deviceSn, String payload);
}
