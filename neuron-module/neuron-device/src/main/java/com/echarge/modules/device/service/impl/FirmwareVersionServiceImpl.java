package com.echarge.modules.device.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.echarge.common.exception.NeuronBootException;
import com.echarge.modules.device.entity.FirmwareVersion;
import com.echarge.modules.device.mapper.FirmwareVersionMapper;
import com.echarge.modules.device.service.IFirmwareVersionService;
import org.springframework.stereotype.Service;

@Service
public class FirmwareVersionServiceImpl extends ServiceImpl<FirmwareVersionMapper, FirmwareVersion>
        implements IFirmwareVersionService {

    @Override
    public void release(String id) {
        FirmwareVersion fw = getById(id);
        if (fw == null) {
            throw new NeuronBootException("固件不存在");
        }
        if (!"DRAFT".equals(fw.getStatus())) {
            throw new NeuronBootException("只有草稿状态的固件才能发布");
        }
        fw.setStatus("RELEASED");
        updateById(fw);
    }

    @Override
    public void deprecate(String id) {
        FirmwareVersion fw = getById(id);
        if (fw == null) {
            throw new NeuronBootException("固件不存在");
        }
        if (!"RELEASED".equals(fw.getStatus())) {
            throw new NeuronBootException("只有已发布状态的固件才能废弃");
        }
        fw.setStatus("DEPRECATED");
        updateById(fw);
    }
}
