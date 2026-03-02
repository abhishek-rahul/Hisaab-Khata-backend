# Hisaab-Khata Backend – Big Picture Review & Phase 4 Readiness

Yeh document **big picture context** ke hisaab se current implementation ka review karta hai aur bataata hai ki **sahi track pe ho ya nahi**, aur **Phase 4 se resume** karke production-level app banane ke liye kya karna hoga.

---

## 1. Big Picture vs Implemented (Phase 1–3) – Alignment

| # | Big Picture Use Case | Status | Notes |
|---|------------------------|--------|--------|
| **1** | **Shop Registration** – Shopkeeper register, owner account, staff, owner staff ko access deta hai | ✅ Done | V1: shop, users, refresh_token. Auth: register, login, refresh. Staff: POST /shops/{id}/staff (OWNER only). ShopContext se current shop. |
| **2** | **Parties** – Customer/Supplier ek hi model (party), CUSTOMER/SUPPLIER type | ✅ Done | V2: party, party_ledger. APIs: create, list (type filter), get, get ledger. Ledger abhi entries=[] (append-only structure ready). |
| **3** | **Products** – Manual add + (future) Bill upload. Category (GLOBAL seeded + SHOP custom). master_product + shop_product. Stock snapshot, negative allowed | ✅ Done (manual only) | V3: category (SHOP scope), master_product, shop_product, stock. Manual: POST /products/manual, GET /products, PATCH. Stock GET, increase/decrease ready for Phase 4. Bill upload Phase 4+. |
| **4** | **Purchase (Supplier Bill)** – DRAFT → REVIEW → POSTED, POSTED pe stock change, supplier memory | ⏳ Phase 4 | Schema/APIs abhi nahi. StockService.increaseStock(shopProductId, qty) ready. |
| **5** | **Sales** – Items select, walk-in (customer optional), POSTED pe stock decrement, customer pe ledger | ⏳ Phase 4 | Schema/APIs abhi nahi. StockService.decreaseStock ready. party_ledger ready for entries. |
| **6** | **Ledger (Khata)** – Customer/Supplier udhaar/payments, append-only, balance on read | ✅ Foundation | party_ledger table + triggers (no update/delete). getPartyLedger() ready; entries Phase 4/5 mein fill hongi. |
| **7** | **Daily Summary** – daily_summary table, dashboard | ⏳ Later | Abhi table/API nahi. |

**Database & schema:**  
- PostgreSQL only ✅  
- Flyway only (ddl-auto=none) ✅  
- Schema rules (category scope, base_unit, stock negative allowed, conversion positive, etc.) ✅  

**Conclusion:** Phase 1–3 **big picture ke saath align** hain. Foundation solid hai.

---

## 2. Kya Sahi Track Pe Ho?

**Haan – sahi track pe ho.**

- **Identity & multi-tenant:** Shop + User (OWNER/STAFF), JWT, ShopContext – sab sahi.  
- **Party model:** Single `party` table with type CUSTOMER/SUPPLIER – big picture jaisa.  
- **Ledger:** Append-only `party_ledger`, dr/cr one-side constraint, update/delete blocked – design sahi.  
- **Products:** master_product (global identity) + shop_product (shop-specific) + stock snapshot – big picture ke hisaab se.  
- **Stock:** Base unit, negative allowed, increase/decrease by shop_product_id – Purchase/Sale ke liye ready.  
- **API style:** ApiResponse wrapper, error codes (PARTY_PHONE_DUPLICATE, SHOP_PRODUCT_NOT_FOUND, etc.), validation – consistent.  
- **Security:** Public: /auth/**, /health. Baaki JWT. Owner-only staff create.  

Koi major design flaw nahi dikhta. Phase 4 se resume karna logical next step hai.

---

## 3. Important: Legacy / Incomplete Code (Phase 4 se pehle resolve karna better)

Codebase mein **purane / incomplete** entities aur controllers hain jo **Flyway migrations (V1–V3) ke saath match nahi karte**:

| Item | Issue | Recommendation |
|------|--------|------------------|
| **Purchase, Sale, SaleItem** | In entities: `product_id`, `Supplier`, `Product` – lekin **V1–V3 mein purchase, sale, sale_item, product, supplier tables hain hi nahi**. Big picture: purchase_invoice + purchase_invoice_line, sales_invoice + sales_invoice_line. | Phase 4 mein **naya schema** banao: purchase_invoice, purchase_invoice_line, supplier_product_mapping; sales_invoice, sales_invoice_line. Purane Purchase/Sale/SaleItem entities ko **replace** karo ya hatao; new design Party + ShopProduct use karega. |
| **Product, Supplier** | Big picture: **Party** (CUSTOMER/SUPPLIER) + **ShopProduct**. Alag Product/Supplier tables doc ke hisaab se nahi. | Phase 4 design Party + ShopProduct pe hi rakhna. Product/Supplier entities agar use nahi ho rahe to deprecate/remove. |
| **V4 migration** | Sirf tab run hota hai jab `purchase` / `sale_item` tables pehle se exist karti hain (e.g. purani ddl-auto run). Fresh Flyway run pe yeh tables hain hi nahi. | Phase 4 ki **nayi migrations** purchase_invoice, sales_invoice wala schema create karein. V4 ko ya to rahne do (no-op on clean DB) ya future cleanup mein hatao. |
| **PurchaseController, SaleController, ReportsController** | Inke backend entities (Purchase, Sale, Supplier, Product) ke liye DB tables Flyway se nahi banti. | Phase 4 mein in controllers/services ko **naye purchase_invoice / sales_invoice** design ke saath **refactor** karo; ya temporarily disable karo taaki confusion na ho. |

**Summary:** Phase 4 start karte waqt **big picture wala schema** (purchase_invoice, purchase_invoice_line, supplier_product_mapping, sales_invoice, sales_invoice_line) Flyway se introduce karo; purane Purchase/Sale/Product/Supplier model ko align karo ya hatao. Isse code aur doc dono ek hi direction mein rahenge.

---

## 4. Phase 4 Resume – High-Level Checklist (Production ki taraf)

Jab Phase 4 start karo, yeh order helpful rahega:

1. **Purchase (Supplier Bill)**  
   - Naya migration: `purchase_invoice` (shop_id, party_id [supplier], status: DRAFT/REVIEW/POSTED, invoice_number, date, …), `purchase_invoice_line` (shop_product_id, quantity_base, cost, …).  
   - POSTED pe StockService.increaseStock() call.  
   - Optional: `supplier_product_mapping` (supplier party_id + supplier item name → shop_product_id) for “supplier memory”.

2. **Sales**  
   - Naya migration: `sales_invoice` (shop_id, party_id nullable [customer], status, …), `sales_invoice_line` (shop_product_id, quantity_base, selling_price, …).  
   - POSTED pe decreaseStock(); customer not null ho to party_ledger mein entry (SALE, dr side).

3. **Ledger**  
   - Purchase POST pe supplier ledger entry; Sale POST pe customer ledger entry; Payment APIs pe PAYMENT_IN/OUT entries. Balance = sum(dr) - sum(cr) on read (already planned).

4. **Daily Summary**  
   - Migration: `daily_summary` (shop_id, date, sales_total, purchase_total, receivable, payable, cash_flow, …).  
   - POSTED purchase/sale aur payment pe is table ko update (sync ya batch).

5. **Cleanup**  
   - Purane Purchase, Sale, SaleItem, Product, Supplier entities/controllers ko new design ke saath replace/remove; conflict na rahe.

---

## 5. Final One-Line Answer

**Haan – pura code (Phase 1–3) sahi track pe hai; big picture ke saath align hai. Resume from Phase 4 karo, naya schema (purchase_invoice, sales_invoice, supplier_product_mapping, daily_summary) Flyway se add karo, aur purane Purchase/Sale/Product/Supplier code ko new design se replace/align karo – isse web app production-level ki taraf badiya shape mein jayegi.**

---

*Document date: March 2025. Codebase: Phase 3 done; Flyway V1–V3; legacy Purchase/Sale entities present but without matching migrations.*
