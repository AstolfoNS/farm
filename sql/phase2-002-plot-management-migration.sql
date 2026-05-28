-- Phase 2 / Step 2
-- 存量数据迁移（UTF-8）

-- =========================================================
-- A) 备份快照（首次执行时生成）
-- =========================================================
CREATE TABLE IF NOT EXISTS farm.user_plots_bak_phase2
AS TABLE farm.user_plots WITH NO DATA;

INSERT INTO farm.user_plots_bak_phase2
SELECT *
FROM farm.user_plots up
WHERE NOT EXISTS (SELECT 1 FROM farm.user_plots_bak_phase2);

CREATE TABLE IF NOT EXISTS farm.user_plot_allocations_bak_phase2
AS TABLE farm.user_plot_allocations WITH NO DATA;

INSERT INTO farm.user_plot_allocations_bak_phase2
SELECT *
FROM farm.user_plot_allocations ua
WHERE NOT EXISTS (SELECT 1 FROM farm.user_plot_allocations_bak_phase2);

-- =========================================================
-- B) 生成地块类型（按 soil_type 自动映射）
-- =========================================================
INSERT INTO farm.plot_types
(
    name,
    icon_url,
    soil_type_id,
    unlock_required,
    default_usable,
    default_unlock_experience_required,
    sort_order,
    description,
    created_at,
    updated_at,
    created_by,
    updated_by,
    status,
    is_deleted,
    opt_lock_version
)
SELECT
    st.name || '地块'                                                     AS name,
    ''                                                                   AS icon_url,
    st.id                                                                 AS soil_type_id,
    true                                                                  AS unlock_required,
    true                                                                  AS default_usable,
    COALESCE(st.unlock_experience_required, 0)                            AS default_unlock_experience_required,
    COALESCE(st.level, 0) * 10                                            AS sort_order,
    COALESCE(st.description, '')                                           AS description,
    CURRENT_TIMESTAMP                                                      AS created_at,
    CURRENT_TIMESTAMP                                                      AS updated_at,
    0                                                                      AS created_by,
    0                                                                      AS updated_by,
    1                                                                      AS status,
    false                                                                  AS is_deleted,
    0                                                                      AS opt_lock_version
FROM farm.soil_types st
WHERE st.is_deleted = false
  AND NOT EXISTS (
    SELECT 1
    FROM farm.plot_types pt
    WHERE pt.soil_type_id = st.id
      AND pt.is_deleted = false
);

-- =========================================================
-- C) 生成全局默认策略（若不存在 active 策略）
-- =========================================================
WITH plot_summary AS (
    SELECT
        COALESCE((SELECT MAX(plot_index) FROM farm.user_plots WHERE is_deleted = false), 6)::smallint AS total_cnt,
        COALESCE(
            MIN(unlocked_cnt)::smallint,
            1::smallint
        )                                                                                       AS unlocked_cnt
    FROM (
        SELECT
            up.user_id,
            COUNT(*) FILTER (WHERE up.is_deleted = false AND up.is_locked = false) AS unlocked_cnt
        FROM farm.user_plots up
        WHERE up.is_deleted = false
        GROUP BY up.user_id
    ) t
),
default_type AS (
    SELECT pt.id
    FROM farm.plot_types pt
    WHERE pt.is_deleted = false
    ORDER BY pt.default_usable DESC, pt.sort_order ASC, pt.id ASC
    LIMIT 1
),
payload AS (
    SELECT
        ps.total_cnt                              AS total_cnt,
        LEAST(ps.unlocked_cnt, ps.total_cnt)      AS unlocked_cnt,
        dt.id                                     AS default_plot_type_id
    FROM plot_summary ps
    CROSS JOIN default_type dt
)
INSERT INTO farm.plot_policies
(
    policy_name,
    active,
    default_total_plot_count,
    default_unlocked_plot_count,
    default_locked_plot_count,
    default_plot_type_id,
    default_lock_rule_code,
    default_lock_reason,
    allocation_rule_json,
    created_at,
    updated_at,
    created_by,
    updated_by,
    status,
    is_deleted,
    opt_lock_version
)
SELECT
    'default-policy-phase2'                                                                      AS policy_name,
    true                                                                                         AS active,
    p.total_cnt                                                                                  AS default_total_plot_count,
    p.unlocked_cnt                                                                               AS default_unlocked_plot_count,
    (p.total_cnt - p.unlocked_cnt)::smallint                                                     AS default_locked_plot_count,
    p.default_plot_type_id                                                                       AS default_plot_type_id,
    'DEFAULT_LOCKED'                                                                             AS default_lock_rule_code,
    '待解锁'                                                                                     AS default_lock_reason,
    jsonb_build_object(
        p.default_plot_type_id::text,
        jsonb_build_object('total', p.total_cnt, 'locked', (p.total_cnt - p.unlocked_cnt))
    )                                                                                             AS allocation_rule_json,
    CURRENT_TIMESTAMP                                                                             AS created_at,
    CURRENT_TIMESTAMP                                                                             AS updated_at,
    0                                                                                             AS created_by,
    0                                                                                             AS updated_by,
    1                                                                                             AS status,
    false                                                                                         AS is_deleted,
    0                                                                                             AS opt_lock_version
FROM payload p
WHERE NOT EXISTS (
    SELECT 1
    FROM farm.plot_policies pp
    WHERE pp.is_deleted = false
      AND pp.active = true
);

-- =========================================================
-- D) 为存量用户生成 allocation 快照
-- =========================================================
WITH base_users AS (
    SELECT u.id AS user_id
    FROM farm.users u
    WHERE u.is_deleted = false
),
plot_stats AS (
    SELECT
        up.user_id,
        COUNT(*)::smallint                                                         AS total_cnt,
        COUNT(*) FILTER (WHERE up.is_locked = false)::smallint                     AS unlocked_cnt,
        COUNT(*) FILTER (WHERE up.is_locked = true)::smallint                      AS locked_cnt
    FROM farm.user_plots up
    WHERE up.is_deleted = false
    GROUP BY up.user_id
),
top_plot_type AS (
    SELECT
        q.user_id,
        q.plot_type_id
    FROM (
        SELECT
            up.user_id,
            pt.id                                                                   AS plot_type_id,
            COUNT(*)                                                                AS cnt,
            ROW_NUMBER() OVER (PARTITION BY up.user_id ORDER BY COUNT(*) DESC, pt.sort_order ASC, pt.id ASC) AS rn
        FROM farm.user_plots up
        JOIN farm.plot_types pt
          ON pt.soil_type_id = up.soil_type_id
         AND pt.is_deleted = false
        WHERE up.is_deleted = false
        GROUP BY up.user_id, pt.id, pt.sort_order
    ) q
    WHERE q.rn = 1
),
rule_json AS (
    SELECT
        s.user_id,
        COALESCE(
            jsonb_object_agg(
                pt_id::text,
                jsonb_build_object('total', total_cnt, 'locked', locked_cnt)
                ORDER BY pt_id
            ),
            '{}'::jsonb
        ) AS data_json
    FROM (
        SELECT
            up.user_id,
            pt.id                                                                    AS pt_id,
            COUNT(*)::int                                                             AS total_cnt,
            COUNT(*) FILTER (WHERE up.is_locked = true)::int                          AS locked_cnt
        FROM farm.user_plots up
        JOIN farm.plot_types pt
          ON pt.soil_type_id = up.soil_type_id
         AND pt.is_deleted = false
        WHERE up.is_deleted = false
        GROUP BY up.user_id, pt.id
    ) s
    GROUP BY s.user_id
)
INSERT INTO farm.user_plot_allocations
(
    user_id,
    active,
    total_plot_count,
    unlocked_plot_count,
    locked_plot_count,
    default_plot_type_id,
    lock_rule_code,
    lock_reason,
    allocation_rule_json,
    applied_at,
    created_at,
    updated_at,
    created_by,
    updated_by,
    status,
    is_deleted,
    opt_lock_version
)
SELECT
    bu.user_id                                                        AS user_id,
    true                                                              AS active,
    COALESCE(ps.total_cnt, 0)::smallint                              AS total_plot_count,
    COALESCE(ps.unlocked_cnt, 0)::smallint                           AS unlocked_plot_count,
    COALESCE(ps.locked_cnt, 0)::smallint                             AS locked_plot_count,
    tpt.plot_type_id                                                  AS default_plot_type_id,
    'DEFAULT_LOCKED'                                                  AS lock_rule_code,
    '待解锁'                                                          AS lock_reason,
    COALESCE(rj.data_json, '{}'::jsonb)                              AS allocation_rule_json,
    CURRENT_TIMESTAMP                                                 AS applied_at,
    CURRENT_TIMESTAMP                                                 AS created_at,
    CURRENT_TIMESTAMP                                                 AS updated_at,
    0                                                                 AS created_by,
    0                                                                 AS updated_by,
    1                                                                 AS status,
    false                                                             AS is_deleted,
    0                                                                 AS opt_lock_version
FROM base_users bu
LEFT JOIN plot_stats ps ON ps.user_id = bu.user_id
LEFT JOIN top_plot_type tpt ON tpt.user_id = bu.user_id
LEFT JOIN rule_json rj ON rj.user_id = bu.user_id
WHERE NOT EXISTS (
    SELECT 1
    FROM farm.user_plot_allocations ua
    WHERE ua.user_id = bu.user_id
      AND ua.is_deleted = false
);
