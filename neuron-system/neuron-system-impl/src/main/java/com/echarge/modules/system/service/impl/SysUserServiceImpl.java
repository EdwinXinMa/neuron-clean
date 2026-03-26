package com.echarge.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.echarge.common.api.vo.Result;
import com.echarge.common.constant.CommonConstant;
import com.echarge.modules.system.entity.SysUser;
import com.echarge.modules.system.mapper.SysUserMapper;
import com.echarge.modules.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户 Service 实现（精简版）
 * @author Edwin
 */
@Slf4j
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    /** {@inheritDoc} */
    @Override
    public SysUser getUserByName(String username) {
        return baseMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)
        );
    }

    /** {@inheritDoc} */
    @Override
    public Result checkUserIsEffective(SysUser sysUser) {
        // 校验用户是否有效
        if (sysUser == null) {
            return Result.error("该用户不存在，请注意用户名大小写");
        }
        // 校验是否被冻结
        if (CommonConstant.USER_FREEZE.equals(sysUser.getStatus())) {
            return Result.error("账号已被冻结，请联系管理员");
        }
        // 校验是否被删除
        if (CommonConstant.DEL_FLAG_1.toString().equals(String.valueOf(sysUser.getDelFlag()))) {
            return Result.error("账号已被删除");
        }
        return Result.ok();
    }
}
