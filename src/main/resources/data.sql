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



-- ==========================================
-- 农场基础字典与种子配置初始化数据（UTF-8）
-- 说明：仅做数据初始化，不做表结构变更
-- ==========================================

-- 0) 默认资源路径
INSERT INTO farm.asset_defaults (asset_key, asset_url, description)
SELECT 'avatar', '/oss/defaults/avatar/default-avatar.png', '用户头像默认图'
WHERE NOT EXISTS (SELECT 1 FROM farm.asset_defaults WHERE asset_key = 'avatar' AND is_deleted = false);

INSERT INTO farm.asset_defaults (asset_key, asset_url, description)
SELECT 'seedCover', '/oss/defaults/seed/seed-cover-default.png', '种子封面默认图'
WHERE NOT EXISTS (SELECT 1 FROM farm.asset_defaults WHERE asset_key = 'seedCover' AND is_deleted = false);

INSERT INTO farm.asset_defaults (asset_key, asset_url, description)
SELECT 'seedStage', '/oss/defaults/seed/seed-stage-default.png', '种子阶段默认图'
WHERE NOT EXISTS (SELECT 1 FROM farm.asset_defaults WHERE asset_key = 'seedStage' AND is_deleted = false);

INSERT INTO farm.asset_defaults (asset_key, asset_url, description)
SELECT 'soilCover', '/oss/defaults/soil/soil-default.png', '土壤默认图'
WHERE NOT EXISTS (SELECT 1 FROM farm.asset_defaults WHERE asset_key = 'soilCover' AND is_deleted = false);

INSERT INTO farm.asset_defaults (asset_key, asset_url, description)
SELECT 'plotCover', '/oss/defaults/plot/plot-cover-default.png', '地块封面默认图'
WHERE NOT EXISTS (SELECT 1 FROM farm.asset_defaults WHERE asset_key = 'plotCover' AND is_deleted = false);

INSERT INTO farm.asset_defaults (asset_key, asset_url, description)
SELECT 'plotIcon', '/oss/defaults/plot/plot-icon-default.png', '地块图标默认图'
WHERE NOT EXISTS (SELECT 1 FROM farm.asset_defaults WHERE asset_key = 'plotIcon' AND is_deleted = false);

INSERT INTO farm.asset_defaults (asset_key, asset_url, description)
SELECT 'bgm', '/resources/sounds/bgm/Must%20Work%20to%20Eat.wav', '默认背景音乐'
WHERE NOT EXISTS (SELECT 1 FROM farm.asset_defaults WHERE asset_key = 'bgm' AND is_deleted = false);

-- 1) 种子品质字典
INSERT INTO farm.seed_qualities (name, description)
SELECT '普通', '普通品质种子'
WHERE NOT EXISTS (SELECT 1 FROM farm.seed_qualities WHERE name = '普通' AND is_deleted = false);

INSERT INTO farm.seed_qualities (name, description)
SELECT '优质', '优质品质种子'
WHERE NOT EXISTS (SELECT 1 FROM farm.seed_qualities WHERE name = '优质' AND is_deleted = false);

INSERT INTO farm.seed_qualities (name, description)
SELECT '稀有', '稀有品质种子'
WHERE NOT EXISTS (SELECT 1 FROM farm.seed_qualities WHERE name = '稀有' AND is_deleted = false);

-- 2) 土地类型字典（bit_code 用于位运算）
INSERT INTO farm.soil_types (name, bit_code, cover_image_url, level, unlock_experience_required, grow_speed_multiplier, description)
SELECT '黄土地', 1, '/oss/defaults/soil/soil-land-default.png', 1, 0, 1.00, '基础土地，适配多数作物'
WHERE NOT EXISTS (SELECT 1 FROM farm.soil_types WHERE bit_code = 1 AND is_deleted = false);

INSERT INTO farm.soil_types (name, bit_code, cover_image_url, level, unlock_experience_required, grow_speed_multiplier, description)
SELECT '黑土地', 2, '/oss/defaults/soil/soil-land-black-default.png', 2, 500, 0.90, '生长速度更快的改良土地'
WHERE NOT EXISTS (SELECT 1 FROM farm.soil_types WHERE bit_code = 2 AND is_deleted = false);

INSERT INTO farm.soil_types (name, bit_code, cover_image_url, level, unlock_experience_required, grow_speed_multiplier, description)
SELECT '金土地', 4, '/oss/defaults/soil/soil-land-gold-default.png', 3, 2000, 0.80, '高级土地，适配高等级作物'
WHERE NOT EXISTS (SELECT 1 FROM farm.soil_types WHERE bit_code = 4 AND is_deleted = false);

UPDATE farm.soil_types
SET cover_image_url = CASE bit_code
    WHEN 1 THEN '/oss/defaults/soil/soil-land-default.png'
    WHEN 2 THEN '/oss/defaults/soil/soil-land-black-default.png'
    WHEN 4 THEN '/oss/defaults/soil/soil-land-gold-default.png'
    ELSE cover_image_url
END,
updated_at = NOW(),
updated_by = 0
WHERE is_deleted = false
  AND bit_code IN (1, 2, 4)
  AND (cover_image_url IS NULL OR cover_image_url = '' OR cover_image_url LIKE '%positioning-land%');

-- 3) 生长阶段字典
INSERT INTO farm.growth_stages (name, description)
SELECT '种子', '播种后的初始阶段'
WHERE NOT EXISTS (SELECT 1 FROM farm.growth_stages WHERE name = '种子' AND is_deleted = false);

INSERT INTO farm.growth_stages (name, description)
SELECT '发芽', '发芽阶段'
WHERE NOT EXISTS (SELECT 1 FROM farm.growth_stages WHERE name = '发芽' AND is_deleted = false);

INSERT INTO farm.growth_stages (name, description)
SELECT '生长期', '快速生长阶段'
WHERE NOT EXISTS (SELECT 1 FROM farm.growth_stages WHERE name = '生长期' AND is_deleted = false);

INSERT INTO farm.growth_stages (name, description)
SELECT '开花', '开花授粉阶段'
WHERE NOT EXISTS (SELECT 1 FROM farm.growth_stages WHERE name = '开花' AND is_deleted = false);

INSERT INTO farm.growth_stages (name, description)
SELECT '成熟', '可收获阶段'
WHERE NOT EXISTS (SELECT 1 FROM farm.growth_stages WHERE name = '成熟' AND is_deleted = false);

-- 4) 种子类型配置（按名称幂等）
INSERT INTO farm.seed_types
(
    name, seed_quality_id, enable_soil_type_bits, level, description,
    max_bug_limit, max_harvest_count, regrow_stage_index,
    price, harvest_experience, harvest_fruit_number, fruit_loss_per_bug,
    bug_kill_coin_reward, bug_kill_experience_reward, bug_kill_score_reward,
    fruit_price, harvest_score
)
SELECT
    '草莓', q.id, 1, 1, '草莓果实鲜红，口感清甜',
    3, 1, NULL,
    5, 50, 10, 1,
    2, 2, 1,
    10, 10
FROM farm.seed_qualities q
WHERE q.name = '普通' AND q.is_deleted = false
  AND NOT EXISTS (SELECT 1 FROM farm.seed_types s WHERE s.name = '草莓' AND s.is_deleted = false);

INSERT INTO farm.seed_types
(
    name, seed_quality_id, enable_soil_type_bits, level, description,
    max_bug_limit, max_harvest_count, regrow_stage_index,
    price, harvest_experience, harvest_fruit_number, fruit_loss_per_bug,
    bug_kill_coin_reward, bug_kill_experience_reward, bug_kill_score_reward,
    fruit_price, harvest_score
)
SELECT
    '茄子', q.id, 1, 1, '常见蔬果作物，产量稳定',
    3, 1, NULL,
    6, 50, 15, 1,
    2, 2, 1,
    20, 15
FROM farm.seed_qualities q
WHERE q.name = '优质' AND q.is_deleted = false
  AND NOT EXISTS (SELECT 1 FROM farm.seed_types s WHERE s.name = '茄子' AND s.is_deleted = false);

INSERT INTO farm.seed_types
(
    name, seed_quality_id, enable_soil_type_bits, level, description,
    max_bug_limit, max_harvest_count, regrow_stage_index,
    price, harvest_experience, harvest_fruit_number, fruit_loss_per_bug,
    bug_kill_coin_reward, bug_kill_experience_reward, bug_kill_score_reward,
    fruit_price, harvest_score
)
SELECT
    '玉米', q.id, 3, 2, '高产作物，适配黄土地和黑土地',
    4, 1, NULL,
    10, 80, 18, 1,
    3, 3, 2,
    24, 20
FROM farm.seed_qualities q
WHERE q.name = '稀有' AND q.is_deleted = false
  AND NOT EXISTS (SELECT 1 FROM farm.seed_types s WHERE s.name = '玉米' AND s.is_deleted = false);

-- 5) 种子生长阶段配置（每种作物 5 阶段）
WITH stage_dict AS (
    SELECT id, name FROM farm.growth_stages WHERE is_deleted = false
),
seed_dict AS (
    SELECT id, name FROM farm.seed_types WHERE is_deleted = false AND name IN ('草莓', '茄子', '玉米')
)
INSERT INTO farm.seed_growth_stages
(
    seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability,
    width, height, offset_x, offset_y
)
SELECT
    sd.id,
    gd.id,
    cfg.stage_index,
    cfg.duration_seconds,
    '',
    cfg.bug_probability,
    100, 140, 50, 80
FROM seed_dict sd
JOIN (
    SELECT '草莓' AS seed_name, '种子' AS stage_name, 1 AS stage_index, 20 AS duration_seconds, 0.0100 AS bug_probability
    UNION ALL SELECT '草莓', '发芽', 2, 25, 0.0200
    UNION ALL SELECT '草莓', '生长期', 3, 30, 0.0500
    UNION ALL SELECT '草莓', '开花', 4, 35, 0.0400
    UNION ALL SELECT '草莓', '成熟', 5, 30, 0.0200

    UNION ALL SELECT '茄子', '种子', 1, 50, 0.0150
    UNION ALL SELECT '茄子', '发芽', 2, 60, 0.0300
    UNION ALL SELECT '茄子', '生长期', 3, 60, 0.0600
    UNION ALL SELECT '茄子', '开花', 4, 60, 0.0500
    UNION ALL SELECT '茄子', '成熟', 5, 60, 0.0200

    UNION ALL SELECT '玉米', '种子', 1, 50, 0.0200
    UNION ALL SELECT '玉米', '发芽', 2, 60, 0.0400
    UNION ALL SELECT '玉米', '生长期', 3, 60, 0.0700
    UNION ALL SELECT '玉米', '开花', 4, 65, 0.0600
    UNION ALL SELECT '玉米', '成熟', 5, 65, 0.0300
) cfg ON cfg.seed_name = sd.name
JOIN stage_dict gd ON gd.name = cfg.stage_name
WHERE NOT EXISTS (
    SELECT 1
    FROM farm.seed_growth_stages sgs
    WHERE sgs.seed_type_id = sd.id
      AND sgs.stage_index = cfg.stage_index
      AND sgs.is_deleted = false
);

DELETE FROM farm.users WHERE true;
-- 6) 用户初始化数据（幂等）
INSERT INTO farm.users
(
    username, nickname, password_hash, email, avatar_url,
    experience, score, coin, preferences_json,
    created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version
)
SELECT
    'liubei', '刘备', '123456', 'liubei@farm.local', '/oss/defaults/avatar/default-avatar.png',
    2000, 575, 4090, '{"audio":{"effectEnabled":true,"effectVolume":0.8,"bgmEnabled":true,"bgmVolume":0.6}}'::jsonb,
    NOW(), NOW(), 0, 0, 'init user', 1, false, 0
WHERE NOT EXISTS (SELECT 1 FROM farm.users WHERE username = 'liubei' AND is_deleted = false);

INSERT INTO farm.users
(
    username, nickname, password_hash, email, avatar_url,
    experience, score, coin, preferences_json,
    created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version
)
SELECT
    'caocao', '曹操', '123456', 'caocao@farm.local', '/oss/defaults/avatar/default-avatar.png',
    1800, 420, 2600, '{"audio":{"effectEnabled":true,"effectVolume":0.8,"bgmEnabled":true,"bgmVolume":0.6}}'::jsonb,
    NOW(), NOW(), 0, 0, 'init user', 1, false, 0
WHERE NOT EXISTS (SELECT 1 FROM farm.users WHERE username = 'caocao' AND is_deleted = false);

INSERT INTO farm.users
(
    username, nickname, password_hash, email, avatar_url,
    experience, score, coin, preferences_json,
    created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version
)
SELECT
    'sunquan', '孙权', '123456', 'sunquan@farm.local', '/oss/defaults/avatar/default-avatar.png',
    1600, 360, 2100, '{"audio":{"effectEnabled":true,"effectVolume":0.8,"bgmEnabled":true,"bgmVolume":0.6}}'::jsonb,
    NOW(), NOW(), 0, 0, 'init user', 1, false, 0
WHERE NOT EXISTS (SELECT 1 FROM farm.users WHERE username = 'sunquan' AND is_deleted = false);

-- 7) 地块类型初始化（幂等）
INSERT INTO farm.plot_types
(
    name, icon_url, soil_type_id, unlock_required, default_usable,
    default_plot_unlock_experience_config, sort_order, description,
    created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version
)
SELECT
    '普通耕地', '', st.id, false, true,
    0, 1, '默认可用地块类型',
    NOW(), NOW(), 0, 0, 'init plot type', 1, false, 0
FROM farm.soil_types st
WHERE st.name = '黄土地' AND st.is_deleted = false
  AND NOT EXISTS (SELECT 1 FROM farm.plot_types pt WHERE pt.name = '普通耕地' AND pt.is_deleted = false);

INSERT INTO farm.plot_types
(
    name, icon_url, soil_type_id, unlock_required, default_usable,
    default_plot_unlock_experience_config, sort_order, description,
    created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version
)
SELECT
    '改良耕地', '', st.id, true, true,
    500, 2, '中级地块类型，需要解锁',
    NOW(), NOW(), 0, 0, 'init plot type', 1, false, 0
FROM farm.soil_types st
WHERE st.name = '黑土地' AND st.is_deleted = false
  AND NOT EXISTS (SELECT 1 FROM farm.plot_types pt WHERE pt.name = '改良耕地' AND pt.is_deleted = false);

INSERT INTO farm.plot_types
(
    name, icon_url, soil_type_id, unlock_required, default_usable,
    default_plot_unlock_experience_config, sort_order, description,
    created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version
)
SELECT
    '高级耕地', '', st.id, true, false,
    2000, 3, '高级地块类型，偏后期解锁',
    NOW(), NOW(), 0, 0, 'init plot type', 1, false, 0
FROM farm.soil_types st
WHERE st.name = '金土地' AND st.is_deleted = false
  AND NOT EXISTS (SELECT 1 FROM farm.plot_types pt WHERE pt.name = '高级耕地' AND pt.is_deleted = false);

-- 8) 地块全局策略初始化（幂等）
WITH default_plot_type AS (
    SELECT id
    FROM farm.plot_types
    WHERE name = '普通耕地' AND is_deleted = false
    ORDER BY id ASC
    LIMIT 1
)
INSERT INTO farm.plot_policies
(
    policy_name, active, default_total_plot_count, default_unlocked_plot_count, default_locked_plot_count,
    default_plot_type_id, default_lock_rule_code, default_lock_reason, allocation_rule_json,
    created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version
)
SELECT
    'default-policy-v1', true, 6, 2, 4,
    dpt.id, 'EXP_REQUIRED', '经验不足，暂未解锁',
    CASE
        WHEN dpt.id IS NULL THEN '{"default":{"total":6,"locked":4}}'::jsonb
        ELSE jsonb_build_object(dpt.id::text, jsonb_build_object('total', 6, 'locked', 4))
    END,
    NOW(), NOW(), 0, 0, 'init plot policy', 1, false, 0
FROM (SELECT 1 AS marker) t
LEFT JOIN default_plot_type dpt ON true
WHERE NOT EXISTS (SELECT 1 FROM farm.plot_policies pp WHERE pp.policy_name = 'default-policy-v1' AND pp.is_deleted = false);

-- 9) 用户地块分配策略初始化（幂等）
WITH default_plot_type AS (
    SELECT id
    FROM farm.plot_types
    WHERE name = '普通耕地' AND is_deleted = false
    ORDER BY id ASC
    LIMIT 1
)
INSERT INTO farm.user_plot_allocations
(
    user_id, active, total_plot_count, unlocked_plot_count, locked_plot_count,
    default_plot_type_id, lock_rule_code, lock_reason, allocation_rule_json, applied_at,
    created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version
)
SELECT
    u.id, true, cfg.total_plot_count, cfg.unlocked_plot_count, cfg.total_plot_count - cfg.unlocked_plot_count,
    dpt.id, 'EXP_REQUIRED', '经验不足，暂未解锁', cfg.allocation_rule_json, NOW(),
    NOW(), NOW(), 0, 0, 'init user allocation', 1, false, 0
FROM farm.users u
LEFT JOIN default_plot_type dpt ON true
JOIN (
    SELECT 'liubei'::varchar AS username, 8::smallint AS total_plot_count, 3::smallint AS unlocked_plot_count, '{"default":{"total":8,"locked":5}}'::jsonb AS allocation_rule_json
    UNION ALL
    SELECT 'caocao', 6, 2, '{"default":{"total":6,"locked":4}}'::jsonb
    UNION ALL
    SELECT 'sunquan', 6, 1, '{"default":{"total":6,"locked":5}}'::jsonb
) cfg ON cfg.username = u.username
WHERE u.is_deleted = false
  AND NOT EXISTS (
        SELECT 1
        FROM farm.user_plot_allocations upa
        WHERE upa.user_id = u.id
          AND upa.is_deleted = false
    );

-- 10) 用户地块实例初始化（幂等）
WITH fallback_soil AS (
    SELECT st.id AS soil_type_id
    FROM farm.soil_types st
    WHERE st.is_deleted = false
    ORDER BY st.level ASC, st.id ASC
    LIMIT 1
),
alloc AS (
    SELECT
        u.id AS user_id,
        u.username,
        COALESCE(upa.total_plot_count, 6)::int AS total_plot_count,
        COALESCE(upa.unlocked_plot_count, 1)::int AS unlocked_plot_count,
        COALESCE(upa.lock_reason, '待解锁') AS lock_reason,
        upa.default_plot_type_id
    FROM farm.users u
    LEFT JOIN farm.user_plot_allocations upa
        ON upa.user_id = u.id
       AND upa.active = true
       AND upa.is_deleted = false
    WHERE u.is_deleted = false
      AND u.username IN ('liubei', 'caocao', 'sunquan')
),
alloc_with_type AS (
    SELECT
        a.user_id,
        a.username,
        a.total_plot_count,
        a.unlocked_plot_count,
        a.lock_reason,
        pt.soil_type_id,
        pt.unlock_required,
        pt.default_plot_unlock_experience_config
    FROM alloc a
    LEFT JOIN farm.plot_types pt
        ON pt.id = a.default_plot_type_id
       AND pt.is_deleted = false
)
INSERT INTO farm.user_plots
(
    user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason,
    created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version
)
SELECT
    awt.user_id,
    COALESCE(awt.soil_type_id, fs.soil_type_id) AS soil_type_id,
    gs.plot_index::smallint AS plot_index,
    CASE
        WHEN gs.plot_index <= awt.unlocked_plot_count THEN 0
        WHEN COALESCE(awt.unlock_required, true) = false THEN 0
        ELSE COALESCE(awt.default_plot_unlock_experience_config, 0) + ((gs.plot_index - awt.unlocked_plot_count - 1) * 200)
    END AS unlock_experience_required,
    (gs.plot_index > awt.unlocked_plot_count) AS is_locked,
    CASE WHEN gs.plot_index <= awt.unlocked_plot_count THEN NOW() ELSE NULL END AS unlocked_at,
    CASE WHEN gs.plot_index > awt.unlocked_plot_count THEN awt.lock_reason ELSE NULL END AS lock_reason,
    NOW(), NOW(), awt.user_id, awt.user_id, 'init user plot', 1, false, 0
FROM alloc_with_type awt
JOIN LATERAL generate_series(1, awt.total_plot_count) AS gs(plot_index) ON true
CROSS JOIN fallback_soil fs
WHERE NOT EXISTS (
    SELECT 1
    FROM farm.user_plots up
    WHERE up.user_id = awt.user_id
      AND up.plot_index = gs.plot_index::smallint
      AND up.is_deleted = false
);

-- ==========================================
-- 11) 增强初始化补丁（差异化阶段 + 更全面用户/地块/库存）
-- ==========================================

-- 11.1 生长阶段字典补齐（包含“枯萎”）
INSERT INTO farm.growth_stages (name, description)
SELECT '幼苗', '发芽后的幼苗期'
WHERE NOT EXISTS (SELECT 1 FROM farm.growth_stages WHERE name = '幼苗' AND is_deleted = false);

INSERT INTO farm.growth_stages (name, description)
SELECT '结果', '开花后结果阶段'
WHERE NOT EXISTS (SELECT 1 FROM farm.growth_stages WHERE name = '结果' AND is_deleted = false);

INSERT INTO farm.growth_stages (name, description)
SELECT '枯萎', '错过收获窗口后的枯萎状态'
WHERE NOT EXISTS (SELECT 1 FROM farm.growth_stages WHERE name = '枯萎' AND is_deleted = false);

-- 11.2 种子类型差异化配置（含多次收获）
WITH quality_dict AS (
    SELECT id, name FROM farm.seed_qualities WHERE is_deleted = false
),
seed_cfg AS (
    SELECT * FROM (VALUES
        ('草莓', '优质', 3::bigint, 2::smallint, '甜度高，可多次结果', 4::smallint, 3::smallint, 4::smallint, 18::bigint, 36::bigint, 5::int, 1::int, 3::bigint, 3::bigint, 2::bigint, 22::bigint, 28::bigint),
        ('茄子', '优质', 3::bigint, 2::smallint, '稳定产出，适合中期', 4::smallint, 2::smallint, 4::smallint, 16::bigint, 30::bigint, 4::int, 1::int, 3::bigint, 2::bigint, 2::bigint, 20::bigint, 24::bigint),
        ('玉米', '普通', 1::bigint, 1::smallint, '阶段较少，节奏快', 3::smallint, 1::smallint, NULL::smallint, 12::bigint, 22::bigint, 3::int, 1::int, 2::bigint, 2::bigint, 1::bigint, 16::bigint, 18::bigint),
        ('蓝莓', '稀有', 7::bigint, 3::smallint, '成长慢但单价高，可连续采收', 5::smallint, 4::smallint, 5::smallint, 36::bigint, 68::bigint, 4::int, 1::int, 4::bigint, 4::bigint, 3::bigint, 38::bigint, 62::bigint),
        ('南瓜', '优质', 3::bigint, 2::smallint, '成熟耗时较长，单次收益高', 4::smallint, 1::smallint, NULL::smallint, 24::bigint, 48::bigint, 6::int, 2::int, 3::bigint, 3::bigint, 2::bigint, 30::bigint, 42::bigint),
        ('辣椒', '稀有', 7::bigint, 3::smallint, '可再生采收，风险和收益并存', 5::smallint, 2::smallint, 4::smallint, 28::bigint, 55::bigint, 4::int, 1::int, 4::bigint, 4::bigint, 3::bigint, 33::bigint, 50::bigint),
        ('水稻', '普通', 1::bigint, 1::smallint, '基础粮作，前中期成长快', 3::smallint, 2::smallint, 3::smallint, 10::bigint, 20::bigint, 3::int, 1::int, 2::bigint, 2::bigint, 1::bigint, 14::bigint, 16::bigint)
    ) AS t(name, quality_name, soil_bits, level, description, max_bug_limit, max_harvest_count, regrow_stage_index, price, harvest_experience, harvest_fruit_number, fruit_loss_per_bug, bug_kill_coin_reward, bug_kill_experience_reward, bug_kill_score_reward, fruit_price, harvest_score)
)
UPDATE farm.seed_types st
SET seed_quality_id = q.id,
    enable_soil_type_bits = cfg.soil_bits,
    level = cfg.level,
    description = cfg.description,
    max_bug_limit = cfg.max_bug_limit,
    max_harvest_count = cfg.max_harvest_count,
    regrow_stage_index = cfg.regrow_stage_index,
    price = cfg.price,
    harvest_experience = cfg.harvest_experience,
    harvest_fruit_number = cfg.harvest_fruit_number,
    fruit_loss_per_bug = cfg.fruit_loss_per_bug,
    bug_kill_coin_reward = cfg.bug_kill_coin_reward,
    bug_kill_experience_reward = cfg.bug_kill_experience_reward,
    bug_kill_score_reward = cfg.bug_kill_score_reward,
    fruit_price = cfg.fruit_price,
    harvest_score = cfg.harvest_score,
    updated_at = NOW(),
    updated_by = 0,
    is_deleted = false,
    status = 1
FROM seed_cfg cfg
JOIN quality_dict q ON q.name = cfg.quality_name
WHERE st.name = cfg.name;

WITH quality_dict AS (
    SELECT id, name FROM farm.seed_qualities WHERE is_deleted = false
),
seed_cfg AS (
    SELECT * FROM (VALUES
        ('蓝莓', '稀有', 7::bigint, 3::smallint, '成长慢但单价高，可连续采收', 5::smallint, 4::smallint, 5::smallint, 36::bigint, 68::bigint, 4::int, 1::int, 4::bigint, 4::bigint, 3::bigint, 38::bigint, 62::bigint),
        ('南瓜', '优质', 3::bigint, 2::smallint, '成熟耗时较长，单次收益高', 4::smallint, 1::smallint, NULL::smallint, 24::bigint, 48::bigint, 6::int, 2::int, 3::bigint, 3::bigint, 2::bigint, 30::bigint, 42::bigint),
        ('辣椒', '稀有', 7::bigint, 3::smallint, '可再生采收，风险和收益并存', 5::smallint, 2::smallint, 4::smallint, 28::bigint, 55::bigint, 4::int, 1::int, 4::bigint, 4::bigint, 3::bigint, 33::bigint, 50::bigint),
        ('水稻', '普通', 1::bigint, 1::smallint, '基础粮作，前中期成长快', 3::smallint, 2::smallint, 3::smallint, 10::bigint, 20::bigint, 3::int, 1::int, 2::bigint, 2::bigint, 1::bigint, 14::bigint, 16::bigint)
    ) AS t(name, quality_name, soil_bits, level, description, max_bug_limit, max_harvest_count, regrow_stage_index, price, harvest_experience, harvest_fruit_number, fruit_loss_per_bug, bug_kill_coin_reward, bug_kill_experience_reward, bug_kill_score_reward, fruit_price, harvest_score)
)
INSERT INTO farm.seed_types
(
    name, cover_image_url, seed_quality_id, enable_soil_type_bits, level, description,
    max_bug_limit, max_harvest_count, regrow_stage_index,
    price, harvest_experience, harvest_fruit_number, fruit_loss_per_bug,
    bug_kill_coin_reward, bug_kill_experience_reward, bug_kill_score_reward,
    fruit_price, harvest_score,
    created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version
)
SELECT
    cfg.name, '/oss/defaults/seed/seed-cover-default.png', q.id, cfg.soil_bits, cfg.level, cfg.description,
    cfg.max_bug_limit, cfg.max_harvest_count, cfg.regrow_stage_index,
    cfg.price, cfg.harvest_experience, cfg.harvest_fruit_number, cfg.fruit_loss_per_bug,
    cfg.bug_kill_coin_reward, cfg.bug_kill_experience_reward, cfg.bug_kill_score_reward,
    cfg.fruit_price, cfg.harvest_score,
    NOW(), NOW(), 0, 0, 'enhanced init seed type', 1, false, 0
FROM seed_cfg cfg
JOIN quality_dict q ON q.name = cfg.quality_name
WHERE NOT EXISTS (
    SELECT 1 FROM farm.seed_types st WHERE st.name = cfg.name AND st.is_deleted = false
);

-- 11.2.1 种子商店经验解锁梯度（全部展示，按经验解锁）
UPDATE farm.seed_types st
SET unlock_experience_required = CASE
    WHEN COALESCE(st.level, 1) <= 1
        THEN CASE
            WHEN COALESCE(st.max_harvest_count, 1) >= 2 THEN 120
            ELSE 0
        END
    WHEN st.level = 2
        THEN 420 + GREATEST(COALESCE(st.max_harvest_count, 1) - 1, 0) * 80
    WHEN st.level = 3
        THEN 980 + GREATEST(COALESCE(st.max_harvest_count, 1) - 1, 0) * 140
    ELSE 1600 + (st.level - 4) * 700 + GREATEST(COALESCE(st.max_harvest_count, 1) - 1, 0) * 180
END,
updated_at = NOW(),
updated_by = 0
WHERE st.is_deleted = false;

-- 11.3 生长阶段配置重建（4/5/6 阶段混合）
WITH stage_dict AS (
    SELECT id, name FROM farm.growth_stages WHERE is_deleted = false
),
seed_dict AS (
    SELECT id, name FROM farm.seed_types
    WHERE is_deleted = false
      AND name IN ('草莓', '茄子', '玉米', '蓝莓', '南瓜', '辣椒', '水稻')
),
cfg AS (
    SELECT * FROM (VALUES
        ('草莓', '种子',   1::smallint, 25::int, 0.0080::numeric(5,4), 92::int, 120::int, 58::int, 162::int),
        ('草莓', '发芽',   2, 30, 0.0140, 95, 126, 56, 156),
        ('草莓', '开花',   3, 35, 0.0200, 102, 132, 52, 150),
        ('草莓', '结果',   4, 40, 0.0260, 108, 138, 48, 145),
        ('草莓', '成熟',   5, 35, 0.0180, 112, 142, 46, 140),

        ('茄子', '种子',   1, 30, 0.0100, 94, 122, 57, 160),
        ('茄子', '发芽',   2, 35, 0.0160, 98, 128, 54, 154),
        ('茄子', '生长期', 3, 45, 0.0240, 104, 136, 50, 148),
        ('茄子', '结果',   4, 50, 0.0300, 110, 142, 46, 142),
        ('茄子', '成熟',   5, 42, 0.0220, 114, 146, 44, 138),

        ('玉米', '种子',   1, 40, 0.0100, 94, 124, 56, 160),
        ('玉米', '幼苗',   2, 55, 0.0180, 102, 136, 51, 150),
        ('玉米', '生长期', 3, 70, 0.0280, 110, 148, 46, 140),
        ('玉米', '成熟',   4, 60, 0.0220, 118, 156, 40, 130),

        ('蓝莓', '种子',   1, 35, 0.0100, 86, 116, 62, 166),
        ('蓝莓', '发芽',   2, 40, 0.0160, 90, 122, 60, 160),
        ('蓝莓', '幼苗',   3, 50, 0.0240, 96, 128, 56, 154),
        ('蓝莓', '生长期', 4, 55, 0.0300, 104, 136, 52, 148),
        ('蓝莓', '开花',   5, 60, 0.0340, 110, 142, 49, 142),
        ('蓝莓', '成熟',   6, 50, 0.0220, 114, 146, 46, 138),

        ('南瓜', '种子',   1, 30, 0.0090, 98, 122, 54, 158),
        ('南瓜', '发芽',   2, 35, 0.0140, 102, 126, 52, 154),
        ('南瓜', '幼苗',   3, 45, 0.0200, 110, 136, 48, 148),
        ('南瓜', '生长期', 4, 65, 0.0300, 120, 150, 42, 136),
        ('南瓜', '成熟',   5, 55, 0.0240, 126, 156, 38, 132),

        ('辣椒', '种子',   1, 28, 0.0100, 90, 118, 60, 164),
        ('辣椒', '发芽',   2, 32, 0.0180, 94, 124, 57, 158),
        ('辣椒', '生长期', 3, 42, 0.0260, 100, 132, 54, 152),
        ('辣椒', '开花',   4, 50, 0.0320, 106, 138, 50, 146),
        ('辣椒', '成熟',   5, 45, 0.0240, 110, 144, 48, 142),

        ('水稻', '种子',   1, 22, 0.0060, 88, 114, 61, 167),
        ('水稻', '幼苗',   2, 30, 0.0120, 94, 122, 58, 160),
        ('水稻', '生长期', 3, 45, 0.0180, 98, 130, 55, 154),
        ('水稻', '成熟',   4, 38, 0.0150, 102, 136, 52, 148)
    ) AS t(seed_name, stage_name, stage_index, duration_seconds, bug_probability, width, height, offset_x, offset_y)
),
resolved AS (
    SELECT
        sd.id AS seed_type_id,
        gd.id AS growth_stage_id,
        cfg.stage_index,
        cfg.duration_seconds,
        cfg.bug_probability,
        cfg.width,
        cfg.height,
        cfg.offset_x,
        cfg.offset_y
    FROM cfg
    JOIN seed_dict sd ON sd.name = cfg.seed_name
    JOIN stage_dict gd ON gd.name = cfg.stage_name
)
UPDATE farm.seed_growth_stages sgs
SET growth_stage_id = r.growth_stage_id,
    duration_seconds = r.duration_seconds,
    bug_probability = r.bug_probability,
    width = r.width,
    height = r.height,
    offset_x = r.offset_x,
    offset_y = r.offset_y,
    asset_url = '/oss/defaults/seed/seed-stage-default.png',
    updated_at = NOW(),
    updated_by = 0,
    is_deleted = false,
    status = 1
FROM resolved r
WHERE sgs.seed_type_id = r.seed_type_id
  AND sgs.stage_index = r.stage_index;

WITH stage_dict AS (
    SELECT id, name FROM farm.growth_stages WHERE is_deleted = false
),
seed_dict AS (
    SELECT id, name FROM farm.seed_types
    WHERE is_deleted = false
      AND name IN ('草莓', '茄子', '玉米', '蓝莓', '南瓜', '辣椒', '水稻')
),
cfg AS (
    SELECT * FROM (VALUES
        ('草莓', '种子',   1::smallint, 25::int, 0.0080::numeric(5,4), 92::int, 120::int, 58::int, 162::int),
        ('草莓', '发芽',   2, 30, 0.0140, 95, 126, 56, 156),
        ('草莓', '开花',   3, 35, 0.0200, 102, 132, 52, 150),
        ('草莓', '结果',   4, 40, 0.0260, 108, 138, 48, 145),
        ('草莓', '成熟',   5, 35, 0.0180, 112, 142, 46, 140),

        ('茄子', '种子',   1, 30, 0.0100, 94, 122, 57, 160),
        ('茄子', '发芽',   2, 35, 0.0160, 98, 128, 54, 154),
        ('茄子', '生长期', 3, 45, 0.0240, 104, 136, 50, 148),
        ('茄子', '结果',   4, 50, 0.0300, 110, 142, 46, 142),
        ('茄子', '成熟',   5, 42, 0.0220, 114, 146, 44, 138),

        ('玉米', '种子',   1, 40, 0.0100, 94, 124, 56, 160),
        ('玉米', '幼苗',   2, 55, 0.0180, 102, 136, 51, 150),
        ('玉米', '生长期', 3, 70, 0.0280, 110, 148, 46, 140),
        ('玉米', '成熟',   4, 60, 0.0220, 118, 156, 40, 130),

        ('蓝莓', '种子',   1, 35, 0.0100, 86, 116, 62, 166),
        ('蓝莓', '发芽',   2, 40, 0.0160, 90, 122, 60, 160),
        ('蓝莓', '幼苗',   3, 50, 0.0240, 96, 128, 56, 154),
        ('蓝莓', '生长期', 4, 55, 0.0300, 104, 136, 52, 148),
        ('蓝莓', '开花',   5, 60, 0.0340, 110, 142, 49, 142),
        ('蓝莓', '成熟',   6, 50, 0.0220, 114, 146, 46, 138),

        ('南瓜', '种子',   1, 30, 0.0090, 98, 122, 54, 158),
        ('南瓜', '发芽',   2, 35, 0.0140, 102, 126, 52, 154),
        ('南瓜', '幼苗',   3, 45, 0.0200, 110, 136, 48, 148),
        ('南瓜', '生长期', 4, 65, 0.0300, 120, 150, 42, 136),
        ('南瓜', '成熟',   5, 55, 0.0240, 126, 156, 38, 132),

        ('辣椒', '种子',   1, 28, 0.0100, 90, 118, 60, 164),
        ('辣椒', '发芽',   2, 32, 0.0180, 94, 124, 57, 158),
        ('辣椒', '生长期', 3, 42, 0.0260, 100, 132, 54, 152),
        ('辣椒', '开花',   4, 50, 0.0320, 106, 138, 50, 146),
        ('辣椒', '成熟',   5, 45, 0.0240, 110, 144, 48, 142),

        ('水稻', '种子',   1, 22, 0.0060, 88, 114, 61, 167),
        ('水稻', '幼苗',   2, 30, 0.0120, 94, 122, 58, 160),
        ('水稻', '生长期', 3, 45, 0.0180, 98, 130, 55, 154),
        ('水稻', '成熟',   4, 38, 0.0150, 102, 136, 52, 148)
    ) AS t(seed_name, stage_name, stage_index, duration_seconds, bug_probability, width, height, offset_x, offset_y)
),
resolved AS (
    SELECT
        sd.id AS seed_type_id,
        gd.id AS growth_stage_id,
        cfg.stage_index,
        cfg.duration_seconds,
        cfg.bug_probability,
        cfg.width,
        cfg.height,
        cfg.offset_x,
        cfg.offset_y
    FROM cfg
    JOIN seed_dict sd ON sd.name = cfg.seed_name
    JOIN stage_dict gd ON gd.name = cfg.stage_name
)
INSERT INTO farm.seed_growth_stages
(
    seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability,
    width, height, offset_x, offset_y,
    created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version
)
SELECT
    r.seed_type_id, r.growth_stage_id, r.stage_index, r.duration_seconds, '/oss/defaults/seed/seed-stage-default.png', r.bug_probability,
    r.width, r.height, r.offset_x, r.offset_y,
    NOW(), NOW(), 0, 0, 'enhanced init seed stage', 1, false, 0
FROM resolved r
WHERE NOT EXISTS (
    SELECT 1
    FROM farm.seed_growth_stages sgs
    WHERE sgs.seed_type_id = r.seed_type_id
      AND sgs.stage_index = r.stage_index
      AND sgs.is_deleted = false
);

WITH managed_seed AS (
    SELECT id FROM farm.seed_types
    WHERE is_deleted = false
      AND name IN ('草莓', '茄子', '玉米', '蓝莓', '南瓜', '辣椒', '水稻')
),
valid_stage AS (
    SELECT * FROM (VALUES
        ('草莓', 1::smallint), ('草莓', 2), ('草莓', 3), ('草莓', 4), ('草莓', 5),
        ('茄子', 1), ('茄子', 2), ('茄子', 3), ('茄子', 4), ('茄子', 5),
        ('玉米', 1), ('玉米', 2), ('玉米', 3), ('玉米', 4),
        ('蓝莓', 1), ('蓝莓', 2), ('蓝莓', 3), ('蓝莓', 4), ('蓝莓', 5), ('蓝莓', 6),
        ('南瓜', 1), ('南瓜', 2), ('南瓜', 3), ('南瓜', 4), ('南瓜', 5),
        ('辣椒', 1), ('辣椒', 2), ('辣椒', 3), ('辣椒', 4), ('辣椒', 5),
        ('水稻', 1), ('水稻', 2), ('水稻', 3), ('水稻', 4)
    ) AS t(seed_name, stage_index)
),
seed_map AS (
    SELECT id, name FROM farm.seed_types WHERE is_deleted = false
)
UPDATE farm.seed_growth_stages sgs
SET is_deleted = true,
    updated_at = NOW(),
    updated_by = 0,
    remark = 'auto-retired by enhanced init'
FROM managed_seed ms
WHERE sgs.seed_type_id = ms.id
  AND sgs.is_deleted = false
  AND NOT EXISTS (
      SELECT 1
      FROM valid_stage v
      JOIN seed_map sm ON sm.name = v.seed_name
      WHERE sm.id = sgs.seed_type_id
        AND v.stage_index = sgs.stage_index
  );

-- 11.4 用户补齐（新增赵云、华佗）
WITH cfg AS (
    SELECT * FROM (VALUES
        ('liubei',  '刘备', 'liubei@farm.local', 2400::bigint, 820::bigint, 5600::bigint),
        ('caocao',  '曹操', 'caocao@farm.local', 2100::bigint, 700::bigint, 4900::bigint),
        ('sunquan', '孙权', 'sunquan@farm.local', 1750::bigint, 540::bigint, 3600::bigint),
        ('zhaoyun', '赵云', 'zhaoyun@farm.local', 1450::bigint, 430::bigint, 2800::bigint),
        ('huatuo',  '华佗', 'huatuo@farm.local', 900::bigint, 260::bigint, 1800::bigint)
    ) AS t(username, nickname, email, experience, score, coin)
)
UPDATE farm.users u
SET nickname = cfg.nickname,
    email = cfg.email,
    avatar_url = '/oss/defaults/avatar/default-avatar.png',
    experience = cfg.experience,
    score = cfg.score,
    coin = cfg.coin,
    preferences_json = '{"audio":{"effectEnabled":true,"effectVolume":0.8,"bgmEnabled":true,"bgmVolume":0.6}}'::jsonb,
    updated_at = NOW(),
    updated_by = 0,
    is_deleted = false,
    status = 1
FROM cfg
WHERE u.username = cfg.username;

INSERT INTO farm.users
(
    username, nickname, password_hash, email, avatar_url,
    experience, score, coin, preferences_json,
    created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version
)
SELECT
    cfg.username, cfg.nickname, '123456', cfg.email, '/oss/defaults/avatar/default-avatar.png',
    cfg.experience, cfg.score, cfg.coin, '{"audio":{"effectEnabled":true,"effectVolume":0.8,"bgmEnabled":true,"bgmVolume":0.6}}'::jsonb,
    NOW(), NOW(), 0, 0, 'enhanced init user', 1, false, 0
FROM (
    VALUES
        ('zhaoyun', '赵云', 'zhaoyun@farm.local', 1450::bigint, 430::bigint, 2800::bigint),
        ('huatuo',  '华佗', 'huatuo@farm.local', 900::bigint, 260::bigint, 1800::bigint)
) AS cfg(username, nickname, email, experience, score, coin)
WHERE NOT EXISTS (
    SELECT 1 FROM farm.users u WHERE u.username = cfg.username AND u.is_deleted = false
);

-- 11.5 地块分配差异化
WITH default_plot_type AS (
    SELECT id FROM farm.plot_types WHERE name = '普通耕地' AND is_deleted = false ORDER BY id ASC LIMIT 1
),
cfg AS (
    SELECT * FROM (VALUES
        ('liubei',  10::smallint, 4::smallint),
        ('caocao',   8::smallint, 3::smallint),
        ('sunquan',  7::smallint, 2::smallint),
        ('zhaoyun',  6::smallint, 2::smallint),
        ('huatuo',   5::smallint, 1::smallint)
    ) AS t(username, total_count, unlocked_count)
),
resolved AS (
    SELECT
        u.id AS user_id,
        cfg.total_count,
        cfg.unlocked_count,
        (cfg.total_count - cfg.unlocked_count)::smallint AS locked_count
    FROM cfg
    JOIN farm.users u ON u.username = cfg.username AND u.is_deleted = false
)
UPDATE farm.user_plot_allocations upa
SET active = true,
    total_plot_count = r.total_count,
    unlocked_plot_count = r.unlocked_count,
    locked_plot_count = r.locked_count,
    default_plot_type_id = dpt.id,
    lock_rule_code = 'EXP_REQUIRED',
    lock_reason = '经验不足，待解锁',
    allocation_rule_json = jsonb_build_object('default', jsonb_build_object('total', r.total_count, 'locked', r.locked_count)),
    applied_at = NOW(),
    updated_at = NOW(),
    updated_by = 0,
    is_deleted = false,
    status = 1
FROM resolved r
CROSS JOIN default_plot_type dpt
WHERE upa.user_id = r.user_id;

WITH default_plot_type AS (
    SELECT id FROM farm.plot_types WHERE name = '普通耕地' AND is_deleted = false ORDER BY id ASC LIMIT 1
),
cfg AS (
    SELECT * FROM (VALUES
        ('liubei',  10::smallint, 4::smallint),
        ('caocao',   8::smallint, 3::smallint),
        ('sunquan',  7::smallint, 2::smallint),
        ('zhaoyun',  6::smallint, 2::smallint),
        ('huatuo',   5::smallint, 1::smallint)
    ) AS t(username, total_count, unlocked_count)
),
resolved AS (
    SELECT
        u.id AS user_id,
        cfg.total_count,
        cfg.unlocked_count,
        (cfg.total_count - cfg.unlocked_count)::smallint AS locked_count
    FROM cfg
    JOIN farm.users u ON u.username = cfg.username AND u.is_deleted = false
)
INSERT INTO farm.user_plot_allocations
(
    user_id, active, total_plot_count, unlocked_plot_count, locked_plot_count,
    default_plot_type_id, lock_rule_code, lock_reason, allocation_rule_json, applied_at,
    created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version
)
SELECT
    r.user_id, true, r.total_count, r.unlocked_count, r.locked_count,
    dpt.id, 'EXP_REQUIRED', '经验不足，待解锁',
    jsonb_build_object('default', jsonb_build_object('total', r.total_count, 'locked', r.locked_count)),
    NOW(),
    NOW(), NOW(), 0, 0, 'enhanced init user allocation', 1, false, 0
FROM resolved r
CROSS JOIN default_plot_type dpt
WHERE NOT EXISTS (
    SELECT 1 FROM farm.user_plot_allocations upa WHERE upa.user_id = r.user_id AND upa.is_deleted = false
);

-- 11.6 用户地块按分配同步（补齐、更新、超额软删除）
WITH managed_user AS (
    SELECT u.id AS user_id, u.username
    FROM farm.users u
    WHERE u.is_deleted = false
      AND u.username IN ('liubei', 'caocao', 'sunquan', 'zhaoyun', 'huatuo')
),
alloc AS (
    SELECT
        mu.user_id,
        upa.total_plot_count::int AS total_plot_count,
        upa.unlocked_plot_count::int AS unlocked_plot_count
    FROM managed_user mu
    JOIN farm.user_plot_allocations upa
      ON upa.user_id = mu.user_id
     AND upa.active = true
     AND upa.is_deleted = false
),
soil_map AS (
    SELECT
        COALESCE(MAX(CASE WHEN bit_code = 1 THEN id END), MIN(id)) AS soil_a,
        COALESCE(MAX(CASE WHEN bit_code = 2 THEN id END), MAX(CASE WHEN bit_code = 1 THEN id END), MIN(id)) AS soil_b,
        COALESCE(MAX(CASE WHEN bit_code = 4 THEN id END), MAX(CASE WHEN bit_code = 2 THEN id END), MAX(CASE WHEN bit_code = 1 THEN id END), MIN(id)) AS soil_c
    FROM farm.soil_types
    WHERE is_deleted = false
),
grid AS (
    SELECT
        a.user_id,
        gs.plot_index::int AS plot_index,
        CASE
            WHEN gs.plot_index % 5 = 0 THEN sm.soil_c
            WHEN gs.plot_index % 2 = 0 THEN sm.soil_b
            ELSE sm.soil_a
        END AS soil_type_id,
        CASE
            WHEN gs.plot_index <= a.unlocked_plot_count THEN 0::bigint
            ELSE 600::bigint + ((gs.plot_index - a.unlocked_plot_count - 1) * 250)::bigint
        END AS unlock_experience_required,
        (gs.plot_index > a.unlocked_plot_count) AS is_locked
    FROM alloc a
    CROSS JOIN soil_map sm
    JOIN LATERAL generate_series(1, a.total_plot_count) AS gs(plot_index) ON true
)
UPDATE farm.user_plots up
SET soil_type_id = g.soil_type_id,
    unlock_experience_required = g.unlock_experience_required,
    is_locked = g.is_locked,
    unlocked_at = CASE WHEN g.is_locked THEN NULL ELSE COALESCE(up.unlocked_at, NOW()) END,
    lock_reason = CASE WHEN g.is_locked THEN '经验不足，待解锁' ELSE NULL END,
    updated_at = NOW(),
    updated_by = g.user_id,
    is_deleted = false,
    status = 1
FROM grid g
WHERE up.user_id = g.user_id
  AND up.plot_index = g.plot_index::smallint;

WITH managed_user AS (
    SELECT u.id AS user_id, u.username
    FROM farm.users u
    WHERE u.is_deleted = false
      AND u.username IN ('liubei', 'caocao', 'sunquan', 'zhaoyun', 'huatuo')
),
alloc AS (
    SELECT
        mu.user_id,
        upa.total_plot_count::int AS total_plot_count,
        upa.unlocked_plot_count::int AS unlocked_plot_count
    FROM managed_user mu
    JOIN farm.user_plot_allocations upa
      ON upa.user_id = mu.user_id
     AND upa.active = true
     AND upa.is_deleted = false
),
soil_map AS (
    SELECT
        COALESCE(MAX(CASE WHEN bit_code = 1 THEN id END), MIN(id)) AS soil_a,
        COALESCE(MAX(CASE WHEN bit_code = 2 THEN id END), MAX(CASE WHEN bit_code = 1 THEN id END), MIN(id)) AS soil_b,
        COALESCE(MAX(CASE WHEN bit_code = 4 THEN id END), MAX(CASE WHEN bit_code = 2 THEN id END), MAX(CASE WHEN bit_code = 1 THEN id END), MIN(id)) AS soil_c
    FROM farm.soil_types
    WHERE is_deleted = false
),
grid AS (
    SELECT
        a.user_id,
        gs.plot_index::int AS plot_index,
        CASE
            WHEN gs.plot_index % 5 = 0 THEN sm.soil_c
            WHEN gs.plot_index % 2 = 0 THEN sm.soil_b
            ELSE sm.soil_a
        END AS soil_type_id,
        CASE
            WHEN gs.plot_index <= a.unlocked_plot_count THEN 0::bigint
            ELSE 600::bigint + ((gs.plot_index - a.unlocked_plot_count - 1) * 250)::bigint
        END AS unlock_experience_required,
        (gs.plot_index > a.unlocked_plot_count) AS is_locked
    FROM alloc a
    CROSS JOIN soil_map sm
    JOIN LATERAL generate_series(1, a.total_plot_count) AS gs(plot_index) ON true
)
INSERT INTO farm.user_plots
(
    user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason,
    created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version
)
SELECT
    g.user_id, g.soil_type_id, g.plot_index::smallint, g.unlock_experience_required, g.is_locked,
    CASE WHEN g.is_locked THEN NULL ELSE NOW() END,
    CASE WHEN g.is_locked THEN '经验不足，待解锁' ELSE NULL END,
    NOW(), NOW(), g.user_id, g.user_id, 'enhanced init user plot', 1, false, 0
FROM grid g
WHERE NOT EXISTS (
    SELECT 1
    FROM farm.user_plots up
    WHERE up.user_id = g.user_id
      AND up.plot_index = g.plot_index::smallint
      AND up.is_deleted = false
);

WITH managed_user AS (
    SELECT u.id AS user_id
    FROM farm.users u
    WHERE u.is_deleted = false
      AND u.username IN ('liubei', 'caocao', 'sunquan', 'zhaoyun', 'huatuo')
),
alloc AS (
    SELECT user_id, total_plot_count
    FROM farm.user_plot_allocations
    WHERE active = true
      AND is_deleted = false
)
UPDATE farm.user_plots up
SET is_deleted = true,
    updated_at = NOW(),
    updated_by = up.user_id,
    remark = 'auto-retired by enhanced allocation sync'
FROM managed_user mu
JOIN alloc a ON a.user_id = mu.user_id
WHERE up.user_id = mu.user_id
  AND up.is_deleted = false
  AND up.plot_index > a.total_plot_count;

-- 11.7 用户种子背包初始化
WITH cfg AS (
    SELECT * FROM (VALUES
        ('liubei',  '草莓', 28::bigint), ('liubei',  '蓝莓', 12::bigint), ('liubei',  '玉米', 20::bigint), ('liubei',  '南瓜', 14::bigint), ('liubei',  '辣椒', 16::bigint), ('liubei',  '水稻', 24::bigint),
        ('caocao',  '草莓', 16::bigint), ('caocao',  '玉米', 18::bigint), ('caocao',  '南瓜', 10::bigint), ('caocao',  '水稻', 18::bigint),
        ('sunquan', '草莓', 12::bigint), ('sunquan', '玉米', 12::bigint), ('sunquan', '水稻', 15::bigint),
        ('zhaoyun', '草莓', 10::bigint), ('zhaoyun', '玉米', 10::bigint), ('zhaoyun', '水稻', 12::bigint),
        ('huatuo',  '草莓',  8::bigint), ('huatuo',  '玉米',  8::bigint), ('huatuo',  '水稻', 10::bigint)
    ) AS t(username, seed_name, quantity)
),
resolved AS (
    SELECT u.id AS user_id, st.id AS seed_type_id, cfg.quantity
    FROM cfg
    JOIN farm.users u ON u.username = cfg.username AND u.is_deleted = false
    JOIN farm.seed_types st ON st.name = cfg.seed_name AND st.is_deleted = false
)
UPDATE farm.user_seeds us
SET quantity = r.quantity,
    frozen_quantity = 0,
    updated_at = NOW(),
    updated_by = r.user_id,
    is_deleted = false,
    status = 1
FROM resolved r
WHERE us.user_id = r.user_id
  AND us.seed_type_id = r.seed_type_id;

WITH cfg AS (
    SELECT * FROM (VALUES
        ('liubei',  '草莓', 28::bigint), ('liubei',  '蓝莓', 12::bigint), ('liubei',  '玉米', 20::bigint), ('liubei',  '南瓜', 14::bigint), ('liubei',  '辣椒', 16::bigint), ('liubei',  '水稻', 24::bigint),
        ('caocao',  '草莓', 16::bigint), ('caocao',  '玉米', 18::bigint), ('caocao',  '南瓜', 10::bigint), ('caocao',  '水稻', 18::bigint),
        ('sunquan', '草莓', 12::bigint), ('sunquan', '玉米', 12::bigint), ('sunquan', '水稻', 15::bigint),
        ('zhaoyun', '草莓', 10::bigint), ('zhaoyun', '玉米', 10::bigint), ('zhaoyun', '水稻', 12::bigint),
        ('huatuo',  '草莓',  8::bigint), ('huatuo',  '玉米',  8::bigint), ('huatuo',  '水稻', 10::bigint)
    ) AS t(username, seed_name, quantity)
),
resolved AS (
    SELECT u.id AS user_id, st.id AS seed_type_id, cfg.quantity
    FROM cfg
    JOIN farm.users u ON u.username = cfg.username AND u.is_deleted = false
    JOIN farm.seed_types st ON st.name = cfg.seed_name AND st.is_deleted = false
)
INSERT INTO farm.user_seeds
(
    user_id, seed_type_id, quantity, frozen_quantity,
    created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version
)
SELECT
    r.user_id, r.seed_type_id, r.quantity, 0,
    NOW(), NOW(), r.user_id, r.user_id, 'enhanced init user seed', 1, false, 0
FROM resolved r
WHERE NOT EXISTS (
    SELECT 1
    FROM farm.user_seeds us
    WHERE us.user_id = r.user_id
      AND us.seed_type_id = r.seed_type_id
      AND us.is_deleted = false
);

-- 11.8 Seed stage harvest/wither model alignment
INSERT INTO farm.asset_defaults (asset_key, asset_url, description)
SELECT 'seedStageWithered', '/oss/defaults/seed/seed-stage-withered-default.png', '种子枯萎阶段默认图'
WHERE NOT EXISTS (SELECT 1 FROM farm.asset_defaults WHERE asset_key = 'seedStageWithered' AND is_deleted = false);

INSERT INTO farm.growth_stages (name, description)
SELECT '枯萎', '作物虫害超限或错过收获窗口后的最终阶段'
WHERE NOT EXISTS (SELECT 1 FROM farm.growth_stages WHERE name = '枯萎' AND is_deleted = false);

WITH seed_cfg AS (
    SELECT * FROM (VALUES
        ('草莓', 1::smallint, 6::smallint, NULL::smallint),
        ('茄子', 1::smallint, 6::smallint, NULL::smallint),
        ('玉米', 1::smallint, 5::smallint, NULL::smallint),
        ('蓝莓', 4::smallint, 7::smallint, 5::smallint),
        ('南瓜', 1::smallint, 6::smallint, NULL::smallint),
        ('辣椒', 2::smallint, 6::smallint, 4::smallint),
        ('水稻', 2::smallint, 5::smallint, 3::smallint)
    ) AS t(name, max_harvest_count, harvest_stage_index, regrow_stage_index)
)
UPDATE farm.seed_types st
SET max_harvest_count = cfg.max_harvest_count,
    harvest_stage_index = cfg.harvest_stage_index,
    regrow_stage_index = cfg.regrow_stage_index,
    updated_at = NOW(),
    updated_by = 0
FROM seed_cfg cfg
WHERE st.name = cfg.name
  AND st.is_deleted = false;

WITH stage_dict AS (
    SELECT id, name FROM farm.growth_stages WHERE is_deleted = false
),
seed_dict AS (
    SELECT id, name FROM farm.seed_types
    WHERE is_deleted = false
      AND name IN ('草莓', '茄子', '玉米', '蓝莓', '南瓜', '辣椒', '水稻')
),
cfg AS (
    SELECT * FROM (VALUES
        ('草莓', '种子',   1::smallint, 25::int, 0.0080::numeric(5,4),  92::int, 120::int, 58::int, 162::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('草莓', '发芽',   2::smallint, 30::int, 0.0140::numeric(5,4),  95::int, 126::int, 56::int, 156::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('草莓', '开花',   3::smallint, 35::int, 0.0200::numeric(5,4), 102::int, 132::int, 52::int, 150::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('草莓', '结果',   4::smallint, 40::int, 0.0260::numeric(5,4), 108::int, 138::int, 48::int, 145::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('草莓', '成熟',   5::smallint, 35::int, 0.0180::numeric(5,4), 112::int, 142::int, 46::int, 140::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('草莓', '枯萎',   6::smallint,  0::int, 0.0000::numeric(5,4), 112::int, 142::int, 46::int, 140::int, '/oss/defaults/seed/seed-stage-withered-default.png'),

        ('茄子', '种子',   1::smallint, 30::int, 0.0100::numeric(5,4),  94::int, 122::int, 57::int, 160::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('茄子', '发芽',   2::smallint, 35::int, 0.0160::numeric(5,4),  98::int, 128::int, 54::int, 154::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('茄子', '生长期', 3::smallint, 45::int, 0.0240::numeric(5,4), 104::int, 136::int, 50::int, 148::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('茄子', '结果',   4::smallint, 50::int, 0.0300::numeric(5,4), 110::int, 142::int, 46::int, 142::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('茄子', '成熟',   5::smallint, 42::int, 0.0220::numeric(5,4), 114::int, 146::int, 44::int, 138::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('茄子', '枯萎',   6::smallint,  0::int, 0.0000::numeric(5,4), 114::int, 146::int, 44::int, 138::int, '/oss/defaults/seed/seed-stage-withered-default.png'),

        ('玉米', '种子',   1::smallint, 40::int, 0.0100::numeric(5,4),  94::int, 124::int, 56::int, 160::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('玉米', '幼苗',   2::smallint, 55::int, 0.0180::numeric(5,4), 102::int, 136::int, 51::int, 150::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('玉米', '生长期', 3::smallint, 70::int, 0.0280::numeric(5,4), 110::int, 148::int, 46::int, 140::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('玉米', '成熟',   4::smallint, 60::int, 0.0220::numeric(5,4), 118::int, 156::int, 40::int, 130::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('玉米', '枯萎',   5::smallint,  0::int, 0.0000::numeric(5,4), 118::int, 156::int, 40::int, 130::int, '/oss/defaults/seed/seed-stage-withered-default.png'),

        ('蓝莓', '种子',   1::smallint, 35::int, 0.0100::numeric(5,4),  86::int, 116::int, 62::int, 166::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('蓝莓', '发芽',   2::smallint, 40::int, 0.0160::numeric(5,4),  90::int, 122::int, 60::int, 160::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('蓝莓', '幼苗',   3::smallint, 50::int, 0.0240::numeric(5,4),  96::int, 128::int, 56::int, 154::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('蓝莓', '生长期', 4::smallint, 55::int, 0.0300::numeric(5,4), 104::int, 136::int, 52::int, 148::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('蓝莓', '开花',   5::smallint, 60::int, 0.0340::numeric(5,4), 110::int, 142::int, 49::int, 142::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('蓝莓', '成熟',   6::smallint, 50::int, 0.0220::numeric(5,4), 114::int, 146::int, 46::int, 138::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('蓝莓', '枯萎',   7::smallint,  0::int, 0.0000::numeric(5,4), 114::int, 146::int, 46::int, 138::int, '/oss/defaults/seed/seed-stage-withered-default.png'),

        ('南瓜', '种子',   1::smallint, 30::int, 0.0090::numeric(5,4),  98::int, 122::int, 54::int, 158::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('南瓜', '发芽',   2::smallint, 35::int, 0.0140::numeric(5,4), 102::int, 126::int, 52::int, 154::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('南瓜', '幼苗',   3::smallint, 45::int, 0.0200::numeric(5,4), 110::int, 136::int, 48::int, 148::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('南瓜', '生长期', 4::smallint, 65::int, 0.0300::numeric(5,4), 120::int, 150::int, 42::int, 136::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('南瓜', '成熟',   5::smallint, 55::int, 0.0240::numeric(5,4), 126::int, 156::int, 38::int, 132::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('南瓜', '枯萎',   6::smallint,  0::int, 0.0000::numeric(5,4), 126::int, 156::int, 38::int, 132::int, '/oss/defaults/seed/seed-stage-withered-default.png'),

        ('辣椒', '种子',   1::smallint, 28::int, 0.0100::numeric(5,4),  90::int, 118::int, 60::int, 164::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('辣椒', '发芽',   2::smallint, 32::int, 0.0180::numeric(5,4),  94::int, 124::int, 57::int, 158::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('辣椒', '生长期', 3::smallint, 42::int, 0.0260::numeric(5,4), 100::int, 132::int, 54::int, 152::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('辣椒', '开花',   4::smallint, 50::int, 0.0320::numeric(5,4), 106::int, 138::int, 50::int, 146::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('辣椒', '成熟',   5::smallint, 45::int, 0.0240::numeric(5,4), 110::int, 144::int, 48::int, 142::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('辣椒', '枯萎',   6::smallint,  0::int, 0.0000::numeric(5,4), 110::int, 144::int, 48::int, 142::int, '/oss/defaults/seed/seed-stage-withered-default.png'),

        ('水稻', '种子',   1::smallint, 22::int, 0.0060::numeric(5,4),  88::int, 114::int, 61::int, 167::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('水稻', '幼苗',   2::smallint, 30::int, 0.0120::numeric(5,4),  94::int, 122::int, 58::int, 160::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('水稻', '生长期', 3::smallint, 45::int, 0.0180::numeric(5,4),  98::int, 130::int, 55::int, 154::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('水稻', '成熟',   4::smallint, 38::int, 0.0150::numeric(5,4), 102::int, 136::int, 52::int, 148::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('水稻', '枯萎',   5::smallint,  0::int, 0.0000::numeric(5,4), 102::int, 136::int, 52::int, 148::int, '/oss/defaults/seed/seed-stage-withered-default.png')
    ) AS t(seed_name, stage_name, stage_index, duration_seconds, bug_probability, width, height, offset_x, offset_y, asset_url)
),
resolved AS (
    SELECT
        sd.id AS seed_type_id,
        gd.id AS growth_stage_id,
        cfg.stage_index,
        cfg.duration_seconds,
        cfg.bug_probability,
        cfg.width,
        cfg.height,
        cfg.offset_x,
        cfg.offset_y,
        cfg.asset_url
    FROM cfg
    JOIN seed_dict sd ON sd.name = cfg.seed_name
    JOIN stage_dict gd ON gd.name = cfg.stage_name
)
UPDATE farm.seed_growth_stages sgs
SET growth_stage_id = r.growth_stage_id,
    duration_seconds = r.duration_seconds,
    bug_probability = r.bug_probability,
    width = r.width,
    height = r.height,
    offset_x = r.offset_x,
    offset_y = r.offset_y,
    asset_url = r.asset_url,
    updated_at = NOW(),
    updated_by = 0,
    is_deleted = false,
    status = 1
FROM resolved r
WHERE sgs.seed_type_id = r.seed_type_id
  AND sgs.stage_index = r.stage_index;

WITH stage_dict AS (
    SELECT id, name FROM farm.growth_stages WHERE is_deleted = false
),
seed_dict AS (
    SELECT id, name FROM farm.seed_types
    WHERE is_deleted = false
      AND name IN ('草莓', '茄子', '玉米', '蓝莓', '南瓜', '辣椒', '水稻')
),
cfg AS (
    SELECT * FROM (VALUES
        ('草莓', '种子',   1::smallint, 25::int, 0.0080::numeric(5,4),  92::int, 120::int, 58::int, 162::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('草莓', '发芽',   2::smallint, 30::int, 0.0140::numeric(5,4),  95::int, 126::int, 56::int, 156::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('草莓', '开花',   3::smallint, 35::int, 0.0200::numeric(5,4), 102::int, 132::int, 52::int, 150::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('草莓', '结果',   4::smallint, 40::int, 0.0260::numeric(5,4), 108::int, 138::int, 48::int, 145::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('草莓', '成熟',   5::smallint, 35::int, 0.0180::numeric(5,4), 112::int, 142::int, 46::int, 140::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('草莓', '枯萎',   6::smallint,  0::int, 0.0000::numeric(5,4), 112::int, 142::int, 46::int, 140::int, '/oss/defaults/seed/seed-stage-withered-default.png'),
        ('茄子', '种子',   1::smallint, 30::int, 0.0100::numeric(5,4),  94::int, 122::int, 57::int, 160::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('茄子', '发芽',   2::smallint, 35::int, 0.0160::numeric(5,4),  98::int, 128::int, 54::int, 154::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('茄子', '生长期', 3::smallint, 45::int, 0.0240::numeric(5,4), 104::int, 136::int, 50::int, 148::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('茄子', '结果',   4::smallint, 50::int, 0.0300::numeric(5,4), 110::int, 142::int, 46::int, 142::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('茄子', '成熟',   5::smallint, 42::int, 0.0220::numeric(5,4), 114::int, 146::int, 44::int, 138::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('茄子', '枯萎',   6::smallint,  0::int, 0.0000::numeric(5,4), 114::int, 146::int, 44::int, 138::int, '/oss/defaults/seed/seed-stage-withered-default.png'),
        ('玉米', '种子',   1::smallint, 40::int, 0.0100::numeric(5,4),  94::int, 124::int, 56::int, 160::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('玉米', '幼苗',   2::smallint, 55::int, 0.0180::numeric(5,4), 102::int, 136::int, 51::int, 150::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('玉米', '生长期', 3::smallint, 70::int, 0.0280::numeric(5,4), 110::int, 148::int, 46::int, 140::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('玉米', '成熟',   4::smallint, 60::int, 0.0220::numeric(5,4), 118::int, 156::int, 40::int, 130::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('玉米', '枯萎',   5::smallint,  0::int, 0.0000::numeric(5,4), 118::int, 156::int, 40::int, 130::int, '/oss/defaults/seed/seed-stage-withered-default.png'),
        ('蓝莓', '种子',   1::smallint, 35::int, 0.0100::numeric(5,4),  86::int, 116::int, 62::int, 166::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('蓝莓', '发芽',   2::smallint, 40::int, 0.0160::numeric(5,4),  90::int, 122::int, 60::int, 160::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('蓝莓', '幼苗',   3::smallint, 50::int, 0.0240::numeric(5,4),  96::int, 128::int, 56::int, 154::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('蓝莓', '生长期', 4::smallint, 55::int, 0.0300::numeric(5,4), 104::int, 136::int, 52::int, 148::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('蓝莓', '开花',   5::smallint, 60::int, 0.0340::numeric(5,4), 110::int, 142::int, 49::int, 142::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('蓝莓', '成熟',   6::smallint, 50::int, 0.0220::numeric(5,4), 114::int, 146::int, 46::int, 138::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('蓝莓', '枯萎',   7::smallint,  0::int, 0.0000::numeric(5,4), 114::int, 146::int, 46::int, 138::int, '/oss/defaults/seed/seed-stage-withered-default.png'),
        ('南瓜', '种子',   1::smallint, 30::int, 0.0090::numeric(5,4),  98::int, 122::int, 54::int, 158::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('南瓜', '发芽',   2::smallint, 35::int, 0.0140::numeric(5,4), 102::int, 126::int, 52::int, 154::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('南瓜', '幼苗',   3::smallint, 45::int, 0.0200::numeric(5,4), 110::int, 136::int, 48::int, 148::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('南瓜', '生长期', 4::smallint, 65::int, 0.0300::numeric(5,4), 120::int, 150::int, 42::int, 136::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('南瓜', '成熟',   5::smallint, 55::int, 0.0240::numeric(5,4), 126::int, 156::int, 38::int, 132::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('南瓜', '枯萎',   6::smallint,  0::int, 0.0000::numeric(5,4), 126::int, 156::int, 38::int, 132::int, '/oss/defaults/seed/seed-stage-withered-default.png'),
        ('辣椒', '种子',   1::smallint, 28::int, 0.0100::numeric(5,4),  90::int, 118::int, 60::int, 164::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('辣椒', '发芽',   2::smallint, 32::int, 0.0180::numeric(5,4),  94::int, 124::int, 57::int, 158::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('辣椒', '生长期', 3::smallint, 42::int, 0.0260::numeric(5,4), 100::int, 132::int, 54::int, 152::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('辣椒', '开花',   4::smallint, 50::int, 0.0320::numeric(5,4), 106::int, 138::int, 50::int, 146::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('辣椒', '成熟',   5::smallint, 45::int, 0.0240::numeric(5,4), 110::int, 144::int, 48::int, 142::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('辣椒', '枯萎',   6::smallint,  0::int, 0.0000::numeric(5,4), 110::int, 144::int, 48::int, 142::int, '/oss/defaults/seed/seed-stage-withered-default.png'),
        ('水稻', '种子',   1::smallint, 22::int, 0.0060::numeric(5,4),  88::int, 114::int, 61::int, 167::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('水稻', '幼苗',   2::smallint, 30::int, 0.0120::numeric(5,4),  94::int, 122::int, 58::int, 160::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('水稻', '生长期', 3::smallint, 45::int, 0.0180::numeric(5,4),  98::int, 130::int, 55::int, 154::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('水稻', '成熟',   4::smallint, 38::int, 0.0150::numeric(5,4), 102::int, 136::int, 52::int, 148::int, '/oss/defaults/seed/seed-stage-default.png'),
        ('水稻', '枯萎',   5::smallint,  0::int, 0.0000::numeric(5,4), 102::int, 136::int, 52::int, 148::int, '/oss/defaults/seed/seed-stage-withered-default.png')
    ) AS t(seed_name, stage_name, stage_index, duration_seconds, bug_probability, width, height, offset_x, offset_y, asset_url)
),
resolved AS (
    SELECT
        sd.id AS seed_type_id,
        gd.id AS growth_stage_id,
        cfg.stage_index,
        cfg.duration_seconds,
        cfg.bug_probability,
        cfg.width,
        cfg.height,
        cfg.offset_x,
        cfg.offset_y,
        cfg.asset_url
    FROM cfg
    JOIN seed_dict sd ON sd.name = cfg.seed_name
    JOIN stage_dict gd ON gd.name = cfg.stage_name
)
INSERT INTO farm.seed_growth_stages
(
    seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability,
    width, height, offset_x, offset_y,
    created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version
)
SELECT
    r.seed_type_id, r.growth_stage_id, r.stage_index, r.duration_seconds, r.asset_url, r.bug_probability,
    r.width, r.height, r.offset_x, r.offset_y,
    NOW(), NOW(), 0, 0, 'stage model aligned with explicit harvest/wither', 1, false, 0
FROM resolved r
WHERE NOT EXISTS (
    SELECT 1
    FROM farm.seed_growth_stages sgs
    WHERE sgs.seed_type_id = r.seed_type_id
      AND sgs.stage_index = r.stage_index
      AND sgs.is_deleted = false
);

WITH valid_stage AS (
    SELECT * FROM (VALUES
        ('草莓', 1::smallint), ('草莓', 2), ('草莓', 3), ('草莓', 4), ('草莓', 5), ('草莓', 6),
        ('茄子', 1), ('茄子', 2), ('茄子', 3), ('茄子', 4), ('茄子', 5), ('茄子', 6),
        ('玉米', 1), ('玉米', 2), ('玉米', 3), ('玉米', 4), ('玉米', 5),
        ('蓝莓', 1), ('蓝莓', 2), ('蓝莓', 3), ('蓝莓', 4), ('蓝莓', 5), ('蓝莓', 6), ('蓝莓', 7),
        ('南瓜', 1), ('南瓜', 2), ('南瓜', 3), ('南瓜', 4), ('南瓜', 5), ('南瓜', 6),
        ('辣椒', 1), ('辣椒', 2), ('辣椒', 3), ('辣椒', 4), ('辣椒', 5), ('辣椒', 6),
        ('水稻', 1), ('水稻', 2), ('水稻', 3), ('水稻', 4), ('水稻', 5)
    ) AS t(seed_name, stage_index)
),
seed_map AS (
    SELECT id, name FROM farm.seed_types WHERE is_deleted = false
)
UPDATE farm.seed_growth_stages sgs
SET is_deleted = true,
    updated_at = NOW(),
    updated_by = 0,
    remark = 'retired by explicit harvest/wither stage alignment'
FROM seed_map sm
WHERE sgs.seed_type_id = sm.id
  AND sm.name IN ('草莓', '茄子', '玉米', '蓝莓', '南瓜', '辣椒', '水稻')
  AND sgs.is_deleted = false
  AND NOT EXISTS (
      SELECT 1
      FROM valid_stage vs
      WHERE vs.seed_name = sm.name
        AND vs.stage_index = sgs.stage_index
  );
