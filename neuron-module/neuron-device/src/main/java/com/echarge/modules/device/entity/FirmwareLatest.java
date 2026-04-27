package com.echarge.modules.device.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 固件最新版本维护表（每个设备类型一条记录）
 * @author Edwin
 */
@Data
@TableName("nc_firmware_latest")
@Accessors(chain = true)
@Schema(description = "固件最新版本")
public class FirmwareLatest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "ID")
    private String id;

    @Schema(description = "设备类型")
    private String deviceType;

    @Schema(description = "最新版本号")
    private String latestVersion;

    @Schema(description = "最新固件ID")
    private String latestFirmwareId;

    @Schema(description = "上一个版本号")
    private String previousVersion;

    @Schema(description = "最新固件上传时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date latestUploadTime;

    @Schema(description = "最新版本说明")
    private String releaseNotes;

    @Schema(description = "版本历史日志")
    private String versionLog;
}
