package com.echarge.modules.system.service.impl;

import com.echarge.modules.system.entity.SysDepartRole;
import com.echarge.modules.system.mapper.SysDepartRoleMapper;
import com.echarge.modules.system.mapper.SysDepartRolePermissionMapper;
import com.echarge.modules.system.mapper.SysDepartRoleUserMapper;
import com.echarge.modules.system.service.ISysDepartRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Description: 部门角色
 * @Author: jeecg-boot
 * @Date:   2020-02-12
 * @Version: V1.0
 */
@Service
public class SysDepartRoleServiceImpl extends ServiceImpl<SysDepartRoleMapper, SysDepartRole> implements ISysDepartRoleService {

    @Autowired
    SysDepartRolePermissionMapper sysDepartRolePermissionMapper;

    @Autowired
    SysDepartRoleUserMapper sysDepartRoleUserMapper;

    @Override
    public List<SysDepartRole> queryDeptRoleByDeptAndUser(String orgCode, String userId) {
        return this.baseMapper.queryDeptRoleByDeptAndUser(orgCode,userId);
    }

    /**
     * 删除部门角色和对应关联表信息
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDepartRole(List<String> ids) {
        this.baseMapper.deleteBatchIds(ids);
        this.sysDepartRolePermissionMapper.deleteByRoleIds(ids);
        this.sysDepartRoleUserMapper.deleteByRoleIds(ids);
    }
}
