package com.echarge.modules.system.service.impl;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.echarge.modules.system.entity.SysRole;
import com.echarge.modules.system.entity.SysUser;
import com.echarge.modules.system.entity.SysUserRole;
import com.echarge.modules.system.mapper.SysUserRoleMapper;
import com.echarge.modules.system.service.ISysRoleService;
import com.echarge.modules.system.service.ISysUserRoleService;
import com.echarge.modules.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * <p>
 * 用户角色表 服务实现类
 * </p>
 *
 * @Author scott
 * @since 2018-12-21
 */
@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements ISysUserRoleService {

}
