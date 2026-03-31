package com.echarge.modules.device.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.echarge.modules.device.entity.NcChargingSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Edwin
 */
@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface NcChargingSessionMapper extends BaseMapper<NcChargingSession> {
}
