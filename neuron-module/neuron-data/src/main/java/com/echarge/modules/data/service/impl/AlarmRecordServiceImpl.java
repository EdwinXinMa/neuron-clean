package com.echarge.modules.data.service.impl;

import com.echarge.modules.data.entity.AlarmRecord;
import com.echarge.modules.data.mapper.AlarmRecordMapper;
import com.echarge.modules.data.service.IAlarmRecordService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service
public class AlarmRecordServiceImpl extends ServiceImpl<AlarmRecordMapper, AlarmRecord> implements IAlarmRecordService {
}
