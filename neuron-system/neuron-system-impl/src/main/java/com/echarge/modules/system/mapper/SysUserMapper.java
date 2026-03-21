package com.echarge.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.echarge.modules.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper（精简版）
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
