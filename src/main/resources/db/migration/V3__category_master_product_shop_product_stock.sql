-- Phase 3: Category + MasterProduct + ShopProduct + Stock snapshot
-- PostgreSQL only; Flyway-managed. No inventory_txn.

-- ENUMS
CREATE TYPE category_scope AS ENUM ('GLOBAL', 'SHOP');
CREATE TYPE base_unit AS ENUM ('PCS', 'GRAM', 'ML');

-- category (SHOP scope for Phase 3; shop_id required for SHOP)
CREATE TABLE category (
    id BIGSERIAL PRIMARY KEY,
    shop_id BIGINT NULL REFERENCES shop(id) ON DELETE CASCADE,
    scope category_scope NOT NULL,
    name TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    CONSTRAINT chk_shop_category_has_shop CHECK (
        (scope = 'SHOP' AND shop_id IS NOT NULL) OR (scope = 'GLOBAL' AND shop_id IS NULL)
    )
);

CREATE INDEX idx_category_shop_id ON category(shop_id);
CREATE INDEX idx_category_shop_scope ON category(shop_id, scope);
CREATE UNIQUE INDEX idx_category_shop_name ON category(shop_id, name) WHERE scope = 'SHOP' AND shop_id IS NOT NULL;

-- master_product (global normalized identity; no shop_id)
CREATE TABLE master_product (
    id BIGSERIAL PRIMARY KEY,
    canonical_name TEXT NOT NULL,
    normalized_name TEXT NOT NULL,
    base_unit base_unit NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW() NOT NULL
);

CREATE INDEX idx_master_product_normalized_name ON master_product(normalized_name);

-- shop_product (shop-specific sellable item; links to master_product and optional category)
CREATE TABLE shop_product (
    id BIGSERIAL PRIMARY KEY,
    shop_id BIGINT NOT NULL REFERENCES shop(id) ON DELETE CASCADE,
    master_product_id BIGINT NOT NULL REFERENCES master_product(id) ON DELETE RESTRICT,
    category_id BIGINT NULL REFERENCES category(id) ON DELETE SET NULL,
    display_name TEXT NOT NULL,
    display_unit TEXT NOT NULL,
    conversion_to_base NUMERIC(18, 6) NOT NULL,
    selling_price NUMERIC(18, 2) NULL,
    min_stock_in_base NUMERIC(18, 6) DEFAULT 0 NOT NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    CONSTRAINT chk_conversion_positive CHECK (conversion_to_base > 0)
);

CREATE INDEX idx_shop_product_shop_id ON shop_product(shop_id);
CREATE INDEX idx_shop_product_master_product_id ON shop_product(master_product_id);
CREATE INDEX idx_shop_product_category_id ON shop_product(category_id);

-- stock (one row per shop_product; quantity in base unit; negative allowed)
CREATE TABLE stock (
    id BIGSERIAL PRIMARY KEY,
    shop_product_id BIGINT NOT NULL REFERENCES shop_product(id) ON DELETE CASCADE,
    quantity NUMERIC(18, 6) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    UNIQUE(shop_product_id)
);

CREATE INDEX idx_stock_shop_product_id ON stock(shop_product_id);
