package com.echarge.modules.openapi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.echarge.modules.openapi.entity.OpenApiLog;
import com.echarge.modules.openapi.mapper.OpenApiLogMapper;
import com.echarge.modules.openapi.service.OpenApiLogService;
import org.springframework.stereotype.Service;

/**
 * @date 2024/12/10 9:53
 */
@Service
public class OpenApiLogServiceImpl extends ServiceImpl<OpenApiLogMapper, OpenApiLog> implements OpenApiLogService {
}
