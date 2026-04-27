-- 固件最新版本维护表（每个设备类型一条记录）
CREATE TABLE IF NOT EXISTS nc_firmware_latest (
    id              VARCHAR(36) PRIMARY KEY,
    device_type     VARCHAR(32) NOT NULL UNIQUE,
    latest_version  VARCHAR(32),
    latest_firmware_id VARCHAR(36),
    previous_version VARCHAR(32),
    latest_upload_time TIMESTAMP,
    release_notes   TEXT,
    version_log     TEXT
);

COMMENT ON TABLE nc_firmware_latest IS '固件最新版本维护';
COMMENT ON COLUMN nc_firmware_latest.device_type IS '设备类型';
COMMENT ON COLUMN nc_firmware_latest.latest_version IS '最新版本号';
COMMENT ON COLUMN nc_firmware_latest.latest_firmware_id IS '最新固件ID';
COMMENT ON COLUMN nc_firmware_latest.previous_version IS '上一个版本号';
COMMENT ON COLUMN nc_firmware_latest.latest_upload_time IS '最新固件上传时间';
COMMENT ON COLUMN nc_firmware_latest.release_notes IS '最新版本说明';
COMMENT ON COLUMN nc_firmware_latest.version_log IS '版本历史日志';
