package com.echarge.modules.message.service.impl;

import com.echarge.common.system.base.service.impl.NeuronServiceImpl;
import com.echarge.modules.message.entity.SysMessage;
import com.echarge.modules.message.mapper.SysMessageMapper;
import com.echarge.modules.message.service.ISysMessageService;
import org.springframework.stereotype.Service;

/**
 * @Description: 消息
 * @Author: jeecg-boot
 * @Date:  2019-04-09
 * @Version: V1.0
 */
@Service
public class SysMessageServiceImpl extends NeuronServiceImpl<SysMessageMapper, SysMessage> implements ISysMessageService {

}
