package com.echarge.modules.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.echarge.common.system.base.entity.NeuronEntity;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@TableName("energy_statistics")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "能耗统计")
public class EnergyStatistics extends NeuronEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "站点ID")
    private String stationId;

    @Schema(description = "设备ID")
    private String deviceId;

    @Schema(description = "统计日期")
    private java.util.Date statisticsDate;

    @Schema(description = "统计类型: DAILY, MONTHLY, YEARLY")
    private String statisticsType;

    @Schema(description = "总用电量(kWh)")
    private BigDecimal totalEnergy;

    @Schema(description = "峰值功率(kW)")
    private BigDecimal peakPower;

    @Schema(description = "平均功率(kW)")
    private BigDecimal avgPower;

    @Schema(description = "充电次数")
    private Integer chargeCount;

    @Schema(description = "充电总时长(分钟)")
    private Integer totalDuration;
}
