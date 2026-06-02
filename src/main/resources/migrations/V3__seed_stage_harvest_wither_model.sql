ALTER TABLE farm.seed_types
    ADD COLUMN IF NOT EXISTS harvest_stage_index SMALLINT NULL;

INSERT INTO farm.asset_defaults (asset_key, asset_url, description)
SELECT 'seedStageWithered', '/oss/defaults/seed/seed-stage-withered-default.png', '种子枯萎阶段默认图'
WHERE NOT EXISTS (
    SELECT 1 FROM farm.asset_defaults WHERE asset_key = 'seedStageWithered' AND is_deleted = false
);

INSERT INTO farm.growth_stages (name, description)
SELECT '枯萎', '作物虫害超限或错过收获窗口后的最终阶段'
WHERE NOT EXISTS (
    SELECT 1 FROM farm.growth_stages WHERE name = '枯萎' AND is_deleted = false
);

WITH wither_dict AS (
    SELECT id
    FROM farm.growth_stages
    WHERE name = '枯萎'
      AND is_deleted = false
    ORDER BY id ASC
    LIMIT 1
),
last_stage AS (
    SELECT DISTINCT ON (sgs.seed_type_id)
        sgs.seed_type_id,
        sgs.stage_index,
        sgs.duration_seconds,
        sgs.width,
        sgs.height,
        sgs.offset_x,
        sgs.offset_y
    FROM farm.seed_growth_stages sgs
    WHERE sgs.is_deleted = false
    ORDER BY sgs.seed_type_id, sgs.stage_index DESC
),
need_append AS (
    SELECT
        ls.seed_type_id,
        (ls.stage_index + 1)::smallint AS stage_index,
        ls.width,
        ls.height,
        ls.offset_x,
        ls.offset_y,
        wd.id AS growth_stage_id
    FROM last_stage ls
    CROSS JOIN wither_dict wd
    WHERE NOT EXISTS (
        SELECT 1
        FROM farm.seed_growth_stages already
        JOIN farm.growth_stages gs ON gs.id = already.growth_stage_id
        WHERE already.seed_type_id = ls.seed_type_id
          AND already.is_deleted = false
          AND gs.is_deleted = false
          AND gs.name = '枯萎'
    )
)
INSERT INTO farm.seed_growth_stages
(
    seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability,
    width, height, offset_x, offset_y,
    created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version
)
SELECT
    na.seed_type_id,
    na.growth_stage_id,
    na.stage_index,
    0,
    '/oss/defaults/seed/seed-stage-withered-default.png',
    0.0000,
    na.width,
    na.height,
    na.offset_x,
    na.offset_y,
    NOW(), NOW(), 0, 0, 'auto-appended wither stage for new model', 1, false, 0
FROM need_append na;

WITH ordered AS (
    SELECT
        sgs.seed_type_id,
        sgs.stage_index,
        gs.name,
        ROW_NUMBER() OVER (PARTITION BY sgs.seed_type_id ORDER BY sgs.stage_index DESC) AS rn
    FROM farm.seed_growth_stages sgs
    JOIN farm.growth_stages gs ON gs.id = sgs.growth_stage_id
    WHERE sgs.is_deleted = false
      AND gs.is_deleted = false
),
agg AS (
    SELECT
        seed_type_id,
        MAX(CASE WHEN rn = 1 THEN stage_index END) AS last_stage_index,
        MAX(CASE WHEN rn = 2 THEN stage_index END) AS harvest_stage_index
    FROM ordered
    GROUP BY seed_type_id
)
UPDATE farm.seed_types st
SET harvest_stage_index = COALESCE(st.harvest_stage_index, COALESCE(agg.harvest_stage_index, agg.last_stage_index)),
    regrow_stage_index = CASE
        WHEN COALESCE(st.max_harvest_count, 1) <= 1 THEN NULL
        ELSE st.regrow_stage_index
    END,
    updated_at = NOW(),
    updated_by = 0
FROM agg
WHERE st.id = agg.seed_type_id
  AND st.is_deleted = false;
