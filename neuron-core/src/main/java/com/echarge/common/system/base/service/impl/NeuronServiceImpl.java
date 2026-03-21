package com.echarge.common.system.base.service.impl;

import com.echarge.common.system.base.entity.NeuronEntity;
import com.echarge.common.system.base.service.NeuronService;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description: ServiceImpl基类
 * @Author: dangzhenghui@163.com
 * @Date: 2019-4-21 8:13
 * @Version: 1.0
 */
@Slf4j
public class NeuronServiceImpl<M extends BaseMapper<T>, T extends NeuronEntity> extends ServiceImpl<M, T> implements NeuronService<T> {

}
