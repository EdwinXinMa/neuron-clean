package com.echarge.modules.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.echarge.common.system.base.entity.NeuronEntity;

import java.io.Serial;
import java.io.Serializable;

@Data
@TableName("alarm_record")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "告警记录")
public class AlarmRecord extends NeuronEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "设备ID")
    private String deviceId;

    @Schema(description = "告警类型")
    private String alarmType;

    @Schema(description = "告警级别: INFO, WARNING, ERROR, CRITICAL")
    private String alarmLevel;

    @Schema(description = "告警内容")
    private String content;

    @Schema(description = "告警状态: ACTIVE, ACKNOWLEDGED, RESOLVED")
    private String status;

    @Schema(description = "告警时间")
    private java.util.Date alarmTime;

    @Schema(description = "解决时间")
    private java.util.Date resolveTime;
}
