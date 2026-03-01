-- Phase 2: Party + Party Ledger foundation
-- PostgreSQL only; Flyway-managed

-- ENUMS
CREATE TYPE party_type AS ENUM ('CUSTOMER', 'SUPPLIER');
CREATE TYPE ledger_entry_type AS ENUM ('SALE', 'PURCHASE', 'PAYMENT_IN', 'PAYMENT_OUT', 'ADJUSTMENT');
CREATE TYPE ledger_reference_type AS ENUM ('SALE', 'PURCHASE', 'PAYMENT');

-- party table
CREATE TABLE party (
    id BIGSERIAL PRIMARY KEY,
    shop_id BIGINT NOT NULL REFERENCES shop(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    phone TEXT NULL,
    type party_type NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW() NOT NULL
);

CREATE INDEX idx_party_shop_id ON party(shop_id);
CREATE INDEX idx_party_shop_id_type ON party(shop_id, type);

-- Unique phone per shop only when phone is not null (partial unique index)
CREATE UNIQUE INDEX idx_party_shop_phone_unique ON party(shop_id, phone) WHERE phone IS NOT NULL;

-- party_ledger table (append-only)
CREATE TABLE party_ledger (
    id BIGSERIAL PRIMARY KEY,
    shop_id BIGINT NOT NULL REFERENCES shop(id) ON DELETE CASCADE,
    party_id BIGINT NOT NULL REFERENCES party(id) ON DELETE CASCADE,
    entry_type ledger_entry_type NOT NULL,
    dr_amount NUMERIC(18, 2) NOT NULL DEFAULT 0,
    cr_amount NUMERIC(18, 2) NOT NULL DEFAULT 0,
    reference_type ledger_reference_type NULL,
    reference_id BIGINT NULL,
    remarks TEXT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    CONSTRAINT chk_party_ledger_non_negative_dr CHECK (dr_amount >= 0),
    CONSTRAINT chk_party_ledger_non_negative_cr CHECK (cr_amount >= 0),
    CONSTRAINT chk_party_ledger_one_side CHECK (
        (dr_amount > 0 AND cr_amount = 0) OR (cr_amount > 0 AND dr_amount = 0)
    )
);

CREATE INDEX idx_party_ledger_shop_party ON party_ledger(shop_id, party_id);
CREATE INDEX idx_party_ledger_created_at ON party_ledger(created_at);

-- Block UPDATE and DELETE on party_ledger (append-only)
CREATE OR REPLACE FUNCTION block_party_ledger_update_delete()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'party_ledger is append-only; UPDATE and DELETE are not allowed';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_party_ledger_no_update
    BEFORE UPDATE ON party_ledger
    FOR EACH ROW EXECUTE PROCEDURE block_party_ledger_update_delete();

CREATE TRIGGER trg_party_ledger_no_delete
    BEFORE DELETE ON party_ledger
    FOR EACH ROW EXECUTE PROCEDURE block_party_ledger_update_delete();
