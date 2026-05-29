-- video_asset 一次性 OSS 上传回调 token（STS 签发时写入，回调校验后清空）
ALTER TABLE video_asset
    ADD COLUMN IF NOT EXISTS callback_token VARCHAR(64);

CREATE UNIQUE INDEX IF NOT EXISTS idx_video_asset_callback_token
    ON video_asset (callback_token)
    WHERE callback_token IS NOT NULL;
