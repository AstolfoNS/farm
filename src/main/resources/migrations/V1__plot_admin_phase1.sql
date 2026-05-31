-- Phase 1: plot admin baseline schema upgrade
-- Safe to run repeatedly on PostgreSQL.

ALTER TABLE IF EXISTS farm.soil_types
    ADD COLUMN IF NOT EXISTS cover_image_url VARCHAR(1024) NOT NULL DEFAULT '';

ALTER TABLE IF EXISTS farm.plot_types
    ADD COLUMN IF NOT EXISTS cover_image_url VARCHAR(1024) NOT NULL DEFAULT '';

ALTER TABLE IF EXISTS farm.plot_policies
    ADD COLUMN IF NOT EXISTS policy_version VARCHAR(64) DEFAULT 'v1',
    ADD COLUMN IF NOT EXISTS effective_scope VARCHAR(32) DEFAULT 'NEW_USER_ONLY',
    ADD COLUMN IF NOT EXISTS publish_status VARCHAR(32) DEFAULT 'DRAFT';

CREATE INDEX IF NOT EXISTS idx_plot_policies_publish_status_active
    ON farm.plot_policies (publish_status, active)
    WHERE is_deleted = false;

CREATE TABLE IF NOT EXISTS farm.plot_policy_apply_logs
(
    id                          BIGINT          NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    policy_id                   BIGINT          NOT NULL,
    applied_scope               VARCHAR(32)     NOT NULL DEFAULT 'MANUAL_APPLY',
    target_user_count           INT             NOT NULL DEFAULT 0,
    success_user_count          INT             NOT NULL DEFAULT 0,
    failed_user_count           INT             NOT NULL DEFAULT 0,
    request_payload_json        JSONB           NOT NULL DEFAULT '{}'::jsonb,
    result_snapshot_json        JSONB           NOT NULL DEFAULT '{}'::jsonb,
    applied_by                  BIGINT              NULL,
    applied_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by                  BIGINT              NULL,
    updated_by                  BIGINT              NULL,
    remark                      TEXT                NULL,

    status                      SMALLINT        NOT NULL DEFAULT 1,
    is_deleted                  BOOLEAN         NOT NULL DEFAULT false,
    opt_lock_version            INT             NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_plot_policy_apply_logs_policy_active
    ON farm.plot_policy_apply_logs (policy_id, applied_at DESC)
    WHERE is_deleted = false;
