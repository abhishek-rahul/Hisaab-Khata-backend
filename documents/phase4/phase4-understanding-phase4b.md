# Phase 4B – Resolve (Mapping + Suggestions + User Approvals)

Yeh document Phase 4B mein **jo implement hua hai** (Resolve – line items ko shop products se map karna) usko simple terms mein samjhata hai.

**Phase 4B ka main goal:** Draft invoice ki har line ko **kisi ek shop_product se resolve** karna, taaki baad mein POST (4C) pe stock/ledger sahi product pe apply ho. Supplier **pehle se select hona zaroori** hai; uske baad auto-resolve (mapping + master_product match) aur jahan confidence kam ho wahan user **suggestion accept / existing choose / naya product create** karke resolve karta hai.

---

## 1. Phase 4 Flow (Context)

- **4D**: Upload + parse → `purchase_upload` + `purchase_upload_line`
- **4A**: Draft persist → `purchase_invoice` + `purchase_invoice_line` (DRAFT)
- **4B (is doc)**: Resolve → mapping decisions + suggestions + user approvals; output `purchase_invoice_line.shop_product_id` (resolved) + `supplier_product_mapping` memory
- **4C**: POST → stock update + supplier ledger + status POSTED (abhi nahi)

---

## 2. Phase 4B – Simple Flow

### 2.1 Supplier zaroori

- Resolve shuru karne se pehle draft pe **supplier_party_id set** hona chahiye (4A create/put me).
- Auto-resolve ya line-resolve bina supplier ke **nahi chalega** (error: SUPPLIER_REQUIRED).

### 2.2 Auto-resolve (POST …/resolve/auto)

1. Draft **DRAFT** hona chahiye, aur **supplier set** hona chahiye.
2. Har line ke liye:
   - **Pehle:** `supplier_product_mapping` me **exact lookup** (shop_id, supplier_party_id, line ka normalized_name).
   - Agar mil gaya → line **RESOLVED**, `shop_product_id` = mapping wala product (**AUTO_MATCH**).
   - Agar nahi mila → **master_product** me match dhundho (exact normalized name, ya “line text me master name contain”) aur **confidence** nikalo.
3. Confidence ke hisaab se (thresholds **hardcoded**):
   - **HIGH** (≥ 0.92): Line ko auto **RESOLVED** karo, shop_product ensure karo (nahi hai to minimal create), aur **supplier_product_mapping** me entry add/update (source = SYSTEM).
   - **MEDIUM** (0.60 – 0.92): Line **UNRESOLVED** hi rahegi; sirf **suggestion** set hoga: `suggested_master_product_id` + `suggested_confidence` (outcome = REVIEW_REQUIRED).
   - **LOW** (< 0.60): Koi suggestion nahi; outcome = **NEW_CANDIDATE** (user baad me CREATE_NEW_PRODUCT ya CHOOSE_EXISTING karega).

### 2.3 Review payload (GET …/review)

- Draft header + version + **lines[]** jisme har line pe:
  - `resolutionStatus` (UNRESOLVED / RESOLVED)
  - `resolvedShopProductId` (jab RESOLVED)
  - `suggestedMasterProductId`, `suggestedConfidence` (jab REVIEW_REQUIRED)
  - **resolutionOutcome**: RESOLVED | AUTO_MATCH | REVIEW_REQUIRED | NEW_CANDIDATE (derived)
- **needsReview**: koi line abhi bhi resolve nahi hui ya REVIEW_REQUIRED hai
- **readyToPost**: saari lines RESOLVED (4C POST ke liye ready)

### 2.4 Line resolve (PUT …/lines/{lineId}/resolve)

User ek line ko manually resolve karta hai. Request me **action** + uske hisaab se extra fields:

- **ACCEPT_SUGGESTION**: Line pe pehle se suggestion hona chahiye; usi master se shop_product ensure karke line resolve + mapping upsert (source = USER).
- **CHOOSE_EXISTING**: `shopProductId` bhejo (us shop ka hi hona chahiye); line resolve + mapping upsert (source = USER).
- **CREATE_NEW_PRODUCT**: **canonicalName** + **baseUnit** (zaroori), **categoryId** (optional). Naya master_product (agar nahi hai) + minimal shop_product (+ stock) banta hai; line resolve + mapping upsert (source = USER).

Resolve hone par: line pe `shop_product_id` set, `resolution_status` = RESOLVED, suggestion clear; `supplier_product_mapping` me (shop_id, supplier_party_id, normalized_name) → shop_product_id + source + confidence save/update.

---

## 3. Database – Flyway V7

**File:** `src/main/resources/db/migration/V7__supplier_product_mapping.sql`

### 3.1 Enum

- **mapping_source**: `USER`, `SYSTEM` (kaun se action se mapping bani)

### 3.2 Table: `supplier_product_mapping`

- **Key:** (shop_id, supplier_party_id, normalized_name) – **UNIQUE**
- **shop_product_id**: is supplier + normalized name ke liye konsa shop_product use karna hai (memory)
- **source**: USER (manual resolve) ya SYSTEM (auto-resolve)
- **confidence_score**: optional (0–1 jaisa)

Is table ki wajah se next time same supplier se same naam ki line aaye to **auto** usi shop_product pe map ho sakti hai.

### 3.3 `purchase_invoice_line` me naye columns (V7)

- **suggested_master_product_id**: REVIEW_REQUIRED wali line ke liye suggested master (FK → master_product)
- **suggested_confidence**: us suggestion ka confidence (NUMERIC 0–1 ke karib)

Resolved line ka final product **pehle se hi** column `shop_product_id` me store hota hai (4A se); 4B usi ko set/clear karta hai.

---

## 4. Thresholds (Hardcoded)

- **HIGH** ≥ **0.92** → Auto RESOLVED + mapping (SYSTEM)
- **LOW** < **0.60** → NEW_CANDIDATE (suggestion nahi)
- **MEDIUM** (0.60 – 0.92) → REVIEW_REQUIRED (suggested_master_product_id + suggested_confidence set)

---

## 5. APIs (Phase 4B)

### 5.1 POST `/purchase/drafts/{draftId}/resolve/auto`

- **Auth:** Login, shop scoped
- **Validation:** Draft DRAFT ho, **supplier_party_id set** ho
- **Response (200):** `DraftReviewResponse` (header + lines + needsReview + readyToPost)

### 5.2 GET `/purchase/drafts/{draftId}/review`

- **Response (200):** Same `DraftReviewResponse` (draft + lines with resolution/suggestions/outcome + needsReview + readyToPost)
- **404:** Draft not found / other shop

### 5.3 PUT `/purchase/drafts/{draftId}/lines/{lineId}/resolve`

- **Body:** `ResolveLineRequest` – **action** (ACCEPT_SUGGESTION | CHOOSE_EXISTING | CREATE_NEW_PRODUCT) + action ke hisaab se shopProductId / canonicalName, baseUnit, categoryId
- **Response (200):** Updated `DraftReviewResponse`
- **Errors:** NO_SUGGESTION (ACCEPT without suggestion), SHOP_PRODUCT_NOT_FOUND, CATEGORY_NOT_FOUND, VALIDATION_FAILED, etc. (sab uniform ApiResponse)

---

## 6. Response / Request (4B)

- **DraftReviewResponse:** draftId, uploadId, status, version, totalAmount, supplierPartyId, invoiceNo, invoiceDate, notes, **lines** (ReviewLineResponse[]), **needsReview**, **readyToPost**
- **ReviewLineResponse:** lineId, lineNo, rawName, normalizedName, quantity, unit, unitPrice, lineAmount, resolutionStatus, **resolvedShopProductId**, **suggestedMasterProductId**, **suggestedConfidence**, **resolutionOutcome**
- **ResolveLineRequest:** **action** + shopProductId (CHOOSE_EXISTING) ya canonicalName, baseUnit, categoryId (CREATE_NEW_PRODUCT)

---

## 7. 4A se Connection

- 4A ka **PUT save (replace-all)** already har line ko **UNRESOLVED** karta hai aur suggested_* clear karta hai; isliye draft edit ke baad resolve dobara chalaana padta hai.
- 4B **stock ya ledger update nahi karta** – sirf mapping + line resolution; POST (4C) me stock/ledger hoga.

---

## 8. Testing – phase4b_e2e.py

**Script:** `scripts/phase4b_e2e.py`

**Flow:** Health → Register → Create category → Create **supplier party** → Upload → Create **draft with supplierPartyId** → **POST resolve/auto** → **GET review** → **PUT resolve** one line (CREATE_NEW_PRODUCT: canonicalName, baseUnit, categoryId) → GET review again (check at least one line RESOLVED).

Deterministic rakhne ke liye test ek line ko **CREATE_NEW_PRODUCT** se resolve karta hai; auto-resolve se kitni lines RESOLVED/REVIEW_REQUIRED/NEW_CANDIDATE aayengi wo master_product data pe depend karta hai.
