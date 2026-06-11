package com.echarge.modules.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * App 用户推送设备注册 ID（支持多端）
 * @author Edwin
 */
@Data
@TableName("app_user_registration")
@Accessors(chain = true)
@Schema(description = "App 用户推送注册 ID")
public class AppUserRegistration implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @Schema(description = "用户 ID")
    private String userId;

    @Schema(description = "极光推送 Registration ID")
    private String regId;

    private Date createTime;
}
