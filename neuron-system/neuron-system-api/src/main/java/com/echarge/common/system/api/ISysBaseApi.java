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
     * 根据用户id查询用户信息
     */
    LoginUser getUserById(String id);

    /**
     * 通过用户账号查询角色集合
     */
    Set<String> queryUserRoles(String username);

    /**
     * 通过用户账号查询权限集合
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
