-- Phase 4C: POST support (posted_at, party_ledger index)
-- PostgreSQL only; Flyway-managed

-- purchase_invoice: posted_at (set when status = POSTED)
ALTER TABLE purchase_invoice
    ADD COLUMN posted_at TIMESTAMPTZ NULL;

-- POSTED => posted_at must be set
ALTER TABLE purchase_invoice
    ADD CONSTRAINT chk_purchase_invoice_posted_has_date
    CHECK (status <> 'POSTED' OR posted_at IS NOT NULL);

-- Index for party_ledger lookups by reference
CREATE INDEX idx_party_ledger_shop_ref_type_ref_id
    ON party_ledger(shop_id, reference_type, reference_id);
