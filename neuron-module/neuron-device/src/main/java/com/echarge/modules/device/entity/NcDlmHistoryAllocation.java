package com.echarge.modules.device.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * DLM 子桩电流分配时序数据
 * @author Edwin
 */
@Data
@TableName("nc_dlm_history_allocation")
@Accessors(chain = true)
@Schema(description = "DLM 子桩电流分配时序数据")
public class NcDlmHistoryAllocation implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "上报时间")
    private Date time;

    @Schema(description = "N3 Lite 设备序列号")
    private String deviceSn;

    @Schema(description = "子桩序列号")
    private String pileSn;

    @Schema(description = "分配电流 A 相 (A)，单相设备只有此字段，B/C 为 0")
    private Float allocatedCurrentA;

    @Schema(description = "分配电流 B 相 (A)，单相时为 0")
    private Float allocatedCurrentB;

    @Schema(description = "分配电流 C 相 (A)，单相时为 0")
    private Float allocatedCurrentC;
}
