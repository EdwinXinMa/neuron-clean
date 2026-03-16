package com.echarge.modules.device.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.echarge.common.system.base.entity.NeuronEntity;

import java.io.Serial;
import java.io.Serializable;

@Data
@TableName("device")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "设备")
public class Device extends NeuronEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "设备序列号")
    private String sn;

    @Schema(description = "设备名称")
    private String name;

    @Schema(description = "设备类型: N3, N3_LITE, ATP_III")
    private String deviceType;

    @Schema(description = "固件版本")
    private String firmwareVersion;

    @Schema(description = "所属站点ID")
    private String stationId;

    @Schema(description = "在线状态: ONLINE, OFFLINE")
    private String onlineStatus;

    @Schema(description = "MQTT客户端ID")
    private String mqttClientId;

    @Schema(description = "最后心跳时间")
    private java.util.Date lastHeartbeat;

    @Schema(description = "备注")
    private String remark;
}
