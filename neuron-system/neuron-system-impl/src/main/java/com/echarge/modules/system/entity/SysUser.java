package com.echarge.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户表（精简版）
 */
@Data
@TableName("sys_user")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SysUser implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /** 用户账号 */
    private String username;

    /** 真实姓名 */
    private String realname;

    /** 密码 */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /** 密码盐 */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String salt;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 头像 */
    private String avatar;

    /** 角色：admin / operator */
    private String role;

    /** 状态：1正常 2冻结 */
    private Integer status;

    /** 删除标记：0正常 1已删除 */
    private Integer delFlag;

    /** 创建人 */
    private String createBy;

    /** 创建时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date createTime;

    /** 更新人 */
    private String updateBy;

    /** 更新时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date updateTime;
}
