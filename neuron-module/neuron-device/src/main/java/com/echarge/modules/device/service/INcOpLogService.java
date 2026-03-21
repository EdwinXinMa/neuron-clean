package com.echarge.modules.device.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.echarge.modules.device.entity.NcOpLog;

/**
 * 设备操作日志 Service
 */
public interface INcOpLogService extends IService<NcOpLog> {

    /**
     * 记录操作日志（成功）
     */
    void record(String deviceSn, String opType, String opContent, String opUser);

    /**
     * 记录操作日志（失败）
     */
    void recordFail(String deviceSn, String opType, String opContent, String opUser, String failReason);
}
