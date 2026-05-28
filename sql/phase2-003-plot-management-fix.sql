-- Phase 2 / Step 3
-- 分配修复脚本（UTF-8）

-- =========================================================
-- A) 修正 allocation 数量一致性
-- =========================================================
WITH calc AS (
    SELECT
        ua.id,
        GREATEST(ua.total_plot_count, 1)::smallint                                    AS total_cnt,
        LEAST(GREATEST(ua.unlocked_plot_count, 0), GREATEST(ua.total_plot_count, 1))::smallint AS unlocked_cnt
    FROM farm.user_plot_allocations ua
    WHERE ua.is_deleted = false
)
UPDATE farm.user_plot_allocations ua
SET
    total_plot_count = c.total_cnt,
    unlocked_plot_count = c.unlocked_cnt,
    locked_plot_count = (c.total_cnt - c.unlocked_cnt)::smallint,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 0
FROM calc c
WHERE ua.id = c.id;

-- =========================================================
-- B) 若 allocation 默认类型为空，回填一个可用 plot_type
-- =========================================================
WITH fallback_type AS (
    SELECT pt.id
    FROM farm.plot_types pt
    WHERE pt.is_deleted = false
    ORDER BY pt.default_usable DESC, pt.sort_order ASC, pt.id ASC
    LIMIT 1
)
UPDATE farm.user_plot_allocations ua
SET
    default_plot_type_id = ft.id,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 0
FROM fallback_type ft
WHERE ua.is_deleted = false
  AND (ua.default_plot_type_id IS NULL OR ua.default_plot_type_id <= 0);

-- =========================================================
-- C) 同步 user_plots 锁定字段与 unlock_experience_required
--    说明：仅修复数量与锁定状态，不改 plot_index
-- =========================================================
WITH ua AS (
    SELECT
        user_id,
        total_plot_count,
        unlocked_plot_count,
        lock_reason
    FROM farm.user_plot_allocations
    WHERE is_deleted = false
),
fix_rows AS (
    SELECT
        up.id,
        up.user_id,
        up.plot_index,
        (up.plot_index > ua.unlocked_plot_count)::boolean AS should_lock,
        COALESCE(ua.lock_reason, '待解锁')                  AS should_reason
    FROM farm.user_plots up
    JOIN ua ON ua.user_id = up.user_id
    WHERE up.is_deleted = false
)
UPDATE farm.user_plots up
SET
    is_locked = fr.should_lock,
    lock_reason = CASE WHEN fr.should_lock THEN fr.should_reason ELSE NULL END,
    unlocked_at = CASE WHEN fr.should_lock THEN NULL ELSE COALESCE(up.unlocked_at, CURRENT_TIMESTAMP) END,
    unlock_experience_required = CASE
        WHEN fr.plot_index <= 1 THEN 0
        ELSE GREATEST(COALESCE(up.unlock_experience_required, 0), 0)
    END,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = up.user_id
FROM fix_rows fr
WHERE up.id = fr.id;

-- =========================================================
-- D) 发现 “allocation 总数 > user_plots 数量” 的用户，补齐缺失地块
-- =========================================================
WITH target AS (
    SELECT
        ua.user_id,
        ua.total_plot_count,
        ua.unlocked_plot_count,
        COALESCE(ua.lock_reason, '待解锁') AS lock_reason,
        ua.default_plot_type_id
    FROM farm.user_plot_allocations ua
    WHERE ua.is_deleted = false
),
fallback_type AS (
    SELECT
        pt.id AS plot_type_id,
        pt.soil_type_id,
        COALESCE(pt.default_unlock_experience_required, 0) AS unlock_exp
    FROM farm.plot_types pt
    WHERE pt.is_deleted = false
),
max_index AS (
    SELECT
        up.user_id,
        COALESCE(MAX(up.plot_index), 0)::int AS max_plot_index
    FROM farm.user_plots up
    WHERE up.is_deleted = false
    GROUP BY up.user_id
)
INSERT INTO farm.user_plots
(
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
    status,
    is_deleted,
    opt_lock_version
)
SELECT
    t.user_id,
    ft.soil_type_id,
    gs.idx::smallint,
    CASE
        WHEN gs.idx <= t.unlocked_plot_count THEN 0
        ELSE GREATEST(ft.unlock_exp, 0)
    END AS unlock_experience_required,
    (gs.idx > t.unlocked_plot_count) AS is_locked,
    CASE WHEN gs.idx > t.unlocked_plot_count THEN NULL ELSE CURRENT_TIMESTAMP END AS unlocked_at,
    CASE WHEN gs.idx > t.unlocked_plot_count THEN t.lock_reason ELSE NULL END AS lock_reason,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    t.user_id,
    t.user_id,
    1,
    false,
    0
FROM target t
LEFT JOIN max_index mi ON mi.user_id = t.user_id
JOIN fallback_type ft ON ft.plot_type_id = t.default_plot_type_id
JOIN LATERAL generate_series(COALESCE(mi.max_plot_index, 0) + 1, t.total_plot_count::int) AS gs(idx) ON TRUE
WHERE t.total_plot_count::int > COALESCE(mi.max_plot_index, 0);

