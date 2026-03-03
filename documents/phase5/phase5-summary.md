# Phase 5 – Sales Draft + Sales POST (Summary)

Yeh document Phase 5 mein jo changes kiye gaye hain (Sales draft, post, payment, daily summary) unko **simple aur easy** terms mein samjhata hai.

---

## 1. Phase 5 Kya Hai? (Big Picture)

Phase 5 **sales (bechai)** ka full flow implement karta hai:

- **Draft:** Shopkeeper sale time pe items select/enter karega → ek **DRAFT** invoice banta hai (payment + discount bhi).
- **Post:** Jab POST karenge tab hi **stock decrease** hoga, **customer ka ledger** update hoga, aur **daily summary** update hogi.
- **Walk-in:** Customer optional hai – bina customer ke bhi sale ho sakti hai (walk-in). Walk-in pe **full payment** zaroori hai (partial nahi).
- **Customer sale:** Customer select ho to **udhaar** allowed – partial payment, baaki khata mein.

---

## 2. Database – Flyway V9 Migration

**File:** `src/main/resources/db/migration/V9__sales_payment_and_discount_support.sql`

**Important:** Migration file ko run hone ke baad **edit mat karna**. Naya change = nayi migration (V10, …).

### 2.1 Enum: `payment_mode`

- **CASH** – sirf cash
- **UPI** – sirf UPI  
- **MIXED** – cash + UPI dono
- **NA** – koi payment nahi (paid = 0)

### 2.2 Table: `sales_invoice`

- **id**, **shop_id**, **customer_party_id** (null = walk-in)
- **invoice_date**, **status** (DRAFT / POSTED), **posted_at**
- **gross_amount** = lines ka total
- **discount_amount** = header-level discount
- **total_amount** = gross − discount (net)
- **cash_paid_amount**, **upi_paid_amount**, **paid_amount** = cash + upi
- **payment_mode**

**Constraints:**

- `paid_amount = cash_paid_amount + upi_paid_amount`
- `total_amount = gross_amount - discount_amount`
- Sab amounts non-negative; `discount_amount <= gross_amount`
- POSTED ho to `posted_at` set hona chahiye

### 2.3 Table: `sales_invoice_line`

- **sales_invoice_id**, **line_no**, **shop_product_id**
- **quantity_in_base**, **unit_price**, **line_amount**
- Line amount = quantity × unit_price; sab non-negative

### 2.4 Table: `daily_summary`

- **shop_id**, **day** (date) – ek row per shop per day
- **total_sales**, **cash_in**, **receivable**
- Sale/Purchase POST pe **upsert** hota hai (same day pe add/update)

---

## 3. APIs (Sales Controller)

**Base path:** `/sales`  
**Controller:** `SalesController.java`

| Method | Path | Kya karta hai |
|--------|------|----------------|
| POST   | `/sales/draft`       | Naya draft invoice banao (items + discount + payment) |
| GET    | `/sales/{id}`        | Ek invoice get karo (id se) |
| POST   | `/sales/{id}/post`   | Draft ko POST karo → stock + ledger + daily_summary update |
| GET    | `/sales?from=&to=`   | Date range mein saari sales list karo |

### 3.1 POST /sales/draft – Request Body

```json
{
  "shopId": 1,
  "customerPartyId": null,
  "discountAmount": 0,
  "cashPaidAmount": 100,
  "upiPaidAmount": 0,
  "items": [
    { "shopProductId": 5, "quantityInBase": 10, "unitPrice": 10 }
  ]
}
```

- **customerPartyId** = null → walk-in (paid must = total)
- **customerPartyId** = id → customer sale (partial payment allowed)
- **gross_amount** backend khud nikalta hai (sum of line_amount)
- **total_amount** = gross − discount; **paid_amount** = cash + upi

### 3.2 Response (Draft / Get)

- **id**, **shopId**, **customerPartyId**, **customerPartyName**
- **invoiceDate**, **status** (DRAFT / POSTED)
- **grossAmount**, **discountAmount**, **totalAmount**
- **paidAmount**, **cashPaidAmount**, **upiPaidAmount**, **paymentMode**
- **postedAt**, **createdAt**
- **lines[]** – har line: shopProductId, quantityInBase, unitPrice, lineAmount

---

## 4. Business Rules (Validation)

- **gross_amount** = sum(line_amount) – backend compute karta hai.
- **total_amount** = gross_amount − discount_amount.
- **paid_amount** = cash_paid_amount + upi_paid_amount.
- **paid_amount** ≤ total_amount (zyada pay nahi kar sakte).
- **Walk-in (customer null):** paid_amount **must equal** total_amount; nahi to **400** (WALKIN_FULL_PAYMENT_REQUIRED).
- **payment_mode:**  
  paid=0 → NA; sirf cash → CASH; sirf UPI → UPI; dono → MIXED.
- **POST:** Sirf **DRAFT** pe allow; agar pehle se **POSTED** ho to **409** (ALREADY_POSTED).

---

## 5. Sales POST – Transaction Mein Kya Hota Hai?

Jab **POST /sales/{id}/post** call hota hai, ek hi **transaction** mein yeh sab hota hai:

1. **Lock:** Us sales_invoice row ko lock kiya jata hai (double post avoid).
2. **Stock:** Har line ke hisaab se us **shop_product** ka stock **ghataaya** jata hai (negative allowed).
3. **Ledger (agar customer ho):**
   - **SALE** entry: DR = total_amount (udhaar badha).
   - **PAYMENT_IN** entry: CR = paid_amount (agar paid > 0).
   - Dono **party_ledger** mein append-only.
4. **daily_summary:** Us **invoice_date** (day) ke liye row **upsert**:
   - total_sales += total_amount  
   - cash_in += paid_amount  
   - agar customer hai to receivable += (total_amount − paid_amount)
5. **Invoice:** status = **POSTED**, posted_at = ab

Agar beech mein kahi fail ho to **pura transaction rollback** – kuch bhi persist nahi hota.

---

## 6. Important Files (Phase 5)

| Type        | Path / File |
|------------|-------------|
| Migration  | `src/main/resources/db/migration/V9__sales_payment_and_discount_support.sql` |
| Controller | `src/main/java/.../controller/SalesController.java` |
| Service    | `src/main/java/.../service/ISalesService.java`, `.../service/impl/SalesServiceImpl.java` |
| Entities   | `SalesInvoice.java`, `SalesInvoiceLine.java`, `DailySummary.java` |
| DTOs       | `dto/sales/SalesDraftRequest.java`, `SalesDraftResponse.java`, `PostedSaleResponse.java`, etc. |
| E2E Script | `scripts/phase5_e2e.py` |

---

## 7. E2E Test (phase5_e2e.py)

Script yeh check karta hai:

1. **Walk-in full payment** – draft banao (paid = total), post karo → success.
2. **Customer partial mixed** – customer ke sath draft (paid < total), post → success.
3. **Walk-in partial** – bina customer, paid < total → **400** (reject).

Run:  
`python scripts/phase5_e2e.py`  
(optional: `python scripts/phase5_e2e.py http://localhost:8080`)

---

## 8. Phase 5 Complete – Aage Kya?

Phase 5 goals achieve ho gaye: Sales draft, post, stock update, ledger (customer), daily_summary.  
Aage **Phase 6 (Ledger/Khata)** – standalone payments, balance-on-read API, supplier payments – par kaam kar sakte ho.
