-- nc_dlm_history_allocation 表：allocated_current 单值拆分为三相 A/B/C
-- 单相设备只有 A 相有值，B/C 为 0
ALTER TABLE nc_dlm_history_allocation
    ADD COLUMN IF NOT EXISTS allocated_current_a real,
    ADD COLUMN IF NOT EXISTS allocated_current_b real,
    ADD COLUMN IF NOT EXISTS allocated_current_c real;

ALTER TABLE nc_dlm_history_allocation
    DROP COLUMN IF EXISTS allocated_current;

COMMENT ON COLUMN nc_dlm_history_allocation.allocated_current_a IS 'DLM 分配给该桩的电流 A 相（A），单相设备只上报此字段，B/C 为 0';
COMMENT ON COLUMN nc_dlm_history_allocation.allocated_current_b IS 'DLM 分配给该桩的电流 B 相（A），单相时为 0';
COMMENT ON COLUMN nc_dlm_history_allocation.allocated_current_c IS 'DLM 分配给该桩的电流 C 相（A），单相时为 0';
