package com.echarge.modules.device.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.echarge.modules.device.entity.NcDlmHistory;

import java.util.Map;

/**
 * @author Edwin
 */
public interface INcDlmHistoryService extends IService<NcDlmHistory> {

    /**
     * 保存 DLM 上报数据到时序表（主表 + 分配表）
     * @param deviceSn 设备序列号
     * @param payload  上报数据JSON字符串
     */
    void saveDlmReport(String deviceSn, String payload);

    /**
     * 查询 DLM 历史图表数据
     * @param deviceSn 设备序列号
     * @param range 时间范围: 1h, 6h, 24h, 7d
     * @return 图表数据（包含 deviceSn, range, bucketSeconds, points）
     */
    Map<String, Object> getChartData(String deviceSn, String range);
}
