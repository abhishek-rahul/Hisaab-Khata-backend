-- Phase 5: Sales payment and discount support; daily_summary for reporting
-- PostgreSQL only; Flyway-managed

-- payment_mode enum (CASH, UPI, MIXED, NA)
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'payment_mode') THEN
    CREATE TYPE payment_mode AS ENUM ('CASH', 'UPI', 'MIXED', 'NA');
  END IF;
END $$;

-- sales_invoice (DRAFT -> POSTED; header-level discount)
CREATE TABLE sales_invoice (
    id BIGSERIAL PRIMARY KEY,
    shop_id BIGINT NOT NULL REFERENCES shop(id) ON DELETE CASCADE,
    customer_party_id BIGINT NULL REFERENCES party(id) ON DELETE SET NULL,
    invoice_date DATE NOT NULL DEFAULT CURRENT_DATE,
    status doc_status NOT NULL DEFAULT 'DRAFT',
    gross_amount NUMERIC(18, 2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(18, 2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(18, 2) NOT NULL DEFAULT 0,
    cash_paid_amount NUMERIC(18, 2) NOT NULL DEFAULT 0,
    upi_paid_amount NUMERIC(18, 2) NOT NULL DEFAULT 0,
    paid_amount NUMERIC(18, 2) NOT NULL DEFAULT 0,
    payment_mode payment_mode NOT NULL DEFAULT 'NA',
    posted_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    CONSTRAINT chk_sales_invoice_paid_equals_cash_plus_upi
        CHECK (paid_amount = cash_paid_amount + upi_paid_amount),
    CONSTRAINT chk_sales_invoice_total_equals_gross_minus_discount
        CHECK (total_amount = gross_amount - discount_amount),
    CONSTRAINT chk_sales_invoice_non_negative_gross CHECK (gross_amount >= 0),
    CONSTRAINT chk_sales_invoice_non_negative_discount CHECK (discount_amount >= 0),
    CONSTRAINT chk_sales_invoice_non_negative_total CHECK (total_amount >= 0),
    CONSTRAINT chk_sales_invoice_non_negative_cash CHECK (cash_paid_amount >= 0),
    CONSTRAINT chk_sales_invoice_non_negative_upi CHECK (upi_paid_amount >= 0),
    CONSTRAINT chk_sales_invoice_non_negative_paid CHECK (paid_amount >= 0),
    CONSTRAINT chk_sales_invoice_discount_lte_gross CHECK (discount_amount <= gross_amount),
    CONSTRAINT chk_sales_invoice_posted_has_date
        CHECK (status <> 'POSTED' OR posted_at IS NOT NULL)
);

CREATE INDEX idx_sales_invoice_shop_id ON sales_invoice(shop_id);
CREATE INDEX idx_sales_invoice_status ON sales_invoice(status);
CREATE INDEX idx_sales_invoice_invoice_date ON sales_invoice(invoice_date);

-- sales_invoice_line
CREATE TABLE sales_invoice_line (
    id BIGSERIAL PRIMARY KEY,
    sales_invoice_id BIGINT NOT NULL REFERENCES sales_invoice(id) ON DELETE CASCADE,
    line_no INT NOT NULL,
    shop_product_id BIGINT NOT NULL REFERENCES shop_product(id) ON DELETE RESTRICT,
    quantity_in_base NUMERIC(18, 6) NOT NULL,
    unit_price NUMERIC(18, 2) NOT NULL,
    line_amount NUMERIC(18, 2) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    CONSTRAINT chk_sales_invoice_line_non_negative_qty CHECK (quantity_in_base >= 0),
    CONSTRAINT chk_sales_invoice_line_non_negative_unit_price CHECK (unit_price >= 0),
    CONSTRAINT chk_sales_invoice_line_non_negative_amount CHECK (line_amount >= 0)
);

CREATE INDEX idx_sales_invoice_line_invoice_id ON sales_invoice_line(sales_invoice_id);

-- daily_summary (one row per shop per day; upserted on sale/purchase post)
CREATE TABLE daily_summary (
    id BIGSERIAL PRIMARY KEY,
    shop_id BIGINT NOT NULL REFERENCES shop(id) ON DELETE CASCADE,
    day DATE NOT NULL,
    total_sales NUMERIC(18, 2) NOT NULL DEFAULT 0,
    cash_in NUMERIC(18, 2) NOT NULL DEFAULT 0,
    receivable NUMERIC(18, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    UNIQUE(shop_id, day),
    CONSTRAINT chk_daily_summary_non_negative_sales CHECK (total_sales >= 0),
    CONSTRAINT chk_daily_summary_non_negative_cash_in CHECK (cash_in >= 0),
    CONSTRAINT chk_daily_summary_non_negative_receivable CHECK (receivable >= 0)
);

CREATE INDEX idx_daily_summary_shop_day ON daily_summary(shop_id, day);
