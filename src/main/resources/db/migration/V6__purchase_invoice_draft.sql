-- Phase 4A: Purchase invoice draft (DRAFT only; POST in 4C)
-- PostgreSQL only; Flyway-managed

-- ENUMS
CREATE TYPE doc_status AS ENUM ('DRAFT', 'POSTED');
CREATE TYPE resolution_status AS ENUM ('UNRESOLVED', 'RESOLVED');

-- purchase_invoice (one per upload when draft created; status DRAFT until 4C POST)
CREATE TABLE purchase_invoice (
    id BIGSERIAL PRIMARY KEY,
    shop_id BIGINT NOT NULL REFERENCES shop(id) ON DELETE CASCADE,
    purchase_upload_id BIGINT NOT NULL REFERENCES purchase_upload(id) ON DELETE CASCADE,
    supplier_party_id BIGINT NULL REFERENCES party(id) ON DELETE SET NULL,
    invoice_no TEXT NULL,
    invoice_date DATE NULL,
    notes TEXT NULL,
    status doc_status NOT NULL DEFAULT 'DRAFT',
    version INT NOT NULL DEFAULT 0,
    total_amount NUMERIC(18, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    UNIQUE(shop_id, purchase_upload_id)
);

CREATE INDEX idx_purchase_invoice_shop_id ON purchase_invoice(shop_id);
CREATE INDEX idx_purchase_invoice_status ON purchase_invoice(status);

-- purchase_invoice_line (copied from upload lines; resolution for 4B)
CREATE TABLE purchase_invoice_line (
    id BIGSERIAL PRIMARY KEY,
    purchase_invoice_id BIGINT NOT NULL REFERENCES purchase_invoice(id) ON DELETE CASCADE,
    line_no INT NOT NULL,
    raw_name TEXT NOT NULL,
    normalized_name TEXT NOT NULL,
    quantity NUMERIC(18, 6) NOT NULL,
    unit TEXT NOT NULL,
    unit_price NUMERIC(18, 2) NOT NULL,
    line_amount NUMERIC(18, 2) NOT NULL,
    resolution_status resolution_status NOT NULL DEFAULT 'UNRESOLVED',
    shop_product_id BIGINT NULL REFERENCES shop_product(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW() NOT NULL
);

CREATE INDEX idx_purchase_invoice_line_invoice_id ON purchase_invoice_line(purchase_invoice_id);
