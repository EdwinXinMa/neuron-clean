package com.echarge.common.api;

import com.echarge.common.system.vo.*;

import java.util.Set;

/**
 * 通用api（精简版）
 * @author Edwin
 */
public interface CommonApi {

    /**
     * 查询用户角色信息
     * @param username 用户账号
     * @return 角色编码集合
     */
    Set<String> queryUserRoles(String username);

    /**
     * 根据用户ID查询角色信息
     * @param userId 用户ID
     * @return 角色编码集合
     */
    Set<String> queryUserRolesById(String userId);

    /**
     * 查询用户权限信息
     * @param userId 用户ID
     * @return 权限编码集合
     */
    Set<String> queryUserAuths(String userId);

    /**
     * 根据用户账号查询用户信息
     * @param username 用户账号
     * @return 登录用户对象
     */
    LoginUser getUserByName(String username);

    /**
     * 根据用户账号查询用户ID
     * @param username 用户账号
     * @return 用户ID
     */
    String getUserIdByName(String username);

    /**
     * 查询用户信息（缓存）
     * @param username 用户账号
     * @return 用户缓存信息
     */
    SysUserCacheInfo getCacheUser(String username);

}
