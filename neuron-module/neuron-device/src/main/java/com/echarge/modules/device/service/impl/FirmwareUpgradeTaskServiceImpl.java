package com.echarge.modules.device.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.echarge.modules.device.entity.FirmwareUpgradeTask;
import com.echarge.modules.device.mapper.FirmwareUpgradeTaskMapper;
import com.echarge.modules.device.service.IFirmwareUpgradeTaskService;
import org.springframework.stereotype.Service;

/**
 * @author Edwin
 */
@Service
public class FirmwareUpgradeTaskServiceImpl extends ServiceImpl<FirmwareUpgradeTaskMapper, FirmwareUpgradeTask> implements IFirmwareUpgradeTaskService {
}
