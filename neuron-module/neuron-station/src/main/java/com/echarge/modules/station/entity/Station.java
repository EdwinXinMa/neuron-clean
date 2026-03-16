package com.echarge.modules.station.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.echarge.common.system.base.entity.NeuronEntity;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@TableName("station")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "充电站")
public class Station extends NeuronEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "站点名称")
    private String name;

    @Schema(description = "站点地址")
    private String address;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "站点状态: ACTIVE, INACTIVE")
    private String status;

    @Schema(description = "备注")
    private String remark;
}
