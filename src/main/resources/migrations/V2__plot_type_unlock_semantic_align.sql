-- Align plot type unlock experience field semantics:
-- from default_unlock_experience_required
-- to   default_plot_unlock_experience_config

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'farm'
          AND table_name = 'plot_types'
          AND column_name = 'default_unlock_experience_required'
    ) THEN
        ALTER TABLE farm.plot_types
            RENAME COLUMN default_unlock_experience_required TO default_plot_unlock_experience_config;
    END IF;
END $$;

ALTER TABLE IF EXISTS farm.plot_types
    ALTER COLUMN default_plot_unlock_experience_config SET DEFAULT 0;
