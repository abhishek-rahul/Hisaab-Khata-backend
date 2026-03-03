# Phase 4 – Purchase Bill Upload (Understanding Doc)

Yeh document Phase 4 mein **abhi tak jo implement hua hai** (sirf **Phase 4D**), use simple aur easy terms mein samjhata hai.

---

## 1. Phase 4 Kya Hai? (Big Picture)

Phase 4 **supplier bill / invoice upload** se related hai. Future mein shopkeeper wholesale supplier ka bill (PDF) upload karega, system usse parse karega, aur phir **draft → resolve → POST** flow chalega.

Phase 4 ko 4 sub-phases mein plan kiya gaya hai:

| Sub-phase | Kya karta hai | Abhi status |
|-----------|----------------|-------------|
| **4D** | Upload + Parse → PDF upload karo, parsed lines (normalized) milen | ✅ Implemented (Mock) |
| **4A** | Draft persist → `purchase_invoice` + lines DRAFT state mein save | ❌ Nahi kiya |
| **4B** | Resolve → Line items ko shop products se map karo, user approve kare | ❌ Nahi kiya |
| **4C** | POST → Stock update + supplier ledger + status POSTED (atomic) | ❌ Nahi kiya |

**Abhi sirf 4D** implement hai: PDF upload → DB mein upload + **3 mocked lines** save → same data GET se bhi milta hai. **Real PDF parsing / OCR abhi nahi** – sab **MOCK** hai, taaki API shape aur DB ready rahein.

---

## 2. Phase 4D – Kya Cheezein Add Hui?

### 2.1 Simple Flow

1. Shopkeeper **POST /purchase/upload** pe PDF bhejta hai (multipart, field name **`file`**).
2. Optional: **supplierPartyId** bhej sakte hain (agar pehle se supplier party pata ho).
3. Backend PDF ko **parse** karta hai – abhi **mock** hai, matlab **3 fixed sample lines** return karta hai (chahe PDF kuch bhi ho).
4. Har line ka **raw name** (jaise "Basmati Rice 5 Kg") ko **normalize** kiya jata hai (lowercase, trim, spaces collapse, unit cleanup) → **normalizedName**.
5. **purchase_upload** (1 row) + **purchase_upload_line** (3 rows) DB mein save hote hain; status = **PARSED**, parser_key = **MOCK:v0**.
6. Response mein **ParsedInvoiceResponse** milta hai: uploadId, docKind, parserKey, aur lines array (har line: lineNo, rawName, normalizedName, quantity, unit, unitPrice, lineAmount, parseConfidence).
7. **GET /purchase/upload/{uploadId}** se wahi data dobara nikal sakte ho (shop-scoped).

---

## 3. Database – Flyway V5

**File:** `src/main/resources/db/migration/V5__purchase_upload_mock_parsed_lines.sql`

**Important:** Is file ko migrate hone ke baad **edit mat karna**. Koi change chahiye to nayi migration (V6, V7 …) banao.

### 3.1 Enums

- **purchase_upload_doc_kind:** Abhi sirf `PDF`.
- **purchase_upload_status:** `UPLOADING`, `PARSED`, `FAILED` – abhi hum hamesha **PARSED** set karte hain mock ke saath.

### 3.2 Table: `purchase_upload`

- **id** – primary key  
- **shop_id** – kis shop ka upload (FK → shop)  
- **supplier_party_id** – optional; agar user ne supplier choose kiya (FK → party)  
- **doc_kind** – PDF (enum)  
- **parser_key** – kaunse parser ne parse kiya, e.g. `MOCK:v0`  
- **status** – PARSED (enum)  
- **original_file_name** – user ne jo filename di thi (optional)  
- **created_at**, **updated_at**

### 3.3 Table: `purchase_upload_line`

Har parsed line item ka ek row (abhi mock mein 3 lines).

- **id** – primary key  
- **purchase_upload_id** – FK → purchase_upload  
- **line_no** – 1, 2, 3 …  
- **raw_name** – bill pe jo naam tha (e.g. "Basmati Rice 5 Kg")  
- **normalized_name** – cleanup ke baad (matching ke liye)  
- **quantity**, **unit**, **unit_price**, **line_amount**  
- **parse_confidence** – optional; future real parser confidence 0–1  
- **created_at**, **updated_at**

---

## 4. APIs (Phase 4D)

### 4.1 POST /purchase/upload

- **Content-Type:** `multipart/form-data`  
- **Part name:** `file` (PDF file; required)  
- **Optional part:** `supplierPartyId` (number)  
- **Auth:** Login required (JWT), shop context automatic  
- **Success (201):** `ApiResponse` with `data` = **ParsedInvoiceResponse** (uploadId, docKind, parserKey, lines[])  
- **Error:** Missing file → 400 (MISSING_FILE / INVALID_MULTIPART), invalid multipart → 400, same **ApiResponse** format (success=false, errorCode, message)

### 4.2 GET /purchase/upload/{uploadId}

- **Auth:** Login required, sirf apne shop ka upload dekh sakte ho  
- **Success (200):** Same **ParsedInvoiceResponse**  
- **Not found / wrong shop:** 404 (UPLOAD_NOT_FOUND)

---

## 5. Code Structure (Simple Terms)

- **InvoiceParser** (interface): `ParsedInvoice parse(byte[] pdfBytes)` – abhi **MockInvoiceParser** use hota hai (3 sample items return). Baad mein real OCR/parser yahi interface implement karega.
- **Normalizer** (interface): `String normalize(String raw)` – **DefaultNormalizer**: lowercase, trim, extra spaces hatao, unit tokens (kg, gm, etc.) thoda cleanup.
- **PurchaseUploadService:** Upload pe file bytes + optional supplierPartyId leta hai → parser chalata hai → har line ka name normalize karta hai → purchase_upload + purchase_upload_line save → **ParsedInvoiceResponse** return. GET pe DB se load karke same response.
- **PurchaseUploadController:** POST (multipart) aur GET (by uploadId) handle karta hai; response hamesha **ApiResponse** wrapper mein (StaffController / PartyController jaisa).

---

## 6. Response DTO – ParsedInvoiceResponse

Future real parser ke saath bhi yeh shape stable rehni chahiye:

- **uploadId** – saved purchase_upload id  
- **docKind** – "PDF"  
- **parserKey** – "MOCK:v0"  
- **lines** – array of:
  - **lineNo**, **rawName**, **normalizedName**
  - **quantity**, **unit**, **unitPrice**, **lineAmount**
  - **parseConfidence** (optional)

---

## 7. Testing – phase4d_e2e.py

Script: `scripts/phase4d_e2e.py`

- Health check → Register shop → **POST /purchase/upload** (dummy PDF) → **GET /purchase/upload/{uploadId}** → assert 3 lines, docKind PDF, parserKey MOCK:v0.
- Phir **POST without file** (empty multipart) → expect **400** + uniform ErrorResponse (success=false, errorCode/message).

---

## 8. Abhi Nahi Kiya (4A, 4B, 4C)

- **purchase_invoice** draft (4A) – nahi  
- Supplier mapping / resolve / suggestions (4B) – nahi  
- Stock update, ledger, POSTED status (4C) – nahi  
- Real PDF/OCR parsing – nahi (sirf MOCK)

Jab 4A/4B/4C implement karenge, tab isi **purchase_upload** / **purchase_upload_line** data ko use karke draft invoice banaenge, resolve karenge, aur phir POST karenge.
