package com.echarge.modules.device.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.echarge.modules.device.entity.FirmwareVersion;

public interface IFirmwareVersionService extends IService<FirmwareVersion> {

    /**
     * \u53d1\u5e03\u56fa\u4ef6 (DRAFT -> RELEASED)
     */
    void release(String id);

    /**
     * \u5e9f\u5f03\u56fa\u4ef6 (RELEASED -> DEPRECATED)
     */
    void deprecate(String id);
}
