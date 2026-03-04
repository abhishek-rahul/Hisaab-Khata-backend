# Hisaab-Khata Postman Collection (Phase 1–6)

Production-ready collection and environments for all implemented endpoints. Request/response shapes are derived from controller DTOs.

## Quick start

1. **Import in Postman**
   - File → Import → select `HisaabKhata_Phase1_6.postman_collection.json`
   - Import `HisaabKhata_local.postman_environment.json` and/or `HisaabKhata_dev.postman_environment.json`

2. **Select environment**
   - Use **HisaabKhata_local** for `http://localhost:8080` (default).
   - Use **HisaabKhata_dev** for a remote dev server (set `baseUrl` in the environment).

3. **Runner order (recommended)**
   - Run the collection with the default folder order, or run the **Flows** folder first:
     1. Health → Auth (Register → Login) → Me  
     2. Parties (Create Customer → Create Supplier)  
     3. Categories (Create)  
     4. Products (Create Manual → List)  
     5. Purchase: Upload → Get Upload → Create Draft → Get Draft → Resolve Auto → Get Review → Resolve Line → Post Draft → Get Purchase  
     6. Sales: Create Draft → Post → Get Sale  
     7. Ledger: Statement → Balance  
     8. Reports: Daily  

4. **Auth**
   - After **Register** or **Login**, the collection tests save `accessToken` and `refreshToken` into the active environment.
   - Protected requests use **Authorization: Bearer {{accessToken}}**.
   - Use **Auth/Refresh Token** to refresh and update `accessToken` (send **Bearer {{refreshToken}}** in Authorization header).

5. **File upload (Purchase)**
   - **POST /purchase/upload** uses `form-data` with a `file` key.
   - In Postman: Body → form-data → key `file`, type **File** → choose a small PDF (or any file; mock parser may accept it).
   - See collection description for placeholder note if no file is attached.

---

## Endpoint inventory

All endpoints below are included in the collection. Tagging:

- **core** – Used by `scripts/regression_core_phase6.py` (Phase 6 regression).
- **optional** – Implemented, not used by E2E yet.
- **legacy** – Old/duplicate API; kept for reference, recommend deprecation.

---

### Health
| Method | Path | Tag | Description |
|--------|------|-----|-------------|
| GET | /health | core | Liveness; returns `{ success, statusCode, message, data: { status } }`. |

---

### Auth
| Method | Path | Tag | Request body | Response |
|--------|------|-----|--------------|----------|
| POST | /auth/register | core | RegisterRequest: shopName, ownerName, mobile, password | ApiResponse + AuthResponse (shopId, userId, token, refreshToken) |
| POST | /auth/login | core | LoginRequest: mobile, password | ApiResponse + AuthResponse |
| POST | /auth/refresh | core | (none; header **Authorization: Bearer {{refreshToken}}**) | ApiResponse + AuthResponse (new token, refreshToken) |

---

### Me
| Method | Path | Tag | Description |
|--------|------|-----|-------------|
| GET | /me | core | Current user/shop; returns ApiResponse + MeResponse (id, shopId, role, etc.). |

---

### Shops / Staff
| Method | Path | Tag | Request body | Description |
|--------|------|-----|--------------|-------------|
| POST | /shops/{shopId}/staff | optional | StaffCreateRequest: name, mobile, password | Owner creates staff (OWNER only). |

---

### Parties
| Method | Path | Tag | Request body | Response |
|--------|------|-----|--------------|----------|
| POST | /parties | core | CreatePartyRequest: name, phone?, type (CUSTOMER \| SUPPLIER) | ApiResponse + PartyResponse |
| GET | /parties | core | Query: type (optional) | ApiResponse + List&lt;PartyResponse&gt; |
| GET | /parties/{partyId} | core | — | ApiResponse + PartyResponse |
| GET | /parties/{partyId}/ledger | optional | — | ApiResponse + PartyLedgerResponse (entries, balance) |

---

### Categories
| Method | Path | Tag | Request body | Response |
|--------|------|-----|--------------|----------|
| POST | /categories | core | CategoryCreateRequest: name | ApiResponse + CategoryResponse (id, name, scope) |
| GET | /categories | core | — | ApiResponse + List&lt;CategoryResponse&gt; |

---

### Products
| Method | Path | Tag | Request body | Response |
|--------|------|-----|--------------|----------|
| POST | /products/manual | core | CreateManualProductRequest: canonicalName, normalizedName, baseUnit (PCS\|GRAM\|ML), categoryId?, displayName, displayUnit, conversionToBase, sellingPrice?, minStockInBase? | ApiResponse + ShopProductResponse |
| GET | /products | core | — | ApiResponse + List&lt;ShopProductResponse&gt; |
| PATCH | /products/{shopProductId} | core | PatchShopProductRequest: sellingPrice?, minStockInBase?, isActive? | ApiResponse + ShopProductResponse |

---

### Stock
| Method | Path | Tag | Description |
|--------|------|-----|-------------|
| GET | /stock | core | List all stock; ApiResponse + List&lt;StockResponse&gt; |
| GET | /stock/{shopProductId} | core | Stock for one product; ApiResponse + StockResponse |

---

### Purchase (upload + drafts + post)
| Method | Path | Tag | Request / params | Response |
|--------|------|-----|------------------|----------|
| POST | /purchase/upload | core | form-data: **file** (PDF) | ApiResponse + ParsedInvoiceResponse (uploadId, lines, docKind, parserKey) |
| GET | /purchase/upload/{uploadId} | core | — | ApiResponse + ParsedInvoiceResponse |
| POST | /purchase/drafts | core | CreateDraftRequest: uploadId, supplierPartyId?, invoiceNo?, invoiceDate?, notes? | ApiResponse + DraftResponse (draftId, version, …) |
| GET | /purchase/drafts/{draftId} | core | — | ApiResponse + DraftResponse |
| PUT | /purchase/drafts/{draftId} | core | SaveDraftRequest: version, supplierPartyId?, invoiceNo?, invoiceDate?, notes?, lines? | ApiResponse + DraftResponse |
| POST | /purchase/drafts/{draftId}/resolve/auto | core | (none) | ApiResponse + DraftReviewResponse (lines, needsReview, readyToPost) |
| GET | /purchase/drafts/{draftId}/review | core | — | ApiResponse + DraftReviewResponse |
| PUT | /purchase/drafts/{draftId}/lines/{lineId}/resolve | core | ResolveLineRequest: action (ACCEPT_SUGGESTION \| CHOOSE_EXISTING \| CREATE_NEW_PRODUCT), shopProductId?, canonicalName?, baseUnit?, categoryId? | ApiResponse + DraftReviewResponse |
| POST | /purchase/drafts/{draftId}/post | core | (none) | ApiResponse + PostedInvoiceSummary (draftId, status, postedAt, totalAmount, supplierPartyId, invoiceNo) |
| GET | /purchase/{id} | core | id = draftId (posted invoice id) | SuccessResponse + PurchaseResponse (id, productId, productName, supplierId, supplierName, quantity, unit, costPrice, paymentMode, createdAt) |

---

### Purchase (legacy – single product)
| Method | Path | Tag | Request body | Description |
|--------|------|-----|--------------|-------------|
| POST | /purchase | legacy | PurchaseCreateRequest: productId, supplierId?, quantity, unit, costPrice, paymentMode | Creates single-product purchase; **deprecated** in favour of upload→draft→post. |
| GET | /purchase | legacy | — | List purchases (legacy). |
| PUT | /purchase/{id} | legacy | Query: supplierId | Update supplier (legacy). |

---

### Sales
| Method | Path | Tag | Request body | Response |
|--------|------|-----|--------------|----------|
| POST | /sales/draft | core | SalesDraftRequest: shopId, customerPartyId?, discountAmount, cashPaidAmount, upiPaidAmount, items: [{ shopProductId, quantityInBase, unitPrice }] | ApiResponse + SalesDraftResponse (id, status, totalAmount, paidAmount, …) |
| GET | /sales/{id} | core | — | ApiResponse + SalesDraftResponse (or posted sale) |
| POST | /sales/{id}/post | core | (none) | ApiResponse + PostedSaleResponse (id, status, …) |
| GET | /sales | optional | Query: from?, to? (date) | ApiResponse + List&lt;SalesDraftResponse&gt; |

---

### Ledger
| Method | Path | Tag | Query params | Response |
|--------|------|-----|--------------|----------|
| GET | /ledger/parties/{partyId}/statement | core | from (date), to (date), limit=50, offset=0 | SuccessResponse + LedgerStatementResponse (entries, balance) |
| GET | /ledger/parties/{partyId}/balance | core | — | SuccessResponse + LedgerBalanceResponse |

---

### Reports
| Method | Path | Tag | Query params | Response |
|--------|------|-----|--------------|----------|
| GET | /report/daily | core | date (YYYY-MM-DD) | SuccessResponse + DailyReportResponse (totalSales, totalPurchase, date, …) |
| GET | /report/sales | optional | from, to | SuccessResponse + SalesReportResponse |
| GET | /report/stock | optional | — | SuccessResponse + StockReportResponse |
| GET | /report/khata | optional | — | SuccessResponse + KhataReportResponse |
| GET | /report/range | optional | from, to | SuccessResponse + DailyRangeReportResponse |
| GET | /report/profit | optional | from, to | SuccessResponse + ProfitReportResponse |

---

### Dashboard
| Method | Path | Tag | Description |
|--------|------|-----|-------------|
| GET | /dashboard/today | optional | SuccessResponse + TodaySummaryResponse |
| GET | /dashboard/week | optional | SuccessResponse + TodaySummaryResponse |
| GET | /dashboard/month | optional | SuccessResponse + TodaySummaryResponse |

---

### Khata
| Method | Path | Tag | Description |
|--------|------|-----|-------------|
| GET | /khata/pending | optional | SuccessResponse + List&lt;PendingKhataResponse&gt; (pending customer balances). |

---

### Search
| Method | Path | Tag | Query | Description |
|--------|------|-----|-------|-------------|
| GET | /search | optional | q (search term) | SuccessResponse + List&lt;SearchResultResponse&gt; |

---

### Closing
| Method | Path | Tag | Description |
|--------|------|-----|-------------|
| POST | /closing | optional | SuccessResponse + DailyClosingResponse |
| GET | /closing | optional | SuccessResponse + List&lt;DailyClosingResponse&gt; |

---

## Environment variables

Set in environment (local/dev):

| Variable | Description | Set by |
|----------|-------------|--------|
| baseUrl | Base API URL (e.g. http://localhost:8080) | Manual / default |
| accessToken | JWT access token | Register / Login / Refresh tests |
| refreshToken | Refresh token | Register / Login tests |
| shopId | Current shop id | Register / Me tests |
| ownerUserId | Owner user id | Register / Me tests |
| partyCustomerId | Created customer party id | Create Party (CUSTOMER) test |
| partySupplierId | Created supplier party id | Create Party (SUPPLIER) test |
| categoryId | Created category id | Create Category test |
| shopProductId | Created shop product id | Create Product Manual test |
| purchaseUploadId | Upload id from POST /purchase/upload | Purchase Upload test |
| purchaseDraftId | Draft id from POST /purchase/drafts | Create Draft test |
| purchaseLineId | First unresolved line id from review | Get Review test (or resolve script) |
| purchaseId | Posted purchase id (same as draftId after post) | Post Draft test |
| salesDraftId | Sales draft id | Sales Draft test |
| saleId | Posted sale id | Sales Post test |
| ledgerPartyId | Party id for ledger (use partyCustomerId) | Manual or from party |
| today | Today date YYYY-MM-DD | Pre-request script if needed |

---

## Response envelopes

- **ApiResponse&lt;T&gt;** (Auth, Me, Parties, Categories, Products, Stock, Purchase drafts/upload, Sales):  
  `success`, `statusCode`, `message`, `data`, `meta?`, `errorCode?`, `errors?` (validation).
- **SuccessResponse&lt;T&gt;** (Ledger, Reports, Dashboard, Khata, Search, Purchase GET):  
  `success`, `status`, `message`, `data`.

Error (4xx/5xx): `success: false`, `statusCode` / `errorCode`, `message`, `errors` (array of `{ field, message }` for validation).

---

## Legacy endpoints (recommend deletion later)

- **POST/GET/PUT /purchase** (single-product legacy flow). Prefer **/purchase/upload** → **/purchase/drafts** → **/purchase/drafts/{id}/post** and **GET /purchase/{id}** (served from purchase_invoice).

---

## Extending for Phase 7+

See `collection_notes.md` for how to add new folders, requests, and variables when new phases are implemented.
