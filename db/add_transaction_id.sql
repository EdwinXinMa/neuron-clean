-- nc_charging_session 表新增 transaction_id 字段（OCPP 事务ID，用于 RemoteStopTransaction）
ALTER TABLE nc_charging_session ADD COLUMN IF NOT EXISTS transaction_id INTEGER;

COMMENT ON COLUMN nc_charging_session.transaction_id IS 'OCPP transactionId（云端生成，用于 RemoteStopTransaction）';
