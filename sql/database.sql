-- ==========================================
-- 初始化 Schema 与 扩展
-- ==========================================
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
    avatar_url                  VARCHAR(1024)       NOT NULL DEFAULT 'https://example.com/default-avatar.png',
    experience                  BIGINT              NOT NULL DEFAULT 0,
    score                       BIGINT              NOT NULL DEFAULT 0,
    coin                        BIGINT              NOT NULL DEFAULT 0,

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
    code                        VARCHAR(64)         NOT NULL,
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
CREATE UNIQUE INDEX uk_seed_qualities_code_active
    ON farm.seed_qualities (code)
    WHERE is_deleted = false;
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
    level                       SMALLINT        NOT NULL,
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
-- 4. 生长阶段类型字典表
-- ==========================================
CREATE TABLE farm.growth_stages
(
    id                          BIGINT          NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    name                        VARCHAR(500)    NOT NULL,
    code                        VARCHAR(64)     NOT NULL,
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
CREATE UNIQUE INDEX uk_growth_stages_code_active
    ON farm.growth_stages(code)
    WHERE is_deleted = false;
CREATE UNIQUE INDEX uk_growth_stages_name_active
    ON farm.growth_stages(name)
    WHERE is_deleted = false;

-- ==========================================
-- 5. 种子类型配置表
-- ==========================================
CREATE TABLE farm.seed_types
(
    id                          BIGINT          NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    name                        VARCHAR(500)    NOT NULL,
    code                        VARCHAR(64)     NOT NULL,
    seed_quality_id             BIGINT          NOT NULL,
    enable_soil_type_bits       BIGINT          NOT NULL,
    level                       SMALLINT        NOT NULL,
    description                 TEXT                NULL,

    -- 机制与事件配置
    bug_probability             NUMERIC(5, 4)   NOT NULL DEFAULT 0.0000,
    max_bug_limit               SMALLINT        NOT NULL DEFAULT 0,
    max_harvest_count           SMALLINT        NOT NULL DEFAULT 1,
    regrow_stage_index          SMALLINT            NULL, -- 【优化】多次收获作物，收获后退回的阶段索引

    -- 经济数值配置
    price                       BIGINT          NOT NULL DEFAULT 0,
    harvest_experience          BIGINT          NOT NULL DEFAULT 0,
    harvest_fruit_number        INT             NOT NULL DEFAULT 0,
    fruit_price                 BIGINT          NOT NULL DEFAULT 0,
    harvest_score               BIGINT          NOT NULL DEFAULT 0,

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
CREATE UNIQUE INDEX uk_seed_types_code_active
    ON farm.seed_types (code)
    WHERE is_deleted = false;
CREATE UNIQUE INDEX uk_seed_types_name_active
    ON farm.seed_types (name)
    WHERE is_deleted = false;

-- ==========================================
-- 6. 种子生长过程配置表
-- ==========================================
CREATE TABLE farm.seed_growth_stages
(
    id                          BIGINT          NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    seed_type_id                BIGINT          NOT NULL,
    growth_stage_id             BIGINT          NOT NULL,

    stage_index                 SMALLINT        NOT NULL,
    duration_seconds            INT             NOT NULL,
    asset_url                   VARCHAR(1024)       NULL,

    -- 【优化】将 UI 渲染相关的字段从 user_crops 剥离到这里
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
-- 7. 用户种子背包表
-- ==========================================
CREATE TABLE farm.user_seeds
(
    id                          BIGINT          NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id                     BIGINT          NOT NULL,
    seed_type_id                BIGINT          NOT NULL,
    quantity                    BIGINT          NOT NULL DEFAULT 0,

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
-- 8. 用户地块表
-- ==========================================
CREATE TABLE farm.user_plots
(
    id                          BIGINT          NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id                     BIGINT          NOT NULL,
    soil_type_id                BIGINT          NOT NULL,
    plot_index                  SMALLINT        NOT NULL,

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
-- 9. 用户种植作物表
-- ==========================================
CREATE TABLE farm.user_crops
(
    id                          BIGINT          NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id                     BIGINT          NOT NULL,
    plot_id                     BIGINT          NOT NULL,
    seed_type_id                BIGINT          NOT NULL,

    -- 【优化】剥离了宽高等静态坐标，只留动态数据
    planted_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_harvest_at             TIMESTAMPTZ         NULL,
    harvest_count               SMALLINT        NOT NULL DEFAULT 0,
    grow_status                 SMALLINT        NOT NULL DEFAULT 0, -- 1:生长中, 2:成熟待收, 3:已枯萎
    bug_count                   SMALLINT        NOT NULL DEFAULT 0,

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
-- 10. 用户果实仓库表
-- ==========================================
CREATE TABLE farm.user_fruits
(
    id                          BIGINT          NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id                     BIGINT          NOT NULL,
    seed_type_id                BIGINT          NOT NULL, -- 指代该种子产出的果实
    quantity                    BIGINT          NOT NULL DEFAULT 0,

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
