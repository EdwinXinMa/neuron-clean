package com.echarge.modules.device.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.echarge.common.system.base.entity.NeuronEntity;

import java.io.Serial;
import java.io.Serializable;

@Data
@TableName("device_config")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "设备配置")
public class DeviceConfig extends NeuronEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "设备ID")
    private String deviceId;

    @Schema(description = "配置项Key")
    private String configKey;

    @Schema(description = "配置项Value")
    private String configValue;

    @Schema(description = "备注")
    private String remark;
}
