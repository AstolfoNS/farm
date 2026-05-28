-- Phase 2 / Rollback
-- 回滚地块管理迁移（UTF-8）

-- =========================================================
-- 0) 安全提示
-- =========================================================
-- 本脚本会：
-- 1. 清空 user_plot_allocations / plot_policies / plot_types
-- 2. 用备份表恢复 user_plots
-- 请确认已执行过 phase2-002 且备份表存在。

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'farm'
          AND table_name = 'user_plots_bak_phase2'
    ) THEN
        RAISE EXCEPTION '缺少备份表 farm.user_plots_bak_phase2，无法回滚';
    END IF;
END $$;

-- =========================================================
-- 1) 先清理新引入策略数据
-- =========================================================
DELETE FROM farm.user_plot_allocations;
DELETE FROM farm.plot_policies;
DELETE FROM farm.plot_types;

-- =========================================================
-- 2) 恢复 user_plots（按备份）
-- =========================================================
TRUNCATE TABLE farm.user_plots RESTART IDENTITY;

INSERT INTO farm.user_plots
OVERRIDING SYSTEM VALUE
(
    id,
    user_id,
    soil_type_id,
    plot_index,
    unlock_experience_required,
    is_locked,
    unlocked_at,
    lock_reason,
    created_at,
    updated_at,
    created_by,
    updated_by,
    remark,
    status,
    is_deleted,
    opt_lock_version
)
SELECT
    id,
    user_id,
    soil_type_id,
    plot_index,
    unlock_experience_required,
    is_locked,
    unlocked_at,
    lock_reason,
    created_at,
    updated_at,
    created_by,
    updated_by,
    remark,
    status,
    is_deleted,
    opt_lock_version
FROM farm.user_plots_bak_phase2;

-- =========================================================
-- 3) 重置 user_plots 序列
-- =========================================================
SELECT setval(
    pg_get_serial_sequence('farm.user_plots', 'id'),
    COALESCE((SELECT MAX(id) FROM farm.user_plots), 1),
    true
);

