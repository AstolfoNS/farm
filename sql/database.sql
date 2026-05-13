-- ==========================================
-- 鍐滃満绯荤粺寤鸿〃鑴氭湰锛堢粺涓€鍩哄噯锛?-- 瑙勫垯锛?-- 1) 鏃犲閿储寮?-- 2) 鏃犳櫘閫氱储寮?-- 3) 鏃犳暟鎹簱绾︽潫锛堝涓婚敭绾︽潫銆佸閿害鏉熴€佹鏌ョ害鏉燂級
-- 4) 鍙互鏈夊敮涓€绱㈠紩
-- 5) 闇€瑕佷富閿储寮曪紙浣跨敤 id 鐨勫敮涓€绱㈠紩瀹炵幇锛?-- ==========================================

DROP SCHEMA IF EXISTS farm CASCADE;
CREATE SCHEMA IF NOT EXISTS farm;

CREATE EXTENSION IF NOT EXISTS citext;

-- ==========================================
-- 1. 鐢ㄦ埛淇℃伅琛?-- ==========================================
CREATE TABLE farm.users
(
    id                          BIGINT GENERATED ALWAYS AS IDENTITY,
    username                    citext,
    nickname                    VARCHAR(500),
    password_hash               VARCHAR(500),
    email                       citext,
    avatar_url                  VARCHAR(1024) DEFAULT 'https://example.com/default-avatar.png',
    experience                  BIGINT DEFAULT 0,
    score                       BIGINT DEFAULT 0,
    coin                        BIGINT DEFAULT 0,
    created_at                  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by                  BIGINT,
    updated_by                  BIGINT,
    remark                      TEXT,
    status                      SMALLINT DEFAULT 1,
    is_deleted                  BOOLEAN DEFAULT false,
    opt_lock_version            INT DEFAULT 0
);
CREATE UNIQUE INDEX uk_users_username_active
    ON farm.users (username)
    WHERE is_deleted = false;
CREATE UNIQUE INDEX uk_users_email_active
    ON farm.users (email)
    WHERE is_deleted = false;

-- ==========================================
-- 2. 绉嶅瓙鍝佽川瀛楀吀琛?-- ==========================================
CREATE TABLE farm.seed_qualities
(
    id                          BIGINT GENERATED ALWAYS AS IDENTITY,
    name                        VARCHAR(500),
    code                        VARCHAR(64),
    description                 TEXT,
    created_at                  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by                  BIGINT,
    updated_by                  BIGINT,
    remark                      TEXT,
    status                      SMALLINT DEFAULT 1,
    is_deleted                  BOOLEAN DEFAULT false,
    opt_lock_version            INT DEFAULT 0
);
CREATE UNIQUE INDEX uk_seed_qualities_code_active
    ON farm.seed_qualities (code)
    WHERE is_deleted = false;
CREATE UNIQUE INDEX uk_seed_qualities_name_active
    ON farm.seed_qualities (name)
    WHERE is_deleted = false;

-- ==========================================
-- 3. 鍦熷湴绫诲瀷瀛楀吀琛?-- ==========================================
CREATE TABLE farm.soil_types
(
    id                          BIGINT GENERATED ALWAYS AS IDENTITY,
    name                        VARCHAR(500),
    bit_code                    INT,
    level                       SMALLINT,
    grow_speed_multiplier       NUMERIC(5, 2) DEFAULT 1.00,
    description                 TEXT,
    created_at                  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by                  BIGINT,
    updated_by                  BIGINT,
    remark                      TEXT,
    status                      SMALLINT DEFAULT 1,
    is_deleted                  BOOLEAN DEFAULT false,
    opt_lock_version            INT DEFAULT 0
);
CREATE UNIQUE INDEX uk_soil_types_bit_code_active
    ON farm.soil_types (bit_code)
    WHERE is_deleted = false;
CREATE UNIQUE INDEX uk_soil_types_name_active
    ON farm.soil_types (name)
    WHERE is_deleted = false;

-- ==========================================
-- 4. 鐢熼暱闃舵瀛楀吀琛?-- ==========================================
CREATE TABLE farm.growth_stages
(
    id                          BIGINT GENERATED ALWAYS AS IDENTITY,
    name                        VARCHAR(500),
    code                        VARCHAR(64),
    description                 TEXT,
    created_at                  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by                  BIGINT,
    updated_by                  BIGINT,
    remark                      TEXT,
    status                      SMALLINT DEFAULT 1,
    is_deleted                  BOOLEAN DEFAULT false,
    opt_lock_version            INT DEFAULT 0
);
CREATE UNIQUE INDEX uk_growth_stages_code_active
    ON farm.growth_stages (code)
    WHERE is_deleted = false;
CREATE UNIQUE INDEX uk_growth_stages_name_active
    ON farm.growth_stages (name)
    WHERE is_deleted = false;

-- ==========================================
-- 5. 绉嶅瓙绫诲瀷閰嶇疆琛?-- ==========================================
CREATE TABLE farm.seed_types
(
    id                          BIGINT GENERATED ALWAYS AS IDENTITY,
    name                        VARCHAR(500),
    code                        VARCHAR(64),
    seed_quality_id             BIGINT,
    enable_soil_type_bits       BIGINT,
    level                       SMALLINT,
    description                 TEXT,
    bug_probability             NUMERIC(5, 4) DEFAULT 0.0000,
    max_bug_limit               SMALLINT DEFAULT 0,
    max_harvest_count           SMALLINT DEFAULT 1,
    regrow_stage_index          SMALLINT,
    price                       BIGINT DEFAULT 0,
    harvest_experience          BIGINT DEFAULT 0,
    harvest_fruit_number        INT DEFAULT 0,
    fruit_price                 BIGINT DEFAULT 0,
    harvest_score               BIGINT DEFAULT 0,
    created_at                  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by                  BIGINT,
    updated_by                  BIGINT,
    remark                      TEXT,
    status                      SMALLINT DEFAULT 1,
    is_deleted                  BOOLEAN DEFAULT false,
    opt_lock_version            INT DEFAULT 0
);
CREATE UNIQUE INDEX uk_seed_types_code_active
    ON farm.seed_types (code)
    WHERE is_deleted = false;
CREATE UNIQUE INDEX uk_seed_types_name_active
    ON farm.seed_types (name)
    WHERE is_deleted = false;

-- ==========================================
-- 6. 绉嶅瓙鐢熼暱闃舵閰嶇疆琛?-- ==========================================
CREATE TABLE farm.seed_growth_stages
(
    id                          BIGINT GENERATED ALWAYS AS IDENTITY,
    seed_type_id                BIGINT,
    growth_stage_id             BIGINT,
    stage_index                 SMALLINT,
    duration_seconds            INT,
    asset_url                   VARCHAR(1024),
    width                       INT DEFAULT 0,
    height                      INT DEFAULT 0,
    offset_x                    INT DEFAULT 0,
    offset_y                    INT DEFAULT 0,
    created_at                  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by                  BIGINT,
    updated_by                  BIGINT,
    remark                      TEXT,
    status                      SMALLINT DEFAULT 1,
    is_deleted                  BOOLEAN DEFAULT false,
    opt_lock_version            INT DEFAULT 0
);
CREATE UNIQUE INDEX uk_seed_growth_stage_index
    ON farm.seed_growth_stages (seed_type_id, stage_index)
    WHERE is_deleted = false;

-- ==========================================
-- 7. 鐢ㄦ埛绉嶅瓙鑳屽寘琛?-- ==========================================
CREATE TABLE farm.user_seeds
(
    id                          BIGINT GENERATED ALWAYS AS IDENTITY,
    user_id                     BIGINT,
    seed_type_id                BIGINT,
    quantity                    BIGINT DEFAULT 0,
    created_at                  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by                  BIGINT,
    updated_by                  BIGINT,
    remark                      TEXT,
    status                      SMALLINT DEFAULT 1,
    is_deleted                  BOOLEAN DEFAULT false,
    opt_lock_version            INT DEFAULT 0
);
CREATE UNIQUE INDEX uk_user_seeds_active
    ON farm.user_seeds (user_id, seed_type_id)
    WHERE is_deleted = false;

-- ==========================================
-- 8. 鐢ㄦ埛鍦板潡琛?-- ==========================================
CREATE TABLE farm.user_plots
(
    id                          BIGINT GENERATED ALWAYS AS IDENTITY,
    user_id                     BIGINT,
    soil_type_id                BIGINT,
    plot_index                  SMALLINT,
    created_at                  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by                  BIGINT,
    updated_by                  BIGINT,
    remark                      TEXT,
    status                      SMALLINT DEFAULT 1,
    is_deleted                  BOOLEAN DEFAULT false,
    opt_lock_version            INT DEFAULT 0
);
CREATE UNIQUE INDEX uk_user_plot_index
    ON farm.user_plots (user_id, plot_index)
    WHERE is_deleted = false;

-- ==========================================
-- 9. 鐢ㄦ埛绉嶆浣滅墿琛?-- ==========================================
CREATE TABLE farm.user_crops
(
    id                          BIGINT GENERATED ALWAYS AS IDENTITY,
    user_id                     BIGINT,
    plot_id                     BIGINT,
    seed_type_id                BIGINT,
    planted_at                  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    last_harvest_at             TIMESTAMPTZ,
    harvest_count               SMALLINT DEFAULT 0,
    grow_status                 SMALLINT DEFAULT 0,
    bug_count                   SMALLINT DEFAULT 0,
    created_at                  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by                  BIGINT,
    updated_by                  BIGINT,
    remark                      TEXT,
    status                      SMALLINT DEFAULT 1,
    is_deleted                  BOOLEAN DEFAULT false,
    opt_lock_version            INT DEFAULT 0
);
CREATE UNIQUE INDEX uk_plot_active_crop
    ON farm.user_crops (plot_id)
    WHERE is_deleted = false;

-- ==========================================
-- 10. 鐢ㄦ埛鏋滃疄浠撳簱琛?-- ==========================================
CREATE TABLE farm.user_fruits
(
    id                          BIGINT GENERATED ALWAYS AS IDENTITY,
    user_id                     BIGINT,
    seed_type_id                BIGINT,
    quantity                    BIGINT DEFAULT 0,
    created_at                  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by                  BIGINT,
    updated_by                  BIGINT,
    remark                      TEXT,
    status                      SMALLINT DEFAULT 1,
    is_deleted                  BOOLEAN DEFAULT false,
    opt_lock_version            INT DEFAULT 0
);
CREATE UNIQUE INDEX uk_user_fruits_active
    ON farm.user_fruits (user_id, seed_type_id)
    WHERE is_deleted = false;

-- ==========================================
-- 涓婚敭绱㈠紩锛堜粎绱㈠紩锛屼笉浣跨敤涓婚敭绾︽潫锛?-- ==========================================
CREATE UNIQUE INDEX pk_users_id ON farm.users (id);
CREATE UNIQUE INDEX pk_seed_qualities_id ON farm.seed_qualities (id);
CREATE UNIQUE INDEX pk_soil_types_id ON farm.soil_types (id);
CREATE UNIQUE INDEX pk_growth_stages_id ON farm.growth_stages (id);
CREATE UNIQUE INDEX pk_seed_types_id ON farm.seed_types (id);
CREATE UNIQUE INDEX pk_seed_growth_stages_id ON farm.seed_growth_stages (id);
CREATE UNIQUE INDEX pk_user_seeds_id ON farm.user_seeds (id);
CREATE UNIQUE INDEX pk_user_plots_id ON farm.user_plots (id);
CREATE UNIQUE INDEX pk_user_crops_id ON farm.user_crops (id);
CREATE UNIQUE INDEX pk_user_fruits_id ON farm.user_fruits (id);

