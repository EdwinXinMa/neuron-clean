package com.echarge.modules.device.service.impl;

import com.echarge.modules.device.entity.NcDevice;
import com.echarge.modules.device.mapper.NcDeviceMapper;
import com.echarge.modules.device.service.INcDeviceService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service
public class NcDeviceServiceImpl extends ServiceImpl<NcDeviceMapper, NcDevice> implements INcDeviceService {
}
