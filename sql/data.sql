-- ==========================================
-- 农场基础字典与种子配置初始化数据
-- 不包含图片路径，asset_url 统一置空
-- ==========================================

-- 1) 种子品质字典
INSERT INTO farm.seed_qualities (name, code, description)
SELECT '普通', 'NORMAL', '普通品质种子'
WHERE NOT EXISTS (SELECT 1 FROM farm.seed_qualities WHERE code = 'NORMAL' AND is_deleted = false);

INSERT INTO farm.seed_qualities (name, code, description)
SELECT '优质', 'ADVANCED', '优质品质种子'
WHERE NOT EXISTS (SELECT 1 FROM farm.seed_qualities WHERE code = 'ADVANCED' AND is_deleted = false);

INSERT INTO farm.seed_qualities (name, code, description)
SELECT '稀有', 'RARE', '稀有品质种子'
WHERE NOT EXISTS (SELECT 1 FROM farm.seed_qualities WHERE code = 'RARE' AND is_deleted = false);

-- 2) 土地类型字典（bit_code 用于位运算）
INSERT INTO farm.soil_types (name, bit_code, level, grow_speed_multiplier, description)
SELECT '黄土地', 1, 1, 1.00, '基础土地，适配多数作物'
WHERE NOT EXISTS (SELECT 1 FROM farm.soil_types WHERE bit_code = 1 AND is_deleted = false);

INSERT INTO farm.soil_types (name, bit_code, level, grow_speed_multiplier, description)
SELECT '黑土地', 2, 2, 0.90, '生长更快的改良土地'
WHERE NOT EXISTS (SELECT 1 FROM farm.soil_types WHERE bit_code = 2 AND is_deleted = false);

INSERT INTO farm.soil_types (name, bit_code, level, grow_speed_multiplier, description)
SELECT '金土地', 4, 3, 0.80, '高等级土地，适配高级作物'
WHERE NOT EXISTS (SELECT 1 FROM farm.soil_types WHERE bit_code = 4 AND is_deleted = false);

-- 3) 成长阶段字典（5阶段）
INSERT INTO farm.growth_stages (name, code, description)
SELECT '种子', 'SEED', '刚播种后的阶段'
WHERE NOT EXISTS (SELECT 1 FROM farm.growth_stages WHERE code = 'SEED' AND is_deleted = false);

INSERT INTO farm.growth_stages (name, code, description)
SELECT '发芽', 'SPROUT', '发芽阶段'
WHERE NOT EXISTS (SELECT 1 FROM farm.growth_stages WHERE code = 'SPROUT' AND is_deleted = false);

INSERT INTO farm.growth_stages (name, code, description)
SELECT '生长期', 'GROWING', '快速生长期'
WHERE NOT EXISTS (SELECT 1 FROM farm.growth_stages WHERE code = 'GROWING' AND is_deleted = false);

INSERT INTO farm.growth_stages (name, code, description)
SELECT '开花', 'FLOWER', '开花授粉阶段'
WHERE NOT EXISTS (SELECT 1 FROM farm.growth_stages WHERE code = 'FLOWER' AND is_deleted = false);

INSERT INTO farm.growth_stages (name, code, description)
SELECT '成熟', 'RIPE', '成熟可收获阶段'
WHERE NOT EXISTS (SELECT 1 FROM farm.growth_stages WHERE code = 'RIPE' AND is_deleted = false);

-- 4) 种子类型配置（code 对应前端种子ID展示）
INSERT INTO farm.seed_types
(
    name, code, seed_quality_id, enable_soil_type_bits, level, description,
    bug_probability, max_bug_limit, max_harvest_count, regrow_stage_index,
    price, harvest_experience, harvest_fruit_number, fruit_price, harvest_score
)
SELECT
    '草莓', '1', q.id, 1, 1, '小贴士：草莓（英文：strawberry）外观呈聚果状圆锥或心形，鲜美红嫩，果肉多汁。',
    0.0500, 3, 1, NULL,
    5, 50, 10, 10, 10
FROM farm.seed_qualities q
WHERE q.code = 'NORMAL' AND q.is_deleted = false
  AND NOT EXISTS (SELECT 1 FROM farm.seed_types s WHERE s.code = '1' AND s.is_deleted = false);

INSERT INTO farm.seed_types
(
    name, code, seed_quality_id, enable_soil_type_bits, level, description,
    bug_probability, max_bug_limit, max_harvest_count, regrow_stage_index,
    price, harvest_experience, harvest_fruit_number, fruit_price, harvest_score
)
SELECT
    '茄子', '6', q.id, 1, 1, '小贴士：茄子（英文：aubergine）中维生素P丰富，保护心血管。',
    0.0600, 3, 1, NULL,
    6, 50, 15, 20, 15
FROM farm.seed_qualities q
WHERE q.code = 'ADVANCED' AND q.is_deleted = false
  AND NOT EXISTS (SELECT 1 FROM farm.seed_types s WHERE s.code = '6' AND s.is_deleted = false);

INSERT INTO farm.seed_types
(
    name, code, seed_quality_id, enable_soil_type_bits, level, description,
    bug_probability, max_bug_limit, max_harvest_count, regrow_stage_index,
    price, harvest_experience, harvest_fruit_number, fruit_price, harvest_score
)
SELECT
    '玉米', '8', q.id, 3, 2, '高产作物，适配黄土地与黑土地。',
    0.0700, 4, 1, NULL,
    10, 80, 18, 24, 20
FROM farm.seed_qualities q
WHERE q.code = 'RARE' AND q.is_deleted = false
  AND NOT EXISTS (SELECT 1 FROM farm.seed_types s WHERE s.code = '8' AND s.is_deleted = false);

-- 5) 种子成长阶段配置（每种作物 5 阶段）
WITH stage_seed AS (
    SELECT id, code FROM farm.growth_stages WHERE is_deleted = false
),
target_seed AS (
    SELECT id, code FROM farm.seed_types WHERE is_deleted = false AND code IN ('1', '6', '8')
)
INSERT INTO farm.seed_growth_stages
(
    seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url,
    width, height, offset_x, offset_y
)
SELECT
    ts.id,
    gs.id,
    stage_data.stage_index,
    stage_data.duration_seconds,
    '',
    100, 140, 50, 80
FROM target_seed ts
JOIN (
    -- 草莓: 总时长 140 秒
    SELECT '1' AS seed_code, 'SEED' AS stage_code, 1 AS stage_index, 20 AS duration_seconds
    UNION ALL SELECT '1', 'SPROUT', 2, 25
    UNION ALL SELECT '1', 'GROWING', 3, 30
    UNION ALL SELECT '1', 'FLOWER', 4, 35
    UNION ALL SELECT '1', 'RIPE', 5, 30

    -- 茄子: 总时长 290 秒
    UNION ALL SELECT '6', 'SEED', 1, 50
    UNION ALL SELECT '6', 'SPROUT', 2, 60
    UNION ALL SELECT '6', 'GROWING', 3, 60
    UNION ALL SELECT '6', 'FLOWER', 4, 60
    UNION ALL SELECT '6', 'RIPE', 5, 60

    -- 玉米: 总时长 300 秒
    UNION ALL SELECT '8', 'SEED', 1, 50
    UNION ALL SELECT '8', 'SPROUT', 2, 60
    UNION ALL SELECT '8', 'GROWING', 3, 60
    UNION ALL SELECT '8', 'FLOWER', 4, 65
    UNION ALL SELECT '8', 'RIPE', 5, 65
) stage_data ON stage_data.seed_code = ts.code
JOIN stage_seed gs ON gs.code = stage_data.stage_code
WHERE NOT EXISTS (
    SELECT 1
    FROM farm.seed_growth_stages sgs
    WHERE sgs.seed_type_id = ts.id
      AND sgs.stage_index = stage_data.stage_index
      AND sgs.is_deleted = false
);
