package com.echarge.modules.alert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 告警记录（v3.0 精简版：只读，无状态流转）
 */
@Data
@TableName("nc_alert")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "告警记录")
public class NcAlert implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "ID")
    private String id;

    @Schema(description = "告警设备序列号（N3 Lite 的 SN）")
    private String deviceSn;

    @Schema(description = "枪号，0=设备本体，null=设备级别")
    private Integer connectorId;

    @Schema(description = "级别：CRITICAL / IMPORTANT / NORMAL / INFO")
    private String alertLevel;

    @Schema(description = "OCPP错误码")
    private String errorCode;

    @Schema(description = "厂商自定义错误码")
    private String vendorErrorCode;

    @Schema(description = "告警描述")
    private String description;

    @Schema(description = "告警触发时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date alertTime;

    @Schema(description = "创建时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date createTime;
}
