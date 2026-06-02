-- ==========================================
-- 初始化 Schema 与 扩展
-- ==========================================
DROP SCHEMA IF EXISTS farm CASCADE;
CREATE SCHEMA IF NOT EXISTS farm;

CREATE EXTENSION IF NOT EXISTS citext;



-- ==========================================
-- 0. 默认资源配置表
-- ==========================================
CREATE TABLE farm.asset_defaults
(
    id                          BIGINT              NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    asset_key                   VARCHAR(128)        NOT NULL,
    asset_url                   VARCHAR(1024)       NOT NULL DEFAULT '',
    description                 TEXT                    NULL,

    created_at                  TIMESTAMPTZ         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMPTZ         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by                  BIGINT                  NULL,
    updated_by                  BIGINT                  NULL,
    remark                      TEXT                    NULL,

    status                      SMALLINT            NOT NULL DEFAULT 1,
    is_deleted                  BOOLEAN             NOT NULL DEFAULT false,
    opt_lock_version            INT                 NOT NULL DEFAULT 0
);
COMMENT ON TABLE farm.asset_defaults IS '默认资源路径配置表';
CREATE UNIQUE INDEX uk_asset_defaults_key_active
    ON farm.asset_defaults(asset_key)
    WHERE is_deleted = false;


-- ==========================================
-- 1. 用户信息表
-- ==========================================
CREATE TABLE farm.users
(
    id                          BIGINT              NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    username                    citext              NOT NULL,
    nickname                    VARCHAR(500)        NOT NULL,
    password_hash               VARCHAR(500)        NOT NULL,
    email                       citext              NOT NULL,
    avatar_url                  VARCHAR(1024)       NOT NULL DEFAULT '',
    experience                  BIGINT              NOT NULL DEFAULT 0,
    score                       BIGINT              NOT NULL DEFAULT 0,
    coin                        BIGINT              NOT NULL DEFAULT 0,
    preferences_json            JSONB               NOT NULL DEFAULT '{}'::jsonb,

    created_at                  TIMESTAMPTZ         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMPTZ         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by                  BIGINT                  NULL,
    updated_by                  BIGINT                  NULL,
    remark                      TEXT                    NULL,

    status                      SMALLINT            NOT NULL DEFAULT 1,
    is_deleted                  BOOLEAN             NOT NULL DEFAULT false,
    opt_lock_version            INT                 NOT NULL DEFAULT 0
);
COMMENT ON TABLE farm.users IS '用户信息表';
CREATE UNIQUE INDEX uk_users_username_active
    ON farm.users (username)
    WHERE is_deleted = false;
CREATE UNIQUE INDEX uk_users_email_active
    ON farm.users (email)
    WHERE is_deleted = false;

-- ==========================================
-- 2. 种子品质表
-- ==========================================
CREATE TABLE farm.seed_qualities
(
    id                          BIGINT              NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    name                        VARCHAR(500)        NOT NULL,
    description                 TEXT                    NULL,

    created_at                  TIMESTAMPTZ         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMPTZ         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by                  BIGINT                  NULL,
    updated_by                  BIGINT                  NULL,
    remark                      TEXT                    NULL,

    status                      SMALLINT            NOT NULL DEFAULT 1,
    is_deleted                  BOOLEAN             NOT NULL DEFAULT false,
    opt_lock_version            INT                 NOT NULL DEFAULT 0
);
COMMENT ON TABLE farm.seed_qualities IS '种子品质表';
CREATE UNIQUE INDEX uk_seed_qualities_name_active
    ON farm.seed_qualities (name)
    WHERE is_deleted = false;

-- ==========================================
-- 3. 土壤类型表
-- ==========================================
CREATE TABLE farm.soil_types
(
    id                          BIGINT          NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    name                        VARCHAR(500)    NOT NULL,
    bit_code                    INT             NOT NULL,
    cover_image_url             VARCHAR(1024)   NOT NULL DEFAULT '',
    level                       SMALLINT        NOT NULL,
    unlock_experience_required  BIGINT          NOT NULL DEFAULT 0,
    grow_speed_multiplier       NUMERIC(5, 2)   NOT NULL DEFAULT 1.00,
    description                 TEXT                NULL,

    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by                  BIGINT              NULL,
    updated_by                  BIGINT              NULL,
    remark                      TEXT                NULL,

    status                      SMALLINT        NOT NULL DEFAULT 1,
    is_deleted                  BOOLEAN         NOT NULL DEFAULT false,
    opt_lock_version            INT             NOT NULL DEFAULT 0
);
COMMENT ON TABLE farm.soil_types IS '土壤类型表';
CREATE UNIQUE INDEX uk_soil_types_bit_code_active
    ON farm.soil_types(bit_code)
    WHERE is_deleted = false;
CREATE UNIQUE INDEX uk_soil_types_name_active
    ON farm.soil_types(name)
    WHERE is_deleted = false;

-- ==========================================
-- 4. 地块类型表
-- ==========================================
CREATE TABLE farm.plot_types
(
    id                                  BIGINT          NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    name                                VARCHAR(128)    NOT NULL,
    icon_url                            VARCHAR(1024)   NOT NULL DEFAULT '',
    cover_image_url                     VARCHAR(1024)   NOT NULL DEFAULT '',
    soil_type_id                        BIGINT          NOT NULL,
    unlock_required                     BOOLEAN         NOT NULL DEFAULT true,
    default_usable                      BOOLEAN         NOT NULL DEFAULT true,
    default_plot_unlock_experience_config BIGINT        NOT NULL DEFAULT 0,
    sort_order                          INT             NOT NULL DEFAULT 0,
    description                         TEXT                NULL,

    created_at                          TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                          TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by                          BIGINT              NULL,
    updated_by                          BIGINT              NULL,
    remark                              TEXT                NULL,

    status                              SMALLINT        NOT NULL DEFAULT 1,
    is_deleted                          BOOLEAN         NOT NULL DEFAULT false,
    opt_lock_version                    INT             NOT NULL DEFAULT 0
);
COMMENT ON TABLE farm.plot_types IS '地块类型表';
CREATE UNIQUE INDEX uk_plot_types_name_active
    ON farm.plot_types(name)
    WHERE is_deleted = false;
CREATE INDEX idx_plot_types_soil_type_active
    ON farm.plot_types(soil_type_id)
    WHERE is_deleted = false;

-- ==========================================
-- 5. 地块全局策略表
-- ==========================================
CREATE TABLE farm.plot_policies
(
    id                              BIGINT          NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    policy_name                     VARCHAR(128)    NOT NULL,
    policy_version                  VARCHAR(64)         NULL DEFAULT 'v1',
    active                          BOOLEAN         NOT NULL DEFAULT true,
    effective_scope                 VARCHAR(32)         NULL DEFAULT 'NEW_USER_ONLY',
    publish_status                  VARCHAR(32)         NULL DEFAULT 'DRAFT',
    default_total_plot_count        SMALLINT        NOT NULL,
    default_unlocked_plot_count     SMALLINT        NOT NULL,
    default_locked_plot_count       SMALLINT        NOT NULL,
    default_plot_type_id            BIGINT              NULL,
    default_lock_rule_code          VARCHAR(64)     NOT NULL DEFAULT 'DEFAULT_LOCKED',
    default_lock_reason             VARCHAR(255)    NOT NULL DEFAULT '待解锁',
    allocation_rule_json            JSONB           NOT NULL DEFAULT '{}'::jsonb,

    created_at                      TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                      TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by                      BIGINT              NULL,
    updated_by                      BIGINT              NULL,
    remark                          TEXT                NULL,

    status                          SMALLINT        NOT NULL DEFAULT 1,
    is_deleted                      BOOLEAN         NOT NULL DEFAULT false,
    opt_lock_version                INT             NOT NULL DEFAULT 0
);
COMMENT ON TABLE farm.plot_policies IS '地块全局策略表';
CREATE INDEX idx_plot_policies_active
    ON farm.plot_policies(active)
    WHERE is_deleted = false;

CREATE INDEX idx_plot_policies_publish_status_active
    ON farm.plot_policies(publish_status, active)
    WHERE is_deleted = false;

-- ==========================================
-- 6. 地块策略应用日志表
-- ==========================================
CREATE TABLE farm.plot_policy_apply_logs
(
    id                          BIGINT          NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    policy_id                   BIGINT          NOT NULL,
    applied_scope               VARCHAR(32)     NOT NULL DEFAULT 'MANUAL_APPLY',
    target_user_count           INT             NOT NULL DEFAULT 0,
    success_user_count          INT             NOT NULL DEFAULT 0,
    failed_user_count           INT             NOT NULL DEFAULT 0,
    request_payload_json        JSONB           NOT NULL DEFAULT '{}'::jsonb,
    result_snapshot_json        JSONB           NOT NULL DEFAULT '{}'::jsonb,
    applied_by                  BIGINT              NULL,
    applied_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by                  BIGINT              NULL,
    updated_by                  BIGINT              NULL,
    remark                      TEXT                NULL,

    status                      SMALLINT        NOT NULL DEFAULT 1,
    is_deleted                  BOOLEAN         NOT NULL DEFAULT false,
    opt_lock_version            INT             NOT NULL DEFAULT 0
);
COMMENT ON TABLE farm.plot_policy_apply_logs IS '地块策略应用日志表';
CREATE INDEX idx_plot_policy_apply_logs_policy_active
    ON farm.plot_policy_apply_logs(policy_id, applied_at DESC)
    WHERE is_deleted = false;

-- ==========================================
-- 7. 用户地块分配策略表
-- ==========================================
CREATE TABLE farm.user_plot_allocations
(
    id                          BIGINT          NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id                     BIGINT          NOT NULL,
    active                      BOOLEAN         NOT NULL DEFAULT true,
    total_plot_count            SMALLINT        NOT NULL,
    unlocked_plot_count         SMALLINT        NOT NULL,
    locked_plot_count           SMALLINT        NOT NULL,
    default_plot_type_id        BIGINT              NULL,
    lock_rule_code              VARCHAR(64)     NOT NULL DEFAULT 'DEFAULT_LOCKED',
    lock_reason                 VARCHAR(255)    NOT NULL DEFAULT '待解锁',
    allocation_rule_json        JSONB           NOT NULL DEFAULT '{}'::jsonb,
    applied_at                  TIMESTAMPTZ         NULL,

    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by                  BIGINT              NULL,
    updated_by                  BIGINT              NULL,
    remark                      TEXT                NULL,

    status                      SMALLINT        NOT NULL DEFAULT 1,
    is_deleted                  BOOLEAN         NOT NULL DEFAULT false,
    opt_lock_version            INT             NOT NULL DEFAULT 0
);
COMMENT ON TABLE farm.user_plot_allocations IS '用户地块分配策略表';
CREATE UNIQUE INDEX uk_user_plot_allocations_user_active
    ON farm.user_plot_allocations(user_id)
    WHERE is_deleted = false;
CREATE INDEX idx_user_plot_allocations_active
    ON farm.user_plot_allocations(active)
    WHERE is_deleted = false;

-- ==========================================
-- 7. 生长阶段类型字典表
-- ==========================================
CREATE TABLE farm.growth_stages
(
    id                          BIGINT          NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    name                        VARCHAR(500)    NOT NULL,
    description                 TEXT                NULL,

    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by                  BIGINT              NULL,
    updated_by                  BIGINT              NULL,
    remark                      TEXT                NULL,

    status                      SMALLINT        NOT NULL DEFAULT 1,
    is_deleted                  BOOLEAN         NOT NULL DEFAULT false,
    opt_lock_version            INT             NOT NULL DEFAULT 0
);
COMMENT ON TABLE farm.growth_stages IS '生长阶段类型字典表';
CREATE UNIQUE INDEX uk_growth_stages_name_active
    ON farm.growth_stages(name)
    WHERE is_deleted = false;

-- ==========================================
-- 8. 种子类型配置表
-- ==========================================
CREATE TABLE farm.seed_types
(
    id                          BIGINT          NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    name                        VARCHAR(500)    NOT NULL,
    cover_image_url             VARCHAR(1024)   NOT NULL DEFAULT '',
    seed_quality_id             BIGINT          NOT NULL,
    enable_soil_type_bits       BIGINT          NOT NULL,
    level                       SMALLINT        NOT NULL,
    unlock_experience_required  BIGINT          NOT NULL DEFAULT 0,
    description                 TEXT                NULL,

    -- 机制与事件配置
    max_bug_limit               SMALLINT        NOT NULL DEFAULT 0,
    max_harvest_count           SMALLINT        NOT NULL DEFAULT 1,
    regrow_stage_index          SMALLINT            NULL, -- 【优化】多次收获作物，收获后退回的阶段索引
    harvest_stage_index         SMALLINT            NULL, -- 收获阶段索引，最后一阶段保留为枯萎阶段

    -- 经济数值配置
    price                       BIGINT          NOT NULL DEFAULT 0,
    harvest_experience          BIGINT          NOT NULL DEFAULT 0,
    harvest_fruit_number        INT             NOT NULL DEFAULT 0,
    fruit_loss_per_bug          INT             NOT NULL DEFAULT 1,
    bug_kill_coin_reward        BIGINT          NOT NULL DEFAULT 0,
    bug_kill_experience_reward  BIGINT          NOT NULL DEFAULT 0,
    bug_kill_score_reward       BIGINT          NOT NULL DEFAULT 0,
    harvest_score               BIGINT          NOT NULL DEFAULT 0,
    fruit_price                 BIGINT          NOT NULL DEFAULT 0,

    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by                  BIGINT              NULL,
    updated_by                  BIGINT              NULL,
    remark                      TEXT                NULL,

    status                      SMALLINT        NOT NULL DEFAULT 1,
    is_deleted                  BOOLEAN         NOT NULL DEFAULT false,
    opt_lock_version            INT             NOT NULL DEFAULT 0
);
COMMENT ON TABLE farm.seed_types IS '种子类型配置表';
CREATE UNIQUE INDEX uk_seed_types_name_active
    ON farm.seed_types (name)
    WHERE is_deleted = false;

-- ==========================================
-- 9. 种子生长过程配置表
-- ==========================================
CREATE TABLE farm.seed_growth_stages
(
    id                          BIGINT          NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    seed_type_id                BIGINT          NOT NULL,
    growth_stage_id             BIGINT          NOT NULL,

    stage_index                 SMALLINT        NOT NULL,
    duration_seconds            INT             NOT NULL,
    asset_url                   VARCHAR(1024)       NOT NULL DEFAULT '',
    bug_probability             NUMERIC(5, 4)   NOT NULL DEFAULT 0.0000,

    width                       INT             NOT NULL DEFAULT 0,
    height                      INT             NOT NULL DEFAULT 0,
    offset_x                    INT             NOT NULL DEFAULT 0,
    offset_y                    INT             NOT NULL DEFAULT 0,

    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by                  BIGINT              NULL,
    updated_by                  BIGINT              NULL,
    remark                      TEXT                NULL,

    status                      SMALLINT        NOT NULL DEFAULT 1,
    is_deleted                  BOOLEAN         NOT NULL DEFAULT false,
    opt_lock_version            INT             NOT NULL DEFAULT 0
);
COMMENT ON TABLE farm.seed_growth_stages IS '种子生长过程配置表';
CREATE UNIQUE INDEX uk_seed_growth_stage_index
    ON farm.seed_growth_stages(seed_type_id, stage_index)
    WHERE is_deleted = false;

-- ==========================================
-- 10. 用户种子背包表
-- ==========================================
CREATE TABLE farm.user_seeds
(
    id                          BIGINT          NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id                     BIGINT          NOT NULL,
    seed_type_id                BIGINT          NOT NULL,
    quantity                    BIGINT          NOT NULL DEFAULT 0,
    frozen_quantity             BIGINT          NOT NULL DEFAULT 0, -- 预留：交易/加工/批量操作时冻结库存

    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by                  BIGINT              NULL,
    updated_by                  BIGINT              NULL,
    remark                      TEXT                NULL,

    status                      SMALLINT        NOT NULL DEFAULT 1,
    is_deleted                  BOOLEAN         NOT NULL DEFAULT false,
    opt_lock_version            INT             NOT NULL DEFAULT 0
);
COMMENT ON TABLE farm.user_seeds IS '用户种子背包表';
CREATE UNIQUE INDEX uk_user_seeds_active
    ON farm.user_seeds(user_id, seed_type_id)
    WHERE is_deleted = false;

-- ==========================================
-- 11. 用户地块表
-- ==========================================
CREATE TABLE farm.user_plots
(
    id                          BIGINT          NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id                     BIGINT          NOT NULL,
    soil_type_id                BIGINT          NOT NULL,
    plot_index                  SMALLINT        NOT NULL,
    unlock_experience_required  BIGINT          NOT NULL DEFAULT 0,
    is_locked                   BOOLEAN         NOT NULL DEFAULT false,
    unlocked_at                 TIMESTAMPTZ         NULL,
    lock_reason                 VARCHAR(255)        NULL,

    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by                  BIGINT              NULL,
    updated_by                  BIGINT              NULL,
    remark                      TEXT                NULL,

    status                      SMALLINT        NOT NULL DEFAULT 1,
    is_deleted                  BOOLEAN         NOT NULL DEFAULT false,
    opt_lock_version            INT             NOT NULL DEFAULT 0
);
COMMENT ON TABLE farm.user_plots IS '用户地块表';
CREATE UNIQUE INDEX uk_user_plot_index
    ON farm.user_plots(user_id, plot_index)
    WHERE is_deleted = false;

-- ==========================================
-- 12. 用户种植作物表
-- ==========================================
CREATE TABLE farm.user_crops
(
    id                          BIGINT          NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id                     BIGINT          NOT NULL,
    plot_id                     BIGINT          NOT NULL,
    seed_type_id                BIGINT          NOT NULL,

    planted_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    stage_started_at            TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_harvest_at             TIMESTAMPTZ         NULL,
    matured_at                  TIMESTAMPTZ         NULL,
    withered_at                 TIMESTAMPTZ         NULL,
    expected_ripe_at            TIMESTAMPTZ         NULL,
    expected_withered_at        TIMESTAMPTZ         NULL,
    harvest_count               SMALLINT        NOT NULL DEFAULT 0,
    current_stage_index         SMALLINT        NOT NULL DEFAULT 1,
    grow_status                 SMALLINT        NOT NULL DEFAULT 1, -- 1:生长中, 2:成熟待收, 3:已枯萎
    bug_count                   SMALLINT        NOT NULL DEFAULT 0,
    last_bug_at                 TIMESTAMPTZ         NULL,
    last_care_at                TIMESTAMPTZ         NULL,

    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by                  BIGINT              NULL,
    updated_by                  BIGINT              NULL,
    remark                      TEXT                NULL,

    status                      SMALLINT        NOT NULL DEFAULT 1,
    is_deleted                  BOOLEAN         NOT NULL DEFAULT false,
    opt_lock_version            INT             NOT NULL DEFAULT 0
);
COMMENT ON TABLE farm.user_crops IS '用户种植作物表';
CREATE UNIQUE INDEX uk_plot_active_crop
    ON farm.user_crops(plot_id)
    WHERE is_deleted = false;

-- ==========================================
-- 13. 用户果实仓库表
-- ==========================================
CREATE TABLE farm.user_fruits
(
    id                          BIGINT          NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id                     BIGINT          NOT NULL,
    seed_type_id                BIGINT          NOT NULL, -- 指代该种子产出的果实
    quantity                    BIGINT          NOT NULL DEFAULT 0,
    frozen_quantity             BIGINT          NOT NULL DEFAULT 0, -- 预留：交易/加工/批量操作时冻结库存

    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by                  BIGINT              NULL,
    updated_by                  BIGINT              NULL,
    remark                      TEXT                NULL,

    status                      SMALLINT        NOT NULL DEFAULT 1,
    is_deleted                  BOOLEAN         NOT NULL DEFAULT false,
    opt_lock_version            INT             NOT NULL DEFAULT 0
);
COMMENT ON TABLE farm.user_fruits IS '用户果实仓库表';
CREATE UNIQUE INDEX uk_user_fruits_active
    ON farm.user_fruits(user_id, seed_type_id)
    WHERE is_deleted = false;

-- ==========================================
-- 14. 用户资产流水表
-- ==========================================
CREATE TABLE farm.user_asset_flows
(
    id                          BIGINT          NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id                     BIGINT          NOT NULL,
    asset_type                  VARCHAR(32)     NOT NULL, -- COIN / SCORE / EXPERIENCE
    operation_type              VARCHAR(32)     NOT NULL, -- INCOME / EXPENSE / ADJUST
    change_amount               BIGINT          NOT NULL,
    before_amount               BIGINT          NOT NULL DEFAULT 0,
    after_amount                BIGINT          NOT NULL DEFAULT 0,
    biz_type                    VARCHAR(64)     NOT NULL DEFAULT '', -- HARVEST / BUY_SEED / SELL_FRUIT / ADMIN
    biz_id                      VARCHAR(128)        NULL,
    occurred_at                 TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ext_data                    JSONB               NULL,

    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by                  BIGINT              NULL,
    updated_by                  BIGINT              NULL,
    remark                      TEXT                NULL,

    status                      SMALLINT        NOT NULL DEFAULT 1,
    is_deleted                  BOOLEAN         NOT NULL DEFAULT false,
    opt_lock_version            INT             NOT NULL DEFAULT 0
);
COMMENT ON TABLE farm.user_asset_flows IS '用户资产流水表';

-- ==========================================
-- 15. 用户库存流水表
-- ==========================================
CREATE TABLE farm.user_inventory_flows
(
    id                          BIGINT          NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id                     BIGINT          NOT NULL,
    item_type                   VARCHAR(32)     NOT NULL, -- SEED / FRUIT
    seed_type_id                BIGINT          NOT NULL,
    operation_type              VARCHAR(32)     NOT NULL, -- INCOME / EXPENSE / FREEZE / UNFREEZE / ADJUST
    change_amount               BIGINT          NOT NULL,
    before_amount               BIGINT          NOT NULL DEFAULT 0,
    after_amount                BIGINT          NOT NULL DEFAULT 0,
    before_frozen_amount        BIGINT          NOT NULL DEFAULT 0,
    after_frozen_amount         BIGINT          NOT NULL DEFAULT 0,
    biz_type                    VARCHAR(64)     NOT NULL DEFAULT '', -- HARVEST / BUY_SEED / SELL_FRUIT / ADMIN
    biz_id                      VARCHAR(128)        NULL,
    occurred_at                 TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ext_data                    JSONB               NULL,

    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by                  BIGINT              NULL,
    updated_by                  BIGINT              NULL,
    remark                      TEXT                NULL,

    status                      SMALLINT        NOT NULL DEFAULT 1,
    is_deleted                  BOOLEAN         NOT NULL DEFAULT false,
    opt_lock_version            INT             NOT NULL DEFAULT 0
);
COMMENT ON TABLE farm.user_inventory_flows IS '用户库存流水表';

-- ==========================================
-- 16. 作物行为日志表
-- ==========================================
CREATE TABLE farm.user_crop_action_logs
(
    id                          BIGINT          NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id                     BIGINT          NOT NULL,
    plot_id                     BIGINT          NOT NULL,
    crop_id                     BIGINT              NULL,
    seed_type_id                BIGINT              NULL,
    action_type                 VARCHAR(32)     NOT NULL, -- PLANT / HARVEST / REMOVE_BUG / CLEAR / WITHER
    action_result               VARCHAR(32)     NOT NULL DEFAULT 'SUCCESS', -- SUCCESS / FAIL
    action_at                   TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    action_snapshot             JSONB               NULL,

    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by                  BIGINT              NULL,
    updated_by                  BIGINT              NULL,
    remark                      TEXT                NULL,

    status                      SMALLINT        NOT NULL DEFAULT 1,
    is_deleted                  BOOLEAN         NOT NULL DEFAULT false,
    opt_lock_version            INT             NOT NULL DEFAULT 0
);
COMMENT ON TABLE farm.user_crop_action_logs IS '作物行为日志表';

-- ==========================================
-- 17. 请求幂等记录表
-- ==========================================
CREATE TABLE farm.request_idempotencies
(
    id                          BIGINT          NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id                     BIGINT          NOT NULL,
    biz_type                    VARCHAR(64)     NOT NULL, -- BUY_SEED / SELL_FRUIT / PLANT / HARVEST
    request_id                  VARCHAR(128)    NOT NULL,
    process_status              VARCHAR(16)     NOT NULL DEFAULT 'PROCESSING', -- PROCESSING / SUCCESS / FAILED
    response_payload            JSONB               NULL,
    error_message               VARCHAR(500)        NULL,
    finished_at                 TIMESTAMPTZ         NULL,

    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by                  BIGINT              NULL,
    updated_by                  BIGINT              NULL,
    remark                      TEXT                NULL,

    status                      SMALLINT        NOT NULL DEFAULT 1,
    is_deleted                  BOOLEAN         NOT NULL DEFAULT false,
    opt_lock_version            INT             NOT NULL DEFAULT 0
);
COMMENT ON TABLE farm.request_idempotencies IS '请求幂等记录表';
