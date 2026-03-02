-- Phase 4D: Purchase upload + parsed lines (mock parser)
-- PostgreSQL only; Flyway-managed

-- ENUMS
CREATE TYPE purchase_upload_doc_kind AS ENUM ('PDF');
CREATE TYPE purchase_upload_status AS ENUM ('UPLOADING', 'PARSED', 'FAILED');

-- purchase_upload (one row per uploaded document)
CREATE TABLE purchase_upload (
    id BIGSERIAL PRIMARY KEY,
    shop_id BIGINT NOT NULL REFERENCES shop(id) ON DELETE CASCADE,
    supplier_party_id BIGINT NULL REFERENCES party(id) ON DELETE SET NULL,
    doc_kind purchase_upload_doc_kind NOT NULL DEFAULT 'PDF',
    parser_key TEXT NOT NULL,
    status purchase_upload_status NOT NULL DEFAULT 'PARSED',
    original_file_name TEXT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW() NOT NULL
);

CREATE INDEX idx_purchase_upload_shop_id ON purchase_upload(shop_id);
CREATE INDEX idx_purchase_upload_status ON purchase_upload(status);

-- purchase_upload_line (parsed line items; normalized names for mapping)
CREATE TABLE purchase_upload_line (
    id BIGSERIAL PRIMARY KEY,
    purchase_upload_id BIGINT NOT NULL REFERENCES purchase_upload(id) ON DELETE CASCADE,
    line_no INT NOT NULL,
    raw_name TEXT NOT NULL,
    normalized_name TEXT NOT NULL,
    quantity NUMERIC(18, 6) NOT NULL,
    unit TEXT NOT NULL,
    unit_price NUMERIC(18, 2) NOT NULL,
    line_amount NUMERIC(18, 2) NOT NULL,
    parse_confidence NUMERIC(5, 4) NULL,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW() NOT NULL
);

CREATE INDEX idx_purchase_upload_line_upload_id ON purchase_upload_line(purchase_upload_id);
