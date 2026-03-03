-- Phase 6: Ledger statement performance + daily_summary purchase/payable/cash_out
-- PostgreSQL only; Flyway-managed. Do not edit V1..V9.

-- 1) ALTER daily_summary: add purchase, cash_out, payable
ALTER TABLE daily_summary
    ADD COLUMN IF NOT EXISTS total_purchase NUMERIC(18, 2) NOT NULL DEFAULT 0;

ALTER TABLE daily_summary
    ADD COLUMN IF NOT EXISTS cash_out NUMERIC(18, 2) NOT NULL DEFAULT 0;

ALTER TABLE daily_summary
    ADD COLUMN IF NOT EXISTS payable NUMERIC(18, 2) NOT NULL DEFAULT 0;

-- Non-negative checks (add only if column was just added; safe to run)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'chk_daily_summary_non_negative_purchase'
  ) THEN
    ALTER TABLE daily_summary
      ADD CONSTRAINT chk_daily_summary_non_negative_purchase CHECK (total_purchase >= 0);
  END IF;
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'chk_daily_summary_non_negative_cash_out'
  ) THEN
    ALTER TABLE daily_summary
      ADD CONSTRAINT chk_daily_summary_non_negative_cash_out CHECK (cash_out >= 0);
  END IF;
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'chk_daily_summary_non_negative_payable'
  ) THEN
    ALTER TABLE daily_summary
      ADD CONSTRAINT chk_daily_summary_non_negative_payable CHECK (payable >= 0);
  END IF;
END $$;

-- 2) Index for ledger statement (shop + party + time order)
CREATE INDEX IF NOT EXISTS idx_party_ledger_shop_party_time
    ON party_ledger(shop_id, party_id, created_at, id);
