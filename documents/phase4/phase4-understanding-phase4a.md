# Phase 4A – Draft Purchase Invoice (Understanding Doc)

Yeh document Phase 4A mein **jo implement hua hai** (Draft Purchase Invoice) usko simple terms mein explain karta hai.

**Phase 4A ka main goal:** Upload (4D) ke parsed lines ko lekar **ek editable DRAFT invoice** DB mein persist karna.

---

## 1. Phase 4 Flow (Context)

Phase 4 ka overall flow yeh hai:

- **4D (done)**: PDF upload + mock parse → `purchase_upload` + `purchase_upload_line`
- **4A (this doc)**: Draft persist → `purchase_invoice` + `purchase_invoice_line` (status = DRAFT)
- **4B (not now)**: Resolve/matching → lines ko shop_product se map karna
- **4C (not now)**: POST → stock update + ledger + status POSTED (atomic)

---

## 2. Phase 4A – Simple User Flow

### 2.1 Create Draft

1. Pehle **upload** hota hai (POST `/purchase/upload`) → `uploadId` milta hai.
2. Phir client **POST `/purchase/drafts`** with `uploadId`.
3. Backend:
   - Check karta hai upload **current shop** ka hai (tenant safe).
   - **Idempotent**: agar same `(shop_id, uploadId)` ka draft already bana hua hai, wahi return kar deta hai (duplicate nahi banata).
   - `purchase_invoice` row banata hai (status=DRAFT, version=0).
   - `purchase_upload_line` ko copy karke `purchase_invoice_line` rows banata hai.
   - `total_amount` = sum(line_amount).

### 2.2 Get Draft

- **GET `/purchase/drafts/{draftId}`** se header + lines + version wapas milta hai.

### 2.3 Save Draft (Replace-All)

- **PUT `/purchase/drafts/{draftId}`** (MVP save)
- Request me `version` + header fields + full `lines[]`.
- Server:
  - Ensure status **DRAFT** hi hai (warna 409 conflict).
  - Optimistic locking: **version match** hona chahiye; mismatch pe 409.
  - Old lines delete + new lines insert (replace-all).
  - Har line ka `normalized_name` Normalizer se **recompute**.
  - `resolution_status` sab lines pe **UNRESOLVED** reset.
  - `total_amount` recompute.
  - `version` increment.

---

## 3. Database – Flyway V6

**File:** `src/main/resources/db/migration/V6__purchase_invoice_draft.sql`

### 3.1 Enums

- **doc_status**: `DRAFT`, `POSTED`
- **resolution_status**: `UNRESOLVED`, `RESOLVED` (4B ke liye placeholder)

### 3.2 Table: `purchase_invoice`

- **shop_id**: tenant isolation
- **purchase_upload_id**: source upload link
- **UNIQUE(shop_id, purchase_upload_id)**: idempotency support
- **status**: default `DRAFT`
- **version**: default `0` (optimistic locking)
- **total_amount**: sum of lines
- Optional header fields: `supplier_party_id`, `invoice_no`, `invoice_date`, `notes`

### 3.3 Table: `purchase_invoice_line`

- Line fields: `line_no`, `raw_name`, `normalized_name`, `quantity`, `unit`, `unit_price`, `line_amount`
- **resolution_status**: default `UNRESOLVED` (4B)
- **shop_product_id**: nullable (4B mapping)

---

## 4. APIs (Phase 4A)

### 4.1 POST `/purchase/drafts`

**Request (JSON):**
- `uploadId` (required)
- `supplierPartyId`, `invoiceNo`, `invoiceDate`, `notes` (optional)

**Behavior:**
- Upload shop validation
- Idempotent by `(shop_id, uploadId)`
- Lines copied from upload → draft

**Response:** `ApiResponse< DraftResponse >` (201)

### 4.2 GET `/purchase/drafts/{draftId}`

**Response:** `ApiResponse< DraftResponse >` (200)  
Not found → 404

### 4.3 PUT `/purchase/drafts/{draftId}`

**Request (JSON):**
- `version` (required)
- Header fields optional
- `lines[]` (full replace-all)

**Rules:**
- Status must be DRAFT (else 409)
- Version must match (else 409 `VERSION_CONFLICT`)

**Response:** `ApiResponse< DraftResponse >` (200)

---

## 5. Response DTO (DraftResponse)

`DraftResponse` mein yeh fields milte hain:

- `draftId`, `uploadId`
- `status` (DRAFT)
- `version`
- `totalAmount`
- Optional header: `supplierPartyId`, `invoiceNo`, `invoiceDate`, `notes`
- `lines[]`:
  - `lineNo`, `rawName`, `normalizedName`
  - `quantity`, `unit`, `unitPrice`, `lineAmount`
  - `resolutionStatus` (UNRESOLVED), `shopProductId` (null)

---

## 6. Error Cases (High-level)

- **UPLOAD_NOT_FOUND**: uploadId invalid / other shop
- **DRAFT_NOT_FOUND**: draftId invalid / other shop
- **SUPPLIER_NOT_FOUND**: supplierPartyId wrong (or other shop)
- **DRAFT_NOT_EDITABLE**: status DRAFT nahi (future: POSTED)
- **VERSION_CONFLICT**: stale version se save kiya

---

## 7. Testing – phase4a_e2e.py

Script: `scripts/phase4a_e2e.py`

Flow:
- register → upload → create draft
- idempotent create (same uploadId) → same draftId
- get draft
- save replace-all (PUT) → version increments
- stale version PUT → expect **409**

