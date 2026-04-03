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
 * App 用户-设备绑定关系
 * @author Edwin
 */
@Data
@TableName("app_user_device")
@Accessors(chain = true)
@Schema(description = "App 用户-设备绑定关系")
public class AppUserDevice implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "设备序列号")
    private String deviceSn;

    @Schema(description = "绑定时 GPS 经度")
    private Double longitude;

    @Schema(description = "绑定时 GPS 纬度")
    private Double latitude;

    private Date createTime;
}
