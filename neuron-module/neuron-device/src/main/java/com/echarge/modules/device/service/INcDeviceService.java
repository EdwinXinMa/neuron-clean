package com.echarge.modules.device.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.echarge.modules.device.entity.NcDevice;

/**
 * @author Edwin
 */
public interface INcDeviceService extends IService<NcDevice> {

    /**
     * 台账录入
     * @param device 设备实体
     */
    void register(NcDevice device);

    /**
     * 禁用设备
     * @param id 设备ID
     */
    void disable(String id);

    /**
     * 启用设备
     * @param id 设备ID
     */
    void enable(String id);

    /**
     * SN唯一检查
     * @param sn 设备序列号
     * @return 是否已存在
     */
    boolean existsBySn(String sn);
}
