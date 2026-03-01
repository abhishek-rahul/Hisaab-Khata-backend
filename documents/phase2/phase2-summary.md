# Phase 2 – Party + Ledger Foundation (Summary)

Yeh document Phase 2 mein jo kuch implement kiya gaya hai, use simple aur easy terms mein consolidate karta hai.

---

## 1. Phase 2 Kya Hai?

Phase 2 **Parties** aur **Ledger (Khata)** ki foundation dalta hai:

- **Party:** Customer ya Supplier dono ko ek hi model se handle karte hain – ek hi table `party` with type CUSTOMER/SUPPLIER.
- **Party Ledger:** Har party ka hisaab (udhaar, payment) baad mein isi table mein append hoga. Abhi Phase 2 mein **koi entry insert nahi hoti** – sirf structure ready hai, ledger empty return hota hai.

---

## 2. Database (Flyway V2)

**File:** `src/main/resources/db/migration/V2__party_and_party_ledger.sql`

### Enums (DB level)

- **party_type:** `CUSTOMER`, `SUPPLIER`
- **ledger_entry_type:** `SALE`, `PURCHASE`, `PAYMENT_IN`, `PAYMENT_OUT`, `ADJUSTMENT`
- **ledger_reference_type:** `SALE`, `PURCHASE`, `PAYMENT`

### Table: `party`

- `id` (bigserial, PK)
- `shop_id` (FK → shop, CASCADE delete)
- `name` (text, required)
- `phone` (text, optional)
- `type` (party_type enum: CUSTOMER / SUPPLIER)
- `created_at`, `updated_at`
- **Rule:** Ek shop ke andar **same phone number do parties ke paas nahi ho sakta** (jab phone null na ho). Iske liye partial unique index: `(shop_id, phone)` **WHERE phone IS NOT NULL**.

### Table: `party_ledger`

- Append-only hisaab entries (baad mein sale/purchase/payment se bharenge).
- Columns: `id`, `shop_id`, `party_id`, `entry_type`, `dr_amount`, `cr_amount`, `reference_type`, `reference_id`, `remarks`, `created_at`.
- **Constraints:** `dr_amount` aur `cr_amount` non-negative; har row mein sirf ek side (ya to dr ya cr) non-zero.
- **Triggers:** UPDATE aur DELETE block – koi entry edit/delete nahi kar sakta, sirf nayi entry add ho sakti hai.

---

## 3. Spring Boot – Entities & Enums

### Enums (Java)

- **PartyType:** CUSTOMER, SUPPLIER
- **LedgerEntryType:** SALE, PURCHASE, PAYMENT_IN, PAYMENT_OUT, ADJUSTMENT
- **LedgerReferenceType:** SALE, PURCHASE, PAYMENT

### Entity: `Party`

- `Party` table se map. Extends `BaseEntity` (created_at, updated_at).
- Fields: id, shop, name, phone, type (PartyType).
- **Important:** Column `type` DB mein PostgreSQL enum hai, isliye Hibernate mein `@JdbcTypeCode(SqlTypes.NAMED_ENUM)` use kiya gaya hai (VARCHAR nahi).

### Entity: `PartyLedger`

- `party_ledger` table se map. Append-only; Phase 2 mein insert nahi hota, sirf structure.

### Repositories

- **PartyRepository:** `findByShopId`, `findByShopIdAndType`, `findByShopIdAndId`, `existsByShopIdAndPhone` (duplicate check).
- **PartyLedgerRepository:** `findByShopIdAndPartyIdOrderByCreatedAtAsc` (baad ke phases ke liye).

---

## 4. DTOs

- **CreatePartyRequest:** name (required), phone (optional), type (required – CUSTOMER/SUPPLIER). Validation: `@NotBlank` name, `@NotNull` type.
- **PartyResponse:** id, name, phone, type.
- **PartyLedgerResponse:** partyId, entries (list), balance. **Phase 2:** entries hamesha empty `[]`, balance hamesha `0`.
- **PartyLedgerEntryResponse:** Single entry ke liye (abhi use nahi ho raha, future ke liye).

---

## 5. Service – PartyService

- **createParty(request):** Current shop (JWT se) ke under nayi party banata hai. Agar phone diya hai aur wahi phone is shop mein pehle se kisi party ke paas hai to **ConflictException** (PARTY_PHONE_DUPLICATE).
- **listParties(type):** Optional `type` (CUSTOMER/SUPPLIER) – agar type diya to filter, nahi to saari parties.
- **getParty(partyId):** Party by id (sirf current shop ki). Nahi mili to **ResourceNotFoundException** (PARTY_NOT_FOUND).
- **getPartyLedger(partyId):** Party verify karke ledger response deta hai – **entries = []**, **balance = 0**. (Asli entries baad ke phases mein aayengi.)

---

## 6. Controller – APIs

**Base path:** `/parties` (sab endpoints authenticated – JWT zaroori)

| Method | Path | Response | Description |
|--------|------|----------|-------------|
| POST | /parties | 201 Created, SuccessResponse&lt;PartyResponse&gt; | Nayi party create (customer ya supplier) |
| GET | /parties?type=CUSTOMER \| SUPPLIER | 200 OK, SuccessResponse&lt;List&lt;PartyResponse&gt;&gt; | Parties list (optional type filter) |
| GET | /parties/{partyId} | 200 OK, SuccessResponse&lt;PartyResponse&gt; | Ek party ki detail |
| GET | /parties/{partyId}/ledger | 200 OK, SuccessResponse&lt;PartyLedgerResponse&gt; | Ledger – abhi entries=[], balance=0 |

Shop scope **authenticated user se** aata hai (Phase 1 jaisa – ShopContext).

---

## 7. Security & Error Handling

- **Security:** `/parties` sab routes `anyRequest().authenticated()` ke under – JWT Bearer token zaroori. Shop current user ke token se resolve hota hai.
- **Duplicate phone:** 409 Conflict, `errorCode`: **PARTY_PHONE_DUPLICATE**, uniform ApiResponse/ErrorResponse format.
- **Party not found:** 404 Not Found, `errorCode`: **PARTY_NOT_FOUND**.

---

## 8. E2E Script

**File:** `scripts/phase2_e2e.py`

**Flow:**

1. **GET /health** – Server up check
2. **POST /auth/register** – Naya shop register karke JWT token lena
3. **POST /parties** – Customer party create (unique phone)
4. **POST /parties** – Supplier party create (alag phone)
5. **GET /parties?type=CUSTOMER** – Sirf customers list
6. **GET /parties/{partyId}/ledger** – Ledger check: entries = [], balance = 0
7. **POST /parties** – Same phone se dubara party create try → **400/409** expect, `errorCode` = **PARTY_PHONE_DUPLICATE**

**Run:**  
`python scripts/phase2_e2e.py`  
ya  
`python scripts/phase2_e2e.py http://localhost:8080`

---

## 9. Important Points

- **Phase 2 mein `party_ledger` mein koi row insert nahi hoti.** Ledger entries Purchase POST / Sale POST / Payments wale phases mein add hongi.
- Response wrappers Phase 1 jaisa hi: **SuccessResponse** with `success`, `data`; errors **ApiResponse** format with `success=false`, `errorCode`, `message`.
- PostgreSQL only, schema sirf **Flyway migrations** se (no Hibernate ddl-auto).
- Branch: **develop_feature_phase2** (develop_feature se cut kiya gaya).

---

## 10. Files Added / Touched (Quick Reference)

| Category | Path / Files |
|----------|----------------|
| Migration | `src/main/resources/db/migration/V2__party_and_party_ledger.sql` |
| Enums | `enums/PartyType.java`, `LedgerEntryType.java`, `LedgerReferenceType.java` |
| Entities | `domain/Party.java`, `domain/PartyLedger.java` |
| Repositories | `repository/PartyRepository.java`, `PartyLedgerRepository.java` |
| DTOs | `dto/partydto/CreatePartyRequest.java`, `PartyResponse.java`, `PartyLedgerResponse.java`, `PartyLedgerEntryResponse.java` |
| Mapper | `mapper/PartyMapper.java` |
| Service | `service/IPartyService.java`, `service/impl/PartyServiceImpl.java` |
| Controller | `controller/PartyController.java` |
| E2E | `scripts/phase2_e2e.py` |

Yeh document Phase 2 implementation ko ek jagah simple terms mein consolidate karta hai; detail ke liye code aur migration file refer karein.
