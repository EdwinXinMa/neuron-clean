package com.echarge.modules.data.service.impl;

import com.echarge.modules.data.entity.TelemetrySnapshot;
import com.echarge.modules.data.mapper.TelemetrySnapshotMapper;
import com.echarge.modules.data.service.ITelemetrySnapshotService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service
public class TelemetrySnapshotServiceImpl extends ServiceImpl<TelemetrySnapshotMapper, TelemetrySnapshot> implements ITelemetrySnapshotService {
}
