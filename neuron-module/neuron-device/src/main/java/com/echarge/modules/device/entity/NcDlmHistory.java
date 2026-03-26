package com.echarge.modules.device.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * DLM 时序数据（N3 Lite 级别）
 * @author Edwin
 */
@Data
@TableName("nc_dlm_history")
@Accessors(chain = true)
@Schema(description = "DLM 时序数据")
public class NcDlmHistory implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "上报时间")
    private Date time;

    @Schema(description = "设备序列号")
    private String deviceSn;

    @Schema(description = "总电流 (A)")
    private Float totalCurrent;

    @Schema(description = "电压 (V)")
    private Float voltage;

    @Schema(description = "总功率 (W)")
    private Float totalPower;

    @Schema(description = "设备温度 (℃)")
    private Float deviceTemp;

    @Schema(description = "WiFi 信号强度 (dBm)")
    private Short wifiRssi;

    @Schema(description = "断路器额定值 (A)")
    private Short breakerRating;
}
