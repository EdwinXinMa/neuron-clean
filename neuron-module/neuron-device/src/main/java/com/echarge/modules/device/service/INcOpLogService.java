package com.echarge.modules.device.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.echarge.modules.device.entity.NcOpLog;

/**
 * 设备操作日志 Service
 * @author Edwin
 */
public interface INcOpLogService extends IService<NcOpLog> {

    /**
     * 记录操作日志（成功）
     * @param deviceSn  设备序列号
     * @param opType    操作类型
     * @param opContent 操作内容
     * @param opUser    操作人
     */
    void record(String deviceSn, String opType, String opContent, String opUser);

    /**
     * 记录操作日志（失败）
     * @param deviceSn   设备序列号
     * @param opType     操作类型
     * @param opContent  操作内容
     * @param opUser     操作人
     * @param failReason 失败原因
     */
    void recordFail(String deviceSn, String opType, String opContent, String opUser, String failReason);
}
