-- App 用户多端推送注册 ID 表
CREATE TABLE IF NOT EXISTS app_user_registration
(
    id          VARCHAR(36)  NOT NULL PRIMARY KEY,
    user_id     VARCHAR(36)  NOT NULL,
    reg_id      VARCHAR(256) NOT NULL,
    create_time TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_reg UNIQUE (user_id, reg_id)
);

CREATE INDEX IF NOT EXISTS idx_user_registration_user_id ON app_user_registration (user_id);
