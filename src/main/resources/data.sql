-- ==========================================
-- 初始化 Schema 与 扩展
-- ==========================================

BEGIN;

DROP SCHEMA IF EXISTS farm CASCADE;
CREATE SCHEMA IF NOT EXISTS farm;

CREATE EXTENSION IF NOT EXISTS citext;



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
-- 2. 默认资源配置表
-- ==========================================
CREATE TABLE farm.asset_defaults
(
    id                          BIGINT              NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    asset_key                   VARCHAR(128)        NOT NULL,
    asset_url                   VARCHAR(1024)       NOT NULL,
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
COMMENT ON TABLE farm.asset_defaults IS '默认资源配置表';
CREATE UNIQUE INDEX uk_asset_defaults_key_active
    ON farm.asset_defaults(asset_key)
    WHERE is_deleted = false;

-- ==========================================
-- 3. 种子品质表
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
-- 4. 土壤类型表
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
    expand_cost_coin            BIGINT          NOT NULL DEFAULT 0,
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
    default_lock_rule_code          VARCHAR(64)     NOT NULL DEFAULT 'DEFAULT_LOCKED',
    default_lock_reason             VARCHAR(255)    NOT NULL DEFAULT '待解锁',

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

-- ==========================================
-- 6. 生长阶段类型字典表
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
-- 7. 种子类型配置表
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
    harvest_stage_index         SMALLINT            NULL, -- 可收获的阶段索引

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
-- 8. 种子生长过程配置表
-- ==========================================
CREATE TABLE farm.seed_growth_stages
(
    id                          BIGINT          NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    seed_type_id                BIGINT          NOT NULL,
    growth_stage_id             BIGINT          NOT NULL,

    stage_index                 SMALLINT        NOT NULL,
    duration_seconds            INT             NOT NULL,
    asset_url                   VARCHAR(1024)       NULL,
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
-- 9. 用户种子背包表
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
-- 10. 用户地块表
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
-- 11. 用户种植作物表
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
-- 12. 用户果实仓库表
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
-- 13. 用户资产流水表
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
-- 14. 用户库存流水表
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
-- 15. 作物行为日志表
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
-- 16. 请求幂等记录表
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
-- 高度优化的幂等数据初始化 (UPSERT 策略)
-- ==========================================

-- 0) 默认资源路径 (合并基础与最终版)
INSERT INTO farm.asset_defaults (asset_key, asset_url, description) VALUES
                                                                        ('avatar', '/oss/defaults/avatar/default-avatar.png', '用户头像默认图'),
                                                                        ('seedCover', '/oss/defaults/seed/seed-cover-default.png', '种子封面默认图'),
                                                                        ('seedStage', '/oss/defaults/seed/seed-stage-default.png', '种子阶段默认图'),
                                                                        ('soilCover', '/oss/defaults/soil/soil-default.png', '土壤默认图'),
                                                                        ('plotCover', '/oss/defaults/plot/plot-cover-default.png', '地块封面默认图'),
                                                                        ('plotIcon', '/oss/defaults/plot/plot-icon-default.png', '地块图标默认图'),
                                                                        ('bgm', '/resources/sounds/bgm/Must%20Work%20to%20Eat.wav', '默认背景音乐'),
                                                                        ('seedStageWithered', '/oss/defaults/seed/seed-stage-withered-default.png', '种子枯萎阶段默认图')
ON CONFLICT (asset_key) WHERE is_deleted = false DO UPDATE SET
                                                               asset_url = EXCLUDED.asset_url, description = EXCLUDED.description;

-- 1) 种子品质字典
INSERT INTO farm.seed_qualities (name, description) VALUES
                                                        ('普通', '普通品质种子'),
                                                        ('优质', '优质品质种子'),
                                                        ('稀有', '稀有品质种子')
ON CONFLICT (name) WHERE is_deleted = false DO NOTHING;

-- 2) 土地类型字典 (集成最终版数据)
INSERT INTO farm.soil_types (name, bit_code, cover_image_url, level, unlock_experience_required, grow_speed_multiplier, expand_cost_coin, description) VALUES
                                                                                                                                                           ('黄土地', 1, '/oss/defaults/soil/soil-land-default.png', 1, 0, 1.00, 0, '基础土地，适配多数作物'),
                                                                                                                                                           ('黑土地', 2, '/oss/defaults/soil/soil-land-black-default.png', 2, 500, 0.90, 1500, '生长速度更快的改良土地'),
                                                                                                                                                           ('金土地', 4, '/oss/defaults/soil/soil-land-gold-default.png', 3, 2000, 0.80, 5000, '高级土地，适配高等级作物')
ON CONFLICT (bit_code) WHERE is_deleted = false DO UPDATE SET
                                                              cover_image_url = EXCLUDED.cover_image_url, updated_at = NOW();

-- 3) 生长阶段字典 (合并全量阶段)
INSERT INTO farm.growth_stages (name, description) VALUES
                                                       ('种子', '播种后的初始阶段'),
                                                       ('发芽', '发芽阶段'),
                                                       ('幼苗', '发芽后的幼苗期'),
                                                       ('生长期', '快速生长阶段'),
                                                       ('开花', '开花授粉阶段'),
                                                       ('结果', '开花后结果阶段'),
                                                       ('成熟', '可收获阶段'),
                                                       ('枯萎', '作物虫害超限或错过收获窗口后的最终阶段')
ON CONFLICT (name) WHERE is_deleted = false DO NOTHING;

-- 4) 种子类型配置 (整合基础+增强补丁的全量字段配置)
WITH seed_data (name, quality_name, soil_bits, level, descr, max_bug, max_harvest, regrow_idx, harvest_idx, price, harv_exp, harv_fruit, fruit_loss, bug_coin, bug_exp, bug_score, fruit_price, harv_score) AS (
    VALUES
        ('草莓', '优质', 3::bigint, 2::smallint, '甜度高，可多次结果', 4::smallint, 6::smallint, NULL::smallint, 6::smallint, 18::bigint, 36::bigint, 5::int, 1::int, 3::bigint, 3::bigint, 2::bigint, 22::bigint, 28::bigint),
        ('茄子', '优质', 3::bigint, 2::smallint, '稳定产出，适合中期', 4::smallint, 6::smallint, NULL::smallint, 6::smallint, 16::bigint, 30::bigint, 4::int, 1::int, 3::bigint, 2::bigint, 2::bigint, 20::bigint, 24::bigint),
        ('玉米', '普通', 1::bigint, 1::smallint, '阶段较少，节奏快', 3::smallint, 5::smallint, NULL::smallint, 5::smallint, 12::bigint, 22::bigint, 3::int, 1::int, 2::bigint, 2::bigint, 1::bigint, 16::bigint, 18::bigint),
        ('蓝莓', '稀有', 7::bigint, 3::smallint, '成长慢但单价高，可连续采收', 5::smallint, 7::smallint, 5::smallint, 7::smallint, 36::bigint, 68::bigint, 4::int, 1::int, 4::bigint, 4::bigint, 3::bigint, 38::bigint, 62::bigint),
        ('南瓜', '优质', 3::bigint, 2::smallint, '成熟耗时较长，单次收益高', 4::smallint, 6::smallint, NULL::smallint, 6::smallint, 24::bigint, 48::bigint, 6::int, 2::int, 3::bigint, 3::bigint, 2::bigint, 30::bigint, 42::bigint),
        ('辣椒', '稀有', 7::bigint, 3::smallint, '可再生采收，风险和收益并存', 5::smallint, 6::smallint, 4::smallint, 6::smallint, 28::bigint, 55::bigint, 4::int, 1::int, 4::bigint, 4::bigint, 3::bigint, 33::bigint, 50::bigint),
        ('水稻', '普通', 1::bigint, 1::smallint, '基础粮作，前中期成长快', 3::smallint, 5::smallint, 3::smallint, 5::smallint, 10::bigint, 20::bigint, 3::int, 1::int, 2::bigint, 2::bigint, 1::bigint, 14::bigint, 16::bigint)
)
INSERT INTO farm.seed_types (
    name, cover_image_url, seed_quality_id, enable_soil_type_bits, level, description,
    max_bug_limit, max_harvest_count, regrow_stage_index, harvest_stage_index,
    price, harvest_experience, harvest_fruit_number, fruit_loss_per_bug,
    bug_kill_coin_reward, bug_kill_experience_reward, bug_kill_score_reward, fruit_price, harvest_score,
    unlock_experience_required
)
SELECT
    d.name, '/oss/defaults/seed/seed-cover-default.png', q.id, d.soil_bits, d.level, d.descr,
    d.max_bug, d.max_harvest, d.regrow_idx, d.harvest_idx,
    d.price, d.harv_exp, d.harv_fruit, d.fruit_loss,
    d.bug_coin, d.bug_exp, d.bug_score, d.fruit_price, d.harv_score,
    CASE
        WHEN d.level <= 1 THEN CASE WHEN d.max_harvest >= 2 THEN 120 ELSE 0 END
        WHEN d.level = 2 THEN 420 + GREATEST(d.max_harvest - 1, 0) * 80
        WHEN d.level = 3 THEN 980 + GREATEST(d.max_harvest - 1, 0) * 140
        ELSE 1600 + (d.level - 4) * 700 + GREATEST(d.max_harvest - 1, 0) * 180
        END
FROM seed_data d
         JOIN farm.seed_qualities q ON q.name = d.quality_name AND q.is_deleted = false
ON CONFLICT (name) WHERE is_deleted = false DO UPDATE SET
                                                          seed_quality_id = EXCLUDED.seed_quality_id, enable_soil_type_bits = EXCLUDED.enable_soil_type_bits,
                                                          level = EXCLUDED.level, description = EXCLUDED.description, max_bug_limit = EXCLUDED.max_bug_limit,
                                                          max_harvest_count = EXCLUDED.max_harvest_count, regrow_stage_index = EXCLUDED.regrow_stage_index,
                                                          harvest_stage_index = EXCLUDED.harvest_stage_index, price = EXCLUDED.price,
                                                          harvest_experience = EXCLUDED.harvest_experience, harvest_fruit_number = EXCLUDED.harvest_fruit_number,
                                                          fruit_loss_per_bug = EXCLUDED.fruit_loss_per_bug, bug_kill_coin_reward = EXCLUDED.bug_kill_coin_reward,
                                                          bug_kill_experience_reward = EXCLUDED.bug_kill_experience_reward, bug_kill_score_reward = EXCLUDED.bug_kill_score_reward,
                                                          fruit_price = EXCLUDED.fruit_price, harvest_score = EXCLUDED.harvest_score,
                                                          unlock_experience_required = EXCLUDED.unlock_experience_required, updated_at = NOW();

-- 5) 种子生长阶段配置 (直接应用最终带枯萎的补丁阶段)
WITH stage_data (seed_name, stage_name, stage_idx, dur_sec, bug_prob, w, h, ox, oy, asset) AS (
    VALUES
        ('草莓','种子',1::smallint,25::int,0.0080::numeric,92,120,58,162,'/oss/defaults/seed/seed-stage-default.png'),
        ('草莓','发芽',2,30,0.0140,95,126,56,156,'/oss/defaults/seed/seed-stage-default.png'),
        ('草莓','开花',3,35,0.0200,102,132,52,150,'/oss/defaults/seed/seed-stage-default.png'),
        ('草莓','结果',4,40,0.0260,108,138,48,145,'/oss/defaults/seed/seed-stage-default.png'),
        ('草莓','成熟',5,35,0.0180,112,142,46,140,'/oss/defaults/seed/seed-stage-default.png'),
        ('草莓','枯萎',6,0,0.0000,112,142,46,140,'/oss/defaults/seed/seed-stage-withered-default.png'),
        ('茄子','种子',1,30,0.0100,94,122,57,160,'/oss/defaults/seed/seed-stage-default.png'),
        ('茄子','发芽',2,35,0.0160,98,128,54,154,'/oss/defaults/seed/seed-stage-default.png'),
        ('茄子','生长期',3,45,0.0240,104,136,50,148,'/oss/defaults/seed/seed-stage-default.png'),
        ('茄子','结果',4,50,0.0300,110,142,46,142,'/oss/defaults/seed/seed-stage-default.png'),
        ('茄子','成熟',5,42,0.0220,114,146,44,138,'/oss/defaults/seed/seed-stage-default.png'),
        ('茄子','枯萎',6,0,0.0000,114,146,44,138,'/oss/defaults/seed/seed-stage-withered-default.png'),
        ('玉米','种子',1,40,0.0100,94,124,56,160,'/oss/defaults/seed/seed-stage-default.png'),
        ('玉米','幼苗',2,55,0.0180,102,136,51,150,'/oss/defaults/seed/seed-stage-default.png'),
        ('玉米','生长期',3,70,0.0280,110,148,46,140,'/oss/defaults/seed/seed-stage-default.png'),
        ('玉米','成熟',4,60,0.0220,118,156,40,130,'/oss/defaults/seed/seed-stage-default.png'),
        ('玉米','枯萎',5,0,0.0000,118,156,40,130,'/oss/defaults/seed/seed-stage-withered-default.png'),
        ('蓝莓','种子',1,35,0.0100,86,116,62,166,'/oss/defaults/seed/seed-stage-default.png'),
        ('蓝莓','发芽',2,40,0.0160,90,122,60,160,'/oss/defaults/seed/seed-stage-default.png'),
        ('蓝莓','幼苗',3,50,0.0240,96,128,56,154,'/oss/defaults/seed/seed-stage-default.png'),
        ('蓝莓','生长期',4,55,0.0300,104,136,52,148,'/oss/defaults/seed/seed-stage-default.png'),
        ('蓝莓','开花',5,60,0.0340,110,142,49,142,'/oss/defaults/seed/seed-stage-default.png'),
        ('蓝莓','成熟',6,50,0.0220,114,146,46,138,'/oss/defaults/seed/seed-stage-default.png'),
        ('蓝莓','枯萎',7,0,0.0000,114,146,46,138,'/oss/defaults/seed/seed-stage-withered-default.png'),
        ('南瓜','种子',1,30,0.0090,98,122,54,158,'/oss/defaults/seed/seed-stage-default.png'),
        ('南瓜','发芽',2,35,0.0140,102,126,52,154,'/oss/defaults/seed/seed-stage-default.png'),
        ('南瓜','幼苗',3,45,0.0200,110,136,48,148,'/oss/defaults/seed/seed-stage-default.png'),
        ('南瓜','生长期',4,65,0.0300,120,150,42,136,'/oss/defaults/seed/seed-stage-default.png'),
        ('南瓜','成熟',5,55,0.0240,126,156,38,132,'/oss/defaults/seed/seed-stage-default.png'),
        ('南瓜','枯萎',6,0,0.0000,126,156,38,132,'/oss/defaults/seed/seed-stage-withered-default.png'),
        ('辣椒','种子',1,28,0.0100,90,118,60,164,'/oss/defaults/seed/seed-stage-default.png'),
        ('辣椒','发芽',2,32,0.0180,94,124,57,158,'/oss/defaults/seed/seed-stage-default.png'),
        ('辣椒','生长期',3,42,0.0260,100,132,54,152,'/oss/defaults/seed/seed-stage-default.png'),
        ('辣椒','开花',4,50,0.0320,106,138,50,146,'/oss/defaults/seed/seed-stage-default.png'),
        ('辣椒','成熟',5,45,0.0240,110,144,48,142,'/oss/defaults/seed/seed-stage-default.png'),
        ('辣椒','枯萎',6,0,0.0000,110,144,48,142,'/oss/defaults/seed/seed-stage-withered-default.png'),
        ('水稻','种子',1,22,0.0060,88,114,61,167,'/oss/defaults/seed/seed-stage-default.png'),
        ('水稻','幼苗',2,30,0.0120,94,122,58,160,'/oss/defaults/seed/seed-stage-default.png'),
        ('水稻','生长期',3,45,0.0180,98,130,55,154,'/oss/defaults/seed/seed-stage-default.png'),
        ('水稻','成熟',4,38,0.0150,102,136,52,148,'/oss/defaults/seed/seed-stage-default.png'),
        ('水稻','枯萎',5,0,0.0000,102,136,52,148,'/oss/defaults/seed/seed-stage-withered-default.png')
)
INSERT INTO farm.seed_growth_stages (
    seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y
)
SELECT sd.id, gd.id, d.stage_idx, d.dur_sec, d.asset, d.bug_prob, d.w, d.h, d.ox, d.oy
FROM stage_data d
         JOIN farm.seed_types sd ON sd.name = d.seed_name AND sd.is_deleted = false
         JOIN farm.growth_stages gd ON gd.name = d.stage_name AND gd.is_deleted = false
ON CONFLICT (seed_type_id, stage_index) WHERE is_deleted = false DO UPDATE SET
                                                                               growth_stage_id = EXCLUDED.growth_stage_id, duration_seconds = EXCLUDED.duration_seconds,
                                                                               asset_url = EXCLUDED.asset_url, bug_probability = EXCLUDED.bug_probability,
                                                                               width = EXCLUDED.width, height = EXCLUDED.height, offset_x = EXCLUDED.offset_x, offset_y = EXCLUDED.offset_y, updated_at = NOW();

-- 软删除作废的阶段 (幂等处理，保留有效阶段即可)
WITH valid_stages (seed_name, max_idx) AS (
    VALUES
        ('草莓', 6::smallint),
        ('茄子', 6::smallint),
        ('玉米', 5::smallint),
        ('蓝莓', 7::smallint),
        ('南瓜', 6::smallint),
        ('辣椒', 6::smallint),
        ('水稻', 5::smallint)
),
     mapped_stages AS (
         SELECT sd.id AS seed_type_id, vs.max_idx
         FROM valid_stages vs
                  JOIN farm.seed_types sd ON sd.name = vs.seed_name AND sd.is_deleted = false
     )
UPDATE farm.seed_growth_stages sgs
SET is_deleted = true, updated_at = NOW(), remark = 'auto-retired by optimization'
FROM mapped_stages ms
WHERE sgs.seed_type_id = ms.seed_type_id
  AND sgs.stage_index > ms.max_idx
  AND sgs.is_deleted = false;

-- 6) 用户初始化数据 (整合包含增强补丁的全量用户信息)
INSERT INTO farm.users (username, nickname, password_hash, email, avatar_url, experience, score, coin, preferences_json) VALUES
                                                                                                                             ('liubei',  '刘备', '123456', 'liubei@farm.local', '/oss/defaults/avatar/default-avatar.png', 2400, 820, 5600, '{"audio":{"effectEnabled":true,"effectVolume":0.8,"bgmEnabled":true,"bgmVolume":0.6}}'),
                                                                                                                             ('caocao',  '曹操', '123456', 'caocao@farm.local', '/oss/defaults/avatar/default-avatar.png', 2100, 700, 4900, '{"audio":{"effectEnabled":true,"effectVolume":0.8,"bgmEnabled":true,"bgmVolume":0.6}}'),
                                                                                                                             ('sunquan', '孙权', '123456', 'sunquan@farm.local', '/oss/defaults/avatar/default-avatar.png', 1750, 540, 3600, '{"audio":{"effectEnabled":true,"effectVolume":0.8,"bgmEnabled":true,"bgmVolume":0.6}}'),
                                                                                                                             ('zhaoyun', '赵云', '123456', 'zhaoyun@farm.local', '/oss/defaults/avatar/default-avatar.png', 1450, 430, 2800, '{"audio":{"effectEnabled":true,"effectVolume":0.8,"bgmEnabled":true,"bgmVolume":0.6}}'),
                                                                                                                             ('huatuo',  '华佗', '123456', 'huatuo@farm.local', '/oss/defaults/avatar/default-avatar.png', 900,  260, 1800, '{"audio":{"effectEnabled":true,"effectVolume":0.8,"bgmEnabled":true,"bgmVolume":0.6}}')
ON CONFLICT (username) WHERE is_deleted = false DO UPDATE SET
                                                              nickname = EXCLUDED.nickname, email = EXCLUDED.email, experience = EXCLUDED.experience,
                                                              score = EXCLUDED.score, coin = EXCLUDED.coin, updated_at = NOW();

-- 7) 全局策略初始化
INSERT INTO farm.plot_policies (policy_name, active, default_total_plot_count, default_unlocked_plot_count, default_locked_plot_count)
VALUES ('default-policy-v1', true, 6, 2, 4)
ON CONFLICT DO NOTHING;

-- 8) 用户地块实例网格化
WITH alloc AS (
    SELECT
        u.id AS user_id,
        COALESCE(pp.default_total_plot_count, 6)::int AS total_plot_count,
        COALESCE(pp.default_unlocked_plot_count, 2)::int AS unlocked_plot_count
    FROM farm.users u
             LEFT JOIN farm.plot_policies pp ON pp.active = true AND pp.is_deleted = false
    WHERE u.is_deleted = false
),
     soil_map AS (
         SELECT
             COALESCE(MAX(CASE WHEN bit_code = 1 THEN id END), MIN(id)) AS soil_a,
             COALESCE(MAX(CASE WHEN bit_code = 2 THEN id END), MAX(CASE WHEN bit_code = 1 THEN id END), MIN(id)) AS soil_b,
             COALESCE(MAX(CASE WHEN bit_code = 4 THEN id END), MAX(CASE WHEN bit_code = 2 THEN id END), MAX(CASE WHEN bit_code = 1 THEN id END), MIN(id)) AS soil_c
         FROM farm.soil_types WHERE is_deleted = false
     ),
     grid AS (
         SELECT
             a.user_id, gs.plot_index::smallint AS plot_index,
             CASE WHEN gs.plot_index % 5 = 0 THEN sm.soil_c WHEN gs.plot_index % 2 = 0 THEN sm.soil_b ELSE sm.soil_a END AS soil_type_id,
             CASE WHEN gs.plot_index <= a.unlocked_plot_count THEN 0::bigint ELSE 600::bigint + ((gs.plot_index - a.unlocked_plot_count - 1) * 250)::bigint END AS unlock_experience_required,
             (gs.plot_index > a.unlocked_plot_count) AS is_locked
         FROM alloc a
                  CROSS JOIN soil_map sm
                  JOIN LATERAL generate_series(1, a.total_plot_count) AS gs(plot_index) ON true
     )
INSERT INTO farm.user_plots (user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason)
SELECT
    g.user_id, g.soil_type_id, g.plot_index, g.unlock_experience_required, g.is_locked,
    CASE WHEN g.is_locked THEN NULL ELSE NOW() END, CASE WHEN g.is_locked THEN '经验不足，待解锁' ELSE NULL END
FROM grid g
ON CONFLICT (user_id, plot_index) WHERE is_deleted = false DO UPDATE SET
                                                                         soil_type_id = EXCLUDED.soil_type_id, unlock_experience_required = EXCLUDED.unlock_experience_required,
                                                                         is_locked = EXCLUDED.is_locked, unlocked_at = CASE WHEN EXCLUDED.is_locked THEN NULL ELSE COALESCE(farm.user_plots.unlocked_at, NOW()) END,
                                                                         lock_reason = EXCLUDED.lock_reason, updated_at = NOW();

-- 超额地块安全软删除
WITH alloc AS (
    SELECT
        u.id AS user_id,
        COALESCE(pp.default_total_plot_count, 6)::int AS total_plot_count
    FROM farm.users u
             LEFT JOIN farm.plot_policies pp ON pp.active = true AND pp.is_deleted = false
    WHERE u.is_deleted = false
)
UPDATE farm.user_plots up
SET is_deleted = true, updated_at = NOW(), remark = 'auto-retired by allocation sync'
FROM alloc a
WHERE up.user_id = a.user_id AND up.plot_index > a.total_plot_count AND up.is_deleted = false;

-- 9) 用户种子背包初始化
WITH cfg (username, seed_name, quantity) AS (
    VALUES
        ('liubei',  '草莓', 28::bigint), ('liubei',  '蓝莓', 12::bigint), ('liubei',  '玉米', 20::bigint), ('liubei',  '南瓜', 14::bigint), ('liubei',  '辣椒', 16::bigint), ('liubei',  '水稻', 24::bigint),
        ('caocao',  '草莓', 16::bigint), ('caocao',  '玉米', 18::bigint), ('caocao',  '南瓜', 10::bigint), ('caocao',  '水稻', 18::bigint),
        ('sunquan', '草莓', 12::bigint), ('sunquan', '玉米', 12::bigint), ('sunquan', '水稻', 15::bigint),
        ('zhaoyun', '草莓', 10::bigint), ('zhaoyun', '玉米', 10::bigint), ('zhaoyun', '水稻', 12::bigint),
        ('huatuo',  '草莓',  8::bigint), ('huatuo',  '玉米',  8::bigint), ('huatuo',  '水稻', 10::bigint)
)
INSERT INTO farm.user_seeds (user_id, seed_type_id, quantity)
SELECT u.id, st.id, cfg.quantity
FROM cfg
         JOIN farm.users u ON u.username = cfg.username AND u.is_deleted = false
         JOIN farm.seed_types st ON st.name = cfg.seed_name AND st.is_deleted = false
ON CONFLICT (user_id, seed_type_id) WHERE is_deleted = false DO UPDATE SET
                                                                           quantity = EXCLUDED.quantity, updated_at = NOW();

COMMIT;

