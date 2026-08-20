ALTER TABLE app_user
    ADD COLUMN IF NOT EXISTS timezone VARCHAR(64) NOT NULL DEFAULT 'Asia/Shanghai';

UPDATE app_user
SET timezone = 'Asia/Shanghai'
WHERE timezone IS NULL OR timezone = '';
