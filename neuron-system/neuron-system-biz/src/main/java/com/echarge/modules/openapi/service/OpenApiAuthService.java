package com.echarge.modules.openapi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.echarge.modules.openapi.entity.OpenApiAuth;

/**
 * @date 2024/12/10 9:50
 */
public interface OpenApiAuthService extends IService<OpenApiAuth> {
    OpenApiAuth getByAppkey(String appkey);
}
