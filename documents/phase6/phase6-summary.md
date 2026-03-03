# Phase 6 – Ledger (Khata) Read + Daily Summary Reports (Summary)

Yeh document Phase 6 mein jo changes kiye gaye hain (Ledger balance/statement, daily report, purchase → daily_summary) unko **simple aur easy** terms mein samjhata hai.

---

## 1. Phase 6 Kya Hai? (Big Picture)

Phase 6 **ledger read** aur **daily summary reports** complete karta hai:

- **Ledger read:** Customer/Supplier ka **balance** (SUM(dr − cr)) aur **statement** (entries with opening/closing/running balance) – sab **party_ledger** se, compute on read.
- **Daily report:** **daily_summary** table se ek din ka summary (total_sales, total_purchase, cash_in, cash_out, receivable, payable) aur date range ke liye list + totals.
- **Purchase → daily_summary:** Pehle sirf **sale post** pe daily_summary update hoti thi. Ab **purchase post** pe bhi daily_summary update hoti hai (total_purchase, payable badhte hain).

---

## 2. Database – Flyway V10 Migration

**File:** `src/main/resources/db/migration/V10__phase6_ledger_reports_and_daily_summary_purchase.sql`

**Important:** Purani migrations (V1..V9) **edit mat karna**. Naya change = nayi migration.

### 2.1 daily_summary – Naye Columns

- **total_purchase** – us din ki purchase ka total (POST pe add)
- **cash_out** – supplier ko diya hua paisa (abhi post pe 0; baad mein payment API pe update)
- **payable** – supplier ka udhaar (purchase post pe badhta hai)

Sab NUMERIC(18,2), default 0, non-negative CHECK.

### 2.2 party_ledger – Naya Index

- **idx_party_ledger_shop_party_time** – (shop_id, party_id, created_at, id)  
  Statement query fast karne ke liye (date range + order).

---

## 3. Entity & Repository

### 3.1 DailySummary.java

- Naye fields: **totalPurchase**, **cashOut**, **payable** (column names: total_purchase, cash_out, payable).
- Naya row banate waqt (sale/purchase post) inhe bhi ZERO set kiya jata hai.

### 3.2 DailySummaryRepository

- **findByShop_IdAndDay(shopId, day)** – pehle se tha.
- **findByShop_IdAndDayBetweenOrderByDayAsc(shopId, from, to)** – range report ke liye.

---

## 4. Patch: Purchase Post → daily_summary

**File:** `PurchasePostServiceImpl.java`

- **DailySummaryRepository** inject kiya.
- Purchase **POST** ke andar, ledger entry ke baad aur invoice POSTED mark karne se pehle:
  - **day** = invoice_date (ya aaj agar null ho).
  - Us day ke liye **daily_summary** fetch karo; nahi mili to nayi banao (sab ZERO).
  - **total_purchase** += invoice total  
  - **payable** += invoice total  
  - **cash_out** abhi +0 (supplier payment API baad mein).
- Save daily_summary.

Isse dashboard pe purchase total aur payable dikh sakte hain.

---

## 5. Ledger Read APIs

**Base path:** `/ledger`  
**Controller:** `LedgerController.java`

| Method | Path | Kya karta hai |
|--------|------|----------------|
| GET | `/ledger/parties/{partyId}/balance` | Party ka current balance = SUM(dr_amount − cr_amount) |
| GET | `/ledger/parties/{partyId}/statement?from=&to=&limit=50&offset=0` | Date range mein entries + openingBalance, runningBalance per entry, closingBalance |

- **Balance:** Sirf ek number – party par kitna udhaar/credit hai (DR − CR).
- **Statement:**  
  - **openingBalance** = from se pehle wale entries ka sum (dr − cr).  
  - **entries** = from–to ke beech ki rows (created_at, id order), har entry ke saath **runningBalance**.  
  - **closingBalance** = opening + in entries ka sum.

Response **SuccessResponse&lt;T&gt;** format mein.

---

## 6. Report APIs (daily_summary)

**Base path:** `/report` (ReportsController)

| Method | Path | Kya karta hai |
|--------|------|----------------|
| GET | `/report/daily?date=YYYY-MM-DD` | Us date ka daily_summary (totalSales, totalPurchase, cashIn, cashOut, receivable, payable). Agar row nahi to sab 0. |
| GET | `/report/range?from=YYYY-MM-DD&to=YYYY-MM-DD` | From–to ke andar har din ka summary list + **totals** (totalSalesSum, totalPurchaseSum, cashInSum, …). |

- **daily** – ek din ka snapshot; dashboard “aaj” ke liye use kar sakta hai.
- **range** – kuch din / hafta / mahine ka summary + sum.

Response **SuccessResponse&lt;T&gt;** format mein.

---

## 7. Important Files (Phase 6)

| Type | Path / File |
|------|-------------|
| Migration | `db/migration/V10__phase6_ledger_reports_and_daily_summary_purchase.sql` |
| Entity | `domain/DailySummary.java` (totalPurchase, cashOut, payable) |
| Repository | `DailySummaryRepository.java` (findByShop_IdAndDayBetween...), `PartyLedgerRepository.java` (sumDrMinusCrBefore, date-range page) |
| Patch | `service/impl/PurchasePostServiceImpl.java` (daily_summary upsert on post) |
| Ledger | `controller/LedgerController.java`, `service/ILedgerService.java`, `service/impl/LedgerServiceImpl.java` |
| DTOs | `dto/ledgerdto/LedgerBalanceResponse.java`, `LedgerStatementResponse.java`, `LedgerStatementEntryResponse.java` |
| Report | `controller/ReportsController.java` (/daily, /range), `service/IReportService.java`, `service/impl/ReportServiceImpl.java` |
| Report DTOs | `dto/reportdto/DailyReportResponse.java`, `DailyRangeReportResponse.java` |
| E2E | `scripts/phase6_e2e.py` |

---

## 8. E2E Test (phase6_e2e.py)

Script yeh check karta hai:

1. Register → product, customer party, supplier party.
2. **Sale post** (customer, partial payment) → ledger mein SALE + PAYMENT_IN aani chahiye.
3. **Purchase post** (upload → draft → supplier set → lines resolve → post) → daily_summary.total_purchase badhna chahiye.
4. **GET /ledger/parties/{id}/statement?from=today&to=today** → entries mein SALE aur PAYMENT_IN hona chahiye.
5. **GET /report/daily?date=today** → total_sales &gt; 0 aur total_purchase &gt; 0.

Run:  
`python scripts/phase6_e2e.py`  
(optional: `python scripts/phase6_e2e.py http://localhost:8080`)

---

## 9. Fix: Save Draft Without Lines (Phase 6 E2E ke liye)

Jab **PUT /purchase/drafts/{id}** sirf `version` aur `supplierPartyId` bhejta hai (bina `lines` ke), to pehle **saveDraft** saari lines clear kar deta tha, isliye draft empty ho jata tha aur post pe total = 0 → party_ledger constraint fail.

**Fix:** `PurchaseDraftServiceImpl.saveDraft()` mein lines **sirf tab** clear/replace karo jab **request.getLines() != null**. Agar `lines` body mein nahi bheje to existing lines **rahene do**; sirf supplier/version/invoiceNo wagaira update ho.

---

## 10. Phase 6 Complete – Aage Kya?

Phase 6 goals: Ledger read (balance + statement), daily/range report, purchase → daily_summary.  
Aage **Phase 7** mein: standalone **supplier payment** (cash_out update), dashboard polish, ya aur reports – jaisa product backlog ho.
