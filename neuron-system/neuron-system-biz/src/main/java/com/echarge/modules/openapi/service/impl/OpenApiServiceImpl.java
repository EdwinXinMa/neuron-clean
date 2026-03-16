package com.echarge.modules.openapi.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.echarge.modules.openapi.entity.OpenApi;
import com.echarge.modules.openapi.mapper.OpenApiMapper;
import com.echarge.modules.openapi.service.OpenApiService;
import org.springframework.stereotype.Service;

@Service
public class OpenApiServiceImpl extends ServiceImpl<OpenApiMapper, OpenApi> implements OpenApiService {
    @Override
    public OpenApi findByPath(String path) {
        return baseMapper.selectOne(Wrappers.lambdaQuery(OpenApi.class).eq(OpenApi::getRequestUrl, path), false);
    }
}
