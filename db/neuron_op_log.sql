-- NeuronCloud 设备操作日志表
-- 记录 OTA升级、DLM修改、远程重启等操作记录

CREATE TABLE IF NOT EXISTS nc_op_log (
    id            VARCHAR(36)  NOT NULL PRIMARY KEY,
    device_sn     VARCHAR(64)  NOT NULL,             -- 设备序列号
    op_type       VARCHAR(30)  NOT NULL,             -- OTA_UPGRADE / DLM_CONFIG / REMOTE_REBOOT / REMOTE_RESET
    op_content    VARCHAR(500),                       -- 操作内容描述，如 "v1.0.2 → v1.0.3" / "32A → 40A"
    op_result     VARCHAR(10)  DEFAULT 'SUCCESS',    -- SUCCESS / FAIL
    fail_reason   VARCHAR(500),                       -- 失败原因
    op_user       VARCHAR(64),                       -- 操作人用户名
    op_time       TIMESTAMP    NOT NULL,             -- 操作时间
    create_time   TIMESTAMP    DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_nc_op_log_sn   ON nc_op_log(device_sn);
CREATE INDEX IF NOT EXISTS idx_nc_op_log_time ON nc_op_log(op_time DESC);
