package com.echarge.modules.device.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.echarge.common.system.base.entity.NeuronEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("nc_device")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "设备台账")
public class NcDevice extends NeuronEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Excel(name = "设备序列号", width = 20)
    @Schema(description = "设备序列号")
    private String sn;

    @Excel(name = "设备类型", width = 15)
    @Schema(description = "设备类型: N3_LITE / ATP_III")
    private String deviceType;

    @Excel(name = "具体型号", width = 15)
    @Schema(description = "具体型号")
    private String deviceModel;

    @Excel(name = "生产批次号", width = 15)
    @Schema(description = "生产批次号")
    private String batchNo;

    @Excel(name = "经销商/渠道", width = 20)
    @Schema(description = "经销商/渠道")
    private String dealer;

    @Excel(name = "生产日期", width = 15, format = "yyyy-MM-dd")
    @Schema(description = "生产日期")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date productionDate;

    @Excel(name = "出货日期", width = 15, format = "yyyy-MM-dd")
    @Schema(description = "出货日期")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date shipDate;

    @Excel(name = "硬件版本号", width = 15)
    @Schema(description = "硬件版本号")
    private String hardwareVersion;

    @Excel(name = "MAC地址", width = 20)
    @Schema(description = "MAC地址")
    private String macAddress;

    @Excel(name = "固件版本", width = 15)
    @Schema(description = "当前固件版本")
    private String firmwareVersion;

    @Excel(name = "在线状态", width = 12)
    @Schema(description = "在线状态: UNACTIVATED / ONLINE / OFFLINE / FAULT")
    private String onlineStatus;

    @Schema(description = "最后心跳时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastHeartbeat;

    @Schema(description = "首次上线时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date firstOnlineTime;

    @Schema(description = "最近上线时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastOnlineTime;

    @Schema(description = "设备IP地址")
    private String ipAddress;

    @Schema(description = "父设备ID（ATP III指向N3 Lite）")
    private String parentDeviceId;

    @Excel(name = "状态", width = 10)
    @Schema(description = "业务状态: NORMAL / DISABLED")
    private String status;

    @TableLogic
    @Schema(description = "删除标记: 0正常 1已删除")
    private Integer delFlag = 0;
}
