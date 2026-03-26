package com.echarge.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.echarge.common.api.vo.Result;
import com.echarge.modules.system.entity.SysUser;

/**
 * 用户 Service（精简版）
 * @author Edwin
 */
public interface ISysUserService extends IService<SysUser> {

    /**
     * 根据用户名查询用户
     * @param username 用户账号
     * @return 用户实体
     */
    SysUser getUserByName(String username);

    /**
     * 校验用户是否有效
     * @param sysUser 用户实体
     * @return 校验结果
     */
    Result checkUserIsEffective(SysUser sysUser);
}
