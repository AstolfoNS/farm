-- ==========================================
-- 农场基础字典与种子配置初始化数据（UTF-8）
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
INSERT INTO farm.soil_types (name, bit_code, level, grow_speed_multiplier, description)
SELECT '黄土地', 1, 1, 1.00, '基础土地，适配多数作物'
WHERE NOT EXISTS (SELECT 1 FROM farm.soil_types WHERE bit_code = 1 AND is_deleted = false);

INSERT INTO farm.soil_types (name, bit_code, level, grow_speed_multiplier, description)
SELECT '黑土地', 2, 2, 0.90, '生长速度更快的改良土地'
WHERE NOT EXISTS (SELECT 1 FROM farm.soil_types WHERE bit_code = 2 AND is_deleted = false);

INSERT INTO farm.soil_types (name, bit_code, level, grow_speed_multiplier, description)
SELECT '金土地', 4, 3, 0.80, '高级土地，适配高级作物'
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
    price, harvest_experience, harvest_fruit_number, fruit_price, harvest_score
)
SELECT
    '草莓', q.id, 1, 1, '草莓果实鲜红，口感清甜',
    3, 1, NULL,
    5, 50, 10, 10, 10
FROM farm.seed_qualities q
WHERE q.name = '普通' AND q.is_deleted = false
  AND NOT EXISTS (SELECT 1 FROM farm.seed_types s WHERE s.name = '草莓' AND s.is_deleted = false);

INSERT INTO farm.seed_types
(
    name, seed_quality_id, enable_soil_type_bits, level, description,
    max_bug_limit, max_harvest_count, regrow_stage_index,
    price, harvest_experience, harvest_fruit_number, fruit_price, harvest_score
)
SELECT
    '茄子', q.id, 1, 1, '常见蔬果作物，产量稳定',
    3, 1, NULL,
    6, 50, 15, 20, 15
FROM farm.seed_qualities q
WHERE q.name = '优质' AND q.is_deleted = false
  AND NOT EXISTS (SELECT 1 FROM farm.seed_types s WHERE s.name = '茄子' AND s.is_deleted = false);

INSERT INTO farm.seed_types
(
    name, seed_quality_id, enable_soil_type_bits, level, description,
    max_bug_limit, max_harvest_count, regrow_stage_index,
    price, harvest_experience, harvest_fruit_number, fruit_price, harvest_score
)
SELECT
    '玉米', q.id, 3, 2, '高产作物，适配黄土地和黑土地',
    4, 1, NULL,
    10, 80, 18, 24, 20
FROM farm.seed_qualities q
WHERE q.name = '稀有' AND q.is_deleted = false
  AND NOT EXISTS (SELECT 1 FROM farm.seed_types s WHERE s.name = '玉米' AND s.is_deleted = false);

-- 5) 种子生长阶段配置（每种作物5阶段，按阶段配置 bug_probability）
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

-- 6) request_idempotencies 初始化样例（演示用途，默认不插入业务数据）
-- INSERT INTO farm.request_idempotencies
-- (user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_by, updated_by, status, is_deleted, opt_lock_version)
-- VALUES
-- (1, 'BUY_SEED', 'demo-request-id-001', 'SUCCESS', '{"sample":true}', NULL, CURRENT_TIMESTAMP, 1, 1, 1, false, 0);
