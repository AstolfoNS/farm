-- ==========================================
-- 农场基础字典与种子配置初始化数据（UTF-8）
-- 说明：仅做数据初始化，不做表结构变更
-- ==========================================

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
INSERT INTO farm.soil_types (name, bit_code, level, unlock_experience_required, grow_speed_multiplier, description)
SELECT '黄土地', 1, 1, 0, 1.00, '基础土地，适配多数作物'
WHERE NOT EXISTS (SELECT 1 FROM farm.soil_types WHERE bit_code = 1 AND is_deleted = false);

INSERT INTO farm.soil_types (name, bit_code, level, unlock_experience_required, grow_speed_multiplier, description)
SELECT '黑土地', 2, 2, 500, 0.90, '生长速度更快的改良土地'
WHERE NOT EXISTS (SELECT 1 FROM farm.soil_types WHERE bit_code = 2 AND is_deleted = false);

INSERT INTO farm.soil_types (name, bit_code, level, unlock_experience_required, grow_speed_multiplier, description)
SELECT '金土地', 4, 3, 2000, 0.80, '高级土地，适配高等级作物'
WHERE NOT EXISTS (SELECT 1 FROM farm.soil_types WHERE bit_code = 4 AND is_deleted = false);

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

-- 6) 用户初始化数据（幂等）
INSERT INTO farm.users
(
    username, nickname, password_hash, email, avatar_url,
    experience, score, coin, preferences_json,
    created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version
)
SELECT
    'liubei', '刘备', '123456', 'liubei@farm.local', '/oss/defaults/avatar/default-avatar.png',
    2000, 575, 4090, '{"audio":{"effectEnable":true,"effectVolume":0.8,"bgmEnable":false,"bgmVolume":0.6}}'::jsonb,
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
    1800, 420, 2600, '{"audio":{"effectEnable":true,"effectVolume":0.8,"bgmEnable":false,"bgmVolume":0.6}}'::jsonb,
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
    1600, 360, 2100, '{"audio":{"effectEnable":true,"effectVolume":0.8,"bgmEnable":false,"bgmVolume":0.6}}'::jsonb,
    NOW(), NOW(), 0, 0, 'init user', 1, false, 0
WHERE NOT EXISTS (SELECT 1 FROM farm.users WHERE username = 'sunquan' AND is_deleted = false);

-- 7) 地块类型初始化（幂等）
INSERT INTO farm.plot_types
(
    name, icon_url, soil_type_id, unlock_required, default_usable,
    default_unlock_experience_required, sort_order, description,
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
    default_unlock_experience_required, sort_order, description,
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
    default_unlock_experience_required, sort_order, description,
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
        pt.default_unlock_experience_required
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
        ELSE COALESCE(awt.default_unlock_experience_required, 0) + ((gs.plot_index - awt.unlocked_plot_count - 1) * 200)
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
