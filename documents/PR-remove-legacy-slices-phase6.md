# PR: Remove legacy API slices (customer/supplier/product/sale) after Phase 6

**Branch:** `develop_feature_modify_till_phase6_deletion` → `develop_feature_modify_till_phase6`

## A) What was removed

### Controllers (deleted)
- **CustomerController** (`/customer`) – duplicate of `/parties` (type=CUSTOMER)
- **SupplierController** (`/supplier`) – duplicate of `/parties` (type=SUPPLIER)
- **SupplierLedgerController** (`/supplier/{id}/ledger`) – supplier-specific ledger (new flow uses party_ledger)
- **ProductController** (`/product`) – duplicate of `/products` (shop_product)
- **SaleController** (`/sale`) – duplicate of `/sales` (draft/post)

### Services (deleted)
- ICustomerService, CustomerServiceImpl
- ISupplierService, SupplierServiceImpl
- IProductService, ProductServiceImpl
- ISaleService, SaleServiceImpl

### DTOs (deleted)
- **customerdto:** CustomerCreateRequest, CustomerResponse, CustomerLedgerResponse, CustomerLedgerEntryResponse
- **supplierdto:** SupplierCreateRequest, SupplierResponse (kept SupplierLedgerResponse/Entry for SupplierLedgerServiceImpl)
- **productdto (legacy):** ProductCreateRequest, ProductUpdateRequest, ProductResponse
- **saledto:** SaleCreateRequest, SaleResponse, SaleItemRequest, SaleItemResponse

### Mappers (deleted)
- CustomerMapper, CustomerLedgerMapper
- SupplierMapper
- ProductMapper
- SaleMapper, SaleItemMapper

### Test (deleted/updated)
- RemoveSaleMappersPostProcessor.java (deleted)
- HisaabKhataApplicationTests: removed SaleMapper mock and RemoveSaleMappersPostProcessor import

### Kept (used by KEEP flows)
- **Entities:** Customer, CustomerLedger, Supplier, SupplierLedger, Product, Sale, SaleItem (used by ReportServiceImpl, DashboardServiceImpl, DailyClosingServiceImpl, PurchaseServiceImpl, SearchServiceImpl)
- **Repositories:** CustomerRepository, CustomerLedgerRepository, SupplierRepository, SupplierLedgerRepository, ProductRepository, SaleRepository
- **ISupplierLedgerService, SupplierLedgerServiceImpl** (used by PurchaseServiceImpl for legacy createPurchase)
- **supplierdto:** SupplierLedgerResponse, SupplierLedgerEntryResponse, SupplierLedgerMapper

## B) Why removed

- Duplicate endpoints that are not part of the Phase 6 new API surface (defined by `scripts/regression_core_phase6.py`).
- Keeping old slices causes confusion and blocks maintenance.

## C) Behavioural change

- **KhataController** (`GET /khata/pending`): now uses `IReportService.getPendingKhataSummary()` instead of `ICustomerService.getPendingKhataSummary()`. Logic moved into ReportServiceImpl (same data: customerRepo + ledgerRepo).
- **ReportServiceImpl:** added `getPendingKhataSummary()`; removed unused `ProductRepository` injection.

## D) How to test

1. `mvn -DskipTests=false test` (or `mvn test`)
2. Start the app (Spring context must load).
3. `python scripts/regression_core_phase6.py http://localhost:8080` – must PASS.

## E) Endpoints removed (for reference)

| Method | Path | Controller (deleted) |
|--------|------|----------------------|
| GET/POST | /customer, /customer/{id}, /customer/{id}/ledger | CustomerController |
| GET/POST | /supplier, /supplier/{id}, /supplier/{id}/ledger | SupplierController, SupplierLedgerController |
| GET/POST/PUT/DELETE | /product, /product/{id} | ProductController |
| GET/POST | /sale, /sale/{id} | SaleController |

## F) Non-goals / explicitly NOT done

- No Flyway or DB schema changes.
- No DB table deletion.
- Legacy entities/repos (Customer, Sale, Product, etc.) retained where still used by reports/dashboard/legacy purchase flow.
