-- ========================================
-- NeuronCloud 业务表初始化脚本
-- ========================================

-- 站点表
CREATE TABLE IF NOT EXISTS `station` (
    `id` varchar(36) NOT NULL COMMENT '主键ID',
    `name` varchar(100) NOT NULL COMMENT '站点名称',
    `address` varchar(500) DEFAULT NULL COMMENT '站点地址',
    `longitude` decimal(10, 7) DEFAULT NULL COMMENT '经度',
    `latitude` decimal(10, 7) DEFAULT NULL COMMENT '纬度',
    `contact_person` varchar(50) DEFAULT NULL COMMENT '联系人',
    `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
    `status` varchar(20) DEFAULT 'ACTIVE' COMMENT '站点状态: ACTIVE, INACTIVE',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `sys_org_code` varchar(64) DEFAULT NULL COMMENT '所属部门',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充电站';

-- 设备表
CREATE TABLE IF NOT EXISTS `device` (
    `id` varchar(36) NOT NULL COMMENT '主键ID',
    `sn` varchar(64) NOT NULL COMMENT '设备序列号',
    `name` varchar(100) DEFAULT NULL COMMENT '设备名称',
    `device_type` varchar(20) NOT NULL COMMENT '设备类型: N3, N3_LITE, ATP_III',
    `firmware_version` varchar(50) DEFAULT NULL COMMENT '固件版本',
    `station_id` varchar(36) DEFAULT NULL COMMENT '所属站点ID',
    `online_status` varchar(20) DEFAULT 'OFFLINE' COMMENT '在线状态: ONLINE, OFFLINE',
    `mqtt_client_id` varchar(100) DEFAULT NULL COMMENT 'MQTT客户端ID',
    `last_heartbeat` datetime DEFAULT NULL COMMENT '最后心跳时间',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `sys_org_code` varchar(64) DEFAULT NULL COMMENT '所属部门',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sn` (`sn`),
    KEY `idx_station_id` (`station_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备';

-- 设备配置表
CREATE TABLE IF NOT EXISTS `device_config` (
    `id` varchar(36) NOT NULL COMMENT '主键ID',
    `device_id` varchar(36) NOT NULL COMMENT '设备ID',
    `config_key` varchar(100) NOT NULL COMMENT '配置项Key',
    `config_value` text COMMENT '配置项Value',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `sys_org_code` varchar(64) DEFAULT NULL COMMENT '所属部门',
    PRIMARY KEY (`id`),
    KEY `idx_device_id` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备配置';

-- 遥测快照表
CREATE TABLE IF NOT EXISTS `telemetry_snapshot` (
    `id` varchar(36) NOT NULL COMMENT '主键ID',
    `device_id` varchar(36) NOT NULL COMMENT '设备ID',
    `voltage` decimal(10, 2) DEFAULT NULL COMMENT '电压(V)',
    `current` decimal(10, 2) DEFAULT NULL COMMENT '电流(A)',
    `power` decimal(10, 3) DEFAULT NULL COMMENT '功率(kW)',
    `energy` decimal(12, 3) DEFAULT NULL COMMENT '电量(kWh)',
    `temperature` decimal(5, 1) DEFAULT NULL COMMENT '温度(°C)',
    `collect_time` datetime NOT NULL COMMENT '采集时间',
    `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `sys_org_code` varchar(64) DEFAULT NULL COMMENT '所属部门',
    PRIMARY KEY (`id`),
    KEY `idx_device_time` (`device_id`, `collect_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='遥测快照';

-- 能耗统计表
CREATE TABLE IF NOT EXISTS `energy_statistics` (
    `id` varchar(36) NOT NULL COMMENT '主键ID',
    `station_id` varchar(36) DEFAULT NULL COMMENT '站点ID',
    `device_id` varchar(36) DEFAULT NULL COMMENT '设备ID',
    `statistics_date` date NOT NULL COMMENT '统计日期',
    `statistics_type` varchar(20) NOT NULL COMMENT '统计类型: DAILY, MONTHLY, YEARLY',
    `total_energy` decimal(12, 3) DEFAULT NULL COMMENT '总用电量(kWh)',
    `peak_power` decimal(10, 3) DEFAULT NULL COMMENT '峰值功率(kW)',
    `avg_power` decimal(10, 3) DEFAULT NULL COMMENT '平均功率(kW)',
    `charge_count` int DEFAULT 0 COMMENT '充电次数',
    `total_duration` int DEFAULT 0 COMMENT '充电总时长(分钟)',
    `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `sys_org_code` varchar(64) DEFAULT NULL COMMENT '所属部门',
    PRIMARY KEY (`id`),
    KEY `idx_station_date` (`station_id`, `statistics_date`),
    KEY `idx_device_date` (`device_id`, `statistics_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='能耗统计';

-- 告警记录表
CREATE TABLE IF NOT EXISTS `alarm_record` (
    `id` varchar(36) NOT NULL COMMENT '主键ID',
    `device_id` varchar(36) NOT NULL COMMENT '设备ID',
    `alarm_type` varchar(50) NOT NULL COMMENT '告警类型',
    `alarm_level` varchar(20) NOT NULL COMMENT '告警级别: INFO, WARNING, ERROR, CRITICAL',
    `content` text COMMENT '告警内容',
    `status` varchar(20) DEFAULT 'ACTIVE' COMMENT '告警状态: ACTIVE, ACKNOWLEDGED, RESOLVED',
    `alarm_time` datetime NOT NULL COMMENT '告警时间',
    `resolve_time` datetime DEFAULT NULL COMMENT '解决时间',
    `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `sys_org_code` varchar(64) DEFAULT NULL COMMENT '所属部门',
    PRIMARY KEY (`id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_alarm_time` (`alarm_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警记录';

-- 充电记录表
CREATE TABLE IF NOT EXISTS `charging_record` (
    `id` varchar(36) NOT NULL COMMENT '主键ID',
    `device_id` varchar(36) NOT NULL COMMENT '设备ID',
    `station_id` varchar(36) DEFAULT NULL COMMENT '站点ID',
    `connector_id` int DEFAULT NULL COMMENT '连接器编号',
    `start_time` datetime DEFAULT NULL COMMENT '开始充电时间',
    `end_time` datetime DEFAULT NULL COMMENT '结束充电时间',
    `start_energy` decimal(12, 3) DEFAULT NULL COMMENT '起始电量(kWh)',
    `end_energy` decimal(12, 3) DEFAULT NULL COMMENT '结束电量(kWh)',
    `charged_energy` decimal(12, 3) DEFAULT NULL COMMENT '充入电量(kWh)',
    `max_power` decimal(10, 3) DEFAULT NULL COMMENT '最大功率(kW)',
    `stop_reason` varchar(50) DEFAULT NULL COMMENT '停止原因',
    `status` varchar(20) DEFAULT 'CHARGING' COMMENT '状态: CHARGING, COMPLETED, FAULTED',
    `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `sys_org_code` varchar(64) DEFAULT NULL COMMENT '所属部门',
    PRIMARY KEY (`id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_station_id` (`station_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充电记录';

-- DLM配置表
CREATE TABLE IF NOT EXISTS `dlm_config` (
    `id` varchar(36) NOT NULL COMMENT '主键ID',
    `station_id` varchar(36) NOT NULL COMMENT '站点ID',
    `max_power` decimal(10, 3) DEFAULT NULL COMMENT '最大功率限制(kW)',
    `strategy` varchar(50) DEFAULT 'EQUAL' COMMENT '分配策略: EQUAL, PRIORITY, FIFO',
    `enabled` tinyint(1) DEFAULT 1 COMMENT '是否启用',
    `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `sys_org_code` varchar(64) DEFAULT NULL COMMENT '所属部门',
    PRIMARY KEY (`id`),
    KEY `idx_station_id` (`station_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态负载管理配置';

-- 固件版本表
CREATE TABLE IF NOT EXISTS `firmware_version` (
    `id` varchar(36) NOT NULL COMMENT '主键ID',
    `version` varchar(50) NOT NULL COMMENT '版本号',
    `device_type` varchar(20) NOT NULL COMMENT '适用设备类型',
    `file_url` varchar(500) DEFAULT NULL COMMENT '固件文件URL',
    `file_size` bigint DEFAULT NULL COMMENT '文件大小(bytes)',
    `checksum` varchar(64) DEFAULT NULL COMMENT '文件校验值',
    `release_notes` text COMMENT '版本说明',
    `status` varchar(20) DEFAULT 'DRAFT' COMMENT '状态: DRAFT, RELEASED, DEPRECATED',
    `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `sys_org_code` varchar(64) DEFAULT NULL COMMENT '所属部门',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='固件版本';

-- 固件升级任务表
CREATE TABLE IF NOT EXISTS `firmware_upgrade_task` (
    `id` varchar(36) NOT NULL COMMENT '主键ID',
    `firmware_id` varchar(36) NOT NULL COMMENT '固件版本ID',
    `device_id` varchar(36) NOT NULL COMMENT '设备ID',
    `status` varchar(20) DEFAULT 'PENDING' COMMENT '状态: PENDING, DOWNLOADING, INSTALLING, COMPLETED, FAILED',
    `progress` int DEFAULT 0 COMMENT '升级进度(%)',
    `error_msg` varchar(500) DEFAULT NULL COMMENT '错误信息',
    `start_time` datetime DEFAULT NULL COMMENT '开始时间',
    `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
    `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `sys_org_code` varchar(64) DEFAULT NULL COMMENT '所属部门',
    PRIMARY KEY (`id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_firmware_id` (`firmware_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='固件升级任务';

-- OCPP充电桩表
CREATE TABLE IF NOT EXISTS `ocpp_charger` (
    `id` varchar(36) NOT NULL COMMENT '主键ID',
    `identity` varchar(64) NOT NULL COMMENT 'OCPP Identity',
    `device_id` varchar(36) DEFAULT NULL COMMENT '关联设备ID',
    `vendor` varchar(100) DEFAULT NULL COMMENT '厂商',
    `model` varchar(100) DEFAULT NULL COMMENT '型号',
    `serial_number` varchar(100) DEFAULT NULL COMMENT '序列号',
    `ocpp_version` varchar(10) DEFAULT '2.0.1' COMMENT 'OCPP版本',
    `connector_count` int DEFAULT 1 COMMENT '连接器数量',
    `status` varchar(20) DEFAULT 'Unavailable' COMMENT 'OCPP状态',
    `last_boot_time` datetime DEFAULT NULL COMMENT '最后启动时间',
    `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `sys_org_code` varchar(64) DEFAULT NULL COMMENT '所属部门',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_identity` (`identity`),
    KEY `idx_device_id` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OCPP充电桩';
