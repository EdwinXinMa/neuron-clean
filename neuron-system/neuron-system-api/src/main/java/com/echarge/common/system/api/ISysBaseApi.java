package com.echarge.common.system.api;

import com.echarge.common.api.CommonApi;
import com.echarge.common.system.vo.LoginUser;
import com.echarge.common.system.vo.SysUserCacheInfo;

import java.util.Set;

/**
 * 底层共通业务API（精简版）
 * @author Edwin
 */
public interface ISysBaseApi extends CommonApi {

    /**
     * 根据用户ID查询用户信息
     * @param id 用户ID
     * @return 登录用户对象
     */
    LoginUser getUserById(String id);

    /**
     * 通过用户账号查询角色集合
     * @param username 用户账号
     * @return 角色编码集合
     */
    Set<String> queryUserRoles(String username);

    /**
     * 通过用户账号查询权限集合
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
