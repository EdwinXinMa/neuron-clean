-- nc_charging_session 表新增 meter_start 字段（电表起始读数，用于计算本次充电电量）
ALTER TABLE nc_charging_session ADD COLUMN IF NOT EXISTS meter_start INTEGER;

COMMENT ON COLUMN nc_charging_session.meter_start IS '电表起始读数（Wh，StartTransaction 上报）';
