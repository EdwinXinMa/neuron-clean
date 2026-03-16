package com.echarge.modules.device.service.impl;

import com.echarge.modules.device.entity.Device;
import com.echarge.modules.device.mapper.DeviceMapper;
import com.echarge.modules.device.service.IDeviceService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements IDeviceService {
}
