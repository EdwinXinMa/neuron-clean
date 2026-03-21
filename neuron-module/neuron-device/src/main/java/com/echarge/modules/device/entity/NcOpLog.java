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
 * 设备操作日志
 */
@Data
@TableName("nc_op_log")
@Accessors(chain = true)
@Schema(description = "设备操作日志")
public class NcOpLog implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "ID")
    private String id;

    @Schema(description = "设备序列号")
    private String deviceSn;

    @Schema(description = "操作类型: OTA_UPGRADE / DLM_CONFIG / REMOTE_REBOOT / REMOTE_RESET")
    private String opType;

    @Schema(description = "操作内容，如 v1.0.2→v1.0.3 / 32A→40A")
    private String opContent;

    @Schema(description = "操作结果: SUCCESS / FAIL")
    private String opResult;

    @Schema(description = "失败原因")
    private String failReason;

    @Schema(description = "操作人用户名")
    private String opUser;

    @Schema(description = "操作时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date opTime;

    @Schema(description = "创建时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    // ==================== 操作类型常量 ====================
    public static final String OTA_UPGRADE    = "OTA_UPGRADE";
    public static final String DLM_CONFIG     = "DLM_CONFIG";
    public static final String REMOTE_REBOOT  = "REMOTE_REBOOT";
    public static final String REMOTE_RESET   = "REMOTE_RESET";

    public static final String SUCCESS = "SUCCESS";
    public static final String FAIL    = "FAIL";
}
