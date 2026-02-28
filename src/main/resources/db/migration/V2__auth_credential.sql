-- V2__auth_credential.sql
-- Auth credentials separate from app_user (Phase 1 Identity + Auth)

BEGIN;

CREATE TABLE IF NOT EXISTS auth_credential (
  user_id       BIGINT NOT NULL PRIMARY KEY REFERENCES app_user(id) ON DELETE CASCADE,
  password_hash TEXT NOT NULL,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

COMMIT;
