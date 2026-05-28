-- Phase 2 / Step 1
-- 地块管理相关表结构（UTF-8）

-- =========================================================
-- 1) 地块类型表
-- =========================================================
CREATE TABLE IF NOT EXISTS farm.plot_types
(
    id                                  BIGINT          NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name                                VARCHAR(128)    NOT NULL,
    icon_url                            VARCHAR(1024)   NOT NULL DEFAULT '',
    soil_type_id                        BIGINT          NOT NULL,
    unlock_required                     BOOLEAN         NOT NULL DEFAULT true,
    default_usable                      BOOLEAN         NOT NULL DEFAULT true,
    default_unlock_experience_required  BIGINT          NOT NULL DEFAULT 0,
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

CREATE UNIQUE INDEX IF NOT EXISTS uk_plot_types_name_active
    ON farm.plot_types(name)
    WHERE is_deleted = false;

CREATE INDEX IF NOT EXISTS idx_plot_types_soil_type_active
    ON farm.plot_types(soil_type_id)
    WHERE is_deleted = false;

-- =========================================================
-- 2) 地块全局策略表
-- =========================================================
CREATE TABLE IF NOT EXISTS farm.plot_policies
(
    id                              BIGINT          NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    policy_name                     VARCHAR(128)    NOT NULL,
    active                          BOOLEAN         NOT NULL DEFAULT true,
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

CREATE INDEX IF NOT EXISTS idx_plot_policies_active
    ON farm.plot_policies(active)
    WHERE is_deleted = false;

-- =========================================================
-- 3) 用户地块分配策略表
-- =========================================================
CREATE TABLE IF NOT EXISTS farm.user_plot_allocations
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

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_plot_allocations_user_active
    ON farm.user_plot_allocations(user_id)
    WHERE is_deleted = false;

CREATE INDEX IF NOT EXISTS idx_user_plot_allocations_active
    ON farm.user_plot_allocations(active)
    WHERE is_deleted = false;

-- =========================================================
-- 4) 约束（使用 DO 保证幂等）
-- =========================================================
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_plot_types_soil_type'
    ) THEN
        ALTER TABLE farm.plot_types
            ADD CONSTRAINT fk_plot_types_soil_type
                FOREIGN KEY (soil_type_id) REFERENCES farm.soil_types(id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_plot_policies_default_plot_type'
    ) THEN
        ALTER TABLE farm.plot_policies
            ADD CONSTRAINT fk_plot_policies_default_plot_type
                FOREIGN KEY (default_plot_type_id) REFERENCES farm.plot_types(id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_user_plot_allocations_user'
    ) THEN
        ALTER TABLE farm.user_plot_allocations
            ADD CONSTRAINT fk_user_plot_allocations_user
                FOREIGN KEY (user_id) REFERENCES farm.users(id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_user_plot_allocations_default_plot_type'
    ) THEN
        ALTER TABLE farm.user_plot_allocations
            ADD CONSTRAINT fk_user_plot_allocations_default_plot_type
                FOREIGN KEY (default_plot_type_id) REFERENCES farm.plot_types(id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'ck_plot_policies_counts'
    ) THEN
        ALTER TABLE farm.plot_policies
            ADD CONSTRAINT ck_plot_policies_counts
                CHECK (
                    default_total_plot_count >= 1
                    AND default_unlocked_plot_count >= 0
                    AND default_locked_plot_count >= 0
                    AND default_unlocked_plot_count <= default_total_plot_count
                    AND default_locked_plot_count = default_total_plot_count - default_unlocked_plot_count
                );
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'ck_user_plot_allocations_counts'
    ) THEN
        ALTER TABLE farm.user_plot_allocations
            ADD CONSTRAINT ck_user_plot_allocations_counts
                CHECK (
                    total_plot_count >= 1
                    AND unlocked_plot_count >= 0
                    AND locked_plot_count >= 0
                    AND unlocked_plot_count <= total_plot_count
                    AND locked_plot_count = total_plot_count - unlocked_plot_count
                );
    END IF;
END $$;

