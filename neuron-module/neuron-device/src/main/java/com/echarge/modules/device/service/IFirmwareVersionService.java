package com.echarge.modules.device.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.echarge.modules.device.entity.FirmwareLatest;
import com.echarge.modules.device.entity.FirmwareVersion;

/**
 * @author Edwin
 */
public interface IFirmwareVersionService extends IService<FirmwareVersion> {

    /**
     * 发布固件 (DRAFT -> RELEASED)，同时更新最新版本记录
     */
    void release(String id);

    /**
     * 废弃固件 (RELEASED -> DEPRECATED)，最新版本不能废弃
     */
    void deprecate(String id);

    /**
     * 上传前校验：查重 + 版本号不能倒退
     */
    void checkUploadVersion(String version, String deviceType);

    /**
     * 获取某设备类型的最新版本记录
     */
    FirmwareLatest getLatest(String deviceType);
}
