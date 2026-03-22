package com.echarge.modules.device.entity;

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
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("nc_connector")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "充电枪/接口")
public class NcConnector implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "ID")
    private String id;

    @Schema(description = "所属桩ID（关联nc_device）")
    private String deviceId;

    @Schema(description = "OCPP connectorId")
    private Integer connectorId;

    @Schema(description = "枪状态: Available/Charging/Faulted/Unavailable等")
    private String status;

    @Schema(description = "当前充电功率(kW)")
    private BigDecimal currentPower;

    @Schema(description = "当前充电电量(kWh)")
    private BigDecimal currentEnergy;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "创建时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date createTime;

    @Schema(description = "更新人")
    private String updateBy;

    @Schema(description = "更新时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date updateTime;
}
