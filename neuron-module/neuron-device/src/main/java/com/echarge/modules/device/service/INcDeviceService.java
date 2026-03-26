package com.echarge.modules.device.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.echarge.modules.device.entity.NcDevice;

/**
 * @author Edwin
 */
public interface INcDeviceService extends IService<NcDevice> {

    /** 台账录入 */
    void register(NcDevice device);

    /** 禁用设备 */
    void disable(String id);

    /** 启用设备 */
    void enable(String id);

    /** SN唯一检查 */
    boolean existsBySn(String sn);
}
