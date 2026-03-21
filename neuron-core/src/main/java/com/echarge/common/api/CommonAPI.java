package com.echarge.common.api;

import com.echarge.common.system.vo.*;

import java.util.Set;

/**
 * 通用api（精简版）
 */
public interface CommonAPI {

    /**
     * 查询用户角色信息
     */
    Set<String> queryUserRoles(String username);

    /**
     * 查询用户角色信息
     */
    Set<String> queryUserRolesById(String userId);

    /**
     * 查询用户权限信息
     */
    Set<String> queryUserAuths(String userId);

    /**
     * 根据用户账号查询用户信息
     */
    LoginUser getUserByName(String username);

    /**
     * 根据用户账号查询用户Id
     */
    String getUserIdByName(String username);

    /**
     * 查询用户信息（缓存）
     */
    SysUserCacheInfo getCacheUser(String username);

}
