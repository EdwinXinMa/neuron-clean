package com.echarge.modules.device.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.echarge.modules.device.entity.NcDevice;

import java.util.List;
import java.util.Map;

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

    /**
     * 下发 DLM 配置（breakerRating）
     * 更新数据库 + Redis + OCPP 下发 + 操作日志
     * @param sn 设备序列号
     * @param breakerRating 断路器额定值
     * @param opUser 操作人
     */
    void sendDlmConfig(String sn, int breakerRating, String opUser);

    /**
     * 下发充电桩工作模式切换
     * OCPP DataTransfer(SetWorkMode) 下发 + 操作日志
     * @param sn N3 Lite 设备序列号
     * @param deviceList 桩列表 [{"sn":"xxx","workMode":"Plc"}]
     * @param opUser 操作人
     */
    void sendWorkMode(String sn, List<Map<String, String>> deviceList, String opUser);
}
