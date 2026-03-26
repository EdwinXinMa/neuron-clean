package com.echarge.modules.device.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.echarge.modules.device.entity.NcConnector;
import com.echarge.modules.device.mapper.NcConnectorMapper;
import com.echarge.modules.device.service.INcConnectorService;
import org.springframework.stereotype.Service;

/**
 * @author Edwin
 */
@Service
public class NcConnectorServiceImpl extends ServiceImpl<NcConnectorMapper, NcConnector> implements INcConnectorService {
}
