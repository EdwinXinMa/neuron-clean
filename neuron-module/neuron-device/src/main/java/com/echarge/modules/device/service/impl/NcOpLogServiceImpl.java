package com.echarge.modules.device.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.echarge.modules.device.entity.NcOpLog;
import com.echarge.modules.device.mapper.NcOpLogMapper;
import com.echarge.modules.device.service.INcOpLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
public class NcOpLogServiceImpl extends ServiceImpl<NcOpLogMapper, NcOpLog> implements INcOpLogService {

    @Override
    public void record(String deviceSn, String opType, String opContent, String opUser) {
        NcOpLog opLog = new NcOpLog();
        opLog.setDeviceSn(deviceSn);
        opLog.setOpType(opType);
        opLog.setOpContent(opContent);
        opLog.setOpResult(NcOpLog.SUCCESS);
        opLog.setOpUser(opUser);
        opLog.setOpTime(new Date());
        opLog.setCreateTime(new Date());
        this.save(opLog);
        log.info("[OpLog] {} | {} | {} | {} | SUCCESS", opUser, deviceSn, opType, opContent);
    }

    @Override
    public void recordFail(String deviceSn, String opType, String opContent, String opUser, String failReason) {
        NcOpLog opLog = new NcOpLog();
        opLog.setDeviceSn(deviceSn);
        opLog.setOpType(opType);
        opLog.setOpContent(opContent);
        opLog.setOpResult(NcOpLog.FAIL);
        opLog.setFailReason(failReason);
        opLog.setOpUser(opUser);
        opLog.setOpTime(new Date());
        opLog.setCreateTime(new Date());
        this.save(opLog);
        log.info("[OpLog] {} | {} | {} | {} | FAIL: {}", opUser, deviceSn, opType, opContent, failReason);
    }
}
