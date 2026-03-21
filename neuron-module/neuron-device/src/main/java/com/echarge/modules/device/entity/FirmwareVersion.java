package com.echarge.modules.device.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.echarge.common.system.base.entity.NeuronEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

@Data
@TableName("firmware_version")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "\u56fa\u4ef6\u7248\u672c")
public class FirmwareVersion extends NeuronEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "\u7248\u672c\u53f7")
    private String version;

    @Schema(description = "\u8bbe\u5907\u7c7b\u578b")
    private String deviceType = "N3_LITE";

    @Schema(description = "MinIO\u5b58\u50a8\u8def\u5f84")
    private String fileUrl;

    @Schema(description = "\u539f\u59cb\u6587\u4ef6\u540d")
    private String fileName;

    @Schema(description = "\u6587\u4ef6\u5927\u5c0f(\u5b57\u8282)")
    private Long fileSize;

    @Schema(description = "MD5\u6821\u9a8c\u503c")
    private String checksum;

    @Schema(description = "\u7248\u672c\u8bf4\u660e")
    private String releaseNotes;

    @Schema(description = "\u72b6\u6001: DRAFT/RELEASED/DEPRECATED")
    private String status = "DRAFT";
}
