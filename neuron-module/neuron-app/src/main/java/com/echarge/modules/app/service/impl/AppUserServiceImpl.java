package com.echarge.modules.app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.echarge.modules.app.entity.AppUser;
import com.echarge.modules.app.mapper.AppUserMapper;
import com.echarge.modules.app.service.IAppUserService;
import org.springframework.stereotype.Service;

/**
 * @author Edwin
 */
@Service
public class AppUserServiceImpl extends ServiceImpl<AppUserMapper, AppUser> implements IAppUserService {
}
