package com.echarge.modules.base.service;

import com.echarge.common.api.dto.LogDTO;
import com.echarge.common.system.vo.LoginUser;

/**
 * common接口
 * @author Edwin
 */
public interface BaseCommonService {

    /**
     * 保存日志
     * @param logDTO 日志数据传输对象
     */
    void addLog(LogDTO logDTO);

    /**
     * 保存日志
     * @param logContent  日志内容
     * @param logType     日志类型
     * @param operateType 操作类型
     * @param user        当前登录用户
     */
    void addLog(String logContent, Integer logType, Integer operateType, LoginUser user);

    /**
     * 保存日志
     * @param logContent  日志内容
     * @param logType     日志类型
     * @param operateType 操作类型
     */
    void addLog(String logContent, Integer logType, Integer operateType);

}
