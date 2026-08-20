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
     * 下发 DLM 配置（breakerRating + 可选 safetyMargin）
     * 更新数据库 + Redis + OCPP 下发 + 操作日志
     * @param sn 设备序列号
     * @param breakerRating 断路器额定值
     * @param safetyMargin 安全余量；为空时按旧协议不下发该字段
     * @param opUser 操作人
     */
    void sendDlmConfig(String sn, int breakerRating, Integer safetyMargin, String opUser);

    /**
     * 下发充电桩工作模式切换
     * OCPP DataTransfer(SetWorkMode) 下发 + 操作日志
     * @param sn N3 Lite 设备序列号
     * @param deviceList 桩列表 [{"sn":"xxx","workMode":"Manual"}]
     * @param opUser 操作人
     */
    void sendWorkMode(String sn, List<Map<String, String>> deviceList, String opUser);

    /**
     * 下发预约充电时间段。
     * OCPP DataTransfer(SetScheduledCharging) 下发 + 等待设备回执 + 操作日志
     * @param sn N3 Lite 设备序列号
     * @param pileSn 充电桩 SN
     * @param timePeriods UTC 时间段，格式 [["22:00","07:00"]]
     * @param opUser 操作人
     */
    void sendScheduledCharging(String sn, String pileSn, List<List<String>> timePeriods, String opUser);

    /**
     * 查询预约充电时间段。
     * OCPP DataTransfer(GetScheduledCharging) 下发 + 等待设备回执
     * @param sn N3 Lite 设备序列号
     * @param pileSn 充电桩 SN
     * @return UTC 时间段
     */
    List<List<String>> getScheduledCharging(String sn, String pileSn);

    /**
     * 下发恢复出厂设置指令。
     * OCPP DataTransfer(FactoryReset) 下发 + 等待设备回执 + 操作日志
     * @param sn N3 Lite 设备序列号
     * @param requestedBy 发起来源：web / app
     * @param opUser 操作人
     */
    void sendFactoryReset(String sn, String requestedBy, String opUser);
}
