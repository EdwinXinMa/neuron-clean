package com.echarge.modules.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.echarge.modules.device.entity.NcDlmHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Edwin
 */
@Mapper
public interface NcDlmHistoryMapper extends BaseMapper<NcDlmHistory> {

    /**
     * 查询 DLM 历史图表数据（TimescaleDB time_bucket 聚合）
     * @param deviceSn 设备序列号
     * @param bucketInterval 聚合间隔（如 '1 minute', '15 minutes', '1 hour'）
     * @param since 起始时间
     * @return 聚合后的数据点列表
     */
    List<Map<String, Object>> queryChartData(@Param("deviceSn") String deviceSn,
                                              @Param("bucketInterval") String bucketInterval,
                                              @Param("since") Date since);
}
