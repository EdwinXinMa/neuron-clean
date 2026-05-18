-- nc_device 表：新增 phase_type 字段，标识 N3 Lite 的供电相型
-- 由 TopologyReport 上报，不再通过电流值推断
ALTER TABLE nc_device
    ADD COLUMN IF NOT EXISTS phase_type VARCHAR(10);

COMMENT ON COLUMN nc_device.phase_type IS 'N3 Lite 供电相型：single（单相）/ three（三相），由 TopologyReport 上报写入，null 表示旧设备未上报';
