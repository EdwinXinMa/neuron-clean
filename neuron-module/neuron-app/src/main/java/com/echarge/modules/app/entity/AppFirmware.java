package com.echarge.modules.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.echarge.common.system.base.entity.NeuronEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * App 用户上传的固件记录（与运维后台 firmware_version 隔离）
 * @author Edwin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("app_firmware")
public class AppFirmware extends NeuronEntity {

    /** 上传用户 ID */
    private String userId;

    /** 目标设备 SN */
    private String deviceSn;

    /** MinIO 存储路径 */
    private String fileUrl;

    /** 原始文件名 */
    private String fileName;

    /** 文件大小（字节） */
    private Long fileSize;

    /** MD5 校验值 */
    private String checksum;
}
