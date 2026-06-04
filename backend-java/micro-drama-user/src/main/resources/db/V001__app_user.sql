-- user_db：C 端用户（Telegram Mini App / 开发占位 / 后续 Web3）
CREATE TABLE IF NOT EXISTS app_user (
    id              UUID PRIMARY KEY,
    telegram_id     VARCHAR(64) UNIQUE,
    nickname        VARCHAR(128) NOT NULL DEFAULT '',
    avatar          VARCHAR(1024) NOT NULL DEFAULT '',
    balance         DECIMAL(18, 2) NOT NULL DEFAULT 0,
    wallet_address  VARCHAR(128),
    status          INTEGER NOT NULL DEFAULT 1,
    auth_provider   VARCHAR(32) NOT NULL DEFAULT 'telegram',
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_app_user_telegram ON app_user (telegram_id);
CREATE INDEX IF NOT EXISTS idx_app_user_wallet ON app_user (wallet_address) WHERE wallet_address IS NOT NULL;
