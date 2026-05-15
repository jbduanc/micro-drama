-- 会员套餐表：与 micro-drama-admin 实体 MemberPlans（@TableName("member_plan")）一致。
-- 在「admin 数据源」所连接的 PostgreSQL 库中执行（例如 user_db 或 platform_db，与 spring.datasource 一致）。
-- 若库中已有同名表，请先核对列后再执行或改用 Flyway/Liquibase 管理。

CREATE TABLE IF NOT EXISTS member_plan (
    id              VARCHAR(64) PRIMARY KEY,
    plan_name       VARCHAR(200) NOT NULL,
    price           NUMERIC(18, 2) NOT NULL DEFAULT 0,
    duration_days   INTEGER NOT NULL DEFAULT 0,
    status          SMALLINT NOT NULL DEFAULT 1,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE member_plan IS '会员套餐';
COMMENT ON COLUMN member_plan.id IS '主键 UUID（应用 ASSIGN_UUID）';
COMMENT ON COLUMN member_plan.plan_name IS '套餐名称';
COMMENT ON COLUMN member_plan.price IS '价格';
COMMENT ON COLUMN member_plan.duration_days IS '有效天数';
COMMENT ON COLUMN member_plan.status IS '状态';
COMMENT ON COLUMN member_plan.created_at IS '创建时间';

CREATE INDEX IF NOT EXISTS idx_member_plan_created_at ON member_plan (created_at DESC);
