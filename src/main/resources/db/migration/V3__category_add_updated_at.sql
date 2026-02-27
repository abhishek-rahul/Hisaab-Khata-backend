-- V3__category_add_updated_at.sql
-- Add updated_at to category so entities extending BaseEntity match schema.

BEGIN;

ALTER TABLE category
  ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT now();

-- Existing rows get updated_at = now(); optionally backfill from created_at:
-- UPDATE category SET updated_at = created_at WHERE updated_at IS NOT NULL;
COMMENT ON COLUMN category.updated_at IS 'Last updated timestamp (for BaseEntity compatibility)';

COMMIT;
