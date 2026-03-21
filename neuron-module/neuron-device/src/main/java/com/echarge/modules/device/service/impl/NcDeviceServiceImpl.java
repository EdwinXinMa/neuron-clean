package com.echarge.modules.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echarge.common.exception.NeuronBootException;
import com.echarge.modules.device.entity.NcDevice;
import com.echarge.modules.device.mapper.NcDeviceMapper;
import com.echarge.modules.device.service.INcDeviceService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service
public class NcDeviceServiceImpl extends ServiceImpl<NcDeviceMapper, NcDevice> implements INcDeviceService {

    @Override
    public void register(NcDevice device) {
        String sn = device.getSn();
        if (StringUtils.isNotBlank(sn)) {
            sn = sn.trim().replaceAll("\\s+", "").toUpperCase();
            device.setSn(sn);
        }
        if (StringUtils.isBlank(device.getSn())) {
            throw new NeuronBootException("\u8bbe\u5907SN\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (StringUtils.isBlank(device.getDealer())) {
            throw new NeuronBootException("\u7ecf\u9500\u5546\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (device.getShipDate() == null) {
            throw new NeuronBootException("\u51fa\u8d27\u65e5\u671f\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (existsBySn(device.getSn())) {
            throw new NeuronBootException("\u8bbe\u5907SN\u5df2\u5b58\u5728");
        }
        device.setDeviceType("N3_LITE");
        device.setOnlineStatus("UNACTIVATED");
        device.setStatus("NORMAL");
        device.setDelFlag(0);
        this.save(device);
    }

    @Override
    public void disable(String id) {
        NcDevice device = this.getById(id);
        if (device == null) {
            throw new NeuronBootException("\u8bbe\u5907\u4e0d\u5b58\u5728");
        }
        device.setStatus("DISABLED");
        this.updateById(device);
    }

    @Override
    public void enable(String id) {
        NcDevice device = this.getById(id);
        if (device == null) {
            throw new NeuronBootException("\u8bbe\u5907\u4e0d\u5b58\u5728");
        }
        device.setStatus("NORMAL");
        this.updateById(device);
    }

    @Override
    public boolean existsBySn(String sn) {
        return this.count(new LambdaQueryWrapper<NcDevice>()
                .eq(NcDevice::getSn, sn)
                .eq(NcDevice::getDelFlag, 0)) > 0;
    }
}