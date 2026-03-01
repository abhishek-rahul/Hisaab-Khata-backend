-- Phase 3 compatibility: add shop_product_id to purchase and sale_item.
-- Run only if these tables exist (e.g. from prior ddl-auto or other migrations).

DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'purchase') THEN
    ALTER TABLE purchase ADD COLUMN IF NOT EXISTS shop_product_id BIGINT REFERENCES shop_product(id) ON DELETE SET NULL;
    CREATE INDEX IF NOT EXISTS idx_purchase_shop_product_id ON purchase(shop_product_id);
  END IF;
  IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'sale_item') THEN
    ALTER TABLE sale_item ADD COLUMN IF NOT EXISTS shop_product_id BIGINT REFERENCES shop_product(id) ON DELETE SET NULL;
    CREATE INDEX IF NOT EXISTS idx_sale_item_shop_product_id ON sale_item(shop_product_id);
  END IF;
END $$;
