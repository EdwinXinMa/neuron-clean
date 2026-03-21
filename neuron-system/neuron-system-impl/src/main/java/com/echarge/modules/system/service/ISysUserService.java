package com.echarge.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.echarge.common.api.vo.Result;
import com.echarge.modules.system.entity.SysUser;

/**
 * 用户 Service（精简版）
 */
public interface ISysUserService extends IService<SysUser> {

    /**
     * 根据用户名查询用户
     */
    SysUser getUserByName(String username);

    /**
     * 校验用户是否有效
     */
    Result checkUserIsEffective(SysUser sysUser);
}
