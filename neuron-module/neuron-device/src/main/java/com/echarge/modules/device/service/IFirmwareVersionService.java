package com.echarge.modules.device.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.echarge.modules.device.entity.FirmwareVersion;

/**
 * @author Edwin
 */
public interface IFirmwareVersionService extends IService<FirmwareVersion> {

    /**
     * 发布固件 (DRAFT -> RELEASED)
     * @param id 固件版本ID
     */
    void release(String id);

    /**
     * 废弃固件 (RELEASED -> DEPRECATED)
     * @param id 固件版本ID
     */
    void deprecate(String id);
}
