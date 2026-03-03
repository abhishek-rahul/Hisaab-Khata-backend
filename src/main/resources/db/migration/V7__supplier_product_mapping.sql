-- Phase 4B: Supplier product mapping (memory) + draft line suggestion columns
-- PostgreSQL only; Flyway-managed

-- ENUM for mapping source
CREATE TYPE mapping_source AS ENUM ('USER', 'SYSTEM');

-- supplier_product_mapping: supplier-specific normalized name -> shop_product_id
CREATE TABLE supplier_product_mapping (
    id BIGSERIAL PRIMARY KEY,
    shop_id BIGINT NOT NULL REFERENCES shop(id) ON DELETE CASCADE,
    supplier_party_id BIGINT NOT NULL REFERENCES party(id) ON DELETE CASCADE,
    normalized_name TEXT NOT NULL,
    shop_product_id BIGINT NOT NULL REFERENCES shop_product(id) ON DELETE CASCADE,
    source mapping_source NOT NULL DEFAULT 'SYSTEM',
    confidence_score NUMERIC(5, 4) NULL,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    UNIQUE(shop_id, supplier_party_id, normalized_name)
);

CREATE INDEX idx_supplier_product_mapping_shop_supplier ON supplier_product_mapping(shop_id, supplier_party_id);
CREATE INDEX idx_supplier_product_mapping_lookup ON supplier_product_mapping(shop_id, supplier_party_id, normalized_name);

-- Add suggestion columns to purchase_invoice_line (for REVIEW_REQUIRED)
ALTER TABLE purchase_invoice_line
    ADD COLUMN suggested_master_product_id BIGINT NULL REFERENCES master_product(id) ON DELETE SET NULL,
    ADD COLUMN suggested_confidence NUMERIC(5, 4) NULL;

CREATE INDEX idx_purchase_invoice_line_suggested_master ON purchase_invoice_line(suggested_master_product_id);
