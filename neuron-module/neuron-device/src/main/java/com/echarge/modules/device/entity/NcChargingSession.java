package com.echarge.modules.device.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 充电会话记录（按枪）
 * @author Edwin
 */
@Data
@TableName("nc_charging_session")
@Accessors(chain = true)
@Schema(description = "充电会话记录")
public class NcChargingSession implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @Schema(description = "N3 Lite 网关序列号")
    private String deviceSn;

    @Schema(description = "子桩序列号")
    private String pileSn;

    @Schema(description = "枪编号")
    private Integer connectorId;

    @Schema(description = "开始充电时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date startTime;

    @Schema(description = "结束充电时间（充电中为 null）")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date endTime;

    @Schema(description = "充电时长（秒）")
    private Integer duration;

    @Schema(description = "充电能量（Wh，桩级别合计）")
    private Integer energy;

    @Schema(description = "充电模式：0=iCharger / 1=N3Lite")
    private Integer chargingMethod;

    @Schema(description = "OCPP transactionId（云端生成，用于 RemoteStopTransaction）")
    private Integer transactionId;

    @Schema(description = "状态：CHARGING / FINISHED")
    private String status;

    @Schema(description = "创建时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date createTime;

    public static final String CHARGING = "CHARGING";
    public static final String FINISHED = "FINISHED";
}
