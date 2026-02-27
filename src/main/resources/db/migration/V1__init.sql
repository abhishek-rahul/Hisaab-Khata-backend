-- V1__init.sql
-- Hisaab-Khata (v1) schema
-- PostgreSQL + Flyway
-- Stock-only (no inventory_txn yet)
-- purchase_invoice idempotency via UNIQUE(shop_id, supplier_party_id, invoice_no)
-- Categories: GLOBAL + per-SHOP

BEGIN;

-- =========
-- ENUM TYPES (guarded)
-- =========
DO $$
BEGIN
  CREATE TYPE user_role AS ENUM ('OWNER', 'STAFF');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
  CREATE TYPE user_status AS ENUM ('ACTIVE', 'INACTIVE');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
  CREATE TYPE party_type AS ENUM ('CUSTOMER', 'SUPPLIER', 'BOTH');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
  CREATE TYPE dr_cr AS ENUM ('DR', 'CR');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
  CREATE TYPE category_scope AS ENUM ('GLOBAL', 'SHOP');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
  CREATE TYPE base_unit AS ENUM ('PCS', 'GRAM', 'ML');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
  CREATE TYPE doc_status AS ENUM ('DRAFT', 'POSTED', 'CANCELLED');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
  CREATE TYPE invoice_source AS ENUM ('OCR', 'MANUAL');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
  CREATE TYPE match_status AS ENUM ('AUTO', 'REVIEW', 'NEW');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
  CREATE TYPE mapping_status AS ENUM ('ACTIVE', 'REJECTED');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
  CREATE TYPE mapped_by AS ENUM ('AUTO', 'USER');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
  CREATE TYPE payment_mode AS ENUM ('CASH', 'UPI', 'CARD', 'MIXED', 'NA');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
  CREATE TYPE ledger_entry_type AS ENUM ('SALE','PURCHASE','PAYMENT_IN','PAYMENT_OUT','ADJUSTMENT','OPENING');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
  CREATE TYPE ledger_doc_type AS ENUM ('PURCHASE_INVOICE','SALES_INVOICE','PAYMENT','NONE');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- =========
-- CORE TABLES
-- =========

-- Shop (tenant)
CREATE TABLE IF NOT EXISTS shop (
  id            BIGSERIAL PRIMARY KEY,
  name          TEXT NOT NULL,
  city          TEXT,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Users
CREATE TABLE IF NOT EXISTS app_user (
  id            BIGSERIAL PRIMARY KEY,
  phone         TEXT NOT NULL,
  name          TEXT NOT NULL,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_app_user_phone UNIQUE (phone)
);

-- Membership
CREATE TABLE IF NOT EXISTS shop_user (
  shop_id       BIGINT NOT NULL REFERENCES shop(id) ON DELETE CASCADE,
  user_id       BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  role          user_role NOT NULL,
  status        user_status NOT NULL DEFAULT 'ACTIVE',
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (shop_id, user_id)
);

-- =========
-- CATEGORY (GLOBAL + SHOP)
-- =========
CREATE TABLE IF NOT EXISTS category (
  id            BIGSERIAL PRIMARY KEY,
  scope         category_scope NOT NULL,
  shop_id       BIGINT REFERENCES shop(id) ON DELETE CASCADE,
  name          TEXT NOT NULL,
  parent_id     BIGINT REFERENCES category(id),
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT ck_category_shop_scope
    CHECK (
      (scope = 'GLOBAL' AND shop_id IS NULL)
      OR
      (scope = 'SHOP' AND shop_id IS NOT NULL)
    )
);

-- Unique names:
-- - GLOBAL: unique by (name) where scope='GLOBAL'
-- - SHOP:   unique by (shop_id, name) where scope='SHOP'
CREATE UNIQUE INDEX IF NOT EXISTS uq_category_global_name
  ON category (name)
  WHERE scope = 'GLOBAL';

CREATE UNIQUE INDEX IF NOT EXISTS uq_category_shop_name
  ON category (shop_id, name)
  WHERE scope = 'SHOP';

CREATE INDEX IF NOT EXISTS idx_category_parent
  ON category (parent_id);

-- =========
-- MASTER PRODUCT (global normalized item-level)
-- =========
CREATE TABLE IF NOT EXISTS master_product (
  id              BIGSERIAL PRIMARY KEY,
  normalized_name TEXT NOT NULL,
  display_name    TEXT,
  base_unit       base_unit,
  category_id     BIGINT REFERENCES category(id),
  is_active       BOOLEAN NOT NULL DEFAULT TRUE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_master_product_normalized UNIQUE (normalized_name)
);

CREATE INDEX IF NOT EXISTS idx_master_product_category
  ON master_product (category_id);

-- =========
-- SHOP PRODUCT (shop truth)
-- =========
CREATE TABLE IF NOT EXISTS shop_product (
  id                 BIGSERIAL PRIMARY KEY,
  shop_id            BIGINT NOT NULL REFERENCES shop(id) ON DELETE CASCADE,
  master_product_id  BIGINT NOT NULL REFERENCES master_product(id),
  display_name       TEXT NOT NULL,
  display_unit       TEXT NOT NULL,
  conversion_to_base NUMERIC(18,6) NOT NULL,
  barcode            TEXT,
  selling_price      NUMERIC(18,2),
  last_cost_price    NUMERIC(18,2),
  category_id        BIGINT REFERENCES category(id),
  created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_shop_product_display UNIQUE (shop_id, display_name)
);

-- barcode unique per shop if present
CREATE UNIQUE INDEX IF NOT EXISTS uq_shop_product_barcode
  ON shop_product (shop_id, barcode)
  WHERE barcode IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_shop_product_master
  ON shop_product (master_product_id);

CREATE INDEX IF NOT EXISTS idx_shop_product_category
  ON shop_product (category_id);

-- =========
-- STOCK (snapshot) (negative allowed)
-- =========
CREATE TABLE IF NOT EXISTS stock (
  shop_id               BIGINT NOT NULL REFERENCES shop(id) ON DELETE CASCADE,
  shop_product_id       BIGINT NOT NULL REFERENCES shop_product(id) ON DELETE CASCADE,
  qty_in_base           NUMERIC(18,6) NOT NULL DEFAULT 0,
  reserved_qty_in_base  NUMERIC(18,6) NOT NULL DEFAULT 0,
  reorder_level_in_base NUMERIC(18,6),
  updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (shop_id, shop_product_id)
);

CREATE INDEX IF NOT EXISTS idx_stock_shop_product
  ON stock (shop_product_id);

-- =========
-- PARTY (customer/supplier/both)
-- =========
CREATE TABLE IF NOT EXISTS party (
  id                    BIGSERIAL PRIMARY KEY,
  shop_id               BIGINT NOT NULL REFERENCES shop(id) ON DELETE CASCADE,
  type                  party_type NOT NULL,
  name                  TEXT NOT NULL,
  phone                 TEXT,
  gstin                 TEXT,
  opening_balance       NUMERIC(18,2) NOT NULL DEFAULT 0,
  opening_balance_type  dr_cr NOT NULL DEFAULT 'DR',
  created_at            TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_party_shop_name
  ON party (shop_id, name);

CREATE INDEX IF NOT EXISTS idx_party_shop_phone
  ON party (shop_id, phone);

-- =========
-- PURCHASES (invoice header + lines)
-- =========
CREATE TABLE IF NOT EXISTS purchase_invoice (
  id                 BIGSERIAL PRIMARY KEY,
  shop_id             BIGINT NOT NULL REFERENCES shop(id) ON DELETE CASCADE,
  supplier_party_id   BIGINT NOT NULL REFERENCES party(id),
  invoice_no          TEXT NOT NULL,
  invoice_date        DATE,
  status              doc_status NOT NULL DEFAULT 'DRAFT',
  source              invoice_source NOT NULL DEFAULT 'OCR',
  total_amount        NUMERIC(18,2),
  created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_purchase_invoice_dedupe UNIQUE (shop_id, supplier_party_id, invoice_no)
);

CREATE INDEX IF NOT EXISTS idx_purchase_invoice_shop_date
  ON purchase_invoice (shop_id, invoice_date);

CREATE TABLE IF NOT EXISTS purchase_invoice_line (
  id                BIGSERIAL PRIMARY KEY,
  invoice_id         BIGINT NOT NULL REFERENCES purchase_invoice(id) ON DELETE CASCADE,
  line_no            INT NOT NULL,
  raw_name           TEXT NOT NULL,
  normalized_name    TEXT NOT NULL,
  master_product_id  BIGINT REFERENCES master_product(id),
  shop_product_id    BIGINT REFERENCES shop_product(id),
  quantity           NUMERIC(18,6) NOT NULL,
  unit_price         NUMERIC(18,2) NOT NULL,
  line_amount        NUMERIC(18,2) NOT NULL,
  match_status       match_status NOT NULL DEFAULT 'REVIEW',
  confidence         NUMERIC(5,4) NOT NULL DEFAULT 0,
  created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_purchase_line UNIQUE (invoice_id, line_no)
);

CREATE INDEX IF NOT EXISTS idx_purchase_line_invoice
  ON purchase_invoice_line (invoice_id);

CREATE INDEX IF NOT EXISTS idx_purchase_line_shop_product
  ON purchase_invoice_line (shop_product_id);

CREATE INDEX IF NOT EXISTS idx_purchase_line_master_product
  ON purchase_invoice_line (master_product_id);

-- Supplier memory mapping
CREATE TABLE IF NOT EXISTS supplier_product_mapping (
  id                 BIGSERIAL PRIMARY KEY,
  shop_id             BIGINT NOT NULL REFERENCES shop(id) ON DELETE CASCADE,
  supplier_party_id   BIGINT NOT NULL REFERENCES party(id),
  normalized_name     TEXT NOT NULL,
  master_product_id   BIGINT REFERENCES master_product(id),
  shop_product_id     BIGINT REFERENCES shop_product(id),
  status              mapping_status NOT NULL DEFAULT 'ACTIVE',
  confidence          NUMERIC(5,4) NOT NULL DEFAULT 0,
  mapped_by           mapped_by NOT NULL DEFAULT 'USER',
  last_used_at        TIMESTAMPTZ,
  created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_supplier_mapping UNIQUE (shop_id, supplier_party_id, normalized_name)
);

CREATE INDEX IF NOT EXISTS idx_supplier_mapping_supplier
  ON supplier_product_mapping (shop_id, supplier_party_id);

CREATE INDEX IF NOT EXISTS idx_supplier_mapping_shop_product
  ON supplier_product_mapping (shop_product_id);

-- =========
-- SALES (walk-in allowed)
-- =========
CREATE TABLE IF NOT EXISTS sales_invoice (
  id                 BIGSERIAL PRIMARY KEY,
  shop_id             BIGINT NOT NULL REFERENCES shop(id) ON DELETE CASCADE,
  customer_party_id   BIGINT REFERENCES party(id),
  invoice_no          TEXT,
  invoice_date        TIMESTAMPTZ NOT NULL DEFAULT now(),
  status              doc_status NOT NULL DEFAULT 'DRAFT',
  total_amount        NUMERIC(18,2) NOT NULL DEFAULT 0,
  paid_amount         NUMERIC(18,2) NOT NULL DEFAULT 0,
  payment_mode        payment_mode NOT NULL DEFAULT 'NA',
  created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_sales_invoice_shop_date
  ON sales_invoice (shop_id, invoice_date);

CREATE TABLE IF NOT EXISTS sales_invoice_line (
  id                BIGSERIAL PRIMARY KEY,
  invoice_id         BIGINT NOT NULL REFERENCES sales_invoice(id) ON DELETE CASCADE,
  line_no            INT NOT NULL,
  shop_product_id    BIGINT NOT NULL REFERENCES shop_product(id),
  quantity_in_base   NUMERIC(18,6) NOT NULL,
  unit_price         NUMERIC(18,2) NOT NULL,
  line_amount        NUMERIC(18,2) NOT NULL,
  CONSTRAINT uq_sales_line UNIQUE (invoice_id, line_no)
);

CREATE INDEX IF NOT EXISTS idx_sales_line_invoice
  ON sales_invoice_line (invoice_id);

CREATE INDEX IF NOT EXISTS idx_sales_line_shop_product
  ON sales_invoice_line (shop_product_id);

-- =========
-- LEDGER (compute balance on read)
-- =========
CREATE TABLE IF NOT EXISTS ledger_account (
  id         BIGSERIAL PRIMARY KEY,
  shop_id     BIGINT NOT NULL REFERENCES shop(id) ON DELETE CASCADE,
  party_id    BIGINT NOT NULL REFERENCES party(id) ON DELETE CASCADE,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_ledger_account UNIQUE (shop_id, party_id)
);

CREATE TABLE IF NOT EXISTS ledger_entry (
  id                BIGSERIAL PRIMARY KEY,
  ledger_account_id  BIGINT NOT NULL REFERENCES ledger_account(id) ON DELETE CASCADE,
  entry_date         TIMESTAMPTZ NOT NULL DEFAULT now(),
  entry_type         ledger_entry_type NOT NULL,
  direction          dr_cr NOT NULL,
  amount             NUMERIC(18,2) NOT NULL,
  doc_type           ledger_doc_type NOT NULL DEFAULT 'NONE',
  doc_id             BIGINT,
  note               TEXT,
  created_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_ledger_entry_account_date
  ON ledger_entry (ledger_account_id, entry_date);

-- =========
-- DAILY SUMMARY (dashboard)
-- =========
CREATE TABLE IF NOT EXISTS daily_summary (
  shop_id               BIGINT NOT NULL REFERENCES shop(id) ON DELETE CASCADE,
  day                   DATE NOT NULL,
  total_sales           NUMERIC(18,2) NOT NULL DEFAULT 0,
  total_purchase        NUMERIC(18,2) NOT NULL DEFAULT 0,
  cash_in               NUMERIC(18,2) NOT NULL DEFAULT 0,
  cash_out              NUMERIC(18,2) NOT NULL DEFAULT 0,
  receivable            NUMERIC(18,2) NOT NULL DEFAULT 0,
  payable               NUMERIC(18,2) NOT NULL DEFAULT 0,
  gross_profit_estimate NUMERIC(18,2),
  created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (shop_id, day)
);

COMMIT;
