# Phase 4C – POST (Stock + Supplier Ledger + Status POSTED)

Yeh document Phase 4C mein **jo implement hua hai** (POST – draft ko final karke stock update aur supplier ledger) usko simple terms mein samjhata hai.

**Phase 4C ka main goal:** Jab draft **saari lines resolved** ho aur supplier set ho, tab **ek hi transaction** me: har line ka quantity **stock** me add karo, supplier ke hisaab me **ek ledger entry** (aggregated) likho, aur invoice ki **status = POSTED** + **posted_at** set karo. POST ke baad invoice **immutable** – edit/save block.

---

## 1. Phase 4 Flow (Context)

- **4D**: Upload + parse
- **4A**: Draft persist (DRAFT)
- **4B**: Resolve (har line → shop_product_id)
- **4C (is doc)**: POST → stock update + **1** supplier ledger row + status POSTED (atomic)

---

## 2. Phase 4C – Simple Flow

### 2.1 Kab post kar sakte ho?

- Draft **DRAFT** hona chahiye (pehle se POSTED ho to 409).
- **Supplier** set hona chahiye (nahi to 400).
- **Saari lines resolved** honi chahiye (har line pe `shop_product_id` set); koi bhi unresolved ho to 400.

### 2.2 POST kya karta hai (ek transaction me)

1. **Lock:** Invoice row ko **SELECT FOR UPDATE** se lock (concurrency safe).
2. **Validate:** Status DRAFT, supplier set, saari lines me resolved_shop_product_id (yahan `shop_product_id`) set.
3. **Total:** Lines se **total_amount** server-side dobara nikalke set karo.
4. **Stock:** Har line ke liye:
   - Us line ka **shop_product** usi shop ka hona chahiye (nahi to 400).
   - Line ki **quantity** ko **base unit** me convert karo (line.unit vs shop_product.display_unit / base_unit – match nahi to 400 UNIT_MISMATCH).
   - **Stock** table me us shop_product_id ke liye row dhundho; agar hai to **quantity += qty_base**, nahi to nayi row (quantity = qty_base).
5. **Ledger:** **Sirf 1 row** `party_ledger` me:
   - shop_id, party_id = supplier_party_id
   - entry_type = **PURCHASE**
   - **dr_amount = 0**, **cr_amount = total**
   - reference_type = **PURCHASE**, reference_id = purchase_invoice.id
6. **Invoice update:** status = **POSTED**, **posted_at = now()**.
7. **Response:** Posted invoice summary (draftId, status, postedAt, totalAmount, supplierPartyId, invoiceNo).

Koi bhi step fail ho to **puri transaction rollback** (atomicity).

### 2.3 Unit conversion (stock)

- Stock **hamesha base unit** me store hota hai (shop_product ka base_unit / conversion_to_base).
- Line ki **unit** ko base me convert karte waqt:
  - Agar line.unit = shop_product.**display_unit** → base qty = line.quantity × **conversion_to_base**.
  - Agar line.unit = master_product.**base_unit** (e.g. PCS) → base qty = line.quantity (1:1).
  - Kuch aur → 400 **UNIT_MISMATCH**.

### 2.4 POSTED ke baad

- **POSTED** invoice ko dobara **post** nahi kar sakte → 409 ALREADY_POSTED.
- **Edit/save** (4A wala PUT save) bhi nahi – status DRAFT nahi to 409 DRAFT_NOT_EDITABLE (immutable).

---

## 3. Database – Flyway V8

**File:** `src/main/resources/db/migration/V8__purchase_post_support.sql`

### 3.1 purchase_invoice

- **posted_at** (TIMESTAMPTZ NULL) – jab status POSTED ho tab set.
- **Check constraint:** `status = 'POSTED'` ⇒ `posted_at IS NOT NULL`.

### 3.2 party_ledger

- **Index:** (shop_id, reference_type, reference_id) – reference se ledger entry dhundhne ke liye.

---

## 4. API (Phase 4C)

### POST `/purchase/drafts/{draftId}/post`

- **Auth:** Login, shop scoped.
- **Success (200):** `PostedInvoiceSummary` – draftId, status (POSTED), postedAt, totalAmount, supplierPartyId, invoiceNo.
- **Errors (uniform ApiResponse):**
  - **404** – DRAFT_NOT_FOUND
  - **409** – ALREADY_POSTED (pehle se POSTED)
  - **400** – SUPPLIER_REQUIRED, UNRESOLVED_LINES, UNIT_MISMATCH, INVALID_SHOP_PRODUCT

---

## 5. Response DTO

**PostedInvoiceSummary:** draftId, status, postedAt, totalAmount, supplierPartyId, invoiceNo.

---

## 6. Concurrency

- Post karte waqt invoice row **pessimistic lock** (SELECT FOR UPDATE) se load hoti hai, taaki do request ek saath same draft post na kar dein.

---

## 7. Abhi nahi (per spec)

- **inventory_txn** – use nahi ho raha.
- **daily_summary** – update nahi (baad me async).

---

## 8. Testing – phase4c_e2e.py

**Script:** `scripts/phase4c_e2e.py`

**Flow:** Register → category → supplier → upload → create draft (with supplier) → **resolve all lines** (CREATE_NEW_PRODUCT) → **post** → **double post** (expect 409) → **PUT save** draft (expect 409 – edit blocked).
