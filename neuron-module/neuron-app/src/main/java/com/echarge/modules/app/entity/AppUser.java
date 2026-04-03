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
 * App 用户
 * @author Edwin
 */
@Data
@TableName("app_user")
@Accessors(chain = true)
@Schema(description = "App 用户")
public class AppUser implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @Schema(description = "邮箱（登录账号）")
    private String email;

    @Schema(description = "密码（加密）")
    private String password;

    @Schema(description = "盐值")
    private String salt;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "状态：1=正常 0=禁用")
    private Integer status;

    private Date createTime;

    private Date updateTime;
}
