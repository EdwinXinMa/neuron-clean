package com.echarge.modules.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.echarge.modules.device.entity.FirmwareVersion;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FirmwareVersionMapper extends BaseMapper<FirmwareVersion> {
}
