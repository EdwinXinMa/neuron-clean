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
@TableName("telemetry_snapshot")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "遥测快照")
public class TelemetrySnapshot extends NeuronEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "设备ID")
    private String deviceId;

    @Schema(description = "电压(V)")
    private BigDecimal voltage;

    @Schema(description = "电流(A)")
    private BigDecimal current;

    @Schema(description = "功率(kW)")
    private BigDecimal power;

    @Schema(description = "电量(kWh)")
    private BigDecimal energy;

    @Schema(description = "温度(°C)")
    private BigDecimal temperature;

    @Schema(description = "采集时间")
    private java.util.Date collectTime;
}
