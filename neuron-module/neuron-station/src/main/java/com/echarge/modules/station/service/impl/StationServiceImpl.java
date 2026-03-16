package com.echarge.modules.station.service.impl;

import com.echarge.modules.station.entity.Station;
import com.echarge.modules.station.mapper.StationMapper;
import com.echarge.modules.station.service.IStationService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service
public class StationServiceImpl extends ServiceImpl<StationMapper, Station> implements IStationService {
}
