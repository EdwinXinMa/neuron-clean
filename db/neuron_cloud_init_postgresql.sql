-- ========================================
-- NeuronCloud 业务表初始化脚本 (PostgreSQL)
-- ========================================

-- 站点表
CREATE TABLE IF NOT EXISTS station (
    id varchar(36) NOT NULL,
    name varchar(100) NOT NULL,
    address varchar(500) DEFAULT NULL,
    longitude decimal(10, 7) DEFAULT NULL,
    latitude decimal(10, 7) DEFAULT NULL,
    contact_person varchar(50) DEFAULT NULL,
    contact_phone varchar(20) DEFAULT NULL,
    status varchar(20) DEFAULT 'ACTIVE',
    remark varchar(500) DEFAULT NULL,
    create_by varchar(50) DEFAULT NULL,
    create_time timestamp DEFAULT NULL,
    update_by varchar(50) DEFAULT NULL,
    update_time timestamp DEFAULT NULL,
    sys_org_code varchar(64) DEFAULT NULL,
    PRIMARY KEY (id)
);
COMMENT ON TABLE station IS '充电站';

-- 设备表
CREATE TABLE IF NOT EXISTS device (
    id varchar(36) NOT NULL,
    sn varchar(64) NOT NULL,
    name varchar(100) DEFAULT NULL,
    device_type varchar(20) NOT NULL,
    firmware_version varchar(50) DEFAULT NULL,
    station_id varchar(36) DEFAULT NULL,
    online_status varchar(20) DEFAULT 'OFFLINE',
    mqtt_client_id varchar(100) DEFAULT NULL,
    last_heartbeat timestamp DEFAULT NULL,
    remark varchar(500) DEFAULT NULL,
    create_by varchar(50) DEFAULT NULL,
    create_time timestamp DEFAULT NULL,
    update_by varchar(50) DEFAULT NULL,
    update_time timestamp DEFAULT NULL,
    sys_org_code varchar(64) DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_sn UNIQUE (sn)
);
CREATE INDEX IF NOT EXISTS idx_device_station_id ON device (station_id);
COMMENT ON TABLE device IS '设备';

-- 设备配置表
CREATE TABLE IF NOT EXISTS device_config (
    id varchar(36) NOT NULL,
    device_id varchar(36) NOT NULL,
    config_key varchar(100) NOT NULL,
    config_value text,
    remark varchar(500) DEFAULT NULL,
    create_by varchar(50) DEFAULT NULL,
    create_time timestamp DEFAULT NULL,
    update_by varchar(50) DEFAULT NULL,
    update_time timestamp DEFAULT NULL,
    sys_org_code varchar(64) DEFAULT NULL,
    PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS idx_device_config_device_id ON device_config (device_id);
COMMENT ON TABLE device_config IS '设备配置';

-- 遥测快照表
CREATE TABLE IF NOT EXISTS telemetry_snapshot (
    id varchar(36) NOT NULL,
    device_id varchar(36) NOT NULL,
    voltage decimal(10, 2) DEFAULT NULL,
    current decimal(10, 2) DEFAULT NULL,
    power decimal(10, 3) DEFAULT NULL,
    energy decimal(12, 3) DEFAULT NULL,
    temperature decimal(5, 1) DEFAULT NULL,
    collect_time timestamp NOT NULL,
    create_by varchar(50) DEFAULT NULL,
    create_time timestamp DEFAULT NULL,
    update_by varchar(50) DEFAULT NULL,
    update_time timestamp DEFAULT NULL,
    sys_org_code varchar(64) DEFAULT NULL,
    PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS idx_telemetry_device_time ON telemetry_snapshot (device_id, collect_time);
COMMENT ON TABLE telemetry_snapshot IS '遥测快照';

-- 能耗统计表
CREATE TABLE IF NOT EXISTS energy_statistics (
    id varchar(36) NOT NULL,
    station_id varchar(36) DEFAULT NULL,
    device_id varchar(36) DEFAULT NULL,
    statistics_date date NOT NULL,
    statistics_type varchar(20) NOT NULL,
    total_energy decimal(12, 3) DEFAULT NULL,
    peak_power decimal(10, 3) DEFAULT NULL,
    avg_power decimal(10, 3) DEFAULT NULL,
    charge_count integer DEFAULT 0,
    total_duration integer DEFAULT 0,
    create_by varchar(50) DEFAULT NULL,
    create_time timestamp DEFAULT NULL,
    update_by varchar(50) DEFAULT NULL,
    update_time timestamp DEFAULT NULL,
    sys_org_code varchar(64) DEFAULT NULL,
    PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS idx_energy_station_date ON energy_statistics (station_id, statistics_date);
CREATE INDEX IF NOT EXISTS idx_energy_device_date ON energy_statistics (device_id, statistics_date);
COMMENT ON TABLE energy_statistics IS '能耗统计';

-- 告警记录表
CREATE TABLE IF NOT EXISTS alarm_record (
    id varchar(36) NOT NULL,
    device_id varchar(36) NOT NULL,
    alarm_type varchar(50) NOT NULL,
    alarm_level varchar(20) NOT NULL,
    content text,
    status varchar(20) DEFAULT 'ACTIVE',
    alarm_time timestamp NOT NULL,
    resolve_time timestamp DEFAULT NULL,
    create_by varchar(50) DEFAULT NULL,
    create_time timestamp DEFAULT NULL,
    update_by varchar(50) DEFAULT NULL,
    update_time timestamp DEFAULT NULL,
    sys_org_code varchar(64) DEFAULT NULL,
    PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS idx_alarm_device_id ON alarm_record (device_id);
CREATE INDEX IF NOT EXISTS idx_alarm_time ON alarm_record (alarm_time);
COMMENT ON TABLE alarm_record IS '告警记录';

-- 充电记录表
CREATE TABLE IF NOT EXISTS charging_record (
    id varchar(36) NOT NULL,
    device_id varchar(36) NOT NULL,
    station_id varchar(36) DEFAULT NULL,
    connector_id integer DEFAULT NULL,
    start_time timestamp DEFAULT NULL,
    end_time timestamp DEFAULT NULL,
    start_energy decimal(12, 3) DEFAULT NULL,
    end_energy decimal(12, 3) DEFAULT NULL,
    charged_energy decimal(12, 3) DEFAULT NULL,
    max_power decimal(10, 3) DEFAULT NULL,
    stop_reason varchar(50) DEFAULT NULL,
    status varchar(20) DEFAULT 'CHARGING',
    create_by varchar(50) DEFAULT NULL,
    create_time timestamp DEFAULT NULL,
    update_by varchar(50) DEFAULT NULL,
    update_time timestamp DEFAULT NULL,
    sys_org_code varchar(64) DEFAULT NULL,
    PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS idx_charging_device_id ON charging_record (device_id);
CREATE INDEX IF NOT EXISTS idx_charging_station_id ON charging_record (station_id);
COMMENT ON TABLE charging_record IS '充电记录';

-- DLM配置表
CREATE TABLE IF NOT EXISTS dlm_config (
    id varchar(36) NOT NULL,
    station_id varchar(36) NOT NULL,
    max_power decimal(10, 3) DEFAULT NULL,
    strategy varchar(50) DEFAULT 'EQUAL',
    enabled smallint DEFAULT 1,
    create_by varchar(50) DEFAULT NULL,
    create_time timestamp DEFAULT NULL,
    update_by varchar(50) DEFAULT NULL,
    update_time timestamp DEFAULT NULL,
    sys_org_code varchar(64) DEFAULT NULL,
    PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS idx_dlm_station_id ON dlm_config (station_id);
COMMENT ON TABLE dlm_config IS '动态负载管理配置';

-- 固件版本表
CREATE TABLE IF NOT EXISTS firmware_version (
    id varchar(36) NOT NULL,
    version varchar(50) NOT NULL,
    device_type varchar(20) NOT NULL,
    file_url varchar(500) DEFAULT NULL,
    file_size bigint DEFAULT NULL,
    checksum varchar(64) DEFAULT NULL,
    release_notes text,
    status varchar(20) DEFAULT 'DRAFT',
    create_by varchar(50) DEFAULT NULL,
    create_time timestamp DEFAULT NULL,
    update_by varchar(50) DEFAULT NULL,
    update_time timestamp DEFAULT NULL,
    sys_org_code varchar(64) DEFAULT NULL,
    PRIMARY KEY (id)
);
COMMENT ON TABLE firmware_version IS '固件版本';

-- 固件升级任务表
CREATE TABLE IF NOT EXISTS firmware_upgrade_task (
    id varchar(36) NOT NULL,
    firmware_id varchar(36) NOT NULL,
    device_id varchar(36) NOT NULL,
    status varchar(20) DEFAULT 'PENDING',
    progress integer DEFAULT 0,
    error_msg varchar(500) DEFAULT NULL,
    start_time timestamp DEFAULT NULL,
    finish_time timestamp DEFAULT NULL,
    create_by varchar(50) DEFAULT NULL,
    create_time timestamp DEFAULT NULL,
    update_by varchar(50) DEFAULT NULL,
    update_time timestamp DEFAULT NULL,
    sys_org_code varchar(64) DEFAULT NULL,
    PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS idx_upgrade_device_id ON firmware_upgrade_task (device_id);
CREATE INDEX IF NOT EXISTS idx_upgrade_firmware_id ON firmware_upgrade_task (firmware_id);
COMMENT ON TABLE firmware_upgrade_task IS '固件升级任务';

-- OCPP充电桩表
CREATE TABLE IF NOT EXISTS ocpp_charger (
    id varchar(36) NOT NULL,
    identity varchar(64) NOT NULL,
    device_id varchar(36) DEFAULT NULL,
    vendor varchar(100) DEFAULT NULL,
    model varchar(100) DEFAULT NULL,
    serial_number varchar(100) DEFAULT NULL,
    ocpp_version varchar(10) DEFAULT '2.0.1',
    connector_count integer DEFAULT 1,
    status varchar(20) DEFAULT 'Unavailable',
    last_boot_time timestamp DEFAULT NULL,
    create_by varchar(50) DEFAULT NULL,
    create_time timestamp DEFAULT NULL,
    update_by varchar(50) DEFAULT NULL,
    update_time timestamp DEFAULT NULL,
    sys_org_code varchar(64) DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_identity UNIQUE (identity)
);
CREATE INDEX IF NOT EXISTS idx_ocpp_device_id ON ocpp_charger (device_id);
COMMENT ON TABLE ocpp_charger IS 'OCPP充电桩';
