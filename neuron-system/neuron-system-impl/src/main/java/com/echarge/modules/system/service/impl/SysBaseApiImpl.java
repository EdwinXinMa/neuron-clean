package com.echarge.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echarge.common.system.api.ISysBaseApi;
import com.echarge.common.system.vo.LoginUser;
import com.echarge.common.system.vo.SysUserCacheInfo;
import com.echarge.modules.system.entity.SysUser;
import com.echarge.modules.system.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

/**
 * 系统基础 API 实现（精简版）
 * @author Edwin
 */
@Slf4j
@Service
public class SysBaseApiImpl implements ISysBaseApi {

    @Resource
    private SysUserMapper userMapper;

    /** {@inheritDoc} */
    @Override
    public Set<String> queryUserRoles(String username) {
        Set<String> roles = new HashSet<>();
        SysUser user = getUserEntityByName(username);
        if (user != null && user.getRole() != null) {
            roles.add(user.getRole());
        }
        return roles;
    }

    /** {@inheritDoc} */
    @Override
    public Set<String> queryUserRolesById(String userId) {
        Set<String> roles = new HashSet<>();
        SysUser user = userMapper.selectById(userId);
        if (user != null && user.getRole() != null) {
            roles.add(user.getRole());
        }
        return roles;
    }

    /** {@inheritDoc} */
    @Override
    public Set<String> queryUserAuths(String userId) {
        // 精简版：所有用户拥有全部功能权限，仅 admin 额外拥有账号管理
        Set<String> auths = new HashSet<>();
        auths.add("user");
        SysUser user = userMapper.selectById(userId);
        if (user != null && "admin".equals(user.getRole())) {
            auths.add("admin");
        }
        return auths;
    }

    /** {@inheritDoc} */
    @Override
    public LoginUser getUserByName(String username) {
        SysUser user = getUserEntityByName(username);
        if (user == null) {
            return null;
        }
        return toLoginUser(user);
    }

    /** {@inheritDoc} */
    @Override
    public String getUserIdByName(String username) {
        SysUser user = getUserEntityByName(username);
        return user != null ? user.getId() : null;
    }

    /** {@inheritDoc} */
    @Override
    public LoginUser getUserById(String id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            return null;
        }
        return toLoginUser(user);
    }

    /** {@inheritDoc} */
    @Override
    public SysUserCacheInfo getCacheUser(String username) {
        SysUserCacheInfo info = new SysUserCacheInfo();
        SysUser user = getUserEntityByName(username);
        if (user != null) {
            info.setSysUserName(user.getUsername());
            info.setSysUserCode(user.getUsername());
        }
        return info;
    }

    // ==================== 私有方法 ====================

    private SysUser getUserEntityByName(String username) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)
        );
    }

    private LoginUser toLoginUser(SysUser user) {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setRealname(user.getRealname());
        loginUser.setPassword(user.getPassword());
        loginUser.setStatus(user.getStatus());
        loginUser.setDelFlag(user.getDelFlag());
        loginUser.setRoleCode(user.getRole());
        return loginUser;
    }
}
