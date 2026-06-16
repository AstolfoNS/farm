--
-- PostgreSQL database dump
--

\restrict v2Oz1kFfRuwbGkCSS55doy4bFkAaNHhC0Vzi2GyXfvLJ8hmdNvHXh6RJHltuH4q

-- Dumped from database version 17.9 (Debian 17.9-1.pgdg13+1)
-- Dumped by pg_dump version 18.1

-- Started on 2026-06-11 20:25:35

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 7 (class 2615 OID 29194)
-- Name: farm; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA farm;


--
-- TOC entry 2 (class 3079 OID 16390)
-- Name: citext; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS citext WITH SCHEMA public;


--
-- TOC entry 3806 (class 0 OID 0)
-- Dependencies: 2
-- Name: EXTENSION citext; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION citext IS 'data type for case-insensitive character strings';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 222 (class 1259 OID 29216)
-- Name: asset_defaults; Type: TABLE; Schema: farm; Owner: -
--

CREATE TABLE farm.asset_defaults (
    id bigint NOT NULL,
    asset_key character varying(128) NOT NULL,
    asset_url character varying(1024) NOT NULL,
    description text,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by bigint,
    updated_by bigint,
    remark text,
    status smallint DEFAULT 1 NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL,
    opt_lock_version integer DEFAULT 0 NOT NULL
);


--
-- TOC entry 3807 (class 0 OID 0)
-- Dependencies: 222
-- Name: TABLE asset_defaults; Type: COMMENT; Schema: farm; Owner: -
--

COMMENT ON TABLE farm.asset_defaults IS '默认资源配置表';


--
-- TOC entry 221 (class 1259 OID 29215)
-- Name: asset_defaults_id_seq; Type: SEQUENCE; Schema: farm; Owner: -
--

ALTER TABLE farm.asset_defaults ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME farm.asset_defaults_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 230 (class 1259 OID 29283)
-- Name: growth_stages; Type: TABLE; Schema: farm; Owner: -
--

CREATE TABLE farm.growth_stages (
    id bigint NOT NULL,
    name character varying(500) NOT NULL,
    description text,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by bigint,
    updated_by bigint,
    remark text,
    status smallint DEFAULT 1 NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL,
    opt_lock_version integer DEFAULT 0 NOT NULL
);


--
-- TOC entry 3808 (class 0 OID 0)
-- Dependencies: 230
-- Name: TABLE growth_stages; Type: COMMENT; Schema: farm; Owner: -
--

COMMENT ON TABLE farm.growth_stages IS '生长阶段类型字典表';


--
-- TOC entry 229 (class 1259 OID 29282)
-- Name: growth_stages_id_seq; Type: SEQUENCE; Schema: farm; Owner: -
--

ALTER TABLE farm.growth_stages ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME farm.growth_stages_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 228 (class 1259 OID 29263)
-- Name: plot_policies; Type: TABLE; Schema: farm; Owner: -
--

CREATE TABLE farm.plot_policies (
    id bigint NOT NULL,
    policy_name character varying(128) NOT NULL,
    policy_version character varying(64) DEFAULT 'v1'::character varying,
    active boolean DEFAULT true NOT NULL,
    effective_scope character varying(32) DEFAULT 'NEW_USER_ONLY'::character varying,
    publish_status character varying(32) DEFAULT 'DRAFT'::character varying,
    default_total_plot_count smallint NOT NULL,
    default_unlocked_plot_count smallint NOT NULL,
    default_locked_plot_count smallint NOT NULL,
    default_lock_rule_code character varying(64) DEFAULT 'DEFAULT_LOCKED'::character varying NOT NULL,
    default_lock_reason character varying(255) DEFAULT '待解锁'::character varying NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by bigint,
    updated_by bigint,
    remark text,
    status smallint DEFAULT 1 NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL,
    opt_lock_version integer DEFAULT 0 NOT NULL
);


--
-- TOC entry 3809 (class 0 OID 0)
-- Dependencies: 228
-- Name: TABLE plot_policies; Type: COMMENT; Schema: farm; Owner: -
--

COMMENT ON TABLE farm.plot_policies IS '地块全局策略表';


--
-- TOC entry 227 (class 1259 OID 29262)
-- Name: plot_policies_id_seq; Type: SEQUENCE; Schema: farm; Owner: -
--

ALTER TABLE farm.plot_policies ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME farm.plot_policies_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 250 (class 1259 OID 29462)
-- Name: request_idempotencies; Type: TABLE; Schema: farm; Owner: -
--

CREATE TABLE farm.request_idempotencies (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    biz_type character varying(64) NOT NULL,
    request_id character varying(128) NOT NULL,
    process_status character varying(16) DEFAULT 'PROCESSING'::character varying NOT NULL,
    response_payload jsonb,
    error_message character varying(500),
    finished_at timestamp with time zone,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by bigint,
    updated_by bigint,
    remark text,
    status smallint DEFAULT 1 NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL,
    opt_lock_version integer DEFAULT 0 NOT NULL
);


--
-- TOC entry 3810 (class 0 OID 0)
-- Dependencies: 250
-- Name: TABLE request_idempotencies; Type: COMMENT; Schema: farm; Owner: -
--

COMMENT ON TABLE farm.request_idempotencies IS '请求幂等记录表';


--
-- TOC entry 249 (class 1259 OID 29461)
-- Name: request_idempotencies_id_seq; Type: SEQUENCE; Schema: farm; Owner: -
--

ALTER TABLE farm.request_idempotencies ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME farm.request_idempotencies_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 234 (class 1259 OID 29324)
-- Name: seed_growth_stages; Type: TABLE; Schema: farm; Owner: -
--

CREATE TABLE farm.seed_growth_stages (
    id bigint NOT NULL,
    seed_type_id bigint NOT NULL,
    growth_stage_id bigint NOT NULL,
    stage_index smallint NOT NULL,
    duration_seconds integer NOT NULL,
    asset_url character varying(1024),
    bug_probability numeric(5,4) DEFAULT 0.0000 NOT NULL,
    width integer DEFAULT 0 NOT NULL,
    height integer DEFAULT 0 NOT NULL,
    offset_x integer DEFAULT 0 NOT NULL,
    offset_y integer DEFAULT 0 NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by bigint,
    updated_by bigint,
    remark text,
    status smallint DEFAULT 1 NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL,
    opt_lock_version integer DEFAULT 0 NOT NULL
);


--
-- TOC entry 3811 (class 0 OID 0)
-- Dependencies: 234
-- Name: TABLE seed_growth_stages; Type: COMMENT; Schema: farm; Owner: -
--

COMMENT ON TABLE farm.seed_growth_stages IS '种子生长过程配置表';


--
-- TOC entry 233 (class 1259 OID 29323)
-- Name: seed_growth_stages_id_seq; Type: SEQUENCE; Schema: farm; Owner: -
--

ALTER TABLE farm.seed_growth_stages ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME farm.seed_growth_stages_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 224 (class 1259 OID 29230)
-- Name: seed_qualities; Type: TABLE; Schema: farm; Owner: -
--

CREATE TABLE farm.seed_qualities (
    id bigint NOT NULL,
    name character varying(500) NOT NULL,
    description text,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by bigint,
    updated_by bigint,
    remark text,
    status smallint DEFAULT 1 NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL,
    opt_lock_version integer DEFAULT 0 NOT NULL
);


--
-- TOC entry 3812 (class 0 OID 0)
-- Dependencies: 224
-- Name: TABLE seed_qualities; Type: COMMENT; Schema: farm; Owner: -
--

COMMENT ON TABLE farm.seed_qualities IS '种子品质表';


--
-- TOC entry 223 (class 1259 OID 29229)
-- Name: seed_qualities_id_seq; Type: SEQUENCE; Schema: farm; Owner: -
--

ALTER TABLE farm.seed_qualities ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME farm.seed_qualities_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 232 (class 1259 OID 29297)
-- Name: seed_types; Type: TABLE; Schema: farm; Owner: -
--

CREATE TABLE farm.seed_types (
    id bigint NOT NULL,
    name character varying(500) NOT NULL,
    cover_image_url character varying(1024) DEFAULT ''::character varying NOT NULL,
    seed_quality_id bigint NOT NULL,
    enable_soil_type_bits bigint NOT NULL,
    level smallint NOT NULL,
    unlock_experience_required bigint DEFAULT 0 NOT NULL,
    description text,
    max_bug_limit smallint DEFAULT 0 NOT NULL,
    max_harvest_count smallint DEFAULT 1 NOT NULL,
    regrow_stage_index smallint,
    harvest_stage_index smallint,
    price bigint DEFAULT 0 NOT NULL,
    harvest_experience bigint DEFAULT 0 NOT NULL,
    harvest_fruit_number integer DEFAULT 0 NOT NULL,
    fruit_loss_per_bug integer DEFAULT 1 NOT NULL,
    bug_kill_coin_reward bigint DEFAULT 0 NOT NULL,
    bug_kill_experience_reward bigint DEFAULT 0 NOT NULL,
    bug_kill_score_reward bigint DEFAULT 0 NOT NULL,
    harvest_score bigint DEFAULT 0 NOT NULL,
    fruit_price bigint DEFAULT 0 NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by bigint,
    updated_by bigint,
    remark text,
    status smallint DEFAULT 1 NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL,
    opt_lock_version integer DEFAULT 0 NOT NULL
);


--
-- TOC entry 3813 (class 0 OID 0)
-- Dependencies: 232
-- Name: TABLE seed_types; Type: COMMENT; Schema: farm; Owner: -
--

COMMENT ON TABLE farm.seed_types IS '种子类型配置表';


--
-- TOC entry 231 (class 1259 OID 29296)
-- Name: seed_types_id_seq; Type: SEQUENCE; Schema: farm; Owner: -
--

ALTER TABLE farm.seed_types ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME farm.seed_types_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 226 (class 1259 OID 29244)
-- Name: soil_types; Type: TABLE; Schema: farm; Owner: -
--

CREATE TABLE farm.soil_types (
    id bigint NOT NULL,
    name character varying(500) NOT NULL,
    bit_code integer NOT NULL,
    cover_image_url character varying(1024) DEFAULT ''::character varying NOT NULL,
    level smallint NOT NULL,
    unlock_experience_required bigint DEFAULT 0 NOT NULL,
    grow_speed_multiplier numeric(5,2) DEFAULT 1.00 NOT NULL,
    expand_cost_coin bigint DEFAULT 0 NOT NULL,
    description text,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by bigint,
    updated_by bigint,
    remark text,
    status smallint DEFAULT 1 NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL,
    opt_lock_version integer DEFAULT 0 NOT NULL
);


--
-- TOC entry 3814 (class 0 OID 0)
-- Dependencies: 226
-- Name: TABLE soil_types; Type: COMMENT; Schema: farm; Owner: -
--

COMMENT ON TABLE farm.soil_types IS '土壤类型表';


--
-- TOC entry 225 (class 1259 OID 29243)
-- Name: soil_types_id_seq; Type: SEQUENCE; Schema: farm; Owner: -
--

ALTER TABLE farm.soil_types ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME farm.soil_types_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 244 (class 1259 OID 29411)
-- Name: user_asset_flows; Type: TABLE; Schema: farm; Owner: -
--

CREATE TABLE farm.user_asset_flows (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    asset_type character varying(32) NOT NULL,
    operation_type character varying(32) NOT NULL,
    change_amount bigint NOT NULL,
    before_amount bigint DEFAULT 0 NOT NULL,
    after_amount bigint DEFAULT 0 NOT NULL,
    biz_type character varying(64) DEFAULT ''::character varying NOT NULL,
    biz_id character varying(128),
    occurred_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ext_data jsonb,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by bigint,
    updated_by bigint,
    remark text,
    status smallint DEFAULT 1 NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL,
    opt_lock_version integer DEFAULT 0 NOT NULL
);


--
-- TOC entry 3815 (class 0 OID 0)
-- Dependencies: 244
-- Name: TABLE user_asset_flows; Type: COMMENT; Schema: farm; Owner: -
--

COMMENT ON TABLE farm.user_asset_flows IS '用户资产流水表';


--
-- TOC entry 243 (class 1259 OID 29410)
-- Name: user_asset_flows_id_seq; Type: SEQUENCE; Schema: farm; Owner: -
--

ALTER TABLE farm.user_asset_flows ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME farm.user_asset_flows_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 248 (class 1259 OID 29447)
-- Name: user_crop_action_logs; Type: TABLE; Schema: farm; Owner: -
--

CREATE TABLE farm.user_crop_action_logs (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    plot_id bigint NOT NULL,
    crop_id bigint,
    seed_type_id bigint,
    action_type character varying(32) NOT NULL,
    action_result character varying(32) DEFAULT 'SUCCESS'::character varying NOT NULL,
    action_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    action_snapshot jsonb,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by bigint,
    updated_by bigint,
    remark text,
    status smallint DEFAULT 1 NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL,
    opt_lock_version integer DEFAULT 0 NOT NULL
);


--
-- TOC entry 3816 (class 0 OID 0)
-- Dependencies: 248
-- Name: TABLE user_crop_action_logs; Type: COMMENT; Schema: farm; Owner: -
--

COMMENT ON TABLE farm.user_crop_action_logs IS '作物行为日志表';


--
-- TOC entry 247 (class 1259 OID 29446)
-- Name: user_crop_action_logs_id_seq; Type: SEQUENCE; Schema: farm; Owner: -
--

ALTER TABLE farm.user_crop_action_logs ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME farm.user_crop_action_logs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 240 (class 1259 OID 29375)
-- Name: user_crops; Type: TABLE; Schema: farm; Owner: -
--

CREATE TABLE farm.user_crops (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    plot_id bigint NOT NULL,
    seed_type_id bigint NOT NULL,
    planted_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    stage_started_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_harvest_at timestamp with time zone,
    matured_at timestamp with time zone,
    withered_at timestamp with time zone,
    expected_ripe_at timestamp with time zone,
    expected_withered_at timestamp with time zone,
    harvest_count smallint DEFAULT 0 NOT NULL,
    current_stage_index smallint DEFAULT 1 NOT NULL,
    grow_status smallint DEFAULT 1 NOT NULL,
    bug_count smallint DEFAULT 0 NOT NULL,
    last_bug_at timestamp with time zone,
    last_care_at timestamp with time zone,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by bigint,
    updated_by bigint,
    remark text,
    status smallint DEFAULT 1 NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL,
    opt_lock_version integer DEFAULT 0 NOT NULL
);


--
-- TOC entry 3817 (class 0 OID 0)
-- Dependencies: 240
-- Name: TABLE user_crops; Type: COMMENT; Schema: farm; Owner: -
--

COMMENT ON TABLE farm.user_crops IS '用户种植作物表';


--
-- TOC entry 239 (class 1259 OID 29374)
-- Name: user_crops_id_seq; Type: SEQUENCE; Schema: farm; Owner: -
--

ALTER TABLE farm.user_crops ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME farm.user_crops_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 242 (class 1259 OID 29395)
-- Name: user_fruits; Type: TABLE; Schema: farm; Owner: -
--

CREATE TABLE farm.user_fruits (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    seed_type_id bigint NOT NULL,
    quantity bigint DEFAULT 0 NOT NULL,
    frozen_quantity bigint DEFAULT 0 NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by bigint,
    updated_by bigint,
    remark text,
    status smallint DEFAULT 1 NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL,
    opt_lock_version integer DEFAULT 0 NOT NULL
);


--
-- TOC entry 3818 (class 0 OID 0)
-- Dependencies: 242
-- Name: TABLE user_fruits; Type: COMMENT; Schema: farm; Owner: -
--

COMMENT ON TABLE farm.user_fruits IS '用户果实仓库表';


--
-- TOC entry 241 (class 1259 OID 29394)
-- Name: user_fruits_id_seq; Type: SEQUENCE; Schema: farm; Owner: -
--

ALTER TABLE farm.user_fruits ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME farm.user_fruits_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 246 (class 1259 OID 29428)
-- Name: user_inventory_flows; Type: TABLE; Schema: farm; Owner: -
--

CREATE TABLE farm.user_inventory_flows (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    item_type character varying(32) NOT NULL,
    seed_type_id bigint NOT NULL,
    operation_type character varying(32) NOT NULL,
    change_amount bigint NOT NULL,
    before_amount bigint DEFAULT 0 NOT NULL,
    after_amount bigint DEFAULT 0 NOT NULL,
    before_frozen_amount bigint DEFAULT 0 NOT NULL,
    after_frozen_amount bigint DEFAULT 0 NOT NULL,
    biz_type character varying(64) DEFAULT ''::character varying NOT NULL,
    biz_id character varying(128),
    occurred_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ext_data jsonb,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by bigint,
    updated_by bigint,
    remark text,
    status smallint DEFAULT 1 NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL,
    opt_lock_version integer DEFAULT 0 NOT NULL
);


--
-- TOC entry 3819 (class 0 OID 0)
-- Dependencies: 246
-- Name: TABLE user_inventory_flows; Type: COMMENT; Schema: farm; Owner: -
--

COMMENT ON TABLE farm.user_inventory_flows IS '用户库存流水表';


--
-- TOC entry 245 (class 1259 OID 29427)
-- Name: user_inventory_flows_id_seq; Type: SEQUENCE; Schema: farm; Owner: -
--

ALTER TABLE farm.user_inventory_flows ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME farm.user_inventory_flows_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 238 (class 1259 OID 29359)
-- Name: user_plots; Type: TABLE; Schema: farm; Owner: -
--

CREATE TABLE farm.user_plots (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    soil_type_id bigint NOT NULL,
    plot_index smallint NOT NULL,
    unlock_experience_required bigint DEFAULT 0 NOT NULL,
    is_locked boolean DEFAULT false NOT NULL,
    unlocked_at timestamp with time zone,
    lock_reason character varying(255),
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by bigint,
    updated_by bigint,
    remark text,
    status smallint DEFAULT 1 NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL,
    opt_lock_version integer DEFAULT 0 NOT NULL
);


--
-- TOC entry 3820 (class 0 OID 0)
-- Dependencies: 238
-- Name: TABLE user_plots; Type: COMMENT; Schema: farm; Owner: -
--

COMMENT ON TABLE farm.user_plots IS '用户地块表';


--
-- TOC entry 237 (class 1259 OID 29358)
-- Name: user_plots_id_seq; Type: SEQUENCE; Schema: farm; Owner: -
--

ALTER TABLE farm.user_plots ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME farm.user_plots_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 236 (class 1259 OID 29343)
-- Name: user_seeds; Type: TABLE; Schema: farm; Owner: -
--

CREATE TABLE farm.user_seeds (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    seed_type_id bigint NOT NULL,
    quantity bigint DEFAULT 0 NOT NULL,
    frozen_quantity bigint DEFAULT 0 NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by bigint,
    updated_by bigint,
    remark text,
    status smallint DEFAULT 1 NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL,
    opt_lock_version integer DEFAULT 0 NOT NULL
);


--
-- TOC entry 3821 (class 0 OID 0)
-- Dependencies: 236
-- Name: TABLE user_seeds; Type: COMMENT; Schema: farm; Owner: -
--

COMMENT ON TABLE farm.user_seeds IS '用户种子背包表';


--
-- TOC entry 235 (class 1259 OID 29342)
-- Name: user_seeds_id_seq; Type: SEQUENCE; Schema: farm; Owner: -
--

ALTER TABLE farm.user_seeds ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME farm.user_seeds_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 220 (class 1259 OID 29196)
-- Name: users; Type: TABLE; Schema: farm; Owner: -
--

CREATE TABLE farm.users (
    id bigint NOT NULL,
    username public.citext NOT NULL,
    nickname character varying(500) NOT NULL,
    password_hash character varying(500) NOT NULL,
    email public.citext NOT NULL,
    avatar_url character varying(1024) DEFAULT ''::character varying NOT NULL,
    experience bigint DEFAULT 0 NOT NULL,
    score bigint DEFAULT 0 NOT NULL,
    coin bigint DEFAULT 0 NOT NULL,
    preferences_json jsonb DEFAULT '{}'::jsonb NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by bigint,
    updated_by bigint,
    remark text,
    status smallint DEFAULT 1 NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL,
    opt_lock_version integer DEFAULT 0 NOT NULL
);


--
-- TOC entry 3822 (class 0 OID 0)
-- Dependencies: 220
-- Name: TABLE users; Type: COMMENT; Schema: farm; Owner: -
--

COMMENT ON TABLE farm.users IS '用户信息表';


--
-- TOC entry 219 (class 1259 OID 29195)
-- Name: users_id_seq; Type: SEQUENCE; Schema: farm; Owner: -
--

ALTER TABLE farm.users ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME farm.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 3772 (class 0 OID 29216)
-- Dependencies: 222
-- Data for Name: asset_defaults; Type: TABLE DATA; Schema: farm; Owner: -
--

INSERT INTO farm.asset_defaults (id, asset_key, asset_url, description, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (1, 'avatar', '/oss/.defaults/avatar/default-avatar.png', '用户头像默认图', '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.asset_defaults (id, asset_key, asset_url, description, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (2, 'seedCover', '/oss/.defaults/seed/seed-cover-default.png', '种子封面默认图', '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.asset_defaults (id, asset_key, asset_url, description, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (3, 'seedStage', '/oss/.defaults/seed/seed-stage-default.png', '种子阶段默认图', '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.asset_defaults (id, asset_key, asset_url, description, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (4, 'soilCover', '/oss/.defaults/soil/soil-default.png', '土壤默认图', '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.asset_defaults (id, asset_key, asset_url, description, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (5, 'plotCover', '/oss/.defaults/plot/plot-cover-default.png', '地块封面默认图', '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.asset_defaults (id, asset_key, asset_url, description, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (6, 'plotIcon', '/oss/.defaults/plot/plot-icon-default.png', '地块图标默认图', '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.asset_defaults (id, asset_key, asset_url, description, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (7, 'bgm', '/resources/sounds/bgm/Must%20Work%20to%20Eat.wav', '默认背景音乐', '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.asset_defaults (id, asset_key, asset_url, description, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (8, 'seedStageWithered', '/oss/.defaults/seed/seed-stage-withered-default.png', '种子枯萎阶段默认图', '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);


--
-- TOC entry 3780 (class 0 OID 29283)
-- Dependencies: 230
-- Data for Name: growth_stages; Type: TABLE DATA; Schema: farm; Owner: -
--

INSERT INTO farm.growth_stages (id, name, description, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (1, '种子', '播种后的初始阶段', '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.growth_stages (id, name, description, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (2, '发芽', '发芽阶段', '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.growth_stages (id, name, description, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (3, '幼苗', '发芽后的幼苗期', '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.growth_stages (id, name, description, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (4, '生长期', '快速生长阶段', '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.growth_stages (id, name, description, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (5, '开花', '开花授粉阶段', '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.growth_stages (id, name, description, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (6, '结果', '开花后结果阶段', '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.growth_stages (id, name, description, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (7, '成熟', '可收获阶段', '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.growth_stages (id, name, description, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (8, '枯萎', '作物虫害超限或错过收获窗口后的最终阶段', '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);


--
-- TOC entry 3778 (class 0 OID 29263)
-- Dependencies: 228
-- Data for Name: plot_policies; Type: TABLE DATA; Schema: farm; Owner: -
--

INSERT INTO farm.plot_policies (id, policy_name, policy_version, active, effective_scope, publish_status, default_total_plot_count, default_unlocked_plot_count, default_locked_plot_count, default_lock_rule_code, default_lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (1, 'default-policy-v1', 'v1', true, 'NEW_USER_ONLY', 'DRAFT', 6, 2, 4, 'DEFAULT_LOCKED', '待解锁', '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);


--
-- TOC entry 3800 (class 0 OID 29462)
-- Dependencies: 250
-- Data for Name: request_idempotencies; Type: TABLE DATA; Schema: farm; Owner: -
--

INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (1, 2, 'PLANT', 'plant_1781053280458_445281', 'SUCCESS', '{"cropId": 1, "plotId": 7, "userId": 2, "growStatus": 1, "seedTypeId": 1, "expectedRipeAt": "2026-06-10T09:02:58.2120424+08:00", "currentStageIndex": 1, "expectedWitheredAt": "2026-06-10T09:03:36.2120424+08:00", "remainSeedQuantity": 17}', NULL, '2026-06-10 01:01:21.729791+00', '2026-06-10 01:01:20.728045+00', '2026-06-10 01:01:21.729791+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (2, 2, 'PLANT', 'plant_1781053281778_409349', 'FAILED', NULL, 'Plot already has crop', '2026-06-10 01:01:22.216018+00', '2026-06-10 01:01:21.955398+00', '2026-06-10 01:01:22.216018+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (3, 2, 'PLANT', 'plant_1781053291725_73019', 'SUCCESS', '{"cropId": 2, "plotId": 8, "userId": 2, "growStatus": 1, "seedTypeId": 3, "expectedRipeAt": "2026-06-10T09:04:46.2479648+08:00", "currentStageIndex": 1, "expectedWitheredAt": "2026-06-10T09:05:48.2479648+08:00", "remainSeedQuantity": 9}', NULL, '2026-06-10 01:01:32.520086+00', '2026-06-10 01:01:31.881248+00', '2026-06-10 01:01:32.520086+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (4, 2, 'PLANT', 'plant_1781053311130_250749', 'SUCCESS', '{"cropId": 3, "plotId": 9, "userId": 2, "growStatus": 1, "seedTypeId": 2, "expectedRipeAt": "2026-06-10T09:04:36.7215506+08:00", "currentStageIndex": 1, "expectedWitheredAt": "2026-06-10T09:05:36.7215506+08:00", "remainSeedQuantity": 17}', NULL, '2026-06-10 01:01:52.036535+00', '2026-06-10 01:01:51.287674+00', '2026-06-10 01:01:52.036535+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (5, 2, 'HARVEST', 'harvest_1781053393356_690296', 'SUCCESS', '{"cropId": 1, "plotId": 7, "userId": 2, "scoreGain": 16, "seedTypeId": 1, "cropCleared": false, "currentScore": 716, "bugCountAfter": 0, "bugCountBefore": 0, "experienceGain": 20, "nextGrowStatus": 1, "nextStageIndex": 3, "bugPenaltyPerBug": 0, "currentExperience": 2120, "harvestFruitNumber": 3, "nextExpectedRipeAt": "2026-06-10T09:03:58.87776+08:00", "totalFruitQuantity": 3, "totalBugPenaltyFruit": 0, "baseHarvestFruitNumber": 3, "nextExpectedWitheredAt": "2026-06-10T09:04:36.87776+08:00"}', NULL, '2026-06-10 01:03:14.502784+00', '2026-06-10 01:03:13.594475+00', '2026-06-10 01:03:14.502784+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (6, 2, 'HARVEST', 'harvest_1781053394451_48507', 'FAILED', NULL, 'Crop is not ripe', '2026-06-10 01:03:15.274486+00', '2026-06-10 01:03:14.700134+00', '2026-06-10 01:03:15.274486+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (7, 2, 'HARVEST', 'harvest_1781053395151_161088', 'FAILED', NULL, 'Crop is not ripe', '2026-06-10 01:03:15.991702+00', '2026-06-10 01:03:15.452502+00', '2026-06-10 01:03:15.991702+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (8, 2, 'BUY_SEED', 'buy_seed_1781053449768_778859', 'SUCCESS', '{"userId": 2, "seedName": "土豆", "afterCoin": 4890, "unitPrice": 10, "beforeCoin": 4900, "seedTypeId": 1, "buyQuantity": 1, "totalCostCoin": 10, "afterSeedQuantity": 18, "beforeSeedQuantity": 17}', NULL, '2026-06-10 01:04:10.63503+00', '2026-06-10 01:04:10.050639+00', '2026-06-10 01:04:10.63503+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (9, 2, 'SELL_FRUIT', 'store_sell_fruit_1781053479013_998789', 'SUCCESS', '{"userId": 2, "seedName": "土豆", "afterCoin": 4904, "beforeCoin": 4890, "seedTypeId": 1, "sellQuantity": 1, "unitFruitPrice": 14, "totalIncomeCoin": 14, "afterFruitQuantity": 2, "beforeFruitQuantity": 3}', NULL, '2026-06-10 01:04:39.723987+00', '2026-06-10 01:04:39.178875+00', '2026-06-10 01:04:39.723987+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (10, 2, 'SELL_FRUIT', 'store_sell_fruit_1781053485669_248395', 'SUCCESS', '{"userId": 2, "seedName": "土豆", "afterCoin": 4918, "beforeCoin": 4904, "seedTypeId": 1, "sellQuantity": 1, "unitFruitPrice": 14, "totalIncomeCoin": 14, "afterFruitQuantity": 1, "beforeFruitQuantity": 2}', NULL, '2026-06-10 01:04:46.326952+00', '2026-06-10 01:04:45.791045+00', '2026-06-10 01:04:46.326952+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (11, 2, 'CLEAR', 'clear_1781054225371_402560', 'SUCCESS', '{"cropId": 1, "plotId": 7, "userId": 2, "cleared": true, "clearedAt": "2026-06-10T09:17:05.7962117+08:00", "seedTypeId": 1, "bugCountBefore": 0, "growStatusBefore": 3, "stageIndexBefore": 5}', NULL, '2026-06-10 01:17:05.982+00', '2026-06-10 01:17:05.584425+00', '2026-06-10 01:17:05.982+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (12, 2, 'CLEAR', 'clear_1781054226896_235451', 'SUCCESS', '{"cropId": 2, "plotId": 8, "userId": 2, "cleared": true, "clearedAt": "2026-06-10T09:17:07.2234714+08:00", "seedTypeId": 3, "bugCountBefore": 1, "growStatusBefore": 3, "stageIndexBefore": 6}', NULL, '2026-06-10 01:17:07.424768+00', '2026-06-10 01:17:06.991519+00', '2026-06-10 01:17:07.424768+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (13, 2, 'CLEAR', 'clear_1781054228311_316693', 'SUCCESS', '{"cropId": 3, "plotId": 9, "userId": 2, "cleared": true, "clearedAt": "2026-06-10T09:17:08.6883656+08:00", "seedTypeId": 2, "bugCountBefore": 2, "growStatusBefore": 3, "stageIndexBefore": 5}', NULL, '2026-06-10 01:17:08.864161+00', '2026-06-10 01:17:08.459841+00', '2026-06-10 01:17:08.864161+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (14, 2, 'PLANT', 'plant_1781054725480_25239', 'SUCCESS', '{"cropId": 4, "plotId": 7, "userId": 2, "growStatus": 1, "seedTypeId": 1, "expectedRipeAt": "2026-06-10T09:27:03.1334225+08:00", "currentStageIndex": 1, "expectedWitheredAt": "2026-06-10T09:27:41.1334225+08:00", "remainSeedQuantity": 17}', NULL, '2026-06-10 01:25:26.577228+00', '2026-06-10 01:25:25.679657+00', '2026-06-10 01:25:26.577228+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (15, 2, 'PLANT', 'plant_1781054804888_457379', 'SUCCESS', '{"cropId": 5, "plotId": 8, "userId": 2, "growStatus": 1, "seedTypeId": 3, "expectedRipeAt": "2026-06-10T09:29:59.4696662+08:00", "currentStageIndex": 1, "expectedWitheredAt": "2026-06-10T09:31:01.4696662+08:00", "remainSeedQuantity": 8}', NULL, '2026-06-10 01:26:45.778646+00', '2026-06-10 01:26:45.058736+00', '2026-06-10 01:26:45.778646+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (16, 2, 'PLANT', 'plant_1781054811334_379095', 'SUCCESS', '{"cropId": 6, "plotId": 9, "userId": 2, "growStatus": 1, "seedTypeId": 5, "expectedRipeAt": "2026-06-10T09:29:01.9546025+08:00", "currentStageIndex": 1, "expectedWitheredAt": "2026-06-10T09:29:36.9546025+08:00", "remainSeedQuantity": 15}', NULL, '2026-06-10 01:26:52.304963+00', '2026-06-10 01:26:51.562417+00', '2026-06-10 01:26:52.304963+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (17, 2, 'HARVEST', 'harvest_1781054826897_171750', 'SUCCESS', '{"cropId": 4, "plotId": 7, "userId": 2, "scoreGain": 17, "seedTypeId": 1, "cropCleared": true, "currentScore": 733, "bugCountAfter": 0, "bugCountBefore": 0, "experienceGain": 21, "nextGrowStatus": null, "nextStageIndex": null, "bugPenaltyPerBug": 1, "currentExperience": 2141, "harvestFruitNumber": 5, "nextExpectedRipeAt": null, "totalFruitQuantity": 6, "totalBugPenaltyFruit": 0, "baseHarvestFruitNumber": 5, "nextExpectedWitheredAt": null}', NULL, '2026-06-10 01:27:07.923384+00', '2026-06-10 01:27:07.087938+00', '2026-06-10 01:27:07.923384+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (18, 2, 'PLANT', 'plant_1781054832360_295855', 'SUCCESS', '{"cropId": 7, "plotId": 7, "userId": 2, "growStatus": 1, "seedTypeId": 1, "expectedRipeAt": "2026-06-10T09:28:49.8310246+08:00", "currentStageIndex": 1, "expectedWitheredAt": "2026-06-10T09:29:27.8310246+08:00", "remainSeedQuantity": 16}', NULL, '2026-06-10 01:27:13.106053+00', '2026-06-10 01:27:12.478792+00', '2026-06-10 01:27:13.106053+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (19, 2, 'PLANT', 'plant_1781054858220_244794', 'SUCCESS', '{"cropId": 8, "plotId": 10, "userId": 2, "growStatus": 1, "seedTypeId": 3, "expectedRipeAt": "2026-06-10T09:30:52.9390086+08:00", "currentStageIndex": 1, "expectedWitheredAt": "2026-06-10T09:31:54.9390086+08:00", "remainSeedQuantity": 7}', NULL, '2026-06-10 01:27:39.351471+00', '2026-06-10 01:27:38.381331+00', '2026-06-10 01:27:39.351471+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (20, 2, 'PLANT', 'plant_1781054862978_684813', 'SUCCESS', '{"cropId": 9, "plotId": 12, "userId": 2, "growStatus": 1, "seedTypeId": 3, "expectedRipeAt": "2026-06-10T09:30:57.5169067+08:00", "currentStageIndex": 1, "expectedWitheredAt": "2026-06-10T09:31:59.5169067+08:00", "remainSeedQuantity": 6}', NULL, '2026-06-10 01:27:43.875141+00', '2026-06-10 01:27:43.108108+00', '2026-06-10 01:27:43.875141+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (22, 2, 'PLANT', 'plant_1781054943099_13368', 'SUCCESS', '{"cropId": 10, "plotId": 11, "userId": 2, "growStatus": 1, "seedTypeId": 5, "expectedRipeAt": "2026-06-10T09:31:46.5617927+08:00", "currentStageIndex": 1, "expectedWitheredAt": "2026-06-10T09:32:29.5617927+08:00", "remainSeedQuantity": 14}', NULL, '2026-06-10 01:29:03.885175+00', '2026-06-10 01:29:03.195672+00', '2026-06-10 01:29:03.885175+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (24, 2, 'SELL_FRUIT', 'store_sell_fruit_1781054964325_312542', 'SUCCESS', '{"userId": 2, "seedName": "土豆", "afterCoin": 4723, "beforeCoin": 4558, "seedTypeId": 1, "sellQuantity": 11, "unitFruitPrice": 15, "totalIncomeCoin": 165, "afterFruitQuantity": 0, "beforeFruitQuantity": 11}', NULL, '2026-06-10 01:29:25.115274+00', '2026-06-10 01:29:24.516925+00', '2026-06-10 01:29:25.115274+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (21, 2, 'HARVEST', 'harvest_1781054937140_708151', 'SUCCESS', '{"cropId": 7, "plotId": 7, "userId": 2, "scoreGain": 17, "seedTypeId": 1, "cropCleared": true, "currentScore": 760, "bugCountAfter": 0, "bugCountBefore": 0, "experienceGain": 21, "nextGrowStatus": null, "nextStageIndex": null, "bugPenaltyPerBug": 1, "currentExperience": 2182, "harvestFruitNumber": 5, "nextExpectedRipeAt": null, "totalFruitQuantity": 11, "totalBugPenaltyFruit": 0, "baseHarvestFruitNumber": 5, "nextExpectedWitheredAt": null}', NULL, '2026-06-10 01:28:58.120477+00', '2026-06-10 01:28:57.296447+00', '2026-06-10 01:28:58.120477+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (23, 2, 'HARVEST', 'harvest_1781054953468_6190', 'SUCCESS', '{"cropId": 6, "plotId": 9, "userId": 2, "scoreGain": 28, "seedTypeId": 5, "cropCleared": false, "currentScore": 793, "bugCountAfter": 0, "bugCountBefore": 0, "experienceGain": 36, "nextGrowStatus": 1, "nextStageIndex": 4, "bugPenaltyPerBug": 1, "currentExperience": 2228, "harvestFruitNumber": 5, "nextExpectedRipeAt": "2026-06-10T09:29:53.9128106+08:00", "totalFruitQuantity": 5, "totalBugPenaltyFruit": 0, "baseHarvestFruitNumber": 5, "nextExpectedWitheredAt": "2026-06-10T09:30:28.9128106+08:00"}', NULL, '2026-06-10 01:29:14.481587+00', '2026-06-10 01:29:13.619869+00', '2026-06-10 01:29:14.481587+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (26, 2, 'BUY_SEED', 'buy_seed_1781054978500_302275', 'SUCCESS', '{"userId": 2, "seedName": "钻石", "afterCoin": 4695, "unitPrice": 36, "beforeCoin": 4767, "seedTypeId": 7, "buyQuantity": 2, "totalCostCoin": 72, "afterSeedQuantity": 2, "beforeSeedQuantity": 0}', NULL, '2026-06-10 01:29:39.294646+00', '2026-06-10 01:29:38.716751+00', '2026-06-10 01:29:39.294646+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (25, 2, 'SELL_FRUIT', 'store_sell_fruit_1781054968374_978792', 'SUCCESS', '{"userId": 2, "seedName": "草莓", "afterCoin": 4767, "beforeCoin": 4723, "seedTypeId": 5, "sellQuantity": 2, "unitFruitPrice": 22, "totalIncomeCoin": 44, "afterFruitQuantity": 3, "beforeFruitQuantity": 5}', NULL, '2026-06-10 01:29:29.284492+00', '2026-06-10 01:29:28.623223+00', '2026-06-10 01:29:29.284492+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (27, 2, 'PLANT', 'plant_1781054989235_319721', 'SUCCESS', '{"cropId": 11, "plotId": 7, "userId": 2, "growStatus": 1, "seedTypeId": 7, "expectedRipeAt": "2026-06-10T09:32:49.7540458+08:00", "currentStageIndex": 1, "expectedWitheredAt": "2026-06-10T09:34:39.7540458+08:00", "remainSeedQuantity": 1}', NULL, '2026-06-10 01:29:50.116686+00', '2026-06-10 01:29:49.398952+00', '2026-06-10 01:29:50.116686+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (28, 2, 'HARVEST', 'harvest_1781054997915_672798', 'SUCCESS', '{"cropId": 6, "plotId": 9, "userId": 2, "scoreGain": 28, "seedTypeId": 5, "cropCleared": false, "currentScore": 826, "bugCountAfter": 0, "bugCountBefore": 0, "experienceGain": 36, "nextGrowStatus": 1, "nextStageIndex": 4, "bugPenaltyPerBug": 1, "currentExperience": 2274, "harvestFruitNumber": 5, "nextExpectedRipeAt": "2026-06-10T09:30:38.3347242+08:00", "totalFruitQuantity": 8, "totalBugPenaltyFruit": 0, "baseHarvestFruitNumber": 5, "nextExpectedWitheredAt": "2026-06-10T09:31:13.3347242+08:00"}', NULL, '2026-06-10 01:29:58.954931+00', '2026-06-10 01:29:58.039454+00', '2026-06-10 01:29:58.954931+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (29, 2, 'HARVEST', 'harvest_1781055000718_35327', 'SUCCESS', '{"cropId": 5, "plotId": 8, "userId": 2, "scoreGain": 42, "seedTypeId": 3, "cropCleared": false, "currentScore": 868, "bugCountAfter": 0, "bugCountBefore": 0, "experienceGain": 48, "nextGrowStatus": 1, "nextStageIndex": 4, "bugPenaltyPerBug": 1, "currentExperience": 2322, "harvestFruitNumber": 6, "nextExpectedRipeAt": "2026-06-10T09:31:13.1648943+08:00", "totalFruitQuantity": 6, "totalBugPenaltyFruit": 0, "baseHarvestFruitNumber": 6, "nextExpectedWitheredAt": "2026-06-10T09:32:14.1648943+08:00"}', NULL, '2026-06-10 01:30:01.783848+00', '2026-06-10 01:30:00.878374+00', '2026-06-10 01:30:01.783848+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (30, 2, 'HARVEST', 'harvest_1781055090332_667274', 'SUCCESS', '{"cropId": 5, "plotId": 8, "userId": 2, "scoreGain": 42, "seedTypeId": 3, "cropCleared": false, "currentScore": 920, "bugCountAfter": 0, "bugCountBefore": 0, "experienceGain": 48, "nextGrowStatus": 1, "nextStageIndex": 4, "bugPenaltyPerBug": 1, "currentExperience": 2390, "harvestFruitNumber": 6, "nextExpectedRipeAt": "2026-06-10T09:32:42.7148367+08:00", "totalFruitQuantity": 12, "totalBugPenaltyFruit": 0, "baseHarvestFruitNumber": 6, "nextExpectedWitheredAt": "2026-06-10T09:33:43.7148367+08:00"}', NULL, '2026-06-10 01:31:31.289111+00', '2026-06-10 01:31:30.446835+00', '2026-06-10 01:31:31.289111+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (31, 2, 'HARVEST', 'harvest_1781055094747_575787', 'SUCCESS', '{"cropId": 8, "plotId": 10, "userId": 2, "scoreGain": 42, "seedTypeId": 3, "cropCleared": false, "currentScore": 962, "bugCountAfter": 0, "bugCountBefore": 0, "experienceGain": 48, "nextGrowStatus": 1, "nextStageIndex": 4, "bugPenaltyPerBug": 1, "currentExperience": 2438, "harvestFruitNumber": 6, "nextExpectedRipeAt": "2026-06-10T09:32:47.1671315+08:00", "totalFruitQuantity": 18, "totalBugPenaltyFruit": 0, "baseHarvestFruitNumber": 6, "nextExpectedWitheredAt": "2026-06-10T09:33:48.1671315+08:00"}', NULL, '2026-06-10 01:31:35.695173+00', '2026-06-10 01:31:34.915117+00', '2026-06-10 01:31:35.695173+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (32, 2, 'HARVEST', 'harvest_1781055100606_100915', 'SUCCESS', '{"cropId": 9, "plotId": 12, "userId": 2, "scoreGain": 42, "seedTypeId": 3, "cropCleared": false, "currentScore": 1004, "bugCountAfter": 0, "bugCountBefore": 0, "experienceGain": 48, "nextGrowStatus": 1, "nextStageIndex": 4, "bugPenaltyPerBug": 1, "currentExperience": 2486, "harvestFruitNumber": 6, "nextExpectedRipeAt": "2026-06-10T09:32:53.1364311+08:00", "totalFruitQuantity": 24, "totalBugPenaltyFruit": 0, "baseHarvestFruitNumber": 6, "nextExpectedWitheredAt": "2026-06-10T09:33:54.1364311+08:00"}', NULL, '2026-06-10 01:31:41.69512+00', '2026-06-10 01:31:40.879957+00', '2026-06-10 01:31:41.69512+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (33, 2, 'HARVEST', 'harvest_1781055126977_616695', 'SUCCESS', '{"cropId": 10, "plotId": 11, "userId": 2, "scoreGain": 28, "seedTypeId": 5, "cropCleared": false, "currentScore": 1032, "bugCountAfter": 0, "bugCountBefore": 0, "experienceGain": 36, "nextGrowStatus": 1, "nextStageIndex": 4, "bugPenaltyPerBug": 1, "currentExperience": 2522, "harvestFruitNumber": 5, "nextExpectedRipeAt": "2026-06-10T09:32:57.4430781+08:00", "totalFruitQuantity": 13, "totalBugPenaltyFruit": 0, "baseHarvestFruitNumber": 5, "nextExpectedWitheredAt": "2026-06-10T09:33:41.4430781+08:00"}', NULL, '2026-06-10 01:32:07.997978+00', '2026-06-10 01:32:07.13055+00', '2026-06-10 01:32:07.997978+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (34, 2, 'CLEAR', 'clear_1781055131017_640469', 'SUCCESS', '{"cropId": 6, "plotId": 9, "userId": 2, "cleared": true, "clearedAt": "2026-06-10T09:32:11.403304+08:00", "seedTypeId": 5, "bugCountBefore": 0, "growStatusBefore": 3, "stageIndexBefore": 6}', NULL, '2026-06-10 01:32:11.601144+00', '2026-06-10 01:32:11.183459+00', '2026-06-10 01:32:11.601144+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (35, 2, 'PLANT', 'plant_1781055144450_895952', 'SUCCESS', '{"cropId": 12, "plotId": 9, "userId": 2, "growStatus": 1, "seedTypeId": 7, "expectedRipeAt": "2026-06-10T09:35:24.9771096+08:00", "currentStageIndex": 1, "expectedWitheredAt": "2026-06-10T09:37:14.9771096+08:00", "remainSeedQuantity": 0}', NULL, '2026-06-10 01:32:25.279843+00', '2026-06-10 01:32:24.566122+00', '2026-06-10 01:32:25.279843+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (36, 2, 'PLANT', 'plant_1781055148610_178685', 'SUCCESS', '{"cropId": 13, "plotId": 31, "userId": 2, "growStatus": 1, "seedTypeId": 2, "expectedRipeAt": "2026-06-10T09:35:32.1360956+08:00", "currentStageIndex": 1, "expectedWitheredAt": "2026-06-10T09:36:39.1360956+08:00", "remainSeedQuantity": 16}', NULL, '2026-06-10 01:32:29.439846+00', '2026-06-10 01:32:28.716355+00', '2026-06-10 01:32:29.439846+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (37, 2, 'HARVEST', 'harvest_1781055165608_170968', 'SUCCESS', '{"cropId": 5, "plotId": 8, "userId": 2, "scoreGain": 42, "seedTypeId": 3, "cropCleared": true, "currentScore": 1074, "bugCountAfter": 0, "bugCountBefore": 0, "experienceGain": 48, "nextGrowStatus": null, "nextStageIndex": null, "bugPenaltyPerBug": 1, "currentExperience": 2570, "harvestFruitNumber": 6, "nextExpectedRipeAt": null, "totalFruitQuantity": 30, "totalBugPenaltyFruit": 0, "baseHarvestFruitNumber": 6, "nextExpectedWitheredAt": null}', NULL, '2026-06-10 01:32:46.572682+00', '2026-06-10 01:32:45.756459+00', '2026-06-10 01:32:46.572682+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (38, 2, 'HARVEST', 'harvest_1781055173400_251993', 'SUCCESS', '{"cropId": 11, "plotId": 7, "userId": 2, "scoreGain": 62, "seedTypeId": 7, "cropCleared": false, "currentScore": 1141, "bugCountAfter": 0, "bugCountBefore": 0, "experienceGain": 68, "nextGrowStatus": 1, "nextStageIndex": 4, "bugPenaltyPerBug": 1, "currentExperience": 2648, "harvestFruitNumber": 6, "nextExpectedRipeAt": "2026-06-10T09:33:48.8005985+08:00", "totalFruitQuantity": 6, "totalBugPenaltyFruit": 0, "baseHarvestFruitNumber": 6, "nextExpectedWitheredAt": "2026-06-10T09:35:38.8005985+08:00"}', NULL, '2026-06-10 01:32:54.320709+00', '2026-06-10 01:32:53.516243+00', '2026-06-10 01:32:54.320709+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (39, 2, 'HARVEST', 'harvest_1781055175487_940018', 'SUCCESS', '{"cropId": 9, "plotId": 12, "userId": 2, "scoreGain": 42, "seedTypeId": 3, "cropCleared": false, "currentScore": 1183, "bugCountAfter": 0, "bugCountBefore": 0, "experienceGain": 48, "nextGrowStatus": 1, "nextStageIndex": 4, "bugPenaltyPerBug": 1, "currentExperience": 2696, "harvestFruitNumber": 6, "nextExpectedRipeAt": "2026-06-10T09:34:07.824843+08:00", "totalFruitQuantity": 36, "totalBugPenaltyFruit": 0, "baseHarvestFruitNumber": 6, "nextExpectedWitheredAt": "2026-06-10T09:35:08.824843+08:00"}', NULL, '2026-06-10 01:32:56.408175+00', '2026-06-10 01:32:55.596595+00', '2026-06-10 01:32:56.408175+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (42, 2, 'HARVEST', 'harvest_1781055181305_437093', 'FAILED', NULL, 'Row was updated or deleted by another transaction (or unsaved-value mapping was incorrect) : [cn.jxufe.farm.entity.UserCrop#10]', '2026-06-10 01:33:02.336154+00', '2026-06-10 01:33:01.440651+00', '2026-06-10 01:33:02.336154+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (40, 2, 'HARVEST', 'harvest_1781055177040_54592', 'SUCCESS', '{"cropId": 8, "plotId": 10, "userId": 2, "scoreGain": 42, "seedTypeId": 3, "cropCleared": false, "currentScore": 1225, "bugCountAfter": 0, "bugCountBefore": 0, "experienceGain": 48, "nextGrowStatus": 1, "nextStageIndex": 4, "bugPenaltyPerBug": 1, "currentExperience": 2744, "harvestFruitNumber": 6, "nextExpectedRipeAt": "2026-06-10T09:34:09.4807195+08:00", "totalFruitQuantity": 42, "totalBugPenaltyFruit": 0, "baseHarvestFruitNumber": 6, "nextExpectedWitheredAt": "2026-06-10T09:35:10.4807195+08:00"}', NULL, '2026-06-10 01:32:58.037013+00', '2026-06-10 01:32:57.19242+00', '2026-06-10 01:32:58.037013+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (41, 2, 'HARVEST', 'harvest_1781055180794_595754', 'SUCCESS', '{"cropId": 10, "plotId": 11, "userId": 2, "scoreGain": 28, "seedTypeId": 5, "cropCleared": false, "currentScore": 1253, "bugCountAfter": 0, "bugCountBefore": 0, "experienceGain": 36, "nextGrowStatus": 1, "nextStageIndex": 4, "bugPenaltyPerBug": 1, "currentExperience": 2780, "harvestFruitNumber": 5, "nextExpectedRipeAt": "2026-06-10T09:33:51.2408367+08:00", "totalFruitQuantity": 18, "totalBugPenaltyFruit": 0, "baseHarvestFruitNumber": 5, "nextExpectedWitheredAt": "2026-06-10T09:34:35.2408367+08:00"}', NULL, '2026-06-10 01:33:01.888177+00', '2026-06-10 01:33:00.945273+00', '2026-06-10 01:33:01.888177+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (44, 2, 'HARVEST', 'harvest_1781055305715_843325', 'SUCCESS', '{"cropId": 11, "plotId": 7, "userId": 2, "scoreGain": 62, "seedTypeId": 7, "cropCleared": false, "currentScore": 1357, "bugCountAfter": 0, "bugCountBefore": 0, "experienceGain": 68, "nextGrowStatus": 1, "nextStageIndex": 4, "bugPenaltyPerBug": 1, "currentExperience": 2896, "harvestFruitNumber": 6, "nextExpectedRipeAt": "2026-06-10T09:36:01.1247971+08:00", "totalFruitQuantity": 12, "totalBugPenaltyFruit": 0, "baseHarvestFruitNumber": 6, "nextExpectedWitheredAt": "2026-06-10T09:37:51.1247971+08:00"}', NULL, '2026-06-10 01:35:06.766359+00', '2026-06-10 01:35:05.830836+00', '2026-06-10 01:35:06.766359+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (43, 2, 'HARVEST', 'harvest_1781055304994_553617', 'SUCCESS', '{"cropId": 9, "plotId": 12, "userId": 2, "scoreGain": 42, "seedTypeId": 3, "cropCleared": true, "currentScore": 1295, "bugCountAfter": 0, "bugCountBefore": 0, "experienceGain": 48, "nextGrowStatus": null, "nextStageIndex": null, "bugPenaltyPerBug": 1, "currentExperience": 2828, "harvestFruitNumber": 6, "nextExpectedRipeAt": null, "totalFruitQuantity": 48, "totalBugPenaltyFruit": 0, "baseHarvestFruitNumber": 6, "nextExpectedWitheredAt": null}', NULL, '2026-06-10 01:35:05.902849+00', '2026-06-10 01:35:05.11849+00', '2026-06-10 01:35:05.902849+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (45, 2, 'HARVEST', 'harvest_1781055306525_109680', 'SUCCESS', '{"cropId": 8, "plotId": 10, "userId": 2, "scoreGain": 42, "seedTypeId": 3, "cropCleared": true, "currentScore": 1399, "bugCountAfter": 0, "bugCountBefore": 0, "experienceGain": 48, "nextGrowStatus": null, "nextStageIndex": null, "bugPenaltyPerBug": 1, "currentExperience": 2944, "harvestFruitNumber": 6, "nextExpectedRipeAt": null, "totalFruitQuantity": 54, "totalBugPenaltyFruit": 0, "baseHarvestFruitNumber": 6, "nextExpectedWitheredAt": null}', NULL, '2026-06-10 01:35:07.446584+00', '2026-06-10 01:35:06.714362+00', '2026-06-10 01:35:07.446584+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (46, 1, 'BUY_SEED', 'buy_seed_1781111304125_830934', 'SUCCESS', '{"userId": 1, "seedName": "钻石", "afterCoin": 5528, "unitPrice": 36, "beforeCoin": 5600, "seedTypeId": 7, "buyQuantity": 2, "totalCostCoin": 72, "afterSeedQuantity": 14, "beforeSeedQuantity": 12}', NULL, '2026-06-10 17:08:25.092343+00', '2026-06-10 17:08:24.369491+00', '2026-06-10 17:08:25.092343+00', 1, 1, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (47, 1, 'PLANT', 'plant_1781111325715_599748', 'SUCCESS', '{"cropId": 14, "plotId": 1, "userId": 1, "growStatus": 1, "seedTypeId": 1, "expectedRipeAt": "2026-06-11T01:10:23.242099093+08:00", "currentStageIndex": 1, "expectedWitheredAt": "2026-06-11T01:11:01.242099093+08:00", "remainSeedQuantity": 23}', NULL, '2026-06-10 17:08:46.577976+00', '2026-06-10 17:08:45.855606+00', '2026-06-10 17:08:46.577976+00', 1, 1, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (48, 1, 'PLANT', 'plant_1781111331574_887166', 'SUCCESS', '{"cropId": 15, "plotId": 2, "userId": 1, "growStatus": 1, "seedTypeId": 2, "expectedRipeAt": "2026-06-11T01:11:55.060661522+08:00", "currentStageIndex": 1, "expectedWitheredAt": "2026-06-11T01:13:02.060661522+08:00", "remainSeedQuantity": 19}', NULL, '2026-06-10 17:08:52.300544+00', '2026-06-10 17:08:51.691334+00', '2026-06-10 17:08:52.300544+00', 1, 1, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (49, 1, 'HARVEST', 'harvest_1781111428177_268329', 'SUCCESS', '{"cropId": 14, "plotId": 1, "userId": 1, "scoreGain": 17, "seedTypeId": 1, "cropCleared": true, "currentScore": 837, "bugCountAfter": 0, "bugCountBefore": 0, "experienceGain": 21, "nextGrowStatus": null, "nextStageIndex": null, "bugPenaltyPerBug": 1, "currentExperience": 2421, "harvestFruitNumber": 5, "nextExpectedRipeAt": null, "totalFruitQuantity": 5, "totalBugPenaltyFruit": 0, "baseHarvestFruitNumber": 5, "nextExpectedWitheredAt": null}', NULL, '2026-06-10 17:10:29.106583+00', '2026-06-10 17:10:28.303196+00', '2026-06-10 17:10:29.106583+00', 1, 1, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (50, 1, 'HARVEST', 'harvest_1781111526705_454998', 'SUCCESS', '{"cropId": 15, "plotId": 2, "userId": 1, "scoreGain": 18, "seedTypeId": 2, "cropCleared": false, "currentScore": 855, "bugCountAfter": 0, "bugCountBefore": 0, "experienceGain": 22, "nextGrowStatus": 1, "nextStageIndex": 3, "bugPenaltyPerBug": 1, "currentExperience": 2443, "harvestFruitNumber": 3, "nextExpectedRipeAt": "2026-06-11T01:13:25.113017902+08:00", "totalFruitQuantity": 3, "totalBugPenaltyFruit": 0, "baseHarvestFruitNumber": 3, "nextExpectedWitheredAt": "2026-06-11T01:14:31.113017902+08:00"}', NULL, '2026-06-10 17:12:07.626392+00', '2026-06-10 17:12:06.874499+00', '2026-06-10 17:12:07.626392+00', 1, 1, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (51, 1, 'HARVEST', 'harvest_1781111608666_308679', 'SUCCESS', '{"cropId": 15, "plotId": 2, "userId": 1, "scoreGain": 18, "seedTypeId": 2, "cropCleared": true, "currentScore": 873, "bugCountAfter": 0, "bugCountBefore": 0, "experienceGain": 22, "nextGrowStatus": null, "nextStageIndex": null, "bugPenaltyPerBug": 1, "currentExperience": 2465, "harvestFruitNumber": 3, "nextExpectedRipeAt": null, "totalFruitQuantity": 6, "totalBugPenaltyFruit": 0, "baseHarvestFruitNumber": 3, "nextExpectedWitheredAt": null}', NULL, '2026-06-10 17:13:31.634154+00', '2026-06-10 17:13:30.882908+00', '2026-06-10 17:13:31.634154+00', 1, 1, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (52, 1, 'HARVEST', 'harvest_1781111611392_654463', 'FAILED', NULL, 'Crop on plot not found', '2026-06-10 17:13:31.846971+00', '2026-06-10 17:13:31.585989+00', '2026-06-10 17:13:31.846971+00', 1, 1, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (53, 2, 'CLEAR', 'clear_1781142986727_703249', 'SUCCESS', '{"cropId": 12, "plotId": 9, "userId": 2, "cleared": true, "clearedAt": "2026-06-11T09:56:27.2744299+08:00", "seedTypeId": 7, "bugCountBefore": 1, "growStatusBefore": 3, "stageIndexBefore": 7}', NULL, '2026-06-11 01:56:27.519127+00', '2026-06-11 01:56:26.994215+00', '2026-06-11 01:56:27.519127+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (54, 2, 'CLEAR', 'clear_1781142988465_104327', 'SUCCESS', '{"cropId": 10, "plotId": 11, "userId": 2, "cleared": true, "clearedAt": "2026-06-11T09:56:28.8580736+08:00", "seedTypeId": 5, "bugCountBefore": 1, "growStatusBefore": 3, "stageIndexBefore": 6}', NULL, '2026-06-11 01:56:29.095379+00', '2026-06-11 01:56:28.639958+00', '2026-06-11 01:56:29.095379+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (55, 2, 'CLEAR', 'clear_1781142990195_739000', 'SUCCESS', '{"cropId": 13, "plotId": 31, "userId": 2, "cleared": true, "clearedAt": "2026-06-11T09:56:30.5706159+08:00", "seedTypeId": 2, "bugCountBefore": 0, "growStatusBefore": 3, "stageIndexBefore": 5}', NULL, '2026-06-11 01:56:30.778768+00', '2026-06-11 01:56:30.31536+00', '2026-06-11 01:56:30.778768+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (56, 2, 'CLEAR', 'clear_1781142992373_31601', 'SUCCESS', '{"cropId": 11, "plotId": 7, "userId": 2, "cleared": true, "clearedAt": "2026-06-11T09:56:32.7219507+08:00", "seedTypeId": 7, "bugCountBefore": 0, "growStatusBefore": 3, "stageIndexBefore": 7}', NULL, '2026-06-11 01:56:32.902934+00', '2026-06-11 01:56:32.525939+00', '2026-06-11 01:56:32.902934+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (57, 2, 'SELL_FRUIT', 'store_sell_fruit_1781143430603_45601', 'SUCCESS', '{"userId": 2, "seedName": "西瓜", "afterCoin": 3025, "beforeCoin": 2995, "seedTypeId": 3, "sellQuantity": 1, "unitFruitPrice": 30, "totalIncomeCoin": 30, "afterFruitQuantity": 53, "beforeFruitQuantity": 54}', NULL, '2026-06-11 02:03:51.322798+00', '2026-06-11 02:03:50.757011+00', '2026-06-11 02:03:51.322798+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (58, 2, 'BUY_SEED', 'buy_seed_1781143480409_534673', 'SUCCESS', '{"userId": 2, "seedName": "钻石", "afterCoin": 2989, "unitPrice": 36, "beforeCoin": 3025, "seedTypeId": 7, "buyQuantity": 1, "totalCostCoin": 36, "afterSeedQuantity": 1, "beforeSeedQuantity": 0}', NULL, '2026-06-11 02:04:41.071832+00', '2026-06-11 02:04:40.598544+00', '2026-06-11 02:04:41.071832+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (59, 2, 'PLANT', 'plant_1781146252241_476896', 'SUCCESS', '{"cropId": 16, "plotId": 12, "userId": 2, "growStatus": 1, "seedTypeId": 2, "expectedRipeAt": "2026-06-11T10:54:18.818689998+08:00", "currentStageIndex": 1, "expectedWitheredAt": "2026-06-11T10:55:33.818689998+08:00", "remainSeedQuantity": 15}', NULL, '2026-06-11 02:50:53.193424+00', '2026-06-11 02:50:52.377486+00', '2026-06-11 02:50:53.193424+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (60, 2, 'PLANT', 'plant_1781146256771_125038', 'SUCCESS', '{"cropId": 17, "plotId": 7, "userId": 2, "growStatus": 1, "seedTypeId": 1, "expectedRipeAt": "2026-06-11T10:52:34.225854299+08:00", "currentStageIndex": 1, "expectedWitheredAt": "2026-06-11T10:53:12.225854299+08:00", "remainSeedQuantity": 15}', NULL, '2026-06-11 02:50:57.490746+00', '2026-06-11 02:50:56.892044+00', '2026-06-11 02:50:57.490746+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (61, 2, 'PLANT', 'plant_1781146269204_245584', 'SUCCESS', '{"cropId": 18, "plotId": 8, "userId": 2, "growStatus": 1, "seedTypeId": 3, "expectedRipeAt": "2026-06-11T10:54:48.648614524+08:00", "currentStageIndex": 1, "expectedWitheredAt": "2026-06-11T10:55:57.648614524+08:00", "remainSeedQuantity": 5}', NULL, '2026-06-11 02:51:09.894826+00', '2026-06-11 02:51:09.308787+00', '2026-06-11 02:51:09.894826+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (62, 2, 'PLANT', 'plant_1781146274212_67752', 'SUCCESS', '{"cropId": 19, "plotId": 9, "userId": 2, "growStatus": 1, "seedTypeId": 5, "expectedRipeAt": "2026-06-11T10:53:24.645019234+08:00", "currentStageIndex": 1, "expectedWitheredAt": "2026-06-11T10:53:59.645019234+08:00", "remainSeedQuantity": 13}', NULL, '2026-06-11 02:51:14.870434+00', '2026-06-11 02:51:14.318308+00', '2026-06-11 02:51:14.870434+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (63, 2, 'PLANT', 'plant_1781146280922_563782', 'SUCCESS', '{"cropId": 20, "plotId": 10, "userId": 2, "growStatus": 1, "seedTypeId": 7, "expectedRipeAt": "2026-06-11T10:55:06.368807094+08:00", "currentStageIndex": 1, "expectedWitheredAt": "2026-06-11T10:57:24.368807094+08:00", "remainSeedQuantity": 0}', NULL, '2026-06-11 02:51:21.605119+00', '2026-06-11 02:51:21.03775+00', '2026-06-11 02:51:21.605119+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (65, 2, 'PLANT', 'plant_1781146297506_246994', 'SUCCESS', '{"cropId": 22, "plotId": 11, "userId": 2, "growStatus": 1, "seedTypeId": 5, "expectedRipeAt": "2026-06-11T10:55:14.949032685+08:00", "currentStageIndex": 1, "expectedWitheredAt": "2026-06-11T10:56:12.949032685+08:00", "remainSeedQuantity": 11}', NULL, '2026-06-11 02:51:38.160695+00', '2026-06-11 02:51:37.614297+00', '2026-06-11 02:51:38.160695+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (67, 2, 'HARVEST', 'harvest_1781146426465_910539', 'SUCCESS', '{"cropId": 19, "plotId": 9, "userId": 2, "scoreGain": 28, "seedTypeId": 5, "cropCleared": false, "currentScore": 5050, "bugCountAfter": 0, "bugCountBefore": 0, "experienceGain": 36, "nextGrowStatus": 1, "nextStageIndex": 4, "bugPenaltyPerBug": 1, "currentExperience": 3021, "harvestFruitNumber": 5, "nextExpectedRipeAt": "2026-06-11T10:54:26.825955973+08:00", "totalFruitQuantity": 23, "totalBugPenaltyFruit": 0, "baseHarvestFruitNumber": 5, "nextExpectedWitheredAt": "2026-06-11T10:55:01.825955973+08:00"}', NULL, '2026-06-11 02:53:47.392428+00', '2026-06-11 02:53:46.59429+00', '2026-06-11 02:53:47.392428+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (71, 2, 'SELL_FRUIT', 'store_sell_fruit_1781146478501_59377', 'SUCCESS', '{"userId": 2, "seedName": "钻石", "afterCoin": 729, "beforeCoin": 249, "seedTypeId": 7, "sellQuantity": 12, "unitFruitPrice": 40, "totalIncomeCoin": 480, "afterFruitQuantity": 0, "beforeFruitQuantity": 12}', NULL, '2026-06-11 02:54:39.142532+00', '2026-06-11 02:54:38.621973+00', '2026-06-11 02:54:39.142532+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (78, 7, 'BUY_SEED', 'buy_seed_1781147107396_677015', 'SUCCESS', '{"userId": 7, "seedName": "土豆", "afterCoin": 4890, "unitPrice": 11, "beforeCoin": 5000, "seedTypeId": 1, "buyQuantity": 10, "totalCostCoin": 110, "afterSeedQuantity": 10, "beforeSeedQuantity": 0}', NULL, '2026-06-11 03:05:08.211368+00', '2026-06-11 03:05:07.62737+00', '2026-06-11 03:05:08.211368+00', 7, 7, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (79, 7, 'BUY_SEED', 'buy_seed_1781147112523_115560', 'SUCCESS', '{"userId": 7, "seedName": "玉米", "afterCoin": 4770, "unitPrice": 12, "beforeCoin": 4890, "seedTypeId": 2, "buyQuantity": 10, "totalCostCoin": 120, "afterSeedQuantity": 10, "beforeSeedQuantity": 0}', NULL, '2026-06-11 03:05:13.310618+00', '2026-06-11 03:05:12.721702+00', '2026-06-11 03:05:13.310618+00', 7, 7, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (80, 7, 'BUY_SEED', 'buy_seed_1781147116776_708092', 'SUCCESS', '{"userId": 7, "seedName": "西瓜", "afterCoin": 4530, "unitPrice": 24, "beforeCoin": 4770, "seedTypeId": 3, "buyQuantity": 10, "totalCostCoin": 240, "afterSeedQuantity": 10, "beforeSeedQuantity": 0}', NULL, '2026-06-11 03:05:17.586547+00', '2026-06-11 03:05:16.966698+00', '2026-06-11 03:05:17.586547+00', 7, 7, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (82, 7, 'BUY_SEED', 'buy_seed_1781147123503_586623', 'SUCCESS', '{"userId": 7, "seedName": "草莓", "afterCoin": 4190, "unitPrice": 18, "beforeCoin": 4370, "seedTypeId": 5, "buyQuantity": 10, "totalCostCoin": 180, "afterSeedQuantity": 10, "beforeSeedQuantity": 0}', NULL, '2026-06-11 03:05:24.356922+00', '2026-06-11 03:05:23.706261+00', '2026-06-11 03:05:24.356922+00', 7, 7, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (83, 7, 'BUY_SEED', 'buy_seed_1781147128173_310854', 'SUCCESS', '{"userId": 7, "seedName": "辣椒", "afterCoin": 3910, "unitPrice": 28, "beforeCoin": 4190, "seedTypeId": 6, "buyQuantity": 10, "totalCostCoin": 280, "afterSeedQuantity": 10, "beforeSeedQuantity": 0}', NULL, '2026-06-11 03:05:29.065346+00', '2026-06-11 03:05:28.388565+00', '2026-06-11 03:05:29.065346+00', 7, 7, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (64, 2, 'PLANT', 'plant_1781146290215_808770', 'SUCCESS', '{"cropId": 21, "plotId": 31, "userId": 2, "growStatus": 1, "seedTypeId": 5, "expectedRipeAt": "2026-06-11T10:54:13.678370062+08:00", "currentStageIndex": 1, "expectedWitheredAt": "2026-06-11T10:54:56.678370062+08:00", "remainSeedQuantity": 12}', NULL, '2026-06-11 02:51:30.932978+00', '2026-06-11 02:51:30.321301+00', '2026-06-11 02:51:30.932978+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (66, 2, 'HARVEST', 'harvest_1781146349169_405975', 'SUCCESS', '{"cropId": 17, "plotId": 7, "userId": 2, "scoreGain": 17, "seedTypeId": 1, "cropCleared": true, "currentScore": 5022, "bugCountAfter": 0, "bugCountBefore": 0, "experienceGain": 21, "nextGrowStatus": null, "nextStageIndex": null, "bugPenaltyPerBug": 1, "currentExperience": 2985, "harvestFruitNumber": 5, "nextExpectedRipeAt": null, "totalFruitQuantity": 5, "totalBugPenaltyFruit": 0, "baseHarvestFruitNumber": 5, "nextExpectedWitheredAt": null}', NULL, '2026-06-11 02:52:29.93797+00', '2026-06-11 02:52:29.28334+00', '2026-06-11 02:52:29.93797+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (68, 2, 'HARVEST', 'harvest_1781146459174_766342', 'SUCCESS', '{"cropId": 16, "plotId": 12, "userId": 2, "scoreGain": 18, "seedTypeId": 2, "cropCleared": false, "currentScore": 5078, "bugCountAfter": 0, "bugCountBefore": 0, "experienceGain": 22, "nextGrowStatus": 1, "nextStageIndex": 3, "bugPenaltyPerBug": 1, "currentExperience": 3063, "harvestFruitNumber": 3, "nextExpectedRipeAt": "2026-06-11T10:55:47.484014218+08:00", "totalFruitQuantity": 3, "totalBugPenaltyFruit": 0, "baseHarvestFruitNumber": 3, "nextExpectedWitheredAt": "2026-06-11T10:57:02.484014218+08:00"}', NULL, '2026-06-11 02:54:19.932191+00', '2026-06-11 02:54:19.282985+00', '2026-06-11 02:54:19.932191+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (69, 2, 'HARVEST', 'harvest_1781146461030_786118', 'SUCCESS', '{"cropId": 21, "plotId": 31, "userId": 2, "scoreGain": 28, "seedTypeId": 5, "cropCleared": false, "currentScore": 5106, "bugCountAfter": 0, "bugCountBefore": 0, "experienceGain": 36, "nextGrowStatus": 1, "nextStageIndex": 4, "bugPenaltyPerBug": 1, "currentExperience": 3099, "harvestFruitNumber": 5, "nextExpectedRipeAt": "2026-06-11T10:55:11.402818263+08:00", "totalFruitQuantity": 28, "totalBugPenaltyFruit": 0, "baseHarvestFruitNumber": 5, "nextExpectedWitheredAt": "2026-06-11T10:55:55.402818263+08:00"}', NULL, '2026-06-11 02:54:21.915742+00', '2026-06-11 02:54:21.139285+00', '2026-06-11 02:54:21.915742+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (70, 2, 'HARVEST', 'harvest_1781146464099_580654', 'SUCCESS', '{"cropId": 19, "plotId": 9, "userId": 2, "scoreGain": 28, "seedTypeId": 5, "cropCleared": false, "currentScore": 5134, "bugCountAfter": 0, "bugCountBefore": 0, "experienceGain": 36, "nextGrowStatus": 1, "nextStageIndex": 4, "bugPenaltyPerBug": 1, "currentExperience": 3135, "harvestFruitNumber": 5, "nextExpectedRipeAt": "2026-06-11T10:55:04.462591768+08:00", "totalFruitQuantity": 33, "totalBugPenaltyFruit": 0, "baseHarvestFruitNumber": 5, "nextExpectedWitheredAt": "2026-06-11T10:55:39.462591768+08:00"}', NULL, '2026-06-11 02:54:24.942337+00', '2026-06-11 02:54:24.204601+00', '2026-06-11 02:54:24.942337+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (72, 2, 'CLEAR', 'clear_1781146824535_321978', 'SUCCESS', '{"cropId": 18, "plotId": 8, "userId": 2, "cleared": true, "clearedAt": "2026-06-11T11:00:25.0933132+08:00", "seedTypeId": 3, "bugCountBefore": 0, "growStatusBefore": 3, "stageIndexBefore": 6}', NULL, '2026-06-11 03:00:25.374751+00', '2026-06-11 03:00:24.763741+00', '2026-06-11 03:00:25.374751+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (73, 2, 'CLEAR', 'clear_1781146825655_418769', 'SUCCESS', '{"cropId": 19, "plotId": 9, "userId": 2, "cleared": true, "clearedAt": "2026-06-11T11:00:26.1149409+08:00", "seedTypeId": 5, "bugCountBefore": 0, "growStatusBefore": 3, "stageIndexBefore": 6}', NULL, '2026-06-11 03:00:26.312755+00', '2026-06-11 03:00:25.859789+00', '2026-06-11 03:00:26.312755+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (74, 2, 'CLEAR', 'clear_1781146826873_462394', 'SUCCESS', '{"cropId": 20, "plotId": 10, "userId": 2, "cleared": true, "clearedAt": "2026-06-11T11:00:27.28474+08:00", "seedTypeId": 7, "bugCountBefore": 0, "growStatusBefore": 3, "stageIndexBefore": 7}', NULL, '2026-06-11 03:00:27.486877+00', '2026-06-11 03:00:27.047888+00', '2026-06-11 03:00:27.486877+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (75, 2, 'CLEAR', 'clear_1781146828421_283548', 'SUCCESS', '{"cropId": 22, "plotId": 11, "userId": 2, "cleared": true, "clearedAt": "2026-06-11T11:00:28.7895247+08:00", "seedTypeId": 5, "bugCountBefore": 0, "growStatusBefore": 3, "stageIndexBefore": 6}', NULL, '2026-06-11 03:00:29.01184+00', '2026-06-11 03:00:28.568008+00', '2026-06-11 03:00:29.01184+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (76, 2, 'CLEAR', 'clear_1781146830252_315593', 'SUCCESS', '{"cropId": 21, "plotId": 31, "userId": 2, "cleared": true, "clearedAt": "2026-06-11T11:00:30.8580324+08:00", "seedTypeId": 5, "bugCountBefore": 0, "growStatusBefore": 3, "stageIndexBefore": 6}', NULL, '2026-06-11 03:00:31.045734+00', '2026-06-11 03:00:30.404834+00', '2026-06-11 03:00:31.045734+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (77, 2, 'CLEAR', 'clear_1781146835239_112067', 'SUCCESS', '{"cropId": 16, "plotId": 12, "userId": 2, "cleared": true, "clearedAt": "2026-06-11T11:00:35.6083495+08:00", "seedTypeId": 2, "bugCountBefore": 0, "growStatusBefore": 3, "stageIndexBefore": 5}', NULL, '2026-06-11 03:00:35.832851+00', '2026-06-11 03:00:35.371861+00', '2026-06-11 03:00:35.832851+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (81, 7, 'BUY_SEED', 'buy_seed_1781147120392_905242', 'SUCCESS', '{"userId": 7, "seedName": "茄子", "afterCoin": 4370, "unitPrice": 16, "beforeCoin": 4530, "seedTypeId": 4, "buyQuantity": 10, "totalCostCoin": 160, "afterSeedQuantity": 10, "beforeSeedQuantity": 0}', NULL, '2026-06-11 03:05:21.146027+00', '2026-06-11 03:05:20.588507+00', '2026-06-11 03:05:21.146027+00', 7, 7, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (84, 7, 'BUY_SEED', 'buy_seed_1781147131068_323152', 'SUCCESS', '{"userId": 7, "seedName": "钻石", "afterCoin": 3550, "unitPrice": 36, "beforeCoin": 3910, "seedTypeId": 7, "buyQuantity": 10, "totalCostCoin": 360, "afterSeedQuantity": 10, "beforeSeedQuantity": 0}', NULL, '2026-06-11 03:05:31.914573+00', '2026-06-11 03:05:31.269345+00', '2026-06-11 03:05:31.914573+00', 7, 7, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (85, 7, 'PLANT', 'plant_1781147153336_629761', 'SUCCESS', '{"cropId": 23, "plotId": 39, "userId": 7, "growStatus": 1, "seedTypeId": 1, "expectedRipeAt": "2026-06-11T11:07:30.906755498+08:00", "currentStageIndex": 1, "expectedWitheredAt": "2026-06-11T11:08:08.906755498+08:00", "remainSeedQuantity": 9}', NULL, '2026-06-11 03:05:54.156227+00', '2026-06-11 03:05:53.458708+00', '2026-06-11 03:05:54.156227+00', 7, 7, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (86, 7, 'PLANT', 'plant_1781147158058_803001', 'SUCCESS', '{"cropId": 24, "plotId": 40, "userId": 7, "growStatus": 1, "seedTypeId": 2, "expectedRipeAt": "2026-06-11T11:08:43.540211317+08:00", "currentStageIndex": 1, "expectedWitheredAt": "2026-06-11T11:09:43.540211317+08:00", "remainSeedQuantity": 9}', NULL, '2026-06-11 03:05:58.812212+00', '2026-06-11 03:05:58.172659+00', '2026-06-11 03:05:58.812212+00', 7, 7, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (87, 7, 'HARVEST', 'harvest_1781147243592_20305', 'SUCCESS', '{"cropId": 23, "plotId": 39, "userId": 7, "scoreGain": 17, "seedTypeId": 1, "cropCleared": false, "currentScore": 3017, "bugCountAfter": 0, "bugCountBefore": 0, "experienceGain": 21, "nextGrowStatus": 1, "nextStageIndex": 3, "bugPenaltyPerBug": 1, "currentExperience": 3021, "harvestFruitNumber": 5, "nextExpectedRipeAt": "2026-06-11T11:08:09.006643222+08:00", "totalFruitQuantity": 5, "totalBugPenaltyFruit": 0, "baseHarvestFruitNumber": 5, "nextExpectedWitheredAt": "2026-06-11T11:08:47.006643222+08:00"}', NULL, '2026-06-11 03:07:24.584394+00', '2026-06-11 03:07:23.759092+00', '2026-06-11 03:07:24.584394+00', 7, 7, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (91, 7, 'PLANT', 'plant_1781147290867_417983', 'SUCCESS', '{"cropId": 28, "plotId": 44, "userId": 7, "growStatus": 1, "seedTypeId": 6, "expectedRipeAt": "2026-06-11T11:11:28.325238933+08:00", "currentStageIndex": 1, "expectedWitheredAt": "2026-06-11T11:11:28.325238933+08:00", "remainSeedQuantity": 9}', NULL, '2026-06-11 03:08:11.632388+00', '2026-06-11 03:08:10.99198+00', '2026-06-11 03:08:11.632388+00', 7, 7, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (95, 7, 'CLEAR', 'clear_1781147350445_411744', 'SUCCESS', '{"cropId": 24, "plotId": 40, "userId": 7, "cleared": true, "clearedAt": "2026-06-11T11:09:10.776501545+08:00", "seedTypeId": 2, "bugCountBefore": 0, "growStatusBefore": 1, "stageIndexBefore": 3}', NULL, '2026-06-11 03:09:10.973915+00', '2026-06-11 03:09:10.568283+00', '2026-06-11 03:09:10.973915+00', 7, 7, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (96, 7, 'SELL_FRUIT', 'store_sell_fruit_1781147362360_1833', 'SUCCESS', '{"userId": 7, "seedName": "土豆", "afterCoin": 2495, "beforeCoin": 2480, "seedTypeId": 1, "sellQuantity": 1, "unitFruitPrice": 15, "totalIncomeCoin": 15, "afterFruitQuantity": 9, "beforeFruitQuantity": 10}', NULL, '2026-06-11 03:09:23.063455+00', '2026-06-11 03:09:22.491162+00', '2026-06-11 03:09:23.063455+00', 7, 7, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (88, 7, 'PLANT', 'plant_1781147277336_768956', 'SUCCESS', '{"cropId": 25, "plotId": 41, "userId": 7, "growStatus": 1, "seedTypeId": 3, "expectedRipeAt": "2026-06-11T11:10:52.919901868+08:00", "currentStageIndex": 1, "expectedWitheredAt": "2026-06-11T11:11:47.919901868+08:00", "remainSeedQuantity": 9}', NULL, '2026-06-11 03:07:58.266377+00', '2026-06-11 03:07:57.495955+00', '2026-06-11 03:07:58.266377+00', 7, 7, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (89, 7, 'PLANT', 'plant_1781147281462_120547', 'SUCCESS', '{"cropId": 26, "plotId": 42, "userId": 7, "growStatus": 1, "seedTypeId": 4, "expectedRipeAt": "2026-06-11T11:10:42.00170415+08:00", "currentStageIndex": 1, "expectedWitheredAt": "2026-06-11T11:11:24.00170415+08:00", "remainSeedQuantity": 9}', NULL, '2026-06-11 03:08:02.358294+00', '2026-06-11 03:08:01.5881+00', '2026-06-11 03:08:02.358294+00', 7, 7, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (90, 7, 'PLANT', 'plant_1781147285554_337910', 'SUCCESS', '{"cropId": 27, "plotId": 43, "userId": 7, "growStatus": 1, "seedTypeId": 5, "expectedRipeAt": "2026-06-11T11:10:16.101864465+08:00", "currentStageIndex": 1, "expectedWitheredAt": "2026-06-11T11:10:51.101864465+08:00", "remainSeedQuantity": 9}', NULL, '2026-06-11 03:08:06.40046+00', '2026-06-11 03:08:05.655263+00', '2026-06-11 03:08:06.40046+00', 7, 7, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (92, 7, 'PLANT', 'plant_1781147295726_174366', 'SUCCESS', '{"cropId": 29, "plotId": 45, "userId": 7, "growStatus": 1, "seedTypeId": 7, "expectedRipeAt": "2026-06-11T11:12:01.260668093+08:00", "currentStageIndex": 1, "expectedWitheredAt": "2026-06-11T11:14:19.260668093+08:00", "remainSeedQuantity": 9}', NULL, '2026-06-11 03:08:16.565874+00', '2026-06-11 03:08:15.825993+00', '2026-06-11 03:08:16.565874+00', 7, 7, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (93, 7, 'HARVEST', 'harvest_1781147300142_541108', 'SUCCESS', '{"cropId": 23, "plotId": 39, "userId": 7, "scoreGain": 17, "seedTypeId": 1, "cropCleared": true, "currentScore": 3039, "bugCountAfter": 0, "bugCountBefore": 0, "experienceGain": 21, "nextGrowStatus": null, "nextStageIndex": null, "bugPenaltyPerBug": 1, "currentExperience": 3052, "harvestFruitNumber": 5, "nextExpectedRipeAt": null, "totalFruitQuantity": 10, "totalBugPenaltyFruit": 0, "baseHarvestFruitNumber": 5, "nextExpectedWitheredAt": null}', NULL, '2026-06-11 03:08:21.088445+00', '2026-06-11 03:08:20.273096+00', '2026-06-11 03:08:21.088445+00', 7, 7, NULL, 1, false, 1);
INSERT INTO farm.request_idempotencies (id, user_id, biz_type, request_id, process_status, response_payload, error_message, finished_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (94, 7, 'HARVEST', 'harvest_1781147317309_211960', 'SUCCESS', '{"cropId": 24, "plotId": 40, "userId": 7, "scoreGain": 18, "seedTypeId": 2, "cropCleared": false, "currentScore": 3062, "bugCountAfter": 0, "bugCountBefore": 0, "experienceGain": 22, "nextGrowStatus": 1, "nextStageIndex": 3, "bugPenaltyPerBug": 1, "currentExperience": 3084, "harvestFruitNumber": 3, "nextExpectedRipeAt": "2026-06-11T11:09:47.671303507+08:00", "totalFruitQuantity": 3, "totalBugPenaltyFruit": 0, "baseHarvestFruitNumber": 3, "nextExpectedWitheredAt": "2026-06-11T11:10:47.671303507+08:00"}', NULL, '2026-06-11 03:08:38.173496+00', '2026-06-11 03:08:37.42027+00', '2026-06-11 03:08:38.173496+00', 7, 7, NULL, 1, false, 1);


--
-- TOC entry 3784 (class 0 OID 29324)
-- Dependencies: 234
-- Data for Name: seed_growth_stages; Type: TABLE DATA; Schema: farm; Owner: -
--

INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (9, 3, 2, 2, 35, '/oss/seed-stage/2026/06/10/1781053100131_32fbdd62e71f49ec849bcbb9282131a5.png', 0.1400, 102, 126, 115, 260, '2026-06-09 18:35:46.794308+00', '2026-06-10 00:58:23.636722+00', NULL, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (13, 1, 3, 2, 30, '/oss/seed-stage/2026/06/10/1781052615341_38a131ccf36e48eaa8840b756f35e8d5.png', 0.1200, 94, 122, 118, 281, '2026-06-09 18:35:46.794308+00', '2026-06-10 00:50:22.109864+00', NULL, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (35, 1, 8, 5, 0, '/oss/.defaults/seed/seed-stage-withered-default.png', 0.0000, 102, 136, 108, 286, '2026-06-09 18:35:46.794308+00', '2026-06-11 03:04:34.607829+00', NULL, 0, NULL, 1, false, 2);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (28, 1, 7, 4, 38, '/oss/seed-stage/2026/06/10/1781052664264_4f4830e6803f4749879812f054d2348e.png', 0.1500, 102, 136, 116, 235, '2026-06-09 18:35:46.794308+00', '2026-06-10 00:51:14.08534+00', NULL, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (14, 3, 3, 3, 45, '/oss/seed-stage/2026/06/10/1781053117736_77a638128e384acf8fb81abac60f2f84.png', 0.2000, 110, 136, 108, 252, '2026-06-09 18:35:46.794308+00', '2026-06-10 00:58:41.633007+00', NULL, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (16, 2, 3, 2, 55, '/oss/seed-stage/2026/06/10/1781052825100_302673d5ce9744a082e5f87dd6c7a5d4.png', 0.1800, 102, 136, 105, 216, '2026-06-09 18:35:46.794308+00', '2026-06-10 00:53:51.407793+00', NULL, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (21, 2, 4, 3, 70, '/oss/seed-stage/2026/06/10/1781052854669_b2ea8710d6b4480b8bf859eca8998318.png', 0.2800, 110, 148, 120, 193, '2026-06-09 18:35:46.794308+00', '2026-06-10 00:54:24.822901+00', NULL, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (32, 2, 7, 4, 60, '/oss/seed-stage/2026/06/10/1781052873094_ad7a97c6422f425eb5959a083c4e0de4.png', 0.2200, 118, 156, 113, 195, '2026-06-09 18:35:46.794308+00', '2026-06-10 00:54:38.227687+00', NULL, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (39, 2, 8, 5, 0, '/oss/.defaults/seed/seed-stage-withered-default.png', 0.0000, 118, 156, 105, 279, '2026-06-09 18:35:46.794308+00', '2026-06-10 00:54:43.760758+00', NULL, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (1, 1, 1, 1, 22, '/oss/seed-stage/2026/06/10/1781053053475_27d0d5e6de804ea39d9994e684e8959e.png', 0.1000, 88, 114, 119, 303, '2026-06-09 18:35:46.794308+00', '2026-06-10 00:57:35.724297+00', NULL, 0, NULL, 1, false, 2);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (5, 2, 1, 1, 40, '/oss/seed-stage/2026/06/10/1781053066146_4586db9fbbb447dab8446ea2cff656fa.png', 0.1000, 94, 124, 118, 283, '2026-06-09 18:35:46.794308+00', '2026-06-10 00:57:49.529625+00', NULL, 0, NULL, 1, false, 2);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (3, 3, 1, 1, 30, '/oss/seed-stage/2026/06/10/1781053077364_82e48b50e72f4f22a75e12be2869835d.png', 0.0090, 98, 122, 117, 280, '2026-06-09 18:35:46.794308+00', '2026-06-10 00:58:00.480032+00', NULL, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (19, 3, 4, 4, 65, '/oss/seed-stage/2026/06/10/1781053129949_be4991b852784f2c9729d858060d1f8b.png', 0.3000, 120, 150, 97, 265, '2026-06-09 18:35:46.794308+00', '2026-06-10 00:58:57.427901+00', NULL, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (30, 3, 7, 5, 55, '/oss/seed-stage/2026/06/10/1781053143869_90419c6f0e6d4bce9b4dc2dc97e83636.png', 0.2400, 126, 156, 91, 256, '2026-06-09 18:35:46.794308+00', '2026-06-10 00:59:12.307331+00', NULL, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (37, 3, 8, 6, 0, '/oss/.defaults/seed/seed-stage-withered-default.png', 0.0000, 126, 156, 90, 269, '2026-06-09 18:35:46.794308+00', '2026-06-10 00:59:17.646391+00', NULL, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (6, 4, 1, 1, 30, '/oss/seed-stage/2026/06/10/1781053660686_0a954b00d6b44eb290ef32e387d1c493.png', 0.0100, 94, 122, 113, 281, '2026-06-09 18:35:46.794308+00', '2026-06-10 01:07:44.730878+00', NULL, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (11, 4, 2, 2, 35, '/oss/seed-stage/2026/06/10/1781053676119_16ad80e763494a52bd94716bfbf73377.png', 0.1600, 98, 128, 112, 232, '2026-06-09 18:35:46.794308+00', '2026-06-10 01:08:03.201855+00', NULL, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (26, 4, 5, 4, 50, '/oss/seed-stage/2026/06/10/1781053807554_bd222fa93f5d45c0b2f4404e36fd8673.png', 0.3000, 110, 142, 107, 244, '2026-06-09 18:35:46.794308+00', '2026-06-10 01:10:11.844279+00', NULL, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (22, 4, 4, 3, 45, '/oss/seed-stage/2026/06/10/1781053791229_48aaffff14be411ab43beafda62241de.png', 0.2400, 104, 136, 109, 264, '2026-06-09 18:35:46.794308+00', '2026-06-10 01:09:56.979963+00', NULL, 0, NULL, 1, false, 2);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (33, 4, 7, 5, 42, '/oss/seed-stage/2026/06/10/1781053820246_8c62a6f7f93043b6997f3d2e52d4c285.png', 0.2200, 114, 146, 102, 240, '2026-06-09 18:35:46.794308+00', '2026-06-10 01:10:23.937347+00', NULL, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (40, 4, 8, 6, 0, '/oss/.defaults/seed/seed-stage-withered-default.png', 0.0000, 114, 146, 100, 288, '2026-06-09 18:35:46.794308+00', '2026-06-10 01:10:30.976876+00', NULL, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (7, 5, 1, 1, 25, '/oss/seed-stage/2026/06/10/1781053886642_91b2fb6970d445af8246bb030a16266c.png', 0.0800, 92, 120, 115, 286, '2026-06-09 18:35:46.794308+00', '2026-06-10 01:11:33.522223+00', NULL, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (12, 5, 2, 2, 30, '/oss/seed-stage/2026/06/10/1781053902746_1211bb3ea1c84132a4db0e066819e74b.png', 0.1400, 95, 126, 111, 253, '2026-06-09 18:35:46.794308+00', '2026-06-10 01:11:48.209299+00', NULL, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (25, 5, 3, 3, 35, '/oss/seed-stage/2026/06/10/1781053936449_d5056784c5b24d709c11a66295f07816.png', 0.2000, 102, 132, 107, 261, '2026-06-09 18:35:46.794308+00', '2026-06-10 01:12:22.567222+00', NULL, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (27, 5, 5, 4, 40, '/oss/seed-stage/2026/06/10/1781053949888_19f082d5ed6043f3953af996aa333db5.png', 0.2600, 108, 138, 109, 230, '2026-06-09 18:35:46.794308+00', '2026-06-10 01:12:37.78342+00', NULL, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (34, 5, 7, 5, 35, '/oss/seed-stage/2026/06/10/1781053965161_a86fa54660d14cd298cf0424238c4e24.png', 0.1800, 112, 142, 111, 229, '2026-06-09 18:35:46.794308+00', '2026-06-10 01:12:50.367389+00', NULL, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (41, 5, 8, 6, 0, '/oss/.defaults/seed/seed-stage-withered-default.png', 0.0000, 112, 142, 106, 285, '2026-06-09 18:35:46.794308+00', '2026-06-10 01:12:57.283998+00', NULL, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (2, 6, 1, 1, 28, '/oss/seed-stage/2026/06/10/1781054039222_71b839e6b6ab42c2b4c734d2740cb769.png', 0.1000, 90, 118, 118, 286, '2026-06-09 18:35:46.794308+00', '2026-06-10 01:14:08.716261+00', NULL, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (8, 6, 2, 2, 32, '/oss/seed-stage/2026/06/10/1781054058677_0ae8262749c94268915b0d5187df5506.png', 0.1800, 94, 124, 109, 261, '2026-06-09 18:35:46.794308+00', '2026-06-10 01:14:22.42125+00', NULL, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (18, 6, 3, 3, 42, '/oss/seed-stage/2026/06/10/1781054072445_3cd8aa83a5154ba4874b1f855f1bcae9.png', 0.2600, 100, 132, 108, 256, '2026-06-09 18:35:46.794308+00', '2026-06-10 01:14:38.420208+00', NULL, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (23, 6, 4, 4, 50, '/oss/seed-stage/2026/06/10/1781054086259_6fa7047c583a489b8b4872a55d842bba.png', 0.0320, 106, 138, 110, 234, '2026-06-09 18:35:46.794308+00', '2026-06-10 01:14:50.759337+00', NULL, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (38, 7, 8, 7, 0, '/oss/.defaults/seed/seed-stage-withered-default.png', 0.0000, 114, 146, 46, 138, '2026-06-09 18:35:46.794308+00', '2026-06-10 01:19:45.658481+00', NULL, 0, NULL, 1, true, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (29, 6, 5, 5, 45, '/oss/seed-stage/2026/06/10/1781054099806_205b1fd6e02844a6a52c228b0aec4e0f.png', 0.2400, 110, 144, 101, 247, '2026-06-09 18:35:46.794308+00', '2026-06-10 01:15:33.358628+00', NULL, 0, NULL, 1, false, 2);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (4, 7, 1, 1, 35, '/oss/seed-stage/2026/06/10/1781054416487_9614c7086b9a44fe8951fe556ded153c.png', 0.1000, 86, 116, 121, 275, '2026-06-09 18:35:46.794308+00', '2026-06-10 01:20:20.761114+00', NULL, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (10, 7, 2, 2, 40, '/oss/seed-stage/2026/06/10/1781054436496_773a0e05b0c344af847b89dd58fe1e14.png', 0.1600, 90, 122, 110, 265, '2026-06-09 18:35:46.794308+00', '2026-06-10 01:20:39.772289+00', NULL, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (15, 7, 3, 3, 50, '/oss/seed-stage/2026/06/10/1781054448480_2f8febce7cf54a0380c2cbf84474f12a.png', 0.2400, 96, 128, 107, 261, '2026-06-09 18:35:46.794308+00', '2026-06-10 01:20:51.923399+00', NULL, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (24, 7, 5, 5, 60, '/oss/seed-stage/2026/06/10/1781054471348_a8ea60a006824476a08b43618ef1d415.png', 0.3400, 110, 142, 106, 243, '2026-06-09 18:35:46.794308+00', '2026-06-10 01:21:14.763108+00', NULL, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (31, 7, 7, 6, 50, '/oss/seed-stage/2026/06/10/1781054484078_0eb90bbd712b44439d1c077b7ac94070.png', 0.2200, 114, 146, 101, 227, '2026-06-09 18:35:46.794308+00', '2026-06-10 01:21:27.944496+00', NULL, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (17, 1, 4, 3, 45, '/oss/seed-stage/2026/06/10/1781052630370_03ec0e9e0e5d4f56a2ca30c4dd33582c.png', 0.1800, 98, 130, 119, 265, '2026-06-09 18:35:46.794308+00', '2026-06-11 03:04:28.225406+00', NULL, 0, NULL, 1, false, 2);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (36, 6, 7, 6, 0, '/oss/seed-stage/2026/06/10/1781054120244_5d097c9a029b4b59837d47222b81a5e2.png', 0.2000, 110, 144, 100, 247, '2026-06-09 18:35:46.794308+00', '2026-06-10 01:15:28.274281+00', NULL, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (44, 6, 8, 7, 30, '/oss/seed-stage/2026/06/10/1781054145957_595c49a5e4314526b24085f0334565dc.png', 0.0000, 100, 120, 110, 280, '2026-06-10 01:15:47.790561+00', '2026-06-10 01:15:47.790561+00', 0, 0, NULL, 1, false, 0);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (20, 7, 4, 4, 55, '/oss/seed-stage/2026/06/10/1781054461028_79de44e2cc814802ba263238aa495fb8.png', 0.3000, 104, 136, 106, 254, '2026-06-09 18:35:46.794308+00', '2026-06-10 01:21:04.668671+00', NULL, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (45, 7, 8, 7, 30, '/oss/seed-stage/2026/06/10/1781054496451_ef87a59b7ceb47e7a5eedbc9885ca2af.png', 0.0000, 100, 120, 110, 280, '2026-06-10 01:19:59.82717+00', '2026-06-10 01:21:37.958395+00', 0, 0, NULL, 1, false, 1);
INSERT INTO farm.seed_growth_stages (id, seed_type_id, growth_stage_id, stage_index, duration_seconds, asset_url, bug_probability, width, height, offset_x, offset_y, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (46, 19, 1, 1, 30, '/oss/seed-stage/2026/06/10/1781060534203_fdad1e0680c64e109e04d08c36a61cd4.png', 0.0000, 100, 120, 110, 280, '2026-06-10 03:02:16.908624+00', '2026-06-10 03:03:19.474432+00', 0, 0, NULL, 1, true, 1);


--
-- TOC entry 3774 (class 0 OID 29230)
-- Dependencies: 224
-- Data for Name: seed_qualities; Type: TABLE DATA; Schema: farm; Owner: -
--

INSERT INTO farm.seed_qualities (id, name, description, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (1, '普通', '普通品质种子', '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.seed_qualities (id, name, description, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (2, '优质', '优质品质种子', '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.seed_qualities (id, name, description, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (3, '稀有', '稀有品质种子', '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);


--
-- TOC entry 3782 (class 0 OID 29297)
-- Dependencies: 232
-- Data for Name: seed_types; Type: TABLE DATA; Schema: farm; Owner: -
--

INSERT INTO farm.seed_types (id, name, cover_image_url, seed_quality_id, enable_soil_type_bits, level, unlock_experience_required, description, max_bug_limit, max_harvest_count, regrow_stage_index, harvest_stage_index, price, harvest_experience, harvest_fruit_number, fruit_loss_per_bug, bug_kill_coin_reward, bug_kill_experience_reward, bug_kill_score_reward, harvest_score, fruit_price, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (7, '钻石', '/oss/seed-cover/2026/06/10/1781054370654_610232d6038d431f8fbcb824fb4e448b.png', 3, 15, 3, 1820, '成长慢但单价高，可连续采收', 5, 7, 4, 5, 36, 68, 6, 1, 0, 10, 5, 62, 40, '2026-06-09 18:35:46.794308+00', '2026-06-11 02:05:39.424644+00', NULL, 0, NULL, 1, false, 8);
INSERT INTO farm.seed_types (id, name, cover_image_url, seed_quality_id, enable_soil_type_bits, level, unlock_experience_required, description, max_bug_limit, max_harvest_count, regrow_stage_index, harvest_stage_index, price, harvest_experience, harvest_fruit_number, fruit_loss_per_bug, bug_kill_coin_reward, bug_kill_experience_reward, bug_kill_score_reward, harvest_score, fruit_price, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (28, '1', '/oss/.defaults/seed/seed-cover-default.png', 1, 1, 1, 1, '', 1, 1, NULL, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, '2026-06-11 02:28:32.937956+00', '2026-06-11 02:28:38.980978+00', 0, 0, NULL, 1, true, 2);
INSERT INTO farm.seed_types (id, name, cover_image_url, seed_quality_id, enable_soil_type_bits, level, unlock_experience_required, description, max_bug_limit, max_harvest_count, regrow_stage_index, harvest_stage_index, price, harvest_experience, harvest_fruit_number, fruit_loss_per_bug, bug_kill_coin_reward, bug_kill_experience_reward, bug_kill_score_reward, harvest_score, fruit_price, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (1, '土豆', '/oss/seed-cover/2026/06/10/1781052581672_cf019b74ce7048de83f4a1be02f87806.png', 1, 9, 1, 0, '基础粮作，前中期成长快', 3, 2, 3, 4, 11, 21, 5, 1, 0, 10, 5, 17, 15, '2026-06-09 18:35:46.794308+00', '2026-06-11 03:03:58.06116+00', NULL, 0, NULL, 1, false, 16);
INSERT INTO farm.seed_types (id, name, cover_image_url, seed_quality_id, enable_soil_type_bits, level, unlock_experience_required, description, max_bug_limit, max_harvest_count, regrow_stage_index, harvest_stage_index, price, harvest_experience, harvest_fruit_number, fruit_loss_per_bug, bug_kill_coin_reward, bug_kill_experience_reward, bug_kill_score_reward, harvest_score, fruit_price, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (6, '辣椒', '/oss/seed-cover/2026/06/10/1781054012691_0bb9f37410864d96bf5941c29fd8e0f2.png', 3, 15, 3, 1680, '可再生采收，风险和收益并存', 5, 5, 5, 6, 28, 55, 4, 1, 0, 10, 5, 50, 33, '2026-06-09 18:35:46.794308+00', '2026-06-10 01:28:53.15867+00', NULL, 0, NULL, 1, false, 6);
INSERT INTO farm.seed_types (id, name, cover_image_url, seed_quality_id, enable_soil_type_bits, level, unlock_experience_required, description, max_bug_limit, max_harvest_count, regrow_stage_index, harvest_stage_index, price, harvest_experience, harvest_fruit_number, fruit_loss_per_bug, bug_kill_coin_reward, bug_kill_experience_reward, bug_kill_score_reward, harvest_score, fruit_price, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (2, '玉米', '/oss/seed-cover/2026/06/10/1781052725566_917b29a17efc4138a1f54c4cf89f2f21.png', 1, 11, 1, 120, '可多次收获', 3, 2, 3, 4, 12, 22, 3, 1, 0, 10, 5, 18, 16, '2026-06-09 18:35:46.794308+00', '2026-06-10 01:30:57.527968+00', NULL, 0, NULL, 1, false, 8);
INSERT INTO farm.seed_types (id, name, cover_image_url, seed_quality_id, enable_soil_type_bits, level, unlock_experience_required, description, max_bug_limit, max_harvest_count, regrow_stage_index, harvest_stage_index, price, harvest_experience, harvest_fruit_number, fruit_loss_per_bug, bug_kill_coin_reward, bug_kill_experience_reward, bug_kill_score_reward, harvest_score, fruit_price, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (3, '西瓜', '/oss/seed-cover/2026/06/10/1781052941852_aa0944ff1a7149e49bfd8c5a4945380c.png', 2, 11, 2, 820, '成熟耗时较长，单次收益高', 4, 3, 4, 5, 24, 48, 6, 1, 0, 10, 5, 42, 30, '2026-06-09 18:35:46.794308+00', '2026-06-10 01:31:02.974127+00', NULL, 0, NULL, 1, false, 5);
INSERT INTO farm.seed_types (id, name, cover_image_url, seed_quality_id, enable_soil_type_bits, level, unlock_experience_required, description, max_bug_limit, max_harvest_count, regrow_stage_index, harvest_stage_index, price, harvest_experience, harvest_fruit_number, fruit_loss_per_bug, bug_kill_coin_reward, bug_kill_experience_reward, bug_kill_score_reward, harvest_score, fruit_price, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (4, '茄子', '/oss/seed-cover/2026/06/10/1781053647381_a2366d32ee784cb99ab080e503a3dcc5.png', 2, 11, 2, 820, '稳定产出，适合中期', 4, 1, NULL, 5, 16, 30, 4, 1, 0, 10, 5, 24, 20, '2026-06-09 18:35:46.794308+00', '2026-06-10 01:31:08.419255+00', NULL, 0, NULL, 1, false, 4);
INSERT INTO farm.seed_types (id, name, cover_image_url, seed_quality_id, enable_soil_type_bits, level, unlock_experience_required, description, max_bug_limit, max_harvest_count, regrow_stage_index, harvest_stage_index, price, harvest_experience, harvest_fruit_number, fruit_loss_per_bug, bug_kill_coin_reward, bug_kill_experience_reward, bug_kill_score_reward, harvest_score, fruit_price, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (5, '草莓', '/oss/seed-cover/2026/06/10/1781053870559_2203b2e800ec4246abd38029cc62156a.png', 2, 15, 2, 820, '甜度高，可多次结果', 4, 6, 4, 5, 18, 36, 5, 1, 0, 10, 5, 28, 22, '2026-06-09 18:35:46.794308+00', '2026-06-10 01:31:20.554517+00', NULL, 0, NULL, 1, false, 6);
INSERT INTO farm.seed_types (id, name, cover_image_url, seed_quality_id, enable_soil_type_bits, level, unlock_experience_required, description, max_bug_limit, max_harvest_count, regrow_stage_index, harvest_stage_index, price, harvest_experience, harvest_fruit_number, fruit_loss_per_bug, bug_kill_coin_reward, bug_kill_experience_reward, bug_kill_score_reward, harvest_score, fruit_price, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (19, '大蒜', '/oss/seed-cover/2026/06/10/1781060483669_5b0c4fa609cc4b5b870e92a531259fa1.png', 1, 1, 10, 100, '大蒜', 3, 5, NULL, NULL, 100, 10, 5, 1, 10, 10, 10, 10, 10, '2026-06-10 03:01:48.433995+00', '2026-06-10 03:03:31.681044+00', 0, 0, NULL, 1, true, 1);


--
-- TOC entry 3776 (class 0 OID 29244)
-- Dependencies: 226
-- Data for Name: soil_types; Type: TABLE DATA; Schema: farm; Owner: -
--

INSERT INTO farm.soil_types (id, name, bit_code, cover_image_url, level, unlock_experience_required, grow_speed_multiplier, expand_cost_coin, description, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (3, '金土地', 4, '/oss/soil-cover/2026/06/10/1781052046122_2f834d26d4b54099b37bd05f8144a883.png', 3, 2000, 0.60, 5000, '高级土地，适配高等级作物', '2026-06-09 18:35:46.794308+00', '2026-06-11 02:48:45.879308+00', 0, 0, NULL, 1, false, 2);
INSERT INTO farm.soil_types (id, name, bit_code, cover_image_url, level, unlock_experience_required, grow_speed_multiplier, expand_cost_coin, description, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (2, '黑土地', 2, '/oss/soil-cover/2026/06/10/1781052027533_340ab8e54e874342943e55d900559573.png', 2, 1000, 0.80, 2500, '生长速度更快的改良土地', '2026-06-09 18:35:46.794308+00', '2026-06-11 02:49:56.055087+00', 0, 0, NULL, 1, false, 5);
INSERT INTO farm.soil_types (id, name, bit_code, cover_image_url, level, unlock_experience_required, grow_speed_multiplier, expand_cost_coin, description, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (1, '黄土地', 1, '/oss/soil-cover/2026/06/10/1781052036713_09195dbc81114c4f89704ce07bb14e85.png', 1, 0, 1.00, 1500, '基础土地，适配多数作物', '2026-06-09 18:35:46.794308+00', '2026-06-11 02:50:03.727252+00', 0, 0, NULL, 1, false, 4);
INSERT INTO farm.soil_types (id, name, bit_code, cover_image_url, level, unlock_experience_required, grow_speed_multiplier, expand_cost_coin, description, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (4, '红土地', 8, '/oss/soil-cover/2026/06/10/1781052114756_e7d19464a75f431ba99230a1378b949c.png', 1, 0, 1.20, 1000, '贫瘠土壤，作物生长较慢', '2026-06-10 00:41:24.369945+00', '2026-06-11 02:50:08.278812+00', 0, 0, '', 1, false, 5);


--
-- TOC entry 3794 (class 0 OID 29411)
-- Dependencies: 244
-- Data for Name: user_asset_flows; Type: TABLE DATA; Schema: farm; Owner: -
--

INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (1, 2, 'COIN', 'EXPENSE', 10, 4900, 4890, 'BUY_SEED', '1:1781053450', '2026-06-10 01:04:10.274618+00', '{"seedTypeId": 1, "buyQuantity": 1}', '2026-06-10 01:04:10.274618+00', '2026-06-10 01:04:10.274618+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (2, 2, 'COIN', 'INCOME', 14, 4890, 4904, 'SELL_FRUIT', '1:1781053479', '2026-06-10 01:04:39.403165+00', '{"seedTypeId": 1, "sellQuantity": 1}', '2026-06-10 01:04:39.403165+00', '2026-06-10 01:04:39.403165+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (3, 2, 'COIN', 'INCOME', 14, 4904, 4918, 'SELL_FRUIT', '1:1781053486', '2026-06-10 01:04:46.010758+00', '{"seedTypeId": 1, "sellQuantity": 1}', '2026-06-10 01:04:46.010758+00', '2026-06-10 01:04:46.010758+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (4, 2, 'COIN', 'EXPENSE', 80, 4918, 4838, 'UNLOCK_PLOT', '10:1781054820', '2026-06-10 01:27:00.432869+00', '{"plotId": 10, "plotIndex": 4, "unlockCost": 80}', '2026-06-10 01:27:00.432869+00', '2026-06-10 01:27:00.432869+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (5, 2, 'COIN', 'EXPENSE', 120, 4838, 4718, 'UNLOCK_PLOT', '11:1781054823', '2026-06-10 01:27:03.431518+00', '{"plotId": 11, "plotIndex": 5, "unlockCost": 120}', '2026-06-10 01:27:03.431518+00', '2026-06-10 01:27:03.431518+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (6, 2, 'COIN', 'EXPENSE', 160, 4718, 4558, 'UNLOCK_PLOT', '12:1781054841', '2026-06-10 01:27:21.610421+00', '{"plotId": 12, "plotIndex": 6, "unlockCost": 160}', '2026-06-10 01:27:21.610421+00', '2026-06-10 01:27:21.610421+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (7, 2, 'EXPERIENCE', 'INCOME', 10, 2141, 2151, 'CARE', 'CARE:5:1781054900', '2026-06-10 01:28:20.145857+00', '{"cropId": 5, "plotId": 8, "bugRemoved": 1}', '2026-06-10 01:28:20.145857+00', '2026-06-10 01:28:20.145857+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (8, 2, 'SCORE', 'INCOME', 5, 733, 738, 'CARE', 'CARE:5:1781054900', '2026-06-10 01:28:20.145857+00', '{"cropId": 5, "plotId": 8, "bugRemoved": 1}', '2026-06-10 01:28:20.145857+00', '2026-06-10 01:28:20.145857+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (9, 2, 'EXPERIENCE', 'INCOME', 10, 2151, 2161, 'CARE', 'CARE:6:1781054901', '2026-06-10 01:28:21.275588+00', '{"cropId": 6, "plotId": 9, "bugRemoved": 1}', '2026-06-10 01:28:21.275588+00', '2026-06-10 01:28:21.275588+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (10, 2, 'SCORE', 'INCOME', 5, 738, 743, 'CARE', 'CARE:6:1781054901', '2026-06-10 01:28:21.275588+00', '{"cropId": 6, "plotId": 9, "bugRemoved": 1}', '2026-06-10 01:28:21.275588+00', '2026-06-10 01:28:21.275588+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (11, 2, 'EXPERIENCE', 'INCOME', 10, 2182, 2192, 'CARE', 'CARE:6:1781054947', '2026-06-10 01:29:07.341001+00', '{"cropId": 6, "plotId": 9, "bugRemoved": 1}', '2026-06-10 01:29:07.341001+00', '2026-06-10 01:29:07.341001+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (12, 2, 'SCORE', 'INCOME', 5, 760, 765, 'CARE', 'CARE:6:1781054947', '2026-06-10 01:29:07.341001+00', '{"cropId": 6, "plotId": 9, "bugRemoved": 1}', '2026-06-10 01:29:07.341001+00', '2026-06-10 01:29:07.341001+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (13, 2, 'COIN', 'INCOME', 165, 4558, 4723, 'SELL_FRUIT', '1:1781054964', '2026-06-10 01:29:24.73758+00', '{"seedTypeId": 1, "sellQuantity": 11}', '2026-06-10 01:29:24.73758+00', '2026-06-10 01:29:24.73758+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (14, 2, 'COIN', 'INCOME', 44, 4723, 4767, 'SELL_FRUIT', '5:1781054968', '2026-06-10 01:29:28.876493+00', '{"seedTypeId": 5, "sellQuantity": 2}', '2026-06-10 01:29:28.876493+00', '2026-06-10 01:29:28.876493+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (15, 2, 'COIN', 'EXPENSE', 72, 4767, 4695, 'BUY_SEED', '7:1781054978', '2026-06-10 01:29:38.925244+00', '{"seedTypeId": 7, "buyQuantity": 2}', '2026-06-10 01:29:38.925244+00', '2026-06-10 01:29:38.925244+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (16, 2, 'EXPERIENCE', 'INCOME', 10, 2228, 2238, 'CARE', 'CARE:8:1781054991', '2026-06-10 01:29:51.780685+00', '{"cropId": 8, "plotId": 10, "bugRemoved": 1}', '2026-06-10 01:29:51.780685+00', '2026-06-10 01:29:51.780685+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (17, 2, 'SCORE', 'INCOME', 5, 793, 798, 'CARE', 'CARE:8:1781054991', '2026-06-10 01:29:51.780685+00', '{"cropId": 8, "plotId": 10, "bugRemoved": 1}', '2026-06-10 01:29:51.780685+00', '2026-06-10 01:29:51.780685+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (18, 2, 'EXPERIENCE', 'INCOME', 10, 2322, 2332, 'CARE', 'CARE:11:1781055031', '2026-06-10 01:30:31.38106+00', '{"cropId": 11, "plotId": 7, "bugRemoved": 1}', '2026-06-10 01:30:31.38106+00', '2026-06-10 01:30:31.38106+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (19, 2, 'SCORE', 'INCOME', 5, 868, 873, 'CARE', 'CARE:11:1781055031', '2026-06-10 01:30:31.38106+00', '{"cropId": 11, "plotId": 7, "bugRemoved": 1}', '2026-06-10 01:30:31.38106+00', '2026-06-10 01:30:31.38106+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (20, 2, 'EXPERIENCE', 'INCOME', 10, 2332, 2342, 'CARE', 'CARE:9:1781055087', '2026-06-10 01:31:27.999146+00', '{"cropId": 9, "plotId": 12, "bugRemoved": 1}', '2026-06-10 01:31:27.999146+00', '2026-06-10 01:31:27.999146+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (21, 2, 'SCORE', 'INCOME', 5, 873, 878, 'CARE', 'CARE:9:1781055087', '2026-06-10 01:31:27.999146+00', '{"cropId": 9, "plotId": 12, "bugRemoved": 1}', '2026-06-10 01:31:27.999146+00', '2026-06-10 01:31:27.999146+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (22, 2, 'COIN', 'EXPENSE', 1500, 4695, 3195, 'EXPAND_PLOT', '31:1781055114', '2026-06-10 01:31:54.41517+00', '{"plotId": 31, "plotIndex": 7, "expandCost": 1500}', '2026-06-10 01:31:54.41517+00', '2026-06-10 01:31:54.41517+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (23, 2, 'COIN', 'EXPENSE', 200, 3195, 2995, 'UNLOCK_PLOT', '31:1781055123', '2026-06-10 01:32:03.67572+00', '{"plotId": 31, "plotIndex": 7, "unlockCost": 200}', '2026-06-10 01:32:03.67572+00', '2026-06-10 01:32:03.67572+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (24, 2, 'EXPERIENCE', 'INCOME', 10, 2570, 2580, 'CARE', 'CARE:8:1781055170', '2026-06-10 01:32:50.688323+00', '{"cropId": 8, "plotId": 10, "bugRemoved": 1}', '2026-06-10 01:32:50.688323+00', '2026-06-10 01:32:50.688323+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (25, 2, 'SCORE', 'INCOME', 5, 1074, 1079, 'CARE', 'CARE:8:1781055170', '2026-06-10 01:32:50.688323+00', '{"cropId": 8, "plotId": 10, "bugRemoved": 1}', '2026-06-10 01:32:50.688323+00', '2026-06-10 01:32:50.688323+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (26, 2, 'EXPERIENCE', 'INCOME', 10, 2944, 2954, 'CARE', 'CARE:12:1781055309', '2026-06-10 01:35:09.32671+00', '{"cropId": 12, "plotId": 9, "bugRemoved": 1}', '2026-06-10 01:35:09.32671+00', '2026-06-10 01:35:09.32671+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (27, 2, 'SCORE', 'INCOME', 5, 1399, 1404, 'CARE', 'CARE:12:1781055309', '2026-06-10 01:35:09.32671+00', '{"cropId": 12, "plotId": 9, "bugRemoved": 1}', '2026-06-10 01:35:09.32671+00', '2026-06-10 01:35:09.32671+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (28, 1, 'COIN', 'EXPENSE', 72, 5600, 5528, 'BUY_SEED', '7:1781111304', '2026-06-10 17:08:24.688302+00', '{"seedTypeId": 7, "buyQuantity": 2}', '2026-06-10 17:08:24.688302+00', '2026-06-10 17:08:24.688302+00', 1, 1, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (29, 1, 'COIN', 'EXPENSE', 80, 5528, 5448, 'UNLOCK_PLOT', '4:1781111342', '2026-06-10 17:09:02.735159+00', '{"plotId": 4, "plotIndex": 4, "unlockCost": 80}', '2026-06-10 17:09:02.735159+00', '2026-06-10 17:09:02.735159+00', 1, 1, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (30, 1, 'COIN', 'EXPENSE', 120, 5448, 5328, 'UNLOCK_PLOT', '5:1781111348', '2026-06-10 17:09:08.293034+00', '{"plotId": 5, "plotIndex": 5, "unlockCost": 120}', '2026-06-10 17:09:08.293034+00', '2026-06-10 17:09:08.293034+00', 1, 1, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (31, 1, 'COIN', 'EXPENSE', 160, 5328, 5168, 'UNLOCK_PLOT', '6:1781111352', '2026-06-10 17:09:12.719063+00', '{"plotId": 6, "plotIndex": 6, "unlockCost": 160}', '2026-06-10 17:09:12.719063+00', '2026-06-10 17:09:12.719063+00', 1, 1, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (32, 2, 'COIN', 'INCOME', 30, 2995, 3025, 'SELL_FRUIT', '3:1781143430', '2026-06-11 02:03:50.997408+00', '{"seedTypeId": 3, "sellQuantity": 1}', '2026-06-11 02:03:50.997408+00', '2026-06-11 02:03:50.997408+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (33, 2, 'COIN', 'EXPENSE', 36, 3025, 2989, 'BUY_SEED', '7:1781143480', '2026-06-11 02:04:40.766296+00', '{"seedTypeId": 7, "buyQuantity": 1}', '2026-06-11 02:04:40.766296+00', '2026-06-11 02:04:40.766296+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (34, 2, 'COIN', 'EXPENSE', 2500, 2989, 489, 'EXPAND_PLOT', '38:1781146337', '2026-06-11 02:52:17.235652+00', '{"plotId": 38, "plotIndex": 8, "expandCost": 2500}', '2026-06-11 02:52:17.235652+00', '2026-06-11 02:52:17.235652+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (35, 2, 'EXPERIENCE', 'INCOME', 10, 2954, 2964, 'CARE', 'CARE:22:1781146345', '2026-06-11 02:52:25.4699+00', '{"cropId": 22, "plotId": 11, "bugRemoved": 1}', '2026-06-11 02:52:25.4699+00', '2026-06-11 02:52:25.4699+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (36, 2, 'SCORE', 'INCOME', 5, 5000, 5005, 'CARE', 'CARE:22:1781146345', '2026-06-11 02:52:25.4699+00', '{"cropId": 22, "plotId": 11, "bugRemoved": 1}', '2026-06-11 02:52:25.4699+00', '2026-06-11 02:52:25.4699+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (37, 2, 'COIN', 'EXPENSE', 240, 489, 249, 'UNLOCK_PLOT', '38:1781146360', '2026-06-11 02:52:40.485005+00', '{"plotId": 38, "plotIndex": 8, "unlockCost": 240}', '2026-06-11 02:52:40.485005+00', '2026-06-11 02:52:40.485005+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (38, 2, 'EXPERIENCE', 'INCOME', 10, 3021, 3031, 'CARE', 'CARE:20:1781146447', '2026-06-11 02:54:07.13064+00', '{"cropId": 20, "plotId": 10, "bugRemoved": 1}', '2026-06-11 02:54:07.13064+00', '2026-06-11 02:54:07.13064+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (39, 2, 'SCORE', 'INCOME', 5, 5050, 5055, 'CARE', 'CARE:20:1781146447', '2026-06-11 02:54:07.13064+00', '{"cropId": 20, "plotId": 10, "bugRemoved": 1}', '2026-06-11 02:54:07.13064+00', '2026-06-11 02:54:07.13064+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (40, 2, 'EXPERIENCE', 'INCOME', 10, 3031, 3041, 'CARE', 'CARE:20:1781146449', '2026-06-11 02:54:09.064937+00', '{"cropId": 20, "plotId": 10, "bugRemoved": 1}', '2026-06-11 02:54:09.064937+00', '2026-06-11 02:54:09.064937+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (41, 2, 'SCORE', 'INCOME', 5, 5055, 5060, 'CARE', 'CARE:20:1781146449', '2026-06-11 02:54:09.064937+00', '{"cropId": 20, "plotId": 10, "bugRemoved": 1}', '2026-06-11 02:54:09.064937+00', '2026-06-11 02:54:09.064937+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (42, 2, 'COIN', 'INCOME', 480, 249, 729, 'SELL_FRUIT', '7:1781146478', '2026-06-11 02:54:38.807743+00', '{"seedTypeId": 7, "sellQuantity": 12}', '2026-06-11 02:54:38.807743+00', '2026-06-11 02:54:38.807743+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (43, 7, 'COIN', 'EXPENSE', 110, 5000, 4890, 'BUY_SEED', '1:1781147107', '2026-06-11 03:05:07.872216+00', '{"seedTypeId": 1, "buyQuantity": 10}', '2026-06-11 03:05:07.872216+00', '2026-06-11 03:05:07.872216+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (44, 7, 'COIN', 'EXPENSE', 120, 4890, 4770, 'BUY_SEED', '2:1781147112', '2026-06-11 03:05:12.9711+00', '{"seedTypeId": 2, "buyQuantity": 10}', '2026-06-11 03:05:12.9711+00', '2026-06-11 03:05:12.9711+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (45, 7, 'COIN', 'EXPENSE', 240, 4770, 4530, 'BUY_SEED', '3:1781147117', '2026-06-11 03:05:17.213595+00', '{"seedTypeId": 3, "buyQuantity": 10}', '2026-06-11 03:05:17.213595+00', '2026-06-11 03:05:17.213595+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (46, 7, 'COIN', 'EXPENSE', 160, 4530, 4370, 'BUY_SEED', '4:1781147120', '2026-06-11 03:05:20.847495+00', '{"seedTypeId": 4, "buyQuantity": 10}', '2026-06-11 03:05:20.847495+00', '2026-06-11 03:05:20.847495+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (47, 7, 'COIN', 'EXPENSE', 180, 4370, 4190, 'BUY_SEED', '5:1781147123', '2026-06-11 03:05:23.968736+00', '{"seedTypeId": 5, "buyQuantity": 10}', '2026-06-11 03:05:23.968736+00', '2026-06-11 03:05:23.968736+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (48, 7, 'COIN', 'EXPENSE', 280, 4190, 3910, 'BUY_SEED', '6:1781147128', '2026-06-11 03:05:28.659539+00', '{"seedTypeId": 6, "buyQuantity": 10}', '2026-06-11 03:05:28.659539+00', '2026-06-11 03:05:28.659539+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (49, 7, 'COIN', 'EXPENSE', 360, 3910, 3550, 'BUY_SEED', '7:1781147131', '2026-06-11 03:05:31.548288+00', '{"seedTypeId": 7, "buyQuantity": 10}', '2026-06-11 03:05:31.548288+00', '2026-06-11 03:05:31.548288+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (50, 7, 'COIN', 'EXPENSE', 80, 3550, 3470, 'UNLOCK_PLOT', '42:1781147166', '2026-06-11 03:06:06.079344+00', '{"plotId": 42, "plotIndex": 4, "unlockCost": 80}', '2026-06-11 03:06:06.079344+00', '2026-06-11 03:06:06.079344+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (51, 7, 'COIN', 'EXPENSE', 120, 3470, 3350, 'UNLOCK_PLOT', '43:1781147169', '2026-06-11 03:06:09.353123+00', '{"plotId": 43, "plotIndex": 5, "unlockCost": 120}', '2026-06-11 03:06:09.353123+00', '2026-06-11 03:06:09.353123+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (52, 7, 'COIN', 'EXPENSE', 160, 3350, 3190, 'UNLOCK_PLOT', '44:1781147174', '2026-06-11 03:06:14.900323+00', '{"plotId": 44, "plotIndex": 6, "unlockCost": 160}', '2026-06-11 03:06:14.900323+00', '2026-06-11 03:06:14.900323+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (53, 7, 'COIN', 'EXPENSE', 2500, 3190, 690, 'EXPAND_PLOT', '45:1781147180', '2026-06-11 03:06:20.837517+00', '{"plotId": 45, "plotIndex": 7, "expandCost": 2500}', '2026-06-11 03:06:20.837517+00', '2026-06-11 03:06:20.837517+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (54, 7, 'COIN', 'EXPENSE', 200, 690, 490, 'UNLOCK_PLOT', '45:1781147207', '2026-06-11 03:06:47.72687+00', '{"plotId": 45, "plotIndex": 7, "unlockCost": 200}', '2026-06-11 03:06:47.72687+00', '2026-06-11 03:06:47.72687+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (55, 7, 'COIN', 'EXPENSE', 5000, 9000, 4000, 'EXPAND_PLOT', '46:1781147230', '2026-06-11 03:07:10.144357+00', '{"plotId": 46, "plotIndex": 8, "expandCost": 5000}', '2026-06-11 03:07:10.144357+00', '2026-06-11 03:07:10.144357+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (56, 7, 'COIN', 'EXPENSE', 240, 4000, 3760, 'UNLOCK_PLOT', '46:1781147234', '2026-06-11 03:07:14.501419+00', '{"plotId": 46, "plotIndex": 8, "unlockCost": 240}', '2026-06-11 03:07:14.501419+00', '2026-06-11 03:07:14.501419+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (57, 7, 'COIN', 'EXPENSE', 1000, 3760, 2760, 'EXPAND_PLOT', '47:1781147239', '2026-06-11 03:07:19.966978+00', '{"plotId": 47, "plotIndex": 9, "expandCost": 1000}', '2026-06-11 03:07:19.966978+00', '2026-06-11 03:07:19.966978+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (58, 7, 'COIN', 'EXPENSE', 280, 2760, 2480, 'UNLOCK_PLOT', '47:1781147250', '2026-06-11 03:07:30.194233+00', '{"plotId": 47, "plotIndex": 9, "unlockCost": 280}', '2026-06-11 03:07:30.194233+00', '2026-06-11 03:07:30.194233+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (59, 7, 'EXPERIENCE', 'INCOME', 10, 3021, 3031, 'CARE', 'CARE:24:1781147259', '2026-06-11 03:07:39.563536+00', '{"cropId": 24, "plotId": 40, "bugRemoved": 1}', '2026-06-11 03:07:39.563536+00', '2026-06-11 03:07:39.563536+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (60, 7, 'SCORE', 'INCOME', 5, 3017, 3022, 'CARE', 'CARE:24:1781147259', '2026-06-11 03:07:39.563536+00', '{"cropId": 24, "plotId": 40, "bugRemoved": 1}', '2026-06-11 03:07:39.563536+00', '2026-06-11 03:07:39.563536+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (61, 7, 'EXPERIENCE', 'INCOME', 10, 3052, 3062, 'CARE', 'CARE:26:1781147311', '2026-06-11 03:08:31.795689+00', '{"cropId": 26, "plotId": 42, "bugRemoved": 1}', '2026-06-11 03:08:31.795689+00', '2026-06-11 03:08:31.795689+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (62, 7, 'SCORE', 'INCOME', 5, 3039, 3044, 'CARE', 'CARE:26:1781147311', '2026-06-11 03:08:31.795689+00', '{"cropId": 26, "plotId": 42, "bugRemoved": 1}', '2026-06-11 03:08:31.795689+00', '2026-06-11 03:08:31.795689+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_asset_flows (id, user_id, asset_type, operation_type, change_amount, before_amount, after_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (63, 7, 'COIN', 'INCOME', 15, 2480, 2495, 'SELL_FRUIT', '1:1781147362', '2026-06-11 03:09:22.717827+00', '{"seedTypeId": 1, "sellQuantity": 1}', '2026-06-11 03:09:22.717827+00', '2026-06-11 03:09:22.717827+00', 7, 7, NULL, 1, false, 0);


--
-- TOC entry 3798 (class 0 OID 29447)
-- Dependencies: 248
-- Data for Name: user_crop_action_logs; Type: TABLE DATA; Schema: farm; Owner: -
--

INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (1, 2, 7, 1, 1, 'PLANT', 'SUCCESS', '2026-06-10 01:01:21.212042+00', '{"remainSeed": 17, "seedTypeId": 1}', '2026-06-10 01:01:21.212042+00', '2026-06-10 01:01:21.212042+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (2, 2, 8, 2, 3, 'PLANT', 'SUCCESS', '2026-06-10 01:01:32.247965+00', '{"remainSeed": 9, "seedTypeId": 3}', '2026-06-10 01:01:32.247965+00', '2026-06-10 01:01:32.247965+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (3, 2, 9, 3, 2, 'PLANT', 'SUCCESS', '2026-06-10 01:01:51.721551+00', '{"remainSeed": 17, "seedTypeId": 2}', '2026-06-10 01:01:51.721551+00', '2026-06-10 01:01:51.721551+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (4, 2, 7, 1, 1, 'HARVEST', 'SUCCESS', '2026-06-10 01:03:13.87776+00', '{"fruitGain": 3, "cropCleared": false, "baseFruitGain": 3, "bugCountBefore": 0, "bugPenaltyPerBug": 0, "totalBugPenaltyFruit": 0}', '2026-06-10 01:03:13.87776+00', '2026-06-10 01:03:13.87776+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (7, 2, 9, 3, 2, 'BUG_SPAWN', 'SUCCESS', '2026-06-10 01:03:26.753241+00', '{"reason": "STAGE_TRANSITION", "bugAddedCount": 1, "bugCountAfter": 1, "enteredStages": [3], "bugCountBefore": 0}', '2026-06-10 01:03:26.753241+00', '2026-06-10 01:03:26.753241+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (8, 2, 8, 2, 3, 'BUG_SPAWN', 'SUCCESS', '2026-06-10 01:03:34.951117+00', '{"reason": "STAGE_TRANSITION", "bugAddedCount": 1, "bugCountAfter": 1, "enteredStages": [4], "bugCountBefore": 0}', '2026-06-10 01:03:34.951117+00', '2026-06-10 01:03:34.951117+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (9, 2, 9, 3, 2, 'BUG_SPAWN', 'SUCCESS', '2026-06-10 01:04:37.668113+00', '{"reason": "STAGE_TRANSITION", "bugAddedCount": 1, "bugCountAfter": 2, "enteredStages": [4], "bugCountBefore": 1}', '2026-06-10 01:04:37.668113+00', '2026-06-10 01:04:37.668113+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (10, 2, 7, 1, 1, 'CLEAR', 'SUCCESS', '2026-06-10 01:17:05.796212+00', '{"bugCountBefore": 0, "growStatusBefore": 3, "stageIndexBefore": 5}', '2026-06-10 01:17:05.796212+00', '2026-06-10 01:17:05.796212+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (11, 2, 8, 2, 3, 'CLEAR', 'SUCCESS', '2026-06-10 01:17:07.223471+00', '{"bugCountBefore": 1, "growStatusBefore": 3, "stageIndexBefore": 6}', '2026-06-10 01:17:07.223471+00', '2026-06-10 01:17:07.223471+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (12, 2, 9, 3, 2, 'CLEAR', 'SUCCESS', '2026-06-10 01:17:08.688366+00', '{"bugCountBefore": 2, "growStatusBefore": 3, "stageIndexBefore": 5}', '2026-06-10 01:17:08.688366+00', '2026-06-10 01:17:08.688366+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (13, 2, 7, 4, 1, 'PLANT', 'SUCCESS', '2026-06-10 01:25:26.133423+00', '{"remainSeed": 17, "seedTypeId": 1}', '2026-06-10 01:25:26.133423+00', '2026-06-10 01:25:26.133423+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (14, 2, 8, 5, 3, 'PLANT', 'SUCCESS', '2026-06-10 01:26:45.469666+00', '{"remainSeed": 8, "seedTypeId": 3}', '2026-06-10 01:26:45.469666+00', '2026-06-10 01:26:45.469666+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (15, 2, 9, 6, 5, 'PLANT', 'SUCCESS', '2026-06-10 01:26:51.954603+00', '{"remainSeed": 15, "seedTypeId": 5}', '2026-06-10 01:26:51.954603+00', '2026-06-10 01:26:51.954603+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (16, 2, 7, 4, 1, 'HARVEST', 'SUCCESS', '2026-06-10 01:27:07.390329+00', '{"fruitGain": 5, "cropCleared": true, "baseFruitGain": 5, "bugCountBefore": 0, "bugPenaltyPerBug": 1, "totalBugPenaltyFruit": 0}', '2026-06-10 01:27:07.390329+00', '2026-06-10 01:27:07.390329+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (17, 2, 7, 7, 1, 'PLANT', 'SUCCESS', '2026-06-10 01:27:12.831025+00', '{"remainSeed": 16, "seedTypeId": 1}', '2026-06-10 01:27:12.831025+00', '2026-06-10 01:27:12.831025+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (18, 2, 10, 8, 3, 'PLANT', 'SUCCESS', '2026-06-10 01:27:38.939009+00', '{"remainSeed": 7, "seedTypeId": 3}', '2026-06-10 01:27:38.939009+00', '2026-06-10 01:27:38.939009+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (19, 2, 12, 9, 3, 'PLANT', 'SUCCESS', '2026-06-10 01:27:43.516907+00', '{"remainSeed": 6, "seedTypeId": 3}', '2026-06-10 01:27:43.516907+00', '2026-06-10 01:27:43.516907+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (20, 2, 9, 6, 5, 'BUG_SPAWN', 'SUCCESS', '2026-06-10 01:27:47.851604+00', '{"reason": "STAGE_TRANSITION", "bugAddedCount": 1, "bugCountAfter": 1, "enteredStages": [3], "bugCountBefore": 0}', '2026-06-10 01:27:47.851604+00', '2026-06-10 01:27:47.851604+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (21, 2, 8, 5, 3, 'BUG_SPAWN', 'SUCCESS', '2026-06-10 01:27:58.591976+00', '{"reason": "STAGE_TRANSITION", "bugAddedCount": 1, "bugCountAfter": 1, "enteredStages": [3], "bugCountBefore": 0}', '2026-06-10 01:27:58.591976+00', '2026-06-10 01:27:58.591976+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (22, 2, 8, 5, 3, 'CARE', 'SUCCESS', '2026-06-10 01:28:20.145857+00', '{"coinGain": 0, "scoreGain": 5, "bugCountAfter": 0, "bugCountBefore": 1, "experienceGain": 10, "bugRemovedCount": 1}', '2026-06-10 01:28:20.145857+00', '2026-06-10 01:28:20.145857+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (23, 2, 9, 6, 5, 'CARE', 'SUCCESS', '2026-06-10 01:28:21.275588+00', '{"coinGain": 0, "scoreGain": 5, "bugCountAfter": 0, "bugCountBefore": 1, "experienceGain": 10, "bugRemovedCount": 1}', '2026-06-10 01:28:21.275588+00', '2026-06-10 01:28:21.275588+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (24, 2, 7, 7, 1, 'HARVEST', 'SUCCESS', '2026-06-10 01:28:57.612627+00', '{"fruitGain": 5, "cropCleared": true, "baseFruitGain": 5, "bugCountBefore": 0, "bugPenaltyPerBug": 1, "totalBugPenaltyFruit": 0}', '2026-06-10 01:28:57.612627+00', '2026-06-10 01:28:57.612627+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (25, 2, 9, 6, 5, 'BUG_SPAWN', 'SUCCESS', '2026-06-10 01:29:01.965799+00', '{"reason": "STAGE_TRANSITION", "bugAddedCount": 1, "bugCountAfter": 1, "enteredStages": [5], "bugCountBefore": 0}', '2026-06-10 01:29:01.965799+00', '2026-06-10 01:29:01.965799+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (26, 2, 11, 10, 5, 'PLANT', 'SUCCESS', '2026-06-10 01:29:03.561793+00', '{"remainSeed": 14, "seedTypeId": 5}', '2026-06-10 01:29:03.561793+00', '2026-06-10 01:29:03.561793+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (27, 2, 9, 6, 5, 'CARE', 'SUCCESS', '2026-06-10 01:29:07.341001+00', '{"coinGain": 0, "scoreGain": 5, "bugCountAfter": 0, "bugCountBefore": 1, "experienceGain": 10, "bugRemovedCount": 1}', '2026-06-10 01:29:07.341001+00', '2026-06-10 01:29:07.341001+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (28, 2, 9, 6, 5, 'HARVEST', 'SUCCESS', '2026-06-10 01:29:13.912811+00', '{"fruitGain": 5, "cropCleared": false, "baseFruitGain": 5, "bugCountBefore": 0, "bugPenaltyPerBug": 1, "totalBugPenaltyFruit": 0}', '2026-06-10 01:29:13.912811+00', '2026-06-10 01:29:13.912811+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (29, 2, 10, 8, 3, 'BUG_SPAWN', 'SUCCESS', '2026-06-10 01:29:41.914021+00', '{"reason": "STAGE_TRANSITION", "bugAddedCount": 1, "bugCountAfter": 1, "enteredStages": [4], "bugCountBefore": 0}', '2026-06-10 01:29:41.914021+00', '2026-06-10 01:29:41.914021+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (30, 2, 7, 11, 7, 'PLANT', 'SUCCESS', '2026-06-10 01:29:49.754046+00', '{"remainSeed": 1, "seedTypeId": 7}', '2026-06-10 01:29:49.754046+00', '2026-06-10 01:29:49.754046+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (31, 2, 10, 8, 3, 'CARE', 'SUCCESS', '2026-06-10 01:29:51.780685+00', '{"coinGain": 0, "scoreGain": 5, "bugCountAfter": 0, "bugCountBefore": 1, "experienceGain": 10, "bugRemovedCount": 1}', '2026-06-10 01:29:51.780685+00', '2026-06-10 01:29:51.780685+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (32, 2, 9, 6, 5, 'HARVEST', 'SUCCESS', '2026-06-10 01:29:58.334724+00', '{"fruitGain": 5, "cropCleared": false, "baseFruitGain": 5, "bugCountBefore": 0, "bugPenaltyPerBug": 1, "totalBugPenaltyFruit": 0}', '2026-06-10 01:29:58.334724+00', '2026-06-10 01:29:58.334724+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (33, 2, 8, 5, 3, 'HARVEST', 'SUCCESS', '2026-06-10 01:30:01.164894+00', '{"fruitGain": 6, "cropCleared": false, "baseFruitGain": 6, "bugCountBefore": 0, "bugPenaltyPerBug": 1, "totalBugPenaltyFruit": 0}', '2026-06-10 01:30:01.164894+00', '2026-06-10 01:30:01.164894+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (34, 2, 7, 11, 7, 'BUG_SPAWN', 'SUCCESS', '2026-06-10 01:30:25.423334+00', '{"reason": "STAGE_TRANSITION", "bugAddedCount": 1, "bugCountAfter": 1, "enteredStages": [2], "bugCountBefore": 0}', '2026-06-10 01:30:25.423334+00', '2026-06-10 01:30:25.423334+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (35, 2, 7, 11, 7, 'CARE', 'SUCCESS', '2026-06-10 01:30:31.38106+00', '{"coinGain": 0, "scoreGain": 5, "bugCountAfter": 0, "bugCountBefore": 1, "experienceGain": 10, "bugRemovedCount": 1}', '2026-06-10 01:30:31.38106+00', '2026-06-10 01:30:31.38106+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (36, 2, 12, 9, 3, 'BUG_SPAWN', 'SUCCESS', '2026-06-10 01:30:59.060161+00', '{"reason": "STAGE_TRANSITION", "bugAddedCount": 1, "bugCountAfter": 1, "enteredStages": [5], "bugCountBefore": 0}', '2026-06-10 01:30:59.060161+00', '2026-06-10 01:30:59.060161+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (38, 2, 8, 5, 3, 'HARVEST', 'SUCCESS', '2026-06-10 01:31:30.714837+00', '{"fruitGain": 6, "cropCleared": false, "baseFruitGain": 6, "bugCountBefore": 0, "bugPenaltyPerBug": 1, "totalBugPenaltyFruit": 0}', '2026-06-10 01:31:30.714837+00', '2026-06-10 01:31:30.714837+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (39, 2, 10, 8, 3, 'HARVEST', 'SUCCESS', '2026-06-10 01:31:35.167132+00', '{"fruitGain": 6, "cropCleared": false, "baseFruitGain": 6, "bugCountBefore": 0, "bugPenaltyPerBug": 1, "totalBugPenaltyFruit": 0}', '2026-06-10 01:31:35.167132+00', '2026-06-10 01:31:35.167132+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (40, 2, 12, 9, 3, 'HARVEST', 'SUCCESS', '2026-06-10 01:31:41.136431+00', '{"fruitGain": 6, "cropCleared": false, "baseFruitGain": 6, "bugCountBefore": 0, "bugPenaltyPerBug": 1, "totalBugPenaltyFruit": 0}', '2026-06-10 01:31:41.136431+00', '2026-06-10 01:31:41.136431+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (41, 2, 11, 10, 5, 'HARVEST', 'SUCCESS', '2026-06-10 01:32:07.443078+00', '{"fruitGain": 5, "cropCleared": false, "baseFruitGain": 5, "bugCountBefore": 0, "bugPenaltyPerBug": 1, "totalBugPenaltyFruit": 0}', '2026-06-10 01:32:07.443078+00', '2026-06-10 01:32:07.443078+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (44, 2, 31, 13, 2, 'PLANT', 'SUCCESS', '2026-06-10 01:32:29.136096+00', '{"remainSeed": 16, "seedTypeId": 2}', '2026-06-10 01:32:29.136096+00', '2026-06-10 01:32:29.136096+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (45, 2, 8, 5, 3, 'HARVEST', 'SUCCESS', '2026-06-10 01:32:46.048459+00', '{"fruitGain": 6, "cropCleared": true, "baseFruitGain": 6, "bugCountBefore": 0, "bugPenaltyPerBug": 1, "totalBugPenaltyFruit": 0}', '2026-06-10 01:32:46.048459+00', '2026-06-10 01:32:46.048459+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (48, 2, 7, 11, 7, 'HARVEST', 'SUCCESS', '2026-06-10 01:32:53.800599+00', '{"fruitGain": 6, "cropCleared": false, "baseFruitGain": 6, "bugCountBefore": 0, "bugPenaltyPerBug": 1, "totalBugPenaltyFruit": 0}', '2026-06-10 01:32:53.800599+00', '2026-06-10 01:32:53.800599+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (50, 2, 10, 8, 3, 'HARVEST', 'SUCCESS', '2026-06-10 01:32:57.48072+00', '{"fruitGain": 6, "cropCleared": false, "baseFruitGain": 6, "bugCountBefore": 0, "bugPenaltyPerBug": 1, "totalBugPenaltyFruit": 0}', '2026-06-10 01:32:57.48072+00', '2026-06-10 01:32:57.48072+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (51, 2, 11, 10, 5, 'HARVEST', 'SUCCESS', '2026-06-10 01:33:01.240837+00', '{"fruitGain": 5, "cropCleared": false, "baseFruitGain": 5, "bugCountBefore": 0, "bugPenaltyPerBug": 1, "totalBugPenaltyFruit": 0}', '2026-06-10 01:33:01.240837+00', '2026-06-10 01:33:01.240837+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (53, 2, 9, 12, 7, 'BUG_SPAWN', 'SUCCESS', '2026-06-10 01:34:30.971057+00', '{"reason": "STAGE_TRANSITION", "bugAddedCount": 1, "bugCountAfter": 1, "enteredStages": [4], "bugCountBefore": 0}', '2026-06-10 01:34:30.971057+00', '2026-06-10 01:34:30.971057+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (55, 2, 7, 11, 7, 'HARVEST', 'SUCCESS', '2026-06-10 01:35:06.124797+00', '{"fruitGain": 6, "cropCleared": false, "baseFruitGain": 6, "bugCountBefore": 0, "bugPenaltyPerBug": 1, "totalBugPenaltyFruit": 0}', '2026-06-10 01:35:06.124797+00', '2026-06-10 01:35:06.124797+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (37, 2, 12, 9, 3, 'CARE', 'SUCCESS', '2026-06-10 01:31:27.999146+00', '{"coinGain": 0, "scoreGain": 5, "bugCountAfter": 0, "bugCountBefore": 1, "experienceGain": 10, "bugRemovedCount": 1}', '2026-06-10 01:31:27.999146+00', '2026-06-10 01:31:27.999146+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (42, 2, 9, 6, 5, 'CLEAR', 'SUCCESS', '2026-06-10 01:32:11.403304+00', '{"bugCountBefore": 0, "growStatusBefore": 3, "stageIndexBefore": 6}', '2026-06-10 01:32:11.403304+00', '2026-06-10 01:32:11.403304+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (43, 2, 9, 12, 7, 'PLANT', 'SUCCESS', '2026-06-10 01:32:24.97711+00', '{"remainSeed": 0, "seedTypeId": 7}', '2026-06-10 01:32:24.97711+00', '2026-06-10 01:32:24.97711+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (46, 2, 10, 8, 3, 'BUG_SPAWN', 'SUCCESS', '2026-06-10 01:32:47.237551+00', '{"reason": "STAGE_TRANSITION", "bugAddedCount": 1, "bugCountAfter": 1, "enteredStages": [5], "bugCountBefore": 0}', '2026-06-10 01:32:47.237551+00', '2026-06-10 01:32:47.237551+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (47, 2, 10, 8, 3, 'CARE', 'SUCCESS', '2026-06-10 01:32:50.688323+00', '{"coinGain": 0, "scoreGain": 5, "bugCountAfter": 0, "bugCountBefore": 1, "experienceGain": 10, "bugRemovedCount": 1}', '2026-06-10 01:32:50.688323+00', '2026-06-10 01:32:50.688323+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (49, 2, 12, 9, 3, 'HARVEST', 'SUCCESS', '2026-06-10 01:32:55.824843+00', '{"fruitGain": 6, "cropCleared": false, "baseFruitGain": 6, "bugCountBefore": 0, "bugPenaltyPerBug": 1, "totalBugPenaltyFruit": 0}', '2026-06-10 01:32:55.824843+00', '2026-06-10 01:32:55.824843+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (52, 2, 11, 10, 5, 'BUG_SPAWN', 'SUCCESS', '2026-06-10 01:33:51.983313+00', '{"reason": "STAGE_TRANSITION", "bugAddedCount": 1, "bugCountAfter": 1, "enteredStages": [5], "bugCountBefore": 0}', '2026-06-10 01:33:51.983313+00', '2026-06-10 01:33:51.983313+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (54, 2, 12, 9, 3, 'HARVEST', 'SUCCESS', '2026-06-10 01:35:05.406389+00', '{"fruitGain": 6, "cropCleared": true, "baseFruitGain": 6, "bugCountBefore": 0, "bugPenaltyPerBug": 1, "totalBugPenaltyFruit": 0}', '2026-06-10 01:35:05.406389+00', '2026-06-10 01:35:05.406389+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (56, 2, 10, 8, 3, 'HARVEST', 'SUCCESS', '2026-06-10 01:35:07.007292+00', '{"fruitGain": 6, "cropCleared": true, "baseFruitGain": 6, "bugCountBefore": 0, "bugPenaltyPerBug": 1, "totalBugPenaltyFruit": 0}', '2026-06-10 01:35:07.007292+00', '2026-06-10 01:35:07.007292+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (57, 2, 9, 12, 7, 'CARE', 'SUCCESS', '2026-06-10 01:35:09.32671+00', '{"coinGain": 0, "scoreGain": 5, "bugCountAfter": 0, "bugCountBefore": 1, "experienceGain": 10, "bugRemovedCount": 1}', '2026-06-10 01:35:09.32671+00', '2026-06-10 01:35:09.32671+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (58, 2, 9, 12, 7, 'BUG_SPAWN', 'SUCCESS', '2026-06-10 01:36:26.224728+00', '{"reason": "STAGE_TRANSITION", "bugAddedCount": 1, "bugCountAfter": 1, "enteredStages": [6], "bugCountBefore": 0}', '2026-06-10 01:36:26.224728+00', '2026-06-10 01:36:26.224728+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (59, 1, 1, 14, 1, 'PLANT', 'SUCCESS', '2026-06-10 17:08:46.242099+00', '{"remainSeed": 23, "seedTypeId": 1}', '2026-06-10 17:08:46.242099+00', '2026-06-10 17:08:46.242099+00', 1, 1, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (60, 1, 2, 15, 2, 'PLANT', 'SUCCESS', '2026-06-10 17:08:52.060662+00', '{"remainSeed": 19, "seedTypeId": 2}', '2026-06-10 17:08:52.060662+00', '2026-06-10 17:08:52.060662+00', 1, 1, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (61, 1, 1, 14, 1, 'HARVEST', 'SUCCESS', '2026-06-10 17:10:28.599637+00', '{"fruitGain": 5, "cropCleared": true, "baseFruitGain": 5, "bugCountBefore": 0, "bugPenaltyPerBug": 1, "totalBugPenaltyFruit": 0}', '2026-06-10 17:10:28.599637+00', '2026-06-10 17:10:28.599637+00', 1, 1, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (62, 1, 2, 15, 2, 'HARVEST', 'SUCCESS', '2026-06-10 17:12:07.113018+00', '{"fruitGain": 3, "cropCleared": false, "baseFruitGain": 3, "bugCountBefore": 0, "bugPenaltyPerBug": 1, "totalBugPenaltyFruit": 0}', '2026-06-10 17:12:07.113018+00', '2026-06-10 17:12:07.113018+00', 1, 1, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (63, 1, 2, 15, 2, 'HARVEST', 'SUCCESS', '2026-06-10 17:13:31.122496+00', '{"fruitGain": 3, "cropCleared": true, "baseFruitGain": 3, "bugCountBefore": 0, "bugPenaltyPerBug": 1, "totalBugPenaltyFruit": 0}', '2026-06-10 17:13:31.122496+00', '2026-06-10 17:13:31.122496+00', 1, 1, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (64, 2, 9, 12, 7, 'CLEAR', 'SUCCESS', '2026-06-11 01:56:27.27443+00', '{"bugCountBefore": 1, "growStatusBefore": 3, "stageIndexBefore": 7}', '2026-06-11 01:56:27.27443+00', '2026-06-11 01:56:27.27443+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (65, 2, 11, 10, 5, 'CLEAR', 'SUCCESS', '2026-06-11 01:56:28.858074+00', '{"bugCountBefore": 1, "growStatusBefore": 3, "stageIndexBefore": 6}', '2026-06-11 01:56:28.858074+00', '2026-06-11 01:56:28.858074+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (66, 2, 31, 13, 2, 'CLEAR', 'SUCCESS', '2026-06-11 01:56:30.570616+00', '{"bugCountBefore": 0, "growStatusBefore": 3, "stageIndexBefore": 5}', '2026-06-11 01:56:30.570616+00', '2026-06-11 01:56:30.570616+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (67, 2, 7, 11, 7, 'CLEAR', 'SUCCESS', '2026-06-11 01:56:32.721951+00', '{"bugCountBefore": 0, "growStatusBefore": 3, "stageIndexBefore": 7}', '2026-06-11 01:56:32.721951+00', '2026-06-11 01:56:32.721951+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (68, 2, 12, 16, 2, 'PLANT', 'SUCCESS', '2026-06-11 02:50:52.81869+00', '{"remainSeed": 15, "seedTypeId": 2}', '2026-06-11 02:50:52.81869+00', '2026-06-11 02:50:52.81869+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (69, 2, 7, 17, 1, 'PLANT', 'SUCCESS', '2026-06-11 02:50:57.225854+00', '{"remainSeed": 15, "seedTypeId": 1}', '2026-06-11 02:50:57.225854+00', '2026-06-11 02:50:57.225854+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (70, 2, 8, 18, 3, 'PLANT', 'SUCCESS', '2026-06-11 02:51:09.648615+00', '{"remainSeed": 5, "seedTypeId": 3}', '2026-06-11 02:51:09.648615+00', '2026-06-11 02:51:09.648615+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (71, 2, 9, 19, 5, 'PLANT', 'SUCCESS', '2026-06-11 02:51:14.645019+00', '{"remainSeed": 13, "seedTypeId": 5}', '2026-06-11 02:51:14.645019+00', '2026-06-11 02:51:14.645019+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (72, 2, 10, 20, 7, 'PLANT', 'SUCCESS', '2026-06-11 02:51:21.368807+00', '{"remainSeed": 0, "seedTypeId": 7}', '2026-06-11 02:51:21.368807+00', '2026-06-11 02:51:21.368807+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (73, 2, 31, 21, 5, 'PLANT', 'SUCCESS', '2026-06-11 02:51:30.67837+00', '{"remainSeed": 12, "seedTypeId": 5}', '2026-06-11 02:51:30.67837+00', '2026-06-11 02:51:30.67837+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (74, 2, 11, 22, 5, 'PLANT', 'SUCCESS', '2026-06-11 02:51:37.949033+00', '{"remainSeed": 11, "seedTypeId": 5}', '2026-06-11 02:51:37.949033+00', '2026-06-11 02:51:37.949033+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (75, 2, 11, 22, 5, 'BUG_SPAWN', 'SUCCESS', '2026-06-11 02:52:20.192952+00', '{"reason": "STAGE_TRANSITION", "bugAddedCount": 1, "bugCountAfter": 1, "enteredStages": [2], "bugCountBefore": 0}', '2026-06-11 02:52:20.192952+00', '2026-06-11 02:52:20.192952+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (76, 2, 11, 22, 5, 'CARE', 'SUCCESS', '2026-06-11 02:52:25.4699+00', '{"coinGain": 0, "scoreGain": 5, "bugCountAfter": 0, "bugCountBefore": 1, "experienceGain": 10, "bugRemovedCount": 1}', '2026-06-11 02:52:25.4699+00', '2026-06-11 02:52:25.4699+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (77, 2, 7, 17, 1, 'HARVEST', 'SUCCESS', '2026-06-11 02:52:29.485989+00', '{"fruitGain": 5, "cropCleared": true, "baseFruitGain": 5, "bugCountBefore": 0, "bugPenaltyPerBug": 1, "totalBugPenaltyFruit": 0}', '2026-06-11 02:52:29.485989+00', '2026-06-11 02:52:29.485989+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (78, 2, 10, 20, 7, 'BUG_SPAWN', 'SUCCESS', '2026-06-11 02:52:56.245166+00', '{"reason": "STAGE_TRANSITION", "bugAddedCount": 1, "bugCountAfter": 1, "enteredStages": [3], "bugCountBefore": 0}', '2026-06-11 02:52:56.245166+00', '2026-06-11 02:52:56.245166+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (79, 2, 10, 20, 7, 'BUG_SPAWN', 'SUCCESS', '2026-06-11 02:53:59.527803+00', '{"reason": "STAGE_TRANSITION", "bugAddedCount": 1, "bugCountAfter": 2, "enteredStages": [4], "bugCountBefore": 1}', '2026-06-11 02:53:59.527803+00', '2026-06-11 02:53:59.527803+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (89, 2, 11, 22, 5, 'CLEAR', 'SUCCESS', '2026-06-11 03:00:28.789525+00', '{"bugCountBefore": 0, "growStatusBefore": 3, "stageIndexBefore": 6}', '2026-06-11 03:00:28.789525+00', '2026-06-11 03:00:28.789525+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (90, 2, 31, 21, 5, 'CLEAR', 'SUCCESS', '2026-06-11 03:00:30.858032+00', '{"bugCountBefore": 0, "growStatusBefore": 3, "stageIndexBefore": 6}', '2026-06-11 03:00:30.858032+00', '2026-06-11 03:00:30.858032+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (80, 2, 9, 19, 5, 'HARVEST', 'SUCCESS', '2026-06-11 02:53:46.825956+00', '{"fruitGain": 5, "cropCleared": false, "baseFruitGain": 5, "bugCountBefore": 0, "bugPenaltyPerBug": 1, "totalBugPenaltyFruit": 0}', '2026-06-11 02:53:46.825956+00', '2026-06-11 02:53:46.825956+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (82, 2, 10, 20, 7, 'CARE', 'SUCCESS', '2026-06-11 02:54:09.064937+00', '{"coinGain": 0, "scoreGain": 5, "bugCountAfter": 0, "bugCountBefore": 1, "experienceGain": 10, "bugRemovedCount": 1}', '2026-06-11 02:54:09.064937+00', '2026-06-11 02:54:09.064937+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (95, 7, 39, 23, 1, 'HARVEST', 'SUCCESS', '2026-06-11 03:07:24.006643+00', '{"fruitGain": 5, "cropCleared": false, "baseFruitGain": 5, "bugCountBefore": 0, "bugPenaltyPerBug": 1, "totalBugPenaltyFruit": 0}', '2026-06-11 03:07:24.006643+00', '2026-06-11 03:07:24.006643+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (81, 2, 10, 20, 7, 'CARE', 'SUCCESS', '2026-06-11 02:54:07.13064+00', '{"coinGain": 0, "scoreGain": 5, "bugCountAfter": 1, "bugCountBefore": 2, "experienceGain": 10, "bugRemovedCount": 1}', '2026-06-11 02:54:07.13064+00', '2026-06-11 02:54:07.13064+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (83, 2, 12, 16, 2, 'HARVEST', 'SUCCESS', '2026-06-11 02:54:19.484014+00', '{"fruitGain": 3, "cropCleared": false, "baseFruitGain": 3, "bugCountBefore": 0, "bugPenaltyPerBug": 1, "totalBugPenaltyFruit": 0}', '2026-06-11 02:54:19.484014+00', '2026-06-11 02:54:19.484014+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (92, 7, 39, 23, 1, 'PLANT', 'SUCCESS', '2026-06-11 03:05:53.906755+00', '{"remainSeed": 9, "seedTypeId": 1}', '2026-06-11 03:05:53.906755+00', '2026-06-11 03:05:53.906755+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (93, 7, 40, 24, 2, 'PLANT', 'SUCCESS', '2026-06-11 03:05:58.540211+00', '{"remainSeed": 9, "seedTypeId": 2}', '2026-06-11 03:05:58.540211+00', '2026-06-11 03:05:58.540211+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (97, 7, 41, 25, 3, 'PLANT', 'SUCCESS', '2026-06-11 03:07:57.919902+00', '{"remainSeed": 9, "seedTypeId": 3}', '2026-06-11 03:07:57.919902+00', '2026-06-11 03:07:57.919902+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (98, 7, 42, 26, 4, 'PLANT', 'SUCCESS', '2026-06-11 03:08:02.001704+00', '{"remainSeed": 9, "seedTypeId": 4}', '2026-06-11 03:08:02.001704+00', '2026-06-11 03:08:02.001704+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (100, 7, 44, 28, 6, 'PLANT', 'SUCCESS', '2026-06-11 03:08:11.325239+00', '{"remainSeed": 9, "seedTypeId": 6}', '2026-06-11 03:08:11.325239+00', '2026-06-11 03:08:11.325239+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (103, 7, 39, 23, 1, 'HARVEST', 'SUCCESS', '2026-06-11 03:08:20.522726+00', '{"fruitGain": 5, "cropCleared": true, "baseFruitGain": 5, "bugCountBefore": 0, "bugPenaltyPerBug": 1, "totalBugPenaltyFruit": 0}', '2026-06-11 03:08:20.522726+00', '2026-06-11 03:08:20.522726+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (106, 7, 40, 24, 2, 'CLEAR', 'SUCCESS', '2026-06-11 03:09:10.776502+00', '{"bugCountBefore": 0, "growStatusBefore": 1, "stageIndexBefore": 3}', '2026-06-11 03:09:10.776502+00', '2026-06-11 03:09:10.776502+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (84, 2, 31, 21, 5, 'HARVEST', 'SUCCESS', '2026-06-11 02:54:21.402818+00', '{"fruitGain": 5, "cropCleared": false, "baseFruitGain": 5, "bugCountBefore": 0, "bugPenaltyPerBug": 1, "totalBugPenaltyFruit": 0}', '2026-06-11 02:54:21.402818+00', '2026-06-11 02:54:21.402818+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (85, 2, 9, 19, 5, 'HARVEST', 'SUCCESS', '2026-06-11 02:54:24.462592+00', '{"fruitGain": 5, "cropCleared": false, "baseFruitGain": 5, "bugCountBefore": 0, "bugPenaltyPerBug": 1, "totalBugPenaltyFruit": 0}', '2026-06-11 02:54:24.462592+00', '2026-06-11 02:54:24.462592+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (86, 2, 8, 18, 3, 'CLEAR', 'SUCCESS', '2026-06-11 03:00:25.093313+00', '{"bugCountBefore": 0, "growStatusBefore": 3, "stageIndexBefore": 6}', '2026-06-11 03:00:25.093313+00', '2026-06-11 03:00:25.093313+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (87, 2, 9, 19, 5, 'CLEAR', 'SUCCESS', '2026-06-11 03:00:26.114941+00', '{"bugCountBefore": 0, "growStatusBefore": 3, "stageIndexBefore": 6}', '2026-06-11 03:00:26.114941+00', '2026-06-11 03:00:26.114941+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (88, 2, 10, 20, 7, 'CLEAR', 'SUCCESS', '2026-06-11 03:00:27.28474+00', '{"bugCountBefore": 0, "growStatusBefore": 3, "stageIndexBefore": 7}', '2026-06-11 03:00:27.28474+00', '2026-06-11 03:00:27.28474+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (91, 2, 12, 16, 2, 'CLEAR', 'SUCCESS', '2026-06-11 03:00:35.60835+00', '{"bugCountBefore": 0, "growStatusBefore": 3, "stageIndexBefore": 5}', '2026-06-11 03:00:35.60835+00', '2026-06-11 03:00:35.60835+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (94, 7, 40, 24, 2, 'BUG_SPAWN', 'SUCCESS', '2026-06-11 03:07:34.655269+00', '{"reason": "STAGE_TRANSITION", "bugAddedCount": 1, "bugCountAfter": 1, "enteredStages": [3], "bugCountBefore": 0}', '2026-06-11 03:07:34.655269+00', '2026-06-11 03:07:34.655269+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (102, 7, 42, 26, 4, 'BUG_SPAWN', 'SUCCESS', '2026-06-11 03:08:33.341982+00', '{"reason": "STAGE_TRANSITION", "bugAddedCount": 1, "bugCountAfter": 1, "enteredStages": [2], "bugCountBefore": 0}', '2026-06-11 03:08:33.341982+00', '2026-06-11 03:08:33.341982+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (107, 7, 41, 25, 3, 'BUG_SPAWN', 'SUCCESS', '2026-06-11 03:09:49.030883+00', '{"reason": "STAGE_TRANSITION", "bugAddedCount": 1, "bugCountAfter": 1, "enteredStages": [4], "bugCountBefore": 0}', '2026-06-11 03:09:49.030883+00', '2026-06-11 03:09:49.030883+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (108, 7, 41, 25, 3, 'BUG_SPAWN', 'SUCCESS', '2026-06-11 03:10:54.426737+00', '{"reason": "STAGE_TRANSITION", "bugAddedCount": 1, "bugCountAfter": 2, "enteredStages": [5], "bugCountBefore": 1}', '2026-06-11 03:10:54.426737+00', '2026-06-11 03:10:54.426737+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (109, 7, 45, 29, 7, 'BUG_SPAWN', 'SUCCESS', '2026-06-11 03:12:03.300385+00', '{"reason": "STAGE_TRANSITION", "bugAddedCount": 1, "bugCountAfter": 1, "enteredStages": [5], "bugCountBefore": 0}', '2026-06-11 03:12:03.300385+00', '2026-06-11 03:12:03.300385+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (96, 7, 40, 24, 2, 'CARE', 'SUCCESS', '2026-06-11 03:07:39.563536+00', '{"coinGain": 0, "scoreGain": 5, "bugCountAfter": 0, "bugCountBefore": 1, "experienceGain": 10, "bugRemovedCount": 1}', '2026-06-11 03:07:39.563536+00', '2026-06-11 03:07:39.563536+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (99, 7, 43, 27, 5, 'PLANT', 'SUCCESS', '2026-06-11 03:08:06.101864+00', '{"remainSeed": 9, "seedTypeId": 5}', '2026-06-11 03:08:06.101864+00', '2026-06-11 03:08:06.101864+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (101, 7, 45, 29, 7, 'PLANT', 'SUCCESS', '2026-06-11 03:08:16.260668+00', '{"remainSeed": 9, "seedTypeId": 7}', '2026-06-11 03:08:16.260668+00', '2026-06-11 03:08:16.260668+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (104, 7, 42, 26, 4, 'CARE', 'SUCCESS', '2026-06-11 03:08:31.795689+00', '{"coinGain": 0, "scoreGain": 5, "bugCountAfter": 0, "bugCountBefore": 1, "experienceGain": 10, "bugRemovedCount": 1}', '2026-06-11 03:08:31.795689+00', '2026-06-11 03:08:31.795689+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_crop_action_logs (id, user_id, plot_id, crop_id, seed_type_id, action_type, action_result, action_at, action_snapshot, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (105, 7, 40, 24, 2, 'HARVEST', 'SUCCESS', '2026-06-11 03:08:37.671304+00', '{"fruitGain": 3, "cropCleared": false, "baseFruitGain": 3, "bugCountBefore": 0, "bugPenaltyPerBug": 1, "totalBugPenaltyFruit": 0}', '2026-06-11 03:08:37.671304+00', '2026-06-11 03:08:37.671304+00', 7, 7, NULL, 1, false, 0);


--
-- TOC entry 3790 (class 0 OID 29375)
-- Dependencies: 240
-- Data for Name: user_crops; Type: TABLE DATA; Schema: farm; Owner: -
--

INSERT INTO farm.user_crops (id, user_id, plot_id, seed_type_id, planted_at, stage_started_at, last_harvest_at, matured_at, withered_at, expected_ripe_at, expected_withered_at, harvest_count, current_stage_index, grow_status, bug_count, last_bug_at, last_care_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (6, 2, 9, 5, '2026-06-10 01:26:51.954603+00', '2026-06-10 01:31:13.851461+00', '2026-06-10 01:29:58.334724+00', '2026-06-10 01:30:38.548778+00', '2026-06-10 01:31:13.851461+00', '2026-06-10 01:30:38.334724+00', '2026-06-10 01:31:13.334724+00', 2, 6, 3, 0, NULL, NULL, '2026-06-10 01:26:51.954603+00', '2026-06-10 01:32:11.403304+00', 2, 2, NULL, 1, true, 24);
INSERT INTO farm.user_crops (id, user_id, plot_id, seed_type_id, planted_at, stage_started_at, last_harvest_at, matured_at, withered_at, expected_ripe_at, expected_withered_at, harvest_count, current_stage_index, grow_status, bug_count, last_bug_at, last_care_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (19, 2, 9, 5, '2026-06-11 02:51:14.645019+00', '2026-06-11 02:55:40.400855+00', '2026-06-11 02:54:24.462592+00', '2026-06-11 02:55:04.708864+00', '2026-06-11 02:55:40.400855+00', '2026-06-11 02:55:04.462592+00', '2026-06-11 02:55:39.462592+00', 2, 6, 3, 0, NULL, NULL, '2026-06-11 02:51:14.645019+00', '2026-06-11 03:00:26.114941+00', 2, 2, NULL, 1, true, 24);
INSERT INTO farm.user_crops (id, user_id, plot_id, seed_type_id, planted_at, stage_started_at, last_harvest_at, matured_at, withered_at, expected_ripe_at, expected_withered_at, harvest_count, current_stage_index, grow_status, bug_count, last_bug_at, last_care_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (5, 2, 8, 3, '2026-06-10 01:26:45.469666+00', '2026-06-10 01:32:42.714837+00', '2026-06-10 01:32:46.048459+00', '2026-06-10 01:32:42.881404+00', NULL, '2026-06-10 01:32:42.714837+00', '2026-06-10 01:33:43.714837+00', 3, 5, 2, 0, NULL, NULL, '2026-06-10 01:26:45.469666+00', '2026-06-10 01:32:46.048459+00', 2, 2, NULL, 1, true, 10);
INSERT INTO farm.user_crops (id, user_id, plot_id, seed_type_id, planted_at, stage_started_at, last_harvest_at, matured_at, withered_at, expected_ripe_at, expected_withered_at, harvest_count, current_stage_index, grow_status, bug_count, last_bug_at, last_care_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (17, 2, 7, 1, '2026-06-11 02:50:57.225854+00', '2026-06-11 02:52:34.225854+00', '2026-06-11 02:52:29.485989+00', '2026-06-11 02:52:35.418952+00', NULL, '2026-06-11 02:52:34.225854+00', '2026-06-11 02:53:12.225854+00', 1, 4, 2, 0, NULL, NULL, '2026-06-11 02:50:57.225854+00', '2026-06-11 02:52:29.485989+00', 2, 2, NULL, 1, true, 4);
INSERT INTO farm.user_crops (id, user_id, plot_id, seed_type_id, planted_at, stage_started_at, last_harvest_at, matured_at, withered_at, expected_ripe_at, expected_withered_at, harvest_count, current_stage_index, grow_status, bug_count, last_bug_at, last_care_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (20, 2, 10, 7, '2026-06-11 02:51:21.368807+00', '2026-06-11 02:57:25.420349+00', NULL, '2026-06-11 02:55:07.260918+00', '2026-06-11 02:57:25.420349+00', '2026-06-11 02:55:06.368807+00', '2026-06-11 02:57:24.368807+00', 0, 7, 3, 0, '2026-06-11 02:53:59.527803+00', '2026-06-11 02:54:09.064937+00', '2026-06-11 02:51:21.368807+00', '2026-06-11 03:00:27.28474+00', 2, 2, NULL, 1, true, 22);
INSERT INTO farm.user_crops (id, user_id, plot_id, seed_type_id, planted_at, stage_started_at, last_harvest_at, matured_at, withered_at, expected_ripe_at, expected_withered_at, harvest_count, current_stage_index, grow_status, bug_count, last_bug_at, last_care_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (1, 2, 7, 1, '2026-06-10 01:01:21.212042+00', '2026-06-10 01:04:37.668113+00', '2026-06-10 01:03:13.87776+00', '2026-06-10 01:03:59.110911+00', '2026-06-10 01:04:37.668113+00', '2026-06-10 01:03:58.87776+00', '2026-06-10 01:04:36.87776+00', 1, 5, 3, 0, NULL, NULL, '2026-06-10 01:01:21.212042+00', '2026-06-10 01:17:05.796212+00', 2, 2, NULL, 1, true, 19);
INSERT INTO farm.user_crops (id, user_id, plot_id, seed_type_id, planted_at, stage_started_at, last_harvest_at, matured_at, withered_at, expected_ripe_at, expected_withered_at, harvest_count, current_stage_index, grow_status, bug_count, last_bug_at, last_care_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (2, 2, 8, 3, '2026-06-10 01:01:32.247965+00', '2026-06-10 01:05:48.337061+00', NULL, '2026-06-10 01:04:47.210607+00', '2026-06-10 01:05:48.337061+00', '2026-06-10 01:04:46.247965+00', '2026-06-10 01:05:48.247965+00', 0, 6, 3, 1, '2026-06-10 01:03:34.951117+00', NULL, '2026-06-10 01:01:32.247965+00', '2026-06-10 01:17:07.223471+00', 2, 2, NULL, 1, true, 18);
INSERT INTO farm.user_crops (id, user_id, plot_id, seed_type_id, planted_at, stage_started_at, last_harvest_at, matured_at, withered_at, expected_ripe_at, expected_withered_at, harvest_count, current_stage_index, grow_status, bug_count, last_bug_at, last_care_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (3, 2, 9, 2, '2026-06-10 01:01:51.721551+00', '2026-06-10 01:05:37.860875+00', NULL, '2026-06-10 01:04:37.668113+00', '2026-06-10 01:05:37.860875+00', '2026-06-10 01:04:36.721551+00', '2026-06-10 01:05:36.721551+00', 0, 5, 3, 2, '2026-06-10 01:04:37.668113+00', NULL, '2026-06-10 01:01:51.721551+00', '2026-06-10 01:17:08.688366+00', 2, 2, NULL, 1, true, 15);
INSERT INTO farm.user_crops (id, user_id, plot_id, seed_type_id, planted_at, stage_started_at, last_harvest_at, matured_at, withered_at, expected_ripe_at, expected_withered_at, harvest_count, current_stage_index, grow_status, bug_count, last_bug_at, last_care_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (9, 2, 12, 3, '2026-06-10 01:27:43.516907+00', '2026-06-10 01:34:07.824843+00', '2026-06-10 01:35:05.406389+00', '2026-06-10 01:34:08.901757+00', NULL, '2026-06-10 01:34:07.824843+00', '2026-06-10 01:35:08.824843+00', 3, 5, 2, 0, NULL, NULL, '2026-06-10 01:27:43.516907+00', '2026-06-10 01:35:05.406389+00', 2, 2, NULL, 1, true, 10);
INSERT INTO farm.user_crops (id, user_id, plot_id, seed_type_id, planted_at, stage_started_at, last_harvest_at, matured_at, withered_at, expected_ripe_at, expected_withered_at, harvest_count, current_stage_index, grow_status, bug_count, last_bug_at, last_care_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (22, 2, 11, 5, '2026-06-11 02:51:37.949033+00', '2026-06-11 02:56:13.920902+00', NULL, '2026-06-11 02:55:15.02432+00', '2026-06-11 02:56:13.920902+00', '2026-06-11 02:55:14.949033+00', '2026-06-11 02:56:12.949033+00', 0, 6, 3, 0, '2026-06-11 02:52:20.192952+00', '2026-06-11 02:52:25.4699+00', '2026-06-11 02:51:37.949033+00', '2026-06-11 03:00:28.789525+00', 2, 2, NULL, 1, true, 17);
INSERT INTO farm.user_crops (id, user_id, plot_id, seed_type_id, planted_at, stage_started_at, last_harvest_at, matured_at, withered_at, expected_ripe_at, expected_withered_at, harvest_count, current_stage_index, grow_status, bug_count, last_bug_at, last_care_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (8, 2, 10, 3, '2026-06-10 01:27:38.939009+00', '2026-06-10 01:34:09.48072+00', '2026-06-10 01:35:07.007292+00', '2026-06-10 01:34:10.683827+00', NULL, '2026-06-10 01:34:09.48072+00', '2026-06-10 01:35:10.48072+00', 3, 5, 2, 0, NULL, NULL, '2026-06-10 01:27:38.939009+00', '2026-06-10 01:35:07.007292+00', 2, 2, NULL, 1, true, 11);
INSERT INTO farm.user_crops (id, user_id, plot_id, seed_type_id, planted_at, stage_started_at, last_harvest_at, matured_at, withered_at, expected_ripe_at, expected_withered_at, harvest_count, current_stage_index, grow_status, bug_count, last_bug_at, last_care_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (4, 2, 7, 1, '2026-06-10 01:25:26.133423+00', '2026-06-10 01:27:03.133423+00', '2026-06-10 01:27:07.390329+00', '2026-06-10 01:27:04.22778+00', NULL, '2026-06-10 01:27:03.133423+00', '2026-06-10 01:27:41.133423+00', 1, 4, 2, 0, NULL, NULL, '2026-06-10 01:25:26.133423+00', '2026-06-10 01:27:07.390329+00', 2, 2, NULL, 1, true, 4);
INSERT INTO farm.user_crops (id, user_id, plot_id, seed_type_id, planted_at, stage_started_at, last_harvest_at, matured_at, withered_at, expected_ripe_at, expected_withered_at, harvest_count, current_stage_index, grow_status, bug_count, last_bug_at, last_care_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (14, 1, 1, 1, '2026-06-10 17:08:46.242099+00', '2026-06-10 17:10:23.242099+00', '2026-06-10 17:10:28.599637+00', '2026-06-10 17:10:23.659835+00', NULL, '2026-06-10 17:10:23.242099+00', '2026-06-10 17:11:01.242099+00', 1, 4, 2, 0, NULL, NULL, '2026-06-10 17:08:46.242099+00', '2026-06-10 17:10:28.599637+00', 1, 1, NULL, 1, true, 4);
INSERT INTO farm.user_crops (id, user_id, plot_id, seed_type_id, planted_at, stage_started_at, last_harvest_at, matured_at, withered_at, expected_ripe_at, expected_withered_at, harvest_count, current_stage_index, grow_status, bug_count, last_bug_at, last_care_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (21, 2, 31, 5, '2026-06-11 02:51:30.67837+00', '2026-06-11 02:55:55.476036+00', '2026-06-11 02:54:21.402818+00', '2026-06-11 02:55:12.437294+00', '2026-06-11 02:55:55.476036+00', '2026-06-11 02:55:11.402818+00', '2026-06-11 02:55:55.402818+00', 1, 6, 3, 0, NULL, NULL, '2026-06-11 02:51:30.67837+00', '2026-06-11 03:00:30.858032+00', 2, 2, NULL, 1, true, 26);
INSERT INTO farm.user_crops (id, user_id, plot_id, seed_type_id, planted_at, stage_started_at, last_harvest_at, matured_at, withered_at, expected_ripe_at, expected_withered_at, harvest_count, current_stage_index, grow_status, bug_count, last_bug_at, last_care_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (23, 7, 39, 1, '2026-06-11 03:05:53.906755+00', '2026-06-11 03:08:09.006643+00', '2026-06-11 03:08:20.522726+00', '2026-06-11 03:08:10.156251+00', NULL, '2026-06-11 03:08:09.006643+00', '2026-06-11 03:08:47.006643+00', 2, 4, 2, 0, NULL, NULL, '2026-06-11 03:05:53.906755+00', '2026-06-11 03:08:20.522726+00', 7, 7, NULL, 1, true, 6);
INSERT INTO farm.user_crops (id, user_id, plot_id, seed_type_id, planted_at, stage_started_at, last_harvest_at, matured_at, withered_at, expected_ripe_at, expected_withered_at, harvest_count, current_stage_index, grow_status, bug_count, last_bug_at, last_care_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (16, 2, 12, 2, '2026-06-11 02:50:52.81869+00', '2026-06-11 02:57:02.526973+00', '2026-06-11 02:54:19.484014+00', '2026-06-11 02:55:48.885613+00', '2026-06-11 02:57:02.526973+00', '2026-06-11 02:55:47.484014+00', '2026-06-11 02:57:02.484014+00', 1, 5, 3, 0, NULL, NULL, '2026-06-11 02:50:52.81869+00', '2026-06-11 03:00:35.60835+00', 2, 2, NULL, 1, true, 7);
INSERT INTO farm.user_crops (id, user_id, plot_id, seed_type_id, planted_at, stage_started_at, last_harvest_at, matured_at, withered_at, expected_ripe_at, expected_withered_at, harvest_count, current_stage_index, grow_status, bug_count, last_bug_at, last_care_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (15, 1, 2, 2, '2026-06-10 17:08:52.060662+00', '2026-06-10 17:13:25.113018+00', '2026-06-10 17:13:31.122496+00', '2026-06-10 17:13:25.256572+00', NULL, '2026-06-10 17:13:25.113018+00', '2026-06-10 17:14:31.113018+00', 2, 4, 2, 0, NULL, NULL, '2026-06-10 17:08:52.060662+00', '2026-06-10 17:13:31.122496+00', 1, 1, NULL, 1, true, 6);
INSERT INTO farm.user_crops (id, user_id, plot_id, seed_type_id, planted_at, stage_started_at, last_harvest_at, matured_at, withered_at, expected_ripe_at, expected_withered_at, harvest_count, current_stage_index, grow_status, bug_count, last_bug_at, last_care_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (12, 2, 9, 7, '2026-06-10 01:32:24.97711+00', '2026-06-10 02:20:41.77234+00', NULL, '2026-06-10 01:35:25.329484+00', '2026-06-10 02:20:41.77234+00', '2026-06-10 01:35:24.97711+00', '2026-06-10 01:37:14.97711+00', 0, 7, 3, 1, '2026-06-10 01:36:26.224728+00', '2026-06-10 01:35:09.32671+00', '2026-06-10 01:32:24.97711+00', '2026-06-11 01:56:27.27443+00', 2, 2, NULL, 1, true, 8);
INSERT INTO farm.user_crops (id, user_id, plot_id, seed_type_id, planted_at, stage_started_at, last_harvest_at, matured_at, withered_at, expected_ripe_at, expected_withered_at, harvest_count, current_stage_index, grow_status, bug_count, last_bug_at, last_care_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (10, 2, 11, 5, '2026-06-10 01:29:03.561793+00', '2026-06-10 01:34:35.276757+00', '2026-06-10 01:33:01.240837+00', '2026-06-10 01:33:51.983313+00', '2026-06-10 01:34:35.276757+00', '2026-06-10 01:33:51.240837+00', '2026-06-10 01:34:35.240837+00', 2, 6, 3, 1, '2026-06-10 01:33:51.983313+00', NULL, '2026-06-10 01:29:03.561793+00', '2026-06-11 01:56:28.858074+00', 2, 2, NULL, 1, true, 22);
INSERT INTO farm.user_crops (id, user_id, plot_id, seed_type_id, planted_at, stage_started_at, last_harvest_at, matured_at, withered_at, expected_ripe_at, expected_withered_at, harvest_count, current_stage_index, grow_status, bug_count, last_bug_at, last_care_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (13, 2, 31, 2, '2026-06-10 01:32:29.136096+00', '2026-06-10 01:36:39.342133+00', NULL, '2026-06-10 01:35:33.36003+00', '2026-06-10 01:36:39.342133+00', '2026-06-10 01:35:32.136096+00', '2026-06-10 01:36:39.136096+00', 0, 5, 3, 0, NULL, NULL, '2026-06-10 01:32:29.136096+00', '2026-06-11 01:56:30.570616+00', 2, 2, NULL, 1, true, 19);
INSERT INTO farm.user_crops (id, user_id, plot_id, seed_type_id, planted_at, stage_started_at, last_harvest_at, matured_at, withered_at, expected_ripe_at, expected_withered_at, harvest_count, current_stage_index, grow_status, bug_count, last_bug_at, last_care_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (11, 2, 7, 7, '2026-06-10 01:29:49.754046+00', '2026-06-10 02:20:41.77234+00', '2026-06-10 01:35:06.124797+00', '2026-06-10 01:36:01.481393+00', '2026-06-10 02:20:41.77234+00', '2026-06-10 01:36:01.124797+00', '2026-06-10 01:37:51.124797+00', 2, 7, 3, 0, NULL, NULL, '2026-06-10 01:29:49.754046+00', '2026-06-11 01:56:32.721951+00', 2, 2, NULL, 1, true, 13);
INSERT INTO farm.user_crops (id, user_id, plot_id, seed_type_id, planted_at, stage_started_at, last_harvest_at, matured_at, withered_at, expected_ripe_at, expected_withered_at, harvest_count, current_stage_index, grow_status, bug_count, last_bug_at, last_care_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (7, 2, 7, 1, '2026-06-10 01:27:12.831025+00', '2026-06-10 01:28:49.831025+00', '2026-06-10 01:28:57.612627+00', '2026-06-10 01:28:51.085008+00', NULL, '2026-06-10 01:28:49.831025+00', '2026-06-10 01:29:27.831025+00', 1, 4, 2, 0, NULL, NULL, '2026-06-10 01:27:12.831025+00', '2026-06-10 01:28:57.612627+00', 2, 2, NULL, 1, true, 4);
INSERT INTO farm.user_crops (id, user_id, plot_id, seed_type_id, planted_at, stage_started_at, last_harvest_at, matured_at, withered_at, expected_ripe_at, expected_withered_at, harvest_count, current_stage_index, grow_status, bug_count, last_bug_at, last_care_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (25, 7, 41, 3, '2026-06-11 03:07:57.919902+00', '2026-06-11 03:11:49.492126+00', NULL, '2026-06-11 03:10:54.426737+00', '2026-06-11 03:11:49.492126+00', '2026-06-11 03:10:52.919902+00', '2026-06-11 03:11:47.919902+00', 0, 6, 3, 2, '2026-06-11 03:10:54.426737+00', NULL, '2026-06-11 03:07:57.919902+00', '2026-06-11 03:12:00.507879+00', 7, 7, NULL, 1, false, 15);
INSERT INTO farm.user_crops (id, user_id, plot_id, seed_type_id, planted_at, stage_started_at, last_harvest_at, matured_at, withered_at, expected_ripe_at, expected_withered_at, harvest_count, current_stage_index, grow_status, bug_count, last_bug_at, last_care_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (27, 7, 43, 5, '2026-06-11 03:08:06.101864+00', '2026-06-11 03:10:51.502426+00', NULL, '2026-06-11 03:10:16.688647+00', '2026-06-11 03:10:51.502426+00', '2026-06-11 03:10:16.101864+00', '2026-06-11 03:10:51.101864+00', 0, 6, 3, 0, NULL, NULL, '2026-06-11 03:08:06.101864+00', '2026-06-11 03:11:05.220085+00', 7, 7, NULL, 1, false, 21);
INSERT INTO farm.user_crops (id, user_id, plot_id, seed_type_id, planted_at, stage_started_at, last_harvest_at, matured_at, withered_at, expected_ripe_at, expected_withered_at, harvest_count, current_stage_index, grow_status, bug_count, last_bug_at, last_care_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (24, 7, 40, 2, '2026-06-11 03:05:58.540211+00', '2026-06-11 03:08:37.671304+00', '2026-06-11 03:08:37.671304+00', NULL, NULL, '2026-06-11 03:09:47.671304+00', '2026-06-11 03:10:47.671304+00', 1, 3, 1, 0, NULL, NULL, '2026-06-11 03:05:58.540211+00', '2026-06-11 03:09:10.776502+00', 7, 7, NULL, 1, true, 6);
INSERT INTO farm.user_crops (id, user_id, plot_id, seed_type_id, planted_at, stage_started_at, last_harvest_at, matured_at, withered_at, expected_ripe_at, expected_withered_at, harvest_count, current_stage_index, grow_status, bug_count, last_bug_at, last_care_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (18, 2, 8, 3, '2026-06-11 02:51:09.648615+00', '2026-06-11 02:55:58.38374+00', NULL, '2026-06-11 02:54:49.664207+00', '2026-06-11 02:55:58.38374+00', '2026-06-11 02:54:48.648615+00', '2026-06-11 02:55:57.648615+00', 0, 6, 3, 0, NULL, NULL, '2026-06-11 02:51:09.648615+00', '2026-06-11 03:00:25.093313+00', 2, 2, NULL, 1, true, 24);
INSERT INTO farm.user_crops (id, user_id, plot_id, seed_type_id, planted_at, stage_started_at, last_harvest_at, matured_at, withered_at, expected_ripe_at, expected_withered_at, harvest_count, current_stage_index, grow_status, bug_count, last_bug_at, last_care_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (26, 7, 42, 4, '2026-06-11 03:08:02.001704+00', '2026-06-11 03:11:24.553285+00', NULL, '2026-06-11 03:10:42.498209+00', '2026-06-11 03:11:24.553285+00', '2026-06-11 03:10:42.001704+00', '2026-06-11 03:11:24.001704+00', 0, 6, 3, 0, '2026-06-11 03:08:33.341982+00', '2026-06-11 03:08:31.795689+00', '2026-06-11 03:08:02.001704+00', '2026-06-11 03:11:37.205405+00', 7, 7, NULL, 1, false, 20);
INSERT INTO farm.user_crops (id, user_id, plot_id, seed_type_id, planted_at, stage_started_at, last_harvest_at, matured_at, withered_at, expected_ripe_at, expected_withered_at, harvest_count, current_stage_index, grow_status, bug_count, last_bug_at, last_care_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (29, 7, 45, 7, '2026-06-11 03:08:16.260668+00', '2026-06-11 03:14:19.883859+00', NULL, '2026-06-11 03:12:01.879339+00', '2026-06-11 03:14:19.883859+00', '2026-06-11 03:12:01.260668+00', '2026-06-11 03:14:19.260668+00', 0, 7, 3, 1, '2026-06-11 03:12:03.300385+00', NULL, '2026-06-11 03:08:16.260668+00', '2026-06-11 03:14:19.883859+00', 7, 7, NULL, 1, false, 7);
INSERT INTO farm.user_crops (id, user_id, plot_id, seed_type_id, planted_at, stage_started_at, last_harvest_at, matured_at, withered_at, expected_ripe_at, expected_withered_at, harvest_count, current_stage_index, grow_status, bug_count, last_bug_at, last_care_at, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (28, 7, 44, 6, '2026-06-11 03:08:11.325239+00', '2026-06-11 03:11:28.559231+00', NULL, NULL, '2026-06-11 03:11:28.559231+00', '2026-06-11 03:11:28.325239+00', '2026-06-11 03:11:28.325239+00', 0, 7, 3, 0, NULL, NULL, '2026-06-11 03:08:11.325239+00', '2026-06-11 03:11:28.559231+00', 7, 7, NULL, 1, false, 5);


--
-- TOC entry 3792 (class 0 OID 29395)
-- Dependencies: 242
-- Data for Name: user_fruits; Type: TABLE DATA; Schema: farm; Owner: -
--

INSERT INTO farm.user_fruits (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (5, 1, 1, 5, 0, '2026-06-10 17:10:28.599637+00', '2026-06-10 17:10:28.599637+00', 1, 1, NULL, 1, false, 0);
INSERT INTO farm.user_fruits (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (6, 1, 2, 6, 0, '2026-06-10 17:12:07.113018+00', '2026-06-10 17:13:31.122496+00', 1, 1, NULL, 1, false, 0);
INSERT INTO farm.user_fruits (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (3, 2, 3, 53, 0, '2026-06-10 01:30:01.164894+00', '2026-06-11 02:03:50.997408+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_fruits (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (1, 2, 1, 5, 0, '2026-06-10 01:03:13.87776+00', '2026-06-11 02:52:29.485989+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_fruits (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (7, 2, 2, 3, 0, '2026-06-11 02:54:19.484014+00', '2026-06-11 02:54:19.484014+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_fruits (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (2, 2, 5, 33, 0, '2026-06-10 01:29:13.912811+00', '2026-06-11 02:54:24.462592+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_fruits (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (4, 2, 7, 0, 0, '2026-06-10 01:32:53.800599+00', '2026-06-11 02:54:38.807743+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_fruits (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (9, 7, 2, 3, 0, '2026-06-11 03:08:37.671304+00', '2026-06-11 03:08:37.671304+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_fruits (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (8, 7, 1, 9, 0, '2026-06-11 03:07:24.006643+00', '2026-06-11 03:09:22.717827+00', 7, 7, NULL, 1, false, 0);


--
-- TOC entry 3796 (class 0 OID 29428)
-- Dependencies: 246
-- Data for Name: user_inventory_flows; Type: TABLE DATA; Schema: farm; Owner: -
--

INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (1, 2, 'SEED', 1, 'EXPENSE', 1, 18, 17, 0, 0, 'PLANT', 'PLANT:1:1781053281', '2026-06-10 01:01:21.212042+00', '{"cropId": 1, "plotId": 7}', '2026-06-10 01:01:21.212042+00', '2026-06-10 01:01:21.212042+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (2, 2, 'SEED', 3, 'EXPENSE', 1, 10, 9, 0, 0, 'PLANT', 'PLANT:2:1781053292', '2026-06-10 01:01:32.247965+00', '{"cropId": 2, "plotId": 8}', '2026-06-10 01:01:32.247965+00', '2026-06-10 01:01:32.247965+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (3, 2, 'SEED', 2, 'EXPENSE', 1, 18, 17, 0, 0, 'PLANT', 'PLANT:3:1781053311', '2026-06-10 01:01:51.721551+00', '{"cropId": 3, "plotId": 9}', '2026-06-10 01:01:51.721551+00', '2026-06-10 01:01:51.721551+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (4, 2, 'FRUIT', 1, 'INCOME', 3, 0, 3, 0, 0, 'HARVEST', 'HARVEST:1:1781053393', '2026-06-10 01:03:13.87776+00', '{"cropId": 1, "plotId": 7}', '2026-06-10 01:03:13.87776+00', '2026-06-10 01:03:13.87776+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (5, 2, 'SEED', 1, 'INCOME', 1, 17, 18, 0, 0, 'BUY_SEED', '1:1781053450', '2026-06-10 01:04:10.274618+00', '{"seedTypeId": 1, "totalCostCoin": 10}', '2026-06-10 01:04:10.274618+00', '2026-06-10 01:04:10.274618+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (6, 2, 'FRUIT', 1, 'EXPENSE', 1, 3, 2, 0, 0, 'SELL_FRUIT', '1:1781053479', '2026-06-10 01:04:39.403165+00', '{"seedTypeId": 1, "unitFruitPrice": 14}', '2026-06-10 01:04:39.403165+00', '2026-06-10 01:04:39.403165+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (7, 2, 'FRUIT', 1, 'EXPENSE', 1, 2, 1, 0, 0, 'SELL_FRUIT', '1:1781053486', '2026-06-10 01:04:46.010758+00', '{"seedTypeId": 1, "unitFruitPrice": 14}', '2026-06-10 01:04:46.010758+00', '2026-06-10 01:04:46.010758+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (8, 2, 'SEED', 1, 'EXPENSE', 1, 18, 17, 0, 0, 'PLANT', 'PLANT:4:1781054726', '2026-06-10 01:25:26.133423+00', '{"cropId": 4, "plotId": 7}', '2026-06-10 01:25:26.133423+00', '2026-06-10 01:25:26.133423+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (9, 2, 'SEED', 3, 'EXPENSE', 1, 9, 8, 0, 0, 'PLANT', 'PLANT:5:1781054805', '2026-06-10 01:26:45.469666+00', '{"cropId": 5, "plotId": 8}', '2026-06-10 01:26:45.469666+00', '2026-06-10 01:26:45.469666+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (10, 2, 'SEED', 5, 'EXPENSE', 1, 16, 15, 0, 0, 'PLANT', 'PLANT:6:1781054811', '2026-06-10 01:26:51.954603+00', '{"cropId": 6, "plotId": 9}', '2026-06-10 01:26:51.954603+00', '2026-06-10 01:26:51.954603+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (11, 2, 'FRUIT', 1, 'INCOME', 5, 1, 6, 0, 0, 'HARVEST', 'HARVEST:4:1781054827', '2026-06-10 01:27:07.390329+00', '{"cropId": 4, "plotId": 7}', '2026-06-10 01:27:07.390329+00', '2026-06-10 01:27:07.390329+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (12, 2, 'SEED', 1, 'EXPENSE', 1, 17, 16, 0, 0, 'PLANT', 'PLANT:7:1781054832', '2026-06-10 01:27:12.831025+00', '{"cropId": 7, "plotId": 7}', '2026-06-10 01:27:12.831025+00', '2026-06-10 01:27:12.831025+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (13, 2, 'SEED', 3, 'EXPENSE', 1, 8, 7, 0, 0, 'PLANT', 'PLANT:8:1781054858', '2026-06-10 01:27:38.939009+00', '{"cropId": 8, "plotId": 10}', '2026-06-10 01:27:38.939009+00', '2026-06-10 01:27:38.939009+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (14, 2, 'SEED', 3, 'EXPENSE', 1, 7, 6, 0, 0, 'PLANT', 'PLANT:9:1781054863', '2026-06-10 01:27:43.516907+00', '{"cropId": 9, "plotId": 12}', '2026-06-10 01:27:43.516907+00', '2026-06-10 01:27:43.516907+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (15, 2, 'FRUIT', 1, 'INCOME', 5, 6, 11, 0, 0, 'HARVEST', 'HARVEST:7:1781054937', '2026-06-10 01:28:57.612627+00', '{"cropId": 7, "plotId": 7}', '2026-06-10 01:28:57.612627+00', '2026-06-10 01:28:57.612627+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (16, 2, 'SEED', 5, 'EXPENSE', 1, 15, 14, 0, 0, 'PLANT', 'PLANT:10:1781054943', '2026-06-10 01:29:03.561793+00', '{"cropId": 10, "plotId": 11}', '2026-06-10 01:29:03.561793+00', '2026-06-10 01:29:03.561793+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (17, 2, 'FRUIT', 5, 'INCOME', 5, 0, 5, 0, 0, 'HARVEST', 'HARVEST:6:1781054953', '2026-06-10 01:29:13.912811+00', '{"cropId": 6, "plotId": 9}', '2026-06-10 01:29:13.912811+00', '2026-06-10 01:29:13.912811+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (18, 2, 'FRUIT', 1, 'EXPENSE', 11, 11, 0, 0, 0, 'SELL_FRUIT', '1:1781054964', '2026-06-10 01:29:24.73758+00', '{"seedTypeId": 1, "unitFruitPrice": 15}', '2026-06-10 01:29:24.73758+00', '2026-06-10 01:29:24.73758+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (19, 2, 'FRUIT', 5, 'EXPENSE', 2, 5, 3, 0, 0, 'SELL_FRUIT', '5:1781054968', '2026-06-10 01:29:28.876493+00', '{"seedTypeId": 5, "unitFruitPrice": 22}', '2026-06-10 01:29:28.876493+00', '2026-06-10 01:29:28.876493+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (20, 2, 'SEED', 7, 'INCOME', 2, 0, 2, 0, 0, 'BUY_SEED', '7:1781054978', '2026-06-10 01:29:38.925244+00', '{"seedTypeId": 7, "totalCostCoin": 72}', '2026-06-10 01:29:38.925244+00', '2026-06-10 01:29:38.925244+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (21, 2, 'SEED', 7, 'EXPENSE', 1, 2, 1, 0, 0, 'PLANT', 'PLANT:11:1781054989', '2026-06-10 01:29:49.754046+00', '{"cropId": 11, "plotId": 7}', '2026-06-10 01:29:49.754046+00', '2026-06-10 01:29:49.754046+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (22, 2, 'FRUIT', 5, 'INCOME', 5, 3, 8, 0, 0, 'HARVEST', 'HARVEST:6:1781054998', '2026-06-10 01:29:58.334724+00', '{"cropId": 6, "plotId": 9}', '2026-06-10 01:29:58.334724+00', '2026-06-10 01:29:58.334724+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (23, 2, 'FRUIT', 3, 'INCOME', 6, 0, 6, 0, 0, 'HARVEST', 'HARVEST:5:1781055001', '2026-06-10 01:30:01.164894+00', '{"cropId": 5, "plotId": 8}', '2026-06-10 01:30:01.164894+00', '2026-06-10 01:30:01.164894+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (24, 2, 'FRUIT', 3, 'INCOME', 6, 6, 12, 0, 0, 'HARVEST', 'HARVEST:5:1781055090', '2026-06-10 01:31:30.714837+00', '{"cropId": 5, "plotId": 8}', '2026-06-10 01:31:30.714837+00', '2026-06-10 01:31:30.714837+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (25, 2, 'FRUIT', 3, 'INCOME', 6, 12, 18, 0, 0, 'HARVEST', 'HARVEST:8:1781055095', '2026-06-10 01:31:35.167132+00', '{"cropId": 8, "plotId": 10}', '2026-06-10 01:31:35.167132+00', '2026-06-10 01:31:35.167132+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (26, 2, 'FRUIT', 3, 'INCOME', 6, 18, 24, 0, 0, 'HARVEST', 'HARVEST:9:1781055101', '2026-06-10 01:31:41.136431+00', '{"cropId": 9, "plotId": 12}', '2026-06-10 01:31:41.136431+00', '2026-06-10 01:31:41.136431+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (27, 2, 'FRUIT', 5, 'INCOME', 5, 8, 13, 0, 0, 'HARVEST', 'HARVEST:10:1781055127', '2026-06-10 01:32:07.443078+00', '{"cropId": 10, "plotId": 11}', '2026-06-10 01:32:07.443078+00', '2026-06-10 01:32:07.443078+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (28, 2, 'SEED', 7, 'EXPENSE', 1, 1, 0, 0, 0, 'PLANT', 'PLANT:12:1781055144', '2026-06-10 01:32:24.97711+00', '{"cropId": 12, "plotId": 9}', '2026-06-10 01:32:24.97711+00', '2026-06-10 01:32:24.97711+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (29, 2, 'SEED', 2, 'EXPENSE', 1, 17, 16, 0, 0, 'PLANT', 'PLANT:13:1781055149', '2026-06-10 01:32:29.136096+00', '{"cropId": 13, "plotId": 31}', '2026-06-10 01:32:29.136096+00', '2026-06-10 01:32:29.136096+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (30, 2, 'FRUIT', 3, 'INCOME', 6, 24, 30, 0, 0, 'HARVEST', 'HARVEST:5:1781055166', '2026-06-10 01:32:46.048459+00', '{"cropId": 5, "plotId": 8}', '2026-06-10 01:32:46.048459+00', '2026-06-10 01:32:46.048459+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (31, 2, 'FRUIT', 7, 'INCOME', 6, 0, 6, 0, 0, 'HARVEST', 'HARVEST:11:1781055173', '2026-06-10 01:32:53.800599+00', '{"cropId": 11, "plotId": 7}', '2026-06-10 01:32:53.800599+00', '2026-06-10 01:32:53.800599+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (32, 2, 'FRUIT', 3, 'INCOME', 6, 30, 36, 0, 0, 'HARVEST', 'HARVEST:9:1781055175', '2026-06-10 01:32:55.824843+00', '{"cropId": 9, "plotId": 12}', '2026-06-10 01:32:55.824843+00', '2026-06-10 01:32:55.824843+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (33, 2, 'FRUIT', 3, 'INCOME', 6, 36, 42, 0, 0, 'HARVEST', 'HARVEST:8:1781055177', '2026-06-10 01:32:57.48072+00', '{"cropId": 8, "plotId": 10}', '2026-06-10 01:32:57.48072+00', '2026-06-10 01:32:57.48072+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (34, 2, 'FRUIT', 5, 'INCOME', 5, 13, 18, 0, 0, 'HARVEST', 'HARVEST:10:1781055181', '2026-06-10 01:33:01.240837+00', '{"cropId": 10, "plotId": 11}', '2026-06-10 01:33:01.240837+00', '2026-06-10 01:33:01.240837+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (35, 2, 'FRUIT', 3, 'INCOME', 6, 42, 48, 0, 0, 'HARVEST', 'HARVEST:9:1781055305', '2026-06-10 01:35:05.406389+00', '{"cropId": 9, "plotId": 12}', '2026-06-10 01:35:05.406389+00', '2026-06-10 01:35:05.406389+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (36, 2, 'FRUIT', 7, 'INCOME', 6, 6, 12, 0, 0, 'HARVEST', 'HARVEST:11:1781055306', '2026-06-10 01:35:06.124797+00', '{"cropId": 11, "plotId": 7}', '2026-06-10 01:35:06.124797+00', '2026-06-10 01:35:06.124797+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (37, 2, 'FRUIT', 3, 'INCOME', 6, 48, 54, 0, 0, 'HARVEST', 'HARVEST:8:1781055307', '2026-06-10 01:35:07.007292+00', '{"cropId": 8, "plotId": 10}', '2026-06-10 01:35:07.007292+00', '2026-06-10 01:35:07.007292+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (38, 1, 'SEED', 7, 'INCOME', 2, 12, 14, 0, 0, 'BUY_SEED', '7:1781111304', '2026-06-10 17:08:24.688302+00', '{"seedTypeId": 7, "totalCostCoin": 72}', '2026-06-10 17:08:24.688302+00', '2026-06-10 17:08:24.688302+00', 1, 1, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (39, 1, 'SEED', 1, 'EXPENSE', 1, 24, 23, 0, 0, 'PLANT', 'PLANT:14:1781111326', '2026-06-10 17:08:46.242099+00', '{"cropId": 14, "plotId": 1}', '2026-06-10 17:08:46.242099+00', '2026-06-10 17:08:46.242099+00', 1, 1, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (40, 1, 'SEED', 2, 'EXPENSE', 1, 20, 19, 0, 0, 'PLANT', 'PLANT:15:1781111332', '2026-06-10 17:08:52.060662+00', '{"cropId": 15, "plotId": 2}', '2026-06-10 17:08:52.060662+00', '2026-06-10 17:08:52.060662+00', 1, 1, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (41, 1, 'FRUIT', 1, 'INCOME', 5, 0, 5, 0, 0, 'HARVEST', 'HARVEST:14:1781111428', '2026-06-10 17:10:28.599637+00', '{"cropId": 14, "plotId": 1}', '2026-06-10 17:10:28.599637+00', '2026-06-10 17:10:28.599637+00', 1, 1, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (42, 1, 'FRUIT', 2, 'INCOME', 3, 0, 3, 0, 0, 'HARVEST', 'HARVEST:15:1781111527', '2026-06-10 17:12:07.113018+00', '{"cropId": 15, "plotId": 2}', '2026-06-10 17:12:07.113018+00', '2026-06-10 17:12:07.113018+00', 1, 1, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (43, 1, 'FRUIT', 2, 'INCOME', 3, 3, 6, 0, 0, 'HARVEST', 'HARVEST:15:1781111611', '2026-06-10 17:13:31.122496+00', '{"cropId": 15, "plotId": 2}', '2026-06-10 17:13:31.122496+00', '2026-06-10 17:13:31.122496+00', 1, 1, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (44, 2, 'FRUIT', 3, 'EXPENSE', 1, 54, 53, 0, 0, 'SELL_FRUIT', '3:1781143430', '2026-06-11 02:03:50.997408+00', '{"seedTypeId": 3, "unitFruitPrice": 30}', '2026-06-11 02:03:50.997408+00', '2026-06-11 02:03:50.997408+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (45, 2, 'SEED', 7, 'INCOME', 1, 0, 1, 0, 0, 'BUY_SEED', '7:1781143480', '2026-06-11 02:04:40.766296+00', '{"seedTypeId": 7, "totalCostCoin": 36}', '2026-06-11 02:04:40.766296+00', '2026-06-11 02:04:40.766296+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (46, 2, 'SEED', 2, 'EXPENSE', 1, 16, 15, 0, 0, 'PLANT', 'PLANT:16:1781146252', '2026-06-11 02:50:52.81869+00', '{"cropId": 16, "plotId": 12}', '2026-06-11 02:50:52.81869+00', '2026-06-11 02:50:52.81869+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (47, 2, 'SEED', 1, 'EXPENSE', 1, 16, 15, 0, 0, 'PLANT', 'PLANT:17:1781146257', '2026-06-11 02:50:57.225854+00', '{"cropId": 17, "plotId": 7}', '2026-06-11 02:50:57.225854+00', '2026-06-11 02:50:57.225854+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (48, 2, 'SEED', 3, 'EXPENSE', 1, 6, 5, 0, 0, 'PLANT', 'PLANT:18:1781146269', '2026-06-11 02:51:09.648615+00', '{"cropId": 18, "plotId": 8}', '2026-06-11 02:51:09.648615+00', '2026-06-11 02:51:09.648615+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (49, 2, 'SEED', 5, 'EXPENSE', 1, 14, 13, 0, 0, 'PLANT', 'PLANT:19:1781146274', '2026-06-11 02:51:14.645019+00', '{"cropId": 19, "plotId": 9}', '2026-06-11 02:51:14.645019+00', '2026-06-11 02:51:14.645019+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (50, 2, 'SEED', 7, 'EXPENSE', 1, 1, 0, 0, 0, 'PLANT', 'PLANT:20:1781146281', '2026-06-11 02:51:21.368807+00', '{"cropId": 20, "plotId": 10}', '2026-06-11 02:51:21.368807+00', '2026-06-11 02:51:21.368807+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (51, 2, 'SEED', 5, 'EXPENSE', 1, 13, 12, 0, 0, 'PLANT', 'PLANT:21:1781146290', '2026-06-11 02:51:30.67837+00', '{"cropId": 21, "plotId": 31}', '2026-06-11 02:51:30.67837+00', '2026-06-11 02:51:30.67837+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (52, 2, 'SEED', 5, 'EXPENSE', 1, 12, 11, 0, 0, 'PLANT', 'PLANT:22:1781146297', '2026-06-11 02:51:37.949033+00', '{"cropId": 22, "plotId": 11}', '2026-06-11 02:51:37.949033+00', '2026-06-11 02:51:37.949033+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (53, 2, 'FRUIT', 1, 'INCOME', 5, 0, 5, 0, 0, 'HARVEST', 'HARVEST:17:1781146349', '2026-06-11 02:52:29.485989+00', '{"cropId": 17, "plotId": 7}', '2026-06-11 02:52:29.485989+00', '2026-06-11 02:52:29.485989+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (54, 2, 'FRUIT', 5, 'INCOME', 5, 18, 23, 0, 0, 'HARVEST', 'HARVEST:19:1781146426', '2026-06-11 02:53:46.825956+00', '{"cropId": 19, "plotId": 9}', '2026-06-11 02:53:46.825956+00', '2026-06-11 02:53:46.825956+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (55, 2, 'FRUIT', 2, 'INCOME', 3, 0, 3, 0, 0, 'HARVEST', 'HARVEST:16:1781146459', '2026-06-11 02:54:19.484014+00', '{"cropId": 16, "plotId": 12}', '2026-06-11 02:54:19.484014+00', '2026-06-11 02:54:19.484014+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (56, 2, 'FRUIT', 5, 'INCOME', 5, 23, 28, 0, 0, 'HARVEST', 'HARVEST:21:1781146461', '2026-06-11 02:54:21.402818+00', '{"cropId": 21, "plotId": 31}', '2026-06-11 02:54:21.402818+00', '2026-06-11 02:54:21.402818+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (57, 2, 'FRUIT', 5, 'INCOME', 5, 28, 33, 0, 0, 'HARVEST', 'HARVEST:19:1781146464', '2026-06-11 02:54:24.462592+00', '{"cropId": 19, "plotId": 9}', '2026-06-11 02:54:24.462592+00', '2026-06-11 02:54:24.462592+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (58, 2, 'FRUIT', 7, 'EXPENSE', 12, 12, 0, 0, 0, 'SELL_FRUIT', '7:1781146478', '2026-06-11 02:54:38.807743+00', '{"seedTypeId": 7, "unitFruitPrice": 40}', '2026-06-11 02:54:38.807743+00', '2026-06-11 02:54:38.807743+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (59, 7, 'SEED', 1, 'INCOME', 10, 0, 10, 0, 0, 'BUY_SEED', '1:1781147107', '2026-06-11 03:05:07.872216+00', '{"seedTypeId": 1, "totalCostCoin": 110}', '2026-06-11 03:05:07.872216+00', '2026-06-11 03:05:07.872216+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (60, 7, 'SEED', 2, 'INCOME', 10, 0, 10, 0, 0, 'BUY_SEED', '2:1781147112', '2026-06-11 03:05:12.9711+00', '{"seedTypeId": 2, "totalCostCoin": 120}', '2026-06-11 03:05:12.9711+00', '2026-06-11 03:05:12.9711+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (61, 7, 'SEED', 3, 'INCOME', 10, 0, 10, 0, 0, 'BUY_SEED', '3:1781147117', '2026-06-11 03:05:17.213595+00', '{"seedTypeId": 3, "totalCostCoin": 240}', '2026-06-11 03:05:17.213595+00', '2026-06-11 03:05:17.213595+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (62, 7, 'SEED', 4, 'INCOME', 10, 0, 10, 0, 0, 'BUY_SEED', '4:1781147120', '2026-06-11 03:05:20.847495+00', '{"seedTypeId": 4, "totalCostCoin": 160}', '2026-06-11 03:05:20.847495+00', '2026-06-11 03:05:20.847495+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (63, 7, 'SEED', 5, 'INCOME', 10, 0, 10, 0, 0, 'BUY_SEED', '5:1781147123', '2026-06-11 03:05:23.968736+00', '{"seedTypeId": 5, "totalCostCoin": 180}', '2026-06-11 03:05:23.968736+00', '2026-06-11 03:05:23.968736+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (64, 7, 'SEED', 6, 'INCOME', 10, 0, 10, 0, 0, 'BUY_SEED', '6:1781147128', '2026-06-11 03:05:28.659539+00', '{"seedTypeId": 6, "totalCostCoin": 280}', '2026-06-11 03:05:28.659539+00', '2026-06-11 03:05:28.659539+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (65, 7, 'SEED', 7, 'INCOME', 10, 0, 10, 0, 0, 'BUY_SEED', '7:1781147131', '2026-06-11 03:05:31.548288+00', '{"seedTypeId": 7, "totalCostCoin": 360}', '2026-06-11 03:05:31.548288+00', '2026-06-11 03:05:31.548288+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (66, 7, 'SEED', 1, 'EXPENSE', 1, 10, 9, 0, 0, 'PLANT', 'PLANT:23:1781147153', '2026-06-11 03:05:53.906755+00', '{"cropId": 23, "plotId": 39}', '2026-06-11 03:05:53.906755+00', '2026-06-11 03:05:53.906755+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (67, 7, 'SEED', 2, 'EXPENSE', 1, 10, 9, 0, 0, 'PLANT', 'PLANT:24:1781147158', '2026-06-11 03:05:58.540211+00', '{"cropId": 24, "plotId": 40}', '2026-06-11 03:05:58.540211+00', '2026-06-11 03:05:58.540211+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (69, 7, 'SEED', 3, 'EXPENSE', 1, 10, 9, 0, 0, 'PLANT', 'PLANT:25:1781147277', '2026-06-11 03:07:57.919902+00', '{"cropId": 25, "plotId": 41}', '2026-06-11 03:07:57.919902+00', '2026-06-11 03:07:57.919902+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (70, 7, 'SEED', 4, 'EXPENSE', 1, 10, 9, 0, 0, 'PLANT', 'PLANT:26:1781147282', '2026-06-11 03:08:02.001704+00', '{"cropId": 26, "plotId": 42}', '2026-06-11 03:08:02.001704+00', '2026-06-11 03:08:02.001704+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (72, 7, 'SEED', 6, 'EXPENSE', 1, 10, 9, 0, 0, 'PLANT', 'PLANT:28:1781147291', '2026-06-11 03:08:11.325239+00', '{"cropId": 28, "plotId": 44}', '2026-06-11 03:08:11.325239+00', '2026-06-11 03:08:11.325239+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (74, 7, 'FRUIT', 1, 'INCOME', 5, 5, 10, 0, 0, 'HARVEST', 'HARVEST:23:1781147300', '2026-06-11 03:08:20.522726+00', '{"cropId": 23, "plotId": 39}', '2026-06-11 03:08:20.522726+00', '2026-06-11 03:08:20.522726+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (68, 7, 'FRUIT', 1, 'INCOME', 5, 0, 5, 0, 0, 'HARVEST', 'HARVEST:23:1781147244', '2026-06-11 03:07:24.006643+00', '{"cropId": 23, "plotId": 39}', '2026-06-11 03:07:24.006643+00', '2026-06-11 03:07:24.006643+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (71, 7, 'SEED', 5, 'EXPENSE', 1, 10, 9, 0, 0, 'PLANT', 'PLANT:27:1781147286', '2026-06-11 03:08:06.101864+00', '{"cropId": 27, "plotId": 43}', '2026-06-11 03:08:06.101864+00', '2026-06-11 03:08:06.101864+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (73, 7, 'SEED', 7, 'EXPENSE', 1, 10, 9, 0, 0, 'PLANT', 'PLANT:29:1781147296', '2026-06-11 03:08:16.260668+00', '{"cropId": 29, "plotId": 45}', '2026-06-11 03:08:16.260668+00', '2026-06-11 03:08:16.260668+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (75, 7, 'FRUIT', 2, 'INCOME', 3, 0, 3, 0, 0, 'HARVEST', 'HARVEST:24:1781147317', '2026-06-11 03:08:37.671304+00', '{"cropId": 24, "plotId": 40}', '2026-06-11 03:08:37.671304+00', '2026-06-11 03:08:37.671304+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_inventory_flows (id, user_id, item_type, seed_type_id, operation_type, change_amount, before_amount, after_amount, before_frozen_amount, after_frozen_amount, biz_type, biz_id, occurred_at, ext_data, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (76, 7, 'FRUIT', 1, 'EXPENSE', 1, 10, 9, 0, 0, 'SELL_FRUIT', '1:1781147362', '2026-06-11 03:09:22.717827+00', '{"seedTypeId": 1, "unitFruitPrice": 15}', '2026-06-11 03:09:22.717827+00', '2026-06-11 03:09:22.717827+00', 7, 7, NULL, 1, false, 0);


--
-- TOC entry 3788 (class 0 OID 29359)
-- Dependencies: 238
-- Data for Name: user_plots; Type: TABLE DATA; Schema: farm; Owner: -
--

INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (1, 1, 1, 1, 0, false, '2026-06-09 18:35:46.794308+00', NULL, '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (2, 1, 2, 2, 0, false, '2026-06-09 18:35:46.794308+00', NULL, '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (7, 2, 1, 1, 0, false, '2026-06-09 18:35:46.794308+00', NULL, '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (8, 2, 2, 2, 0, false, '2026-06-09 18:35:46.794308+00', NULL, '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (13, 3, 1, 1, 0, false, '2026-06-09 18:35:46.794308+00', NULL, '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (14, 3, 2, 2, 0, false, '2026-06-09 18:35:46.794308+00', NULL, '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (15, 3, 1, 3, 600, true, NULL, '经验不足，待解锁', '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (16, 3, 2, 4, 850, true, NULL, '经验不足，待解锁', '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (17, 3, 3, 5, 1100, true, NULL, '经验不足，待解锁', '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (18, 3, 2, 6, 1350, true, NULL, '经验不足，待解锁', '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (19, 4, 1, 1, 0, false, '2026-06-09 18:35:46.794308+00', NULL, '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (20, 4, 2, 2, 0, false, '2026-06-09 18:35:46.794308+00', NULL, '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (21, 4, 1, 3, 600, true, NULL, '经验不足，待解锁', '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (22, 4, 2, 4, 850, true, NULL, '经验不足，待解锁', '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (23, 4, 3, 5, 1100, true, NULL, '经验不足，待解锁', '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (24, 4, 2, 6, 1350, true, NULL, '经验不足，待解锁', '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (25, 5, 1, 1, 0, false, '2026-06-09 18:35:46.794308+00', NULL, '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (26, 5, 2, 2, 0, false, '2026-06-09 18:35:46.794308+00', NULL, '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (27, 5, 1, 3, 600, true, NULL, '经验不足，待解锁', '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (28, 5, 2, 4, 850, true, NULL, '经验不足，待解锁', '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (29, 5, 3, 5, 1100, true, NULL, '经验不足，待解锁', '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (30, 5, 2, 6, 1350, true, NULL, '经验不足，待解锁', '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (9, 2, 1, 3, 600, false, '2026-06-10 01:01:44.408123+00', NULL, '2026-06-09 18:35:46.794308+00', '2026-06-10 01:01:44.408123+00', NULL, 2, NULL, 1, false, 1);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (10, 2, 2, 4, 850, false, '2026-06-10 01:27:00.432869+00', NULL, '2026-06-09 18:35:46.794308+00', '2026-06-10 01:27:00.432869+00', NULL, 2, NULL, 1, false, 1);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (11, 2, 3, 5, 1100, false, '2026-06-10 01:27:03.431518+00', NULL, '2026-06-09 18:35:46.794308+00', '2026-06-10 01:27:03.431518+00', NULL, 2, NULL, 1, false, 1);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (12, 2, 2, 6, 1350, false, '2026-06-10 01:27:21.610421+00', NULL, '2026-06-09 18:35:46.794308+00', '2026-06-10 01:27:21.610421+00', NULL, 2, NULL, 1, false, 1);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (31, 2, 2, 7, 600, false, '2026-06-10 01:32:03.67572+00', NULL, '2026-06-10 01:31:54.41517+00', '2026-06-10 01:32:03.67572+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (32, 6, 1, 1, 0, false, '2026-06-10 01:34:21.357978+00', NULL, '2026-06-10 01:34:21.357978+00', '2026-06-10 01:34:21.357978+00', 6, 6, NULL, 1, false, 0);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (33, 6, 1, 2, 100, false, '2026-06-10 01:34:21.357978+00', NULL, '2026-06-10 01:34:21.357978+00', '2026-06-10 01:34:21.357978+00', 6, 6, NULL, 1, false, 0);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (34, 6, 1, 3, 200, true, NULL, '待解锁', '2026-06-10 01:34:21.357978+00', '2026-06-10 01:34:21.357978+00', 6, 6, NULL, 1, false, 0);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (35, 6, 1, 4, 300, true, NULL, '待解锁', '2026-06-10 01:34:21.357978+00', '2026-06-10 01:34:21.357978+00', 6, 6, NULL, 1, false, 0);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (36, 6, 1, 5, 400, true, NULL, '待解锁', '2026-06-10 01:34:21.357978+00', '2026-06-10 01:34:21.357978+00', 6, 6, NULL, 1, false, 0);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (37, 6, 1, 6, 500, true, NULL, '待解锁', '2026-06-10 01:34:21.357978+00', '2026-06-10 01:34:21.357978+00', 6, 6, NULL, 1, false, 0);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (3, 1, 1, 3, 600, false, '2026-06-10 17:08:58.812548+00', NULL, '2026-06-09 18:35:46.794308+00', '2026-06-10 17:08:58.812548+00', NULL, 1, NULL, 1, false, 1);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (4, 1, 2, 4, 850, false, '2026-06-10 17:09:02.735159+00', NULL, '2026-06-09 18:35:46.794308+00', '2026-06-10 17:09:02.735159+00', NULL, 1, NULL, 1, false, 1);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (5, 1, 3, 5, 1100, false, '2026-06-10 17:09:08.293034+00', NULL, '2026-06-09 18:35:46.794308+00', '2026-06-10 17:09:08.293034+00', NULL, 1, NULL, 1, false, 1);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (6, 1, 2, 6, 1350, false, '2026-06-10 17:09:12.719063+00', NULL, '2026-06-09 18:35:46.794308+00', '2026-06-10 17:09:12.719063+00', NULL, 1, NULL, 1, false, 1);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (38, 2, 2, 8, 700, false, '2026-06-11 02:52:40.485005+00', NULL, '2026-06-11 02:52:17.235652+00', '2026-06-11 02:52:40.485005+00', 2, 2, NULL, 1, false, 1);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (39, 7, 1, 1, 0, false, '2026-06-11 03:02:21.972293+00', NULL, '2026-06-11 03:02:21.972293+00', '2026-06-11 03:02:21.972293+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (40, 7, 1, 2, 100, false, '2026-06-11 03:02:21.972293+00', NULL, '2026-06-11 03:02:21.972293+00', '2026-06-11 03:02:21.972293+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (41, 7, 1, 3, 200, false, '2026-06-11 03:06:04.090521+00', NULL, '2026-06-11 03:02:21.972293+00', '2026-06-11 03:06:04.090521+00', 7, 7, NULL, 1, false, 1);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (42, 7, 1, 4, 300, false, '2026-06-11 03:06:06.079344+00', NULL, '2026-06-11 03:02:21.972293+00', '2026-06-11 03:06:06.079344+00', 7, 7, NULL, 1, false, 1);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (43, 7, 1, 5, 400, false, '2026-06-11 03:06:09.353123+00', NULL, '2026-06-11 03:02:21.972293+00', '2026-06-11 03:06:09.353123+00', 7, 7, NULL, 1, false, 1);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (44, 7, 1, 6, 500, false, '2026-06-11 03:06:14.900323+00', NULL, '2026-06-11 03:02:21.972293+00', '2026-06-11 03:06:14.900323+00', 7, 7, NULL, 1, false, 1);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (45, 7, 2, 7, 600, false, '2026-06-11 03:06:47.72687+00', NULL, '2026-06-11 03:06:20.837517+00', '2026-06-11 03:06:47.72687+00', 7, 7, NULL, 1, false, 1);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (46, 7, 3, 8, 700, false, '2026-06-11 03:07:14.501419+00', NULL, '2026-06-11 03:07:10.144357+00', '2026-06-11 03:07:14.501419+00', 7, 7, NULL, 1, false, 1);
INSERT INTO farm.user_plots (id, user_id, soil_type_id, plot_index, unlock_experience_required, is_locked, unlocked_at, lock_reason, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (47, 7, 4, 9, 800, false, '2026-06-11 03:07:30.194233+00', NULL, '2026-06-11 03:07:19.966978+00', '2026-06-11 03:07:30.194233+00', 7, 7, NULL, 1, false, 1);


--
-- TOC entry 3786 (class 0 OID 29343)
-- Dependencies: 236
-- Data for Name: user_seeds; Type: TABLE DATA; Schema: farm; Owner: -
--

INSERT INTO farm.user_seeds (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (1, 5, 1, 10, 0, '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.user_seeds (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (2, 4, 1, 12, 0, '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.user_seeds (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (3, 3, 1, 15, 0, '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.user_seeds (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (6, 5, 2, 8, 0, '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.user_seeds (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (7, 4, 2, 10, 0, '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.user_seeds (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (8, 3, 2, 12, 0, '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.user_seeds (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (12, 1, 3, 14, 0, '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.user_seeds (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (13, 5, 5, 8, 0, '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.user_seeds (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (14, 4, 5, 10, 0, '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.user_seeds (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (15, 3, 5, 12, 0, '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.user_seeds (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (17, 1, 5, 28, 0, '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.user_seeds (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (18, 1, 6, 16, 0, '2026-06-09 18:35:46.794308+00', '2026-06-09 18:35:46.794308+00', NULL, NULL, NULL, 1, false, 0);
INSERT INTO farm.user_seeds (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (19, 1, 7, 14, 0, '2026-06-09 18:35:46.794308+00', '2026-06-10 17:08:24.688302+00', NULL, 1, NULL, 1, false, 0);
INSERT INTO farm.user_seeds (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (5, 1, 1, 23, 0, '2026-06-09 18:35:46.794308+00', '2026-06-10 17:08:46.242099+00', NULL, 1, NULL, 1, false, 0);
INSERT INTO farm.user_seeds (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (10, 1, 2, 19, 0, '2026-06-09 18:35:46.794308+00', '2026-06-10 17:08:52.060662+00', NULL, 1, NULL, 1, false, 0);
INSERT INTO farm.user_seeds (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (9, 2, 2, 15, 0, '2026-06-09 18:35:46.794308+00', '2026-06-11 02:50:52.81869+00', NULL, 2, NULL, 1, false, 0);
INSERT INTO farm.user_seeds (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (4, 2, 1, 15, 0, '2026-06-09 18:35:46.794308+00', '2026-06-11 02:50:57.225854+00', NULL, 2, NULL, 1, false, 0);
INSERT INTO farm.user_seeds (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (11, 2, 3, 5, 0, '2026-06-09 18:35:46.794308+00', '2026-06-11 02:51:09.648615+00', NULL, 2, NULL, 1, false, 0);
INSERT INTO farm.user_seeds (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (20, 2, 7, 0, 0, '2026-06-10 01:29:38.925244+00', '2026-06-11 02:51:21.368807+00', 2, 2, NULL, 1, false, 0);
INSERT INTO farm.user_seeds (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (16, 2, 5, 11, 0, '2026-06-09 18:35:46.794308+00', '2026-06-11 02:51:37.949033+00', NULL, 2, NULL, 1, false, 0);
INSERT INTO farm.user_seeds (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (21, 7, 1, 9, 0, '2026-06-11 03:05:07.872216+00', '2026-06-11 03:05:53.906755+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_seeds (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (22, 7, 2, 9, 0, '2026-06-11 03:05:12.9711+00', '2026-06-11 03:05:58.540211+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_seeds (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (23, 7, 3, 9, 0, '2026-06-11 03:05:17.213595+00', '2026-06-11 03:07:57.919902+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_seeds (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (24, 7, 4, 9, 0, '2026-06-11 03:05:20.847495+00', '2026-06-11 03:08:02.001704+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_seeds (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (25, 7, 5, 9, 0, '2026-06-11 03:05:23.968736+00', '2026-06-11 03:08:06.101864+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_seeds (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (26, 7, 6, 9, 0, '2026-06-11 03:05:28.659539+00', '2026-06-11 03:08:11.325239+00', 7, 7, NULL, 1, false, 0);
INSERT INTO farm.user_seeds (id, user_id, seed_type_id, quantity, frozen_quantity, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (27, 7, 7, 9, 0, '2026-06-11 03:05:31.548288+00', '2026-06-11 03:08:16.260668+00', 7, 7, NULL, 1, false, 0);


--
-- TOC entry 3770 (class 0 OID 29196)
-- Dependencies: 220
-- Data for Name: users; Type: TABLE DATA; Schema: farm; Owner: -
--

INSERT INTO farm.users (id, username, nickname, password_hash, email, avatar_url, experience, score, coin, preferences_json, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (3, 'guanyv', '关羽', '123456', 'sunquan@farm.local', 'avatar/2026/06/10/1781051822205_cb778d6f6fdc4f558985a589cffcdb36.jpg', 1750, 540, 3600, '{"audio": {"bgmVolume": 0.6, "bgmEnabled": true, "effectVolume": 0.8, "effectEnabled": true}}', '2026-06-09 18:35:46.794308+00', '2026-06-10 00:37:22.715339+00', NULL, 0, NULL, 1, false, 3);
INSERT INTO farm.users (id, username, nickname, password_hash, email, avatar_url, experience, score, coin, preferences_json, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (5, 'zhugeliang', '诸葛亮', '123456', 'huatuo@farm.local', 'avatar/2026/06/10/1781051922237_a40f02f5b25440bea5c54584ca2e50d4.png', 900, 260, 1800, '{"audio": {"bgmVolume": 0.6, "bgmEnabled": true, "effectVolume": 0.8, "effectEnabled": true}}', '2026-06-09 18:35:46.794308+00', '2026-06-10 00:38:43.230362+00', NULL, 0, NULL, 1, false, 2);
INSERT INTO farm.users (id, username, nickname, password_hash, email, avatar_url, experience, score, coin, preferences_json, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (7, 'liubei', '刘备', '123456', 'liubei_1781146941749@farm.local', 'avatar/2026/06/11/1781147196657_6da4e09ae8244eb6b04e5968192e0cd1.jpg', 3084, 3062, 2495, '{"audio": {"bgmVolume": 0.6, "bgmEnabled": true, "effectVolume": 0.8, "effectEnabled": true}}', '2026-06-11 03:02:21.749342+00', '2026-06-11 03:09:22.717827+00', 0, 7, NULL, 1, false, 3);
INSERT INTO farm.users (id, username, nickname, password_hash, email, avatar_url, experience, score, coin, preferences_json, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (6, '1', '1', '123456', '1_1781055261217@farm.local', 'avatar/2026/06/10/1781055254520_8384e2aafd874c49aac7bff747e76efa.png', 0, 0, 0, '{"audio": {"bgmVolume": 0.6, "bgmEnabled": true, "effectVolume": 0.8, "effectEnabled": true}}', '2026-06-10 01:34:21.21746+00', '2026-06-10 01:34:33.962661+00', 0, 0, NULL, 1, true, 1);
INSERT INTO farm.users (id, username, nickname, password_hash, email, avatar_url, experience, score, coin, preferences_json, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (2, 'caocao', '曹操', '123456', 'caocao@farm.local', 'avatar/2026/06/10/1781051778613_872d84d848604f01b341b70ad4d2f6dc.png', 3135, 5134, 729, '{"audio": {"bgmVolume": 0.4, "bgmEnabled": true, "effectVolume": 0.8, "effectEnabled": true}}', '2026-06-09 18:35:46.794308+00', '2026-06-11 02:58:34.96221+00', NULL, 2, NULL, 1, false, 9);
INSERT INTO farm.users (id, username, nickname, password_hash, email, avatar_url, experience, score, coin, preferences_json, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (1, 'liubei', '刘备', '123456', 'liubei@farm.local', 'avatar/2026/06/10/1781051770437_17c6cb5799a14b6e9ed1345696b0fc65.jpg', 2465, 873, 5168, '{"audio": {"bgmVolume": 0.6, "bgmEnabled": true, "effectVolume": 0.8, "effectEnabled": true}}', '2026-06-09 18:35:46.794308+00', '2026-06-11 03:01:02.301938+00', NULL, 0, NULL, 1, true, 4);
INSERT INTO farm.users (id, username, nickname, password_hash, email, avatar_url, experience, score, coin, preferences_json, created_at, updated_at, created_by, updated_by, remark, status, is_deleted, opt_lock_version) OVERRIDING SYSTEM VALUE VALUES (4, 'lvbu', '吕布', '123456', 'zhaoyun@farm.local', 'avatar/2026/06/10/1781051913421_c88ea335e43f45a39729bcd038a6e2e3.jpg', 1450, 430, 2800, '{"audio": {"bgmVolume": 0.6, "bgmEnabled": true, "effectVolume": 0.8, "effectEnabled": true}}', '2026-06-09 18:35:46.794308+00', '2026-06-11 02:46:06.050536+00', NULL, 0, NULL, 1, true, 3);


--
-- TOC entry 3823 (class 0 OID 0)
-- Dependencies: 221
-- Name: asset_defaults_id_seq; Type: SEQUENCE SET; Schema: farm; Owner: -
--

SELECT pg_catalog.setval('farm.asset_defaults_id_seq', 8, true);


--
-- TOC entry 3824 (class 0 OID 0)
-- Dependencies: 229
-- Name: growth_stages_id_seq; Type: SEQUENCE SET; Schema: farm; Owner: -
--

SELECT pg_catalog.setval('farm.growth_stages_id_seq', 8, true);


--
-- TOC entry 3825 (class 0 OID 0)
-- Dependencies: 227
-- Name: plot_policies_id_seq; Type: SEQUENCE SET; Schema: farm; Owner: -
--

SELECT pg_catalog.setval('farm.plot_policies_id_seq', 1, true);


--
-- TOC entry 3826 (class 0 OID 0)
-- Dependencies: 249
-- Name: request_idempotencies_id_seq; Type: SEQUENCE SET; Schema: farm; Owner: -
--

SELECT pg_catalog.setval('farm.request_idempotencies_id_seq', 96, true);


--
-- TOC entry 3827 (class 0 OID 0)
-- Dependencies: 233
-- Name: seed_growth_stages_id_seq; Type: SEQUENCE SET; Schema: farm; Owner: -
--

SELECT pg_catalog.setval('farm.seed_growth_stages_id_seq', 46, true);


--
-- TOC entry 3828 (class 0 OID 0)
-- Dependencies: 223
-- Name: seed_qualities_id_seq; Type: SEQUENCE SET; Schema: farm; Owner: -
--

SELECT pg_catalog.setval('farm.seed_qualities_id_seq', 3, true);


--
-- TOC entry 3829 (class 0 OID 0)
-- Dependencies: 231
-- Name: seed_types_id_seq; Type: SEQUENCE SET; Schema: farm; Owner: -
--

SELECT pg_catalog.setval('farm.seed_types_id_seq', 28, true);


--
-- TOC entry 3830 (class 0 OID 0)
-- Dependencies: 225
-- Name: soil_types_id_seq; Type: SEQUENCE SET; Schema: farm; Owner: -
--

SELECT pg_catalog.setval('farm.soil_types_id_seq', 4, true);


--
-- TOC entry 3831 (class 0 OID 0)
-- Dependencies: 243
-- Name: user_asset_flows_id_seq; Type: SEQUENCE SET; Schema: farm; Owner: -
--

SELECT pg_catalog.setval('farm.user_asset_flows_id_seq', 63, true);


--
-- TOC entry 3832 (class 0 OID 0)
-- Dependencies: 247
-- Name: user_crop_action_logs_id_seq; Type: SEQUENCE SET; Schema: farm; Owner: -
--

SELECT pg_catalog.setval('farm.user_crop_action_logs_id_seq', 109, true);


--
-- TOC entry 3833 (class 0 OID 0)
-- Dependencies: 239
-- Name: user_crops_id_seq; Type: SEQUENCE SET; Schema: farm; Owner: -
--

SELECT pg_catalog.setval('farm.user_crops_id_seq', 29, true);


--
-- TOC entry 3834 (class 0 OID 0)
-- Dependencies: 241
-- Name: user_fruits_id_seq; Type: SEQUENCE SET; Schema: farm; Owner: -
--

SELECT pg_catalog.setval('farm.user_fruits_id_seq', 9, true);


--
-- TOC entry 3835 (class 0 OID 0)
-- Dependencies: 245
-- Name: user_inventory_flows_id_seq; Type: SEQUENCE SET; Schema: farm; Owner: -
--

SELECT pg_catalog.setval('farm.user_inventory_flows_id_seq', 76, true);


--
-- TOC entry 3836 (class 0 OID 0)
-- Dependencies: 237
-- Name: user_plots_id_seq; Type: SEQUENCE SET; Schema: farm; Owner: -
--

SELECT pg_catalog.setval('farm.user_plots_id_seq', 47, true);


--
-- TOC entry 3837 (class 0 OID 0)
-- Dependencies: 235
-- Name: user_seeds_id_seq; Type: SEQUENCE SET; Schema: farm; Owner: -
--

SELECT pg_catalog.setval('farm.user_seeds_id_seq', 27, true);


--
-- TOC entry 3838 (class 0 OID 0)
-- Dependencies: 219
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: farm; Owner: -
--

SELECT pg_catalog.setval('farm.users_id_seq', 7, true);


--
-- TOC entry 3583 (class 2606 OID 29227)
-- Name: asset_defaults asset_defaults_pkey; Type: CONSTRAINT; Schema: farm; Owner: -
--

ALTER TABLE ONLY farm.asset_defaults
    ADD CONSTRAINT asset_defaults_pkey PRIMARY KEY (id);


--
-- TOC entry 3596 (class 2606 OID 29294)
-- Name: growth_stages growth_stages_pkey; Type: CONSTRAINT; Schema: farm; Owner: -
--

ALTER TABLE ONLY farm.growth_stages
    ADD CONSTRAINT growth_stages_pkey PRIMARY KEY (id);


--
-- TOC entry 3594 (class 2606 OID 29280)
-- Name: plot_policies plot_policies_pkey; Type: CONSTRAINT; Schema: farm; Owner: -
--

ALTER TABLE ONLY farm.plot_policies
    ADD CONSTRAINT plot_policies_pkey PRIMARY KEY (id);


--
-- TOC entry 3623 (class 2606 OID 29474)
-- Name: request_idempotencies request_idempotencies_pkey; Type: CONSTRAINT; Schema: farm; Owner: -
--

ALTER TABLE ONLY farm.request_idempotencies
    ADD CONSTRAINT request_idempotencies_pkey PRIMARY KEY (id);


--
-- TOC entry 3602 (class 2606 OID 29340)
-- Name: seed_growth_stages seed_growth_stages_pkey; Type: CONSTRAINT; Schema: farm; Owner: -
--

ALTER TABLE ONLY farm.seed_growth_stages
    ADD CONSTRAINT seed_growth_stages_pkey PRIMARY KEY (id);


--
-- TOC entry 3586 (class 2606 OID 29241)
-- Name: seed_qualities seed_qualities_pkey; Type: CONSTRAINT; Schema: farm; Owner: -
--

ALTER TABLE ONLY farm.seed_qualities
    ADD CONSTRAINT seed_qualities_pkey PRIMARY KEY (id);


--
-- TOC entry 3599 (class 2606 OID 29321)
-- Name: seed_types seed_types_pkey; Type: CONSTRAINT; Schema: farm; Owner: -
--

ALTER TABLE ONLY farm.seed_types
    ADD CONSTRAINT seed_types_pkey PRIMARY KEY (id);


--
-- TOC entry 3589 (class 2606 OID 29259)
-- Name: soil_types soil_types_pkey; Type: CONSTRAINT; Schema: farm; Owner: -
--

ALTER TABLE ONLY farm.soil_types
    ADD CONSTRAINT soil_types_pkey PRIMARY KEY (id);


--
-- TOC entry 3617 (class 2606 OID 29426)
-- Name: user_asset_flows user_asset_flows_pkey; Type: CONSTRAINT; Schema: farm; Owner: -
--

ALTER TABLE ONLY farm.user_asset_flows
    ADD CONSTRAINT user_asset_flows_pkey PRIMARY KEY (id);


--
-- TOC entry 3621 (class 2606 OID 29460)
-- Name: user_crop_action_logs user_crop_action_logs_pkey; Type: CONSTRAINT; Schema: farm; Owner: -
--

ALTER TABLE ONLY farm.user_crop_action_logs
    ADD CONSTRAINT user_crop_action_logs_pkey PRIMARY KEY (id);


--
-- TOC entry 3612 (class 2606 OID 29392)
-- Name: user_crops user_crops_pkey; Type: CONSTRAINT; Schema: farm; Owner: -
--

ALTER TABLE ONLY farm.user_crops
    ADD CONSTRAINT user_crops_pkey PRIMARY KEY (id);


--
-- TOC entry 3615 (class 2606 OID 29408)
-- Name: user_fruits user_fruits_pkey; Type: CONSTRAINT; Schema: farm; Owner: -
--

ALTER TABLE ONLY farm.user_fruits
    ADD CONSTRAINT user_fruits_pkey PRIMARY KEY (id);


--
-- TOC entry 3619 (class 2606 OID 29445)
-- Name: user_inventory_flows user_inventory_flows_pkey; Type: CONSTRAINT; Schema: farm; Owner: -
--

ALTER TABLE ONLY farm.user_inventory_flows
    ADD CONSTRAINT user_inventory_flows_pkey PRIMARY KEY (id);


--
-- TOC entry 3609 (class 2606 OID 29372)
-- Name: user_plots user_plots_pkey; Type: CONSTRAINT; Schema: farm; Owner: -
--

ALTER TABLE ONLY farm.user_plots
    ADD CONSTRAINT user_plots_pkey PRIMARY KEY (id);


--
-- TOC entry 3606 (class 2606 OID 29356)
-- Name: user_seeds user_seeds_pkey; Type: CONSTRAINT; Schema: farm; Owner: -
--

ALTER TABLE ONLY farm.user_seeds
    ADD CONSTRAINT user_seeds_pkey PRIMARY KEY (id);


--
-- TOC entry 3581 (class 2606 OID 29212)
-- Name: users users_pkey; Type: CONSTRAINT; Schema: farm; Owner: -
--

ALTER TABLE ONLY farm.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- TOC entry 3592 (class 1259 OID 29281)
-- Name: idx_plot_policies_active; Type: INDEX; Schema: farm; Owner: -
--

CREATE INDEX idx_plot_policies_active ON farm.plot_policies USING btree (active) WHERE (is_deleted = false);


--
-- TOC entry 3584 (class 1259 OID 29228)
-- Name: uk_asset_defaults_key_active; Type: INDEX; Schema: farm; Owner: -
--

CREATE UNIQUE INDEX uk_asset_defaults_key_active ON farm.asset_defaults USING btree (asset_key) WHERE (is_deleted = false);


--
-- TOC entry 3597 (class 1259 OID 29295)
-- Name: uk_growth_stages_name_active; Type: INDEX; Schema: farm; Owner: -
--

CREATE UNIQUE INDEX uk_growth_stages_name_active ON farm.growth_stages USING btree (name) WHERE (is_deleted = false);


--
-- TOC entry 3610 (class 1259 OID 29393)
-- Name: uk_plot_active_crop; Type: INDEX; Schema: farm; Owner: -
--

CREATE UNIQUE INDEX uk_plot_active_crop ON farm.user_crops USING btree (plot_id) WHERE (is_deleted = false);


--
-- TOC entry 3603 (class 1259 OID 29341)
-- Name: uk_seed_growth_stage_index; Type: INDEX; Schema: farm; Owner: -
--

CREATE UNIQUE INDEX uk_seed_growth_stage_index ON farm.seed_growth_stages USING btree (seed_type_id, stage_index) WHERE (is_deleted = false);


--
-- TOC entry 3587 (class 1259 OID 29242)
-- Name: uk_seed_qualities_name_active; Type: INDEX; Schema: farm; Owner: -
--

CREATE UNIQUE INDEX uk_seed_qualities_name_active ON farm.seed_qualities USING btree (name) WHERE (is_deleted = false);


--
-- TOC entry 3600 (class 1259 OID 29322)
-- Name: uk_seed_types_name_active; Type: INDEX; Schema: farm; Owner: -
--

CREATE UNIQUE INDEX uk_seed_types_name_active ON farm.seed_types USING btree (name) WHERE (is_deleted = false);


--
-- TOC entry 3590 (class 1259 OID 29260)
-- Name: uk_soil_types_bit_code_active; Type: INDEX; Schema: farm; Owner: -
--

CREATE UNIQUE INDEX uk_soil_types_bit_code_active ON farm.soil_types USING btree (bit_code) WHERE (is_deleted = false);


--
-- TOC entry 3591 (class 1259 OID 29261)
-- Name: uk_soil_types_name_active; Type: INDEX; Schema: farm; Owner: -
--

CREATE UNIQUE INDEX uk_soil_types_name_active ON farm.soil_types USING btree (name) WHERE (is_deleted = false);


--
-- TOC entry 3613 (class 1259 OID 29409)
-- Name: uk_user_fruits_active; Type: INDEX; Schema: farm; Owner: -
--

CREATE UNIQUE INDEX uk_user_fruits_active ON farm.user_fruits USING btree (user_id, seed_type_id) WHERE (is_deleted = false);


--
-- TOC entry 3607 (class 1259 OID 29373)
-- Name: uk_user_plot_index; Type: INDEX; Schema: farm; Owner: -
--

CREATE UNIQUE INDEX uk_user_plot_index ON farm.user_plots USING btree (user_id, plot_index) WHERE (is_deleted = false);


--
-- TOC entry 3604 (class 1259 OID 29357)
-- Name: uk_user_seeds_active; Type: INDEX; Schema: farm; Owner: -
--

CREATE UNIQUE INDEX uk_user_seeds_active ON farm.user_seeds USING btree (user_id, seed_type_id) WHERE (is_deleted = false);


--
-- TOC entry 3578 (class 1259 OID 29214)
-- Name: uk_users_email_active; Type: INDEX; Schema: farm; Owner: -
--

CREATE UNIQUE INDEX uk_users_email_active ON farm.users USING btree (email) WHERE (is_deleted = false);


--
-- TOC entry 3579 (class 1259 OID 29213)
-- Name: uk_users_username_active; Type: INDEX; Schema: farm; Owner: -
--

CREATE UNIQUE INDEX uk_users_username_active ON farm.users USING btree (username) WHERE (is_deleted = false);


-- Completed on 2026-06-11 20:25:42

--
-- PostgreSQL database dump complete
--

\unrestrict v2Oz1kFfRuwbGkCSS55doy4bFkAaNHhC0Vzi2GyXfvLJ8hmdNvHXh6RJHltuH4q

