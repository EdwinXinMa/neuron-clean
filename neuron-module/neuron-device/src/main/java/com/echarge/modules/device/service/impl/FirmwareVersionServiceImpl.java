package com.echarge.modules.device.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.echarge.common.constant.BizConstant;
import com.echarge.common.exception.NeuronBootException;
import com.echarge.modules.device.entity.FirmwareVersion;
import com.echarge.modules.device.mapper.FirmwareVersionMapper;
import com.echarge.modules.device.service.IFirmwareVersionService;
import org.springframework.stereotype.Service;

/**
 * @author Edwin
 */
@Service
public class FirmwareVersionServiceImpl extends ServiceImpl<FirmwareVersionMapper, FirmwareVersion>
        implements IFirmwareVersionService {

    /** {@inheritDoc} */
    @Override
    public void release(String id) {
        FirmwareVersion fw = getById(id);
        if (fw == null) {
            throw new NeuronBootException("固件不存在");
        }
        if (!BizConstant.FIRMWARE_DRAFT.equals(fw.getStatus())) {
            throw new NeuronBootException("只有草稿状态的固件才能发布");
        }
        fw.setStatus(BizConstant.FIRMWARE_RELEASED);
        updateById(fw);
    }

    /** {@inheritDoc} */
    @Override
    public void deprecate(String id) {
        FirmwareVersion fw = getById(id);
        if (fw == null) {
            throw new NeuronBootException("固件不存在");
        }
        if (!BizConstant.FIRMWARE_RELEASED.equals(fw.getStatus())) {
            throw new NeuronBootException("只有已发布状态的固件才能废弃");
        }
        fw.setStatus(BizConstant.FIRMWARE_DEPRECATED);
        updateById(fw);
    }
}
