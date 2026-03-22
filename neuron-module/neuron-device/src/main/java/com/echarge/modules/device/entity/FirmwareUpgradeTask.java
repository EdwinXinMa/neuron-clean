package com.echarge.modules.device.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.echarge.common.system.base.entity.NeuronEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("firmware_upgrade_task")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "固件升级任务")
public class FirmwareUpgradeTask extends NeuronEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "固件ID")
    private String firmwareId;

    @Schema(description = "设备SN")
    private String deviceSn;

    @Schema(description = "状态: PENDING/DOWNLOADING/INSTALLING/COMPLETED/FAILED")
    private String status;

    @Schema(description = "进度百分比")
    private Integer progress;

    @Schema(description = "错误信息")
    private String errorMsg;

    @Schema(description = "开始时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @Date2026-03-22
    private Date startTime;

    @Schema(description = "完成时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @Date2026-03-22
    private Date finishTime;
}
