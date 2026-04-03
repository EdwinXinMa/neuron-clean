package com.echarge.modules.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.echarge.modules.app.entity.AppUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Edwin
 */
@Mapper
public interface AppUserMapper extends BaseMapper<AppUser> {
}
