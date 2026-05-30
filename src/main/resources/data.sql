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
    preferences_json = '{"audio":{"effectEnable":true,"effectVolume":0.8,"bgmEnable":false,"bgmVolume":0.6}}'::jsonb,
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
    cfg.experience, cfg.score, cfg.coin, '{"audio":{"effectEnable":true,"effectVolume":0.8,"bgmEnable":false,"bgmVolume":0.6}}'::jsonb,
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
        MAX(CASE WHEN name = '黄壤土' THEN id END) AS soil_a,
        MAX(CASE WHEN name = '黑壤土' THEN id END) AS soil_b,
        MAX(CASE WHEN name = '金砂土' THEN id END) AS soil_c
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
        MAX(CASE WHEN name = '黄壤土' THEN id END) AS soil_a,
        MAX(CASE WHEN name = '黑壤土' THEN id END) AS soil_b,
        MAX(CASE WHEN name = '金砂土' THEN id END) AS soil_c
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
