# Phase 3 – Category + Master Product + Shop Product + Stock (Summary)

Yeh document Phase 3 mein jo kuch implement kiya gaya hai, use **simple aur easy terms** mein detail ke saath samjhata hai.

---

## 1. Phase 3 Kya Hai? (Big Picture)

Phase 3 **products aur stock** ki foundation dalta hai – bina purchase/sale/bill-upload ke. Matlab:

- **Category:** Dukan ke products ko group karne ke liye (jaise "Groceries", "Snacks"). Abhi sirf **SHOP** scope – matlab har shop apni categories khud banata hai.
- **Master Product:** Product ki **global identity** – jaise "Rice" ka ek normalised naam (canonical + normalized) aur base unit (PCS, GRAM, ML). Yeh table shop-specific nahi; baad mein bill upload / suggestions ke liye use hogi.
- **Shop Product:** **Dukan ke hisaab se** wohi product – display name, display unit (KG, Ltr), conversion (1 KG = 1000 GRAM), selling price, min stock, active/inactive. Har shop apna shop_product banaata hai; ek master_product pe multiple shops ke shop_products ho sakte hain.
- **Stock:** Har **shop_product** ka **ek row** – quantity **base unit** mein (negative allowed). Koi alag inventory_txn table nahi; sirf snapshot.

**Phase 3 mein:**  
Sirf category create, manual product add (master + shop_product + stock qty=0), product list, product update (price/min stock/active), aur stock list. Purchase / sale / bill upload **nahi** hai.

---

## 2. Database – Flyway V3 Migration

**File:** `src/main/resources/db/migration/V3__category_master_product_shop_product_stock.sql`

**Important:** Is file ko migrate hone ke baad **edit mat karna**. Koi change chahiye to nayi migration (jaise V4) banao.

### 2.1 Enums (DB Level)

- **category_scope:** `GLOBAL`, `SHOP`  
  - GLOBAL = sab shops ke liye common (abhi use nahi).  
  - SHOP = sirf us shop ki category.

- **base_unit:** `PCS`, `GRAM`, `ML`  
  - Stock hamesha isi unit mein store hota hai (e.g. Rice = GRAM, Cola = ML, Biscuit = PCS).

### 2.2 Table: `category`

- **id** – primary key  
- **shop_id** – FK → shop (SHOP scope ke liye required; GLOBAL ke liye null)  
- **scope** – category_scope enum (Phase 3 mein hum sirf SHOP use karte hain)  
- **name** – category ka naam (e.g. "Groceries")  
- **created_at**, **updated_at**  

**Rules:**

- SHOP scope = shop_id **zaroor** hona chahiye.  
- GLOBAL scope = shop_id **null**.  
- Ek shop ke andar **same name ki do SHOP categories nahi** ho sakti – iske liye unique index: `(shop_id, name)` **WHERE scope = 'SHOP'**.

### 2.3 Table: `master_product`

Yeh table **shop-specific nahi** – global product identity ke liye.

- **id** – primary key  
- **canonical_name** – human-readable naam (e.g. "Basmati Rice")  
- **normalized_name** – lowercase/trimmed, matching ke liye (e.g. "basmati rice")  
- **base_unit** – PCS / GRAM / ML  
- **created_at**, **updated_at**  

Index: `normalized_name` – taaki same product dubara na bane, lookup fast ho.

### 2.4 Table: `shop_product`

Har shop apne products yahan define karta hai; link master_product se.

- **id** – primary key  
- **shop_id** – FK → shop (zaroori)  
- **master_product_id** – FK → master_product (zaroori)  
- **category_id** – FK → category (optional; null ho sakta hai)  
- **display_name** – dukaan pe dikhne wala naam (e.g. "Rice 1kg Pack")  
- **display_unit** – jis unit mein bechte hain (e.g. "KG", "PCS")  
- **conversion_to_base** – 1 display unit = kitne base unit (e.g. 1 KG = 1000 GRAM → 1000)  
- **selling_price** – selling price (null allowed)  
- **min_stock_in_base** – minimum stock alert ke liye (default 0)  
- **is_active** – product ab bik raha hai ya nahi (default true)  
- **created_at**, **updated_at**  

**Constraint:** `conversion_to_base` **positive** hona chahiye ( > 0 ).

### 2.5 Table: `stock`

Har **shop_product** ka **sirf ek** stock row – quantity base unit mein.

- **id** – primary key  
- **shop_product_id** – FK → shop_product, **UNIQUE** (ek product = ek stock row)  
- **quantity** – numeric; **negative allowed** (out-of-stock / credit sale ke liye)  
- **created_at**, **updated_at**  

**Koi inventory_txn table nahi** – Phase 3 mein sirf yeh snapshot table hai.

---

## 3. Java – Enums aur Entities

### 3.1 Enums (Java)

- **CategoryScope:** GLOBAL, SHOP  
- **BaseUnit:** PCS, GRAM, ML  

DB enum se map karne ke liye **@JdbcTypeCode(SqlTypes.NAMED_ENUM)** use hota hai (PostgreSQL enum type ke saath).

### 3.2 Entity: `Category`

- Table: `category`  
- Fields: id, **shop** (ManyToOne, optional – GLOBAL ke liye null), **scope** (CategoryScope), **name**  
- BaseEntity se created_at, updated_at aate hain.

### 3.3 Entity: `MasterProduct`

- Table: `master_product`  
- Fields: id, **canonicalName**, **normalizedName**, **baseUnit** (BaseUnit enum)

### 3.4 Entity: `ShopProduct`

- Table: `shop_product`  
- Fields: id, **shop**, **masterProduct**, **category** (optional), **displayName**, **displayUnit**, **conversionToBase** (BigDecimal), **sellingPrice**, **minStockInBase**, **isActive**

### 3.5 Entity: `Stock`

- Table: `stock`  
- Fields: id, **shopProduct** (OneToOne), **quantity** (BigDecimal, base unit; negative allowed)  
- Phase 3 se pehle yeh **Product** se linked tha; ab **ShopProduct** se linked hai.

### 3.6 Repositories

- **CategoryRepository:**  
  - `findByShop_IdAndScope(shopId, scope)` – categories list  
  - `findByShop_Id(shopId)` – saari categories us shop ki (backward compat)  
  - `existsByShop_IdAndScopeAndName(...)` – duplicate name check  

- **MasterProductRepository:**  
  - `findByNormalizedNameIgnoreCase(name)` – same product dhoondhne ke liye  

- **ShopProductRepository:**  
  - `findByShop_Id(shopId)` – shop ke saare products  
  - `findByShop_IdAndId(shopId, id)` – ek product (shop-scoped)  

- **StockRepository:**  
  - `findByShopProduct_Id(shopProductId)` – us product ka stock  
  - `findByShopProduct_Shop_Id(shopId)` – shop ke saare stock rows  

---

## 4. DTOs (Data Transfer Objects)

### 4.1 Category

- **CategoryCreateRequest:**  
  - **name** – required, `@NotBlank`  

- **CategoryResponse:**  
  - id, name, **scope** (String: "GLOBAL" / "SHOP")  

### 4.2 Manual Product (Phase 3)

- **CreateManualProductRequest:**  
  - **canonicalName** – required  
  - **normalizedName** – required  
  - **baseUnit** – required (PCS/GRAM/ML)  
  - **categoryId** – optional  
  - **displayName** – required  
  - **displayUnit** – required  
  - **conversionToBase** – required, positive (e.g. 1000 for 1 KG = 1000 GRAM)  
  - **sellingPrice** – optional  
  - **minStockInBase** – optional  

- **ShopProductResponse:**  
  - id, masterProductId, canonicalName, normalizedName, baseUnit  
  - categoryId, categoryName  
  - displayName, displayUnit, conversionToBase, sellingPrice, minStockInBase, isActive  
  - **stockQty** – us product ka current stock (base unit mein)  

- **PatchShopProductRequest:**  
  - **sellingPrice** – optional update  
  - **minStockInBase** – optional, >= 0  
  - **isActive** – optional (true/false)  

### 4.3 Stock

- **StockResponse:**  
  - **shopProductId** – kaun sa product  
  - **displayName** – product ka display naam  
  - **baseUnit** – PCS/GRAM/ML  
  - **quantity** – base unit mein (negative ho sakta hai)  

---

## 5. Services – Kya Kya Karta Hai

### 5.1 CategoryService

- **createCategory(request):**  
  - Current shop (JWT se) ke under nayi **SHOP** category banata hai.  
  - Name trim karke check; blank to **BusinessValidationException** (CATEGORY_NAME_REQUIRED).  
  - Same shop + SHOP + same name pehle se hai to **ConflictException** (CATEGORY_NAME_DUPLICATE).  

- **getAllCategories(scope):**  
  - Current shop ki categories; **scope** filter (null = default SHOP).  
  - Returns: list of CategoryResponse.  

### 5.2 ShopProductService (Phase 3 Products)

- **createManual(request):**  
  1. **Master product:** normalized_name se dhoondo; nahi mila to naya master_product banao (canonical_name, normalized_name, base_unit).  
  2. **Shop product:** current shop + master_product + category (agar categoryId diya) + display_name, display_unit, conversion_to_base, selling_price, min_stock_in_base, is_active = true.  
  3. **Stock:** us shop_product ke liye ek row, **quantity = 0**.  
  - Response: ShopProductResponse with stockQty = 0.  

- **listProducts():**  
  - Current shop ke saare shop_products; har ke saath stock qty (base unit) bhi.  
  - Returns: List&lt;ShopProductResponse&gt;.  

- **patchProduct(shopProductId, request):**  
  - Sirf **current shop** ka woh product update karo (sellingPrice, minStockInBase, isActive – jo diya gaya ho).  
  - Nahi mila to **ResourceNotFoundException** (SHOP_PRODUCT_NOT_FOUND).  
  - Returns: updated ShopProductResponse with current stockQty.  

### 5.3 StockService

- **getAllStock():**  
  - Current shop ke saare stock rows (shop_product ke through).  
  - Returns: List&lt;StockResponse&gt; (shopProductId, displayName, baseUnit, quantity).  

- **getStockByShopProductId(shopProductId):**  
  - Us shop_product ka stock (sirf current shop).  
  - Nahi mila to **ResourceNotFoundException**.  

- **increaseStock(shopProductId, qtyInBase):**  
  - Us shop_product ka stock **badhao** (quantity + qtyInBase).  
  - Stock row nahi hai to nayi banao (qty = 0 + qtyInBase).  
  - (Purchase wale phase mein use hoga.)  

- **decreaseStock(shopProductId, qtyInBase):**  
  - Stock **ghatao** (quantity - qtyInBase).  
  - Negative ja sakta hai (allowed).  
  - (Sale wale phase mein use hoga.)  

**Legacy (Phase 3 ke baad ke code ke liye):**  
- **increaseStockLegacy(productId, qty, shopId)** aur **decreaseStockLegacy(productId, qty)** – yeh **throw** karte hain (UnsupportedOperationException), kyunki ab stock **shop_product_id** se chal raha hai.  

---

## 6. Controllers – APIs (Simple Terms)

Sab jagah **ApiResponse** wrapper – success, message, data; error pe success=false, errorCode, message (Phase 1/2 jaisa).

### 6.1 Category APIs

**Base path:** `/categories` (JWT zaroori)

| Method | Path | Kya karta hai |
|--------|------|----------------|
| **POST** | /categories | Nayi **SHOP** category banata hai (body: `{ "name": "Groceries" }`). Same shop mein same name nahi ho sakta. |
| **GET** | /categories?scope=SHOP | Current shop ki categories list (scope = SHOP ya optional; default SHOP). |

### 6.2 Product APIs (Phase 3 – Shop Product)

**Base path:** `/products` (JWT zaroori)

| Method | Path | Kya karta hai |
|--------|------|----------------|
| **POST** | /products/manual | **Manual product** add: agar normalized name se master_product mil gaya to use karo, nahi to naya master_product banao; phir shop_product banao; phir stock row (qty=0). Body mein canonicalName, normalizedName, baseUnit, categoryId (optional), displayName, displayUnit, conversionToBase, sellingPrice, minStockInBase. |
| **GET** | /products | Current shop ke **saare products** list (master + category + **stock qty** ke saath). |
| **PATCH** | /products/{shopProductId} | Us product ko **update** – sellingPrice, minStockInBase, isActive (jo bhejo, wohi update). |

### 6.3 Stock API

**Base path:** `/stock` (JWT zaroori)

| Method | Path | Kya karta hai |
|--------|------|----------------|
| **GET** | /stock | Current shop ka **saara stock** – har row: shopProductId, displayName, baseUnit, quantity. |
| **GET** | /stock/{shopProductId} | Sirf us **ek product** ka stock. |

---

## 7. Validation (Short Summary)

- **Category:** name blank nahi; duplicate name same shop + SHOP scope = CONFLICT (CATEGORY_NAME_DUPLICATE).  
- **Manual product:** canonicalName, normalizedName, baseUnit required; displayName, displayUnit, conversionToBase required; conversionToBase positive.  
- **Patch product:** sellingPrice, minStockInBase, isActive optional; minStockInBase agar diya to >= 0.  
- **Multi-tenant:** Sab read/write **current shop_id** se – JWT se shop resolve hota hai (ShopContext).  

---

## 8. E2E Script – Phase 3 Test

**File:** `scripts/phase3_e2e.py`

**Flow (step-by-step):**

1. **GET /health** – Server up hai ya nahi.  
2. **POST /auth/register** – Naya shop register karke **JWT token** lena (Phase 1).  
3. **POST /categories** – Ek **SHOP** category create (e.g. "Phase3 Category").  
4. **GET /categories?scope=SHOP** – Categories list; jo abhi banayi woh dikhni chahiye.  
5. **POST /products/manual** – Ek manual product (e.g. Rice – canonicalName, normalizedName, base_unit GRAM, displayUnit KG, conversionToBase 1000).  
6. **GET /products** – Products list; is product ka **stockQty = 0** hona chahiye.  
7. **PATCH /products/{shopProductId}** – Us product ka sellingPrice, minStockInBase, isActive update.  
8. **GET /stock** – Stock list; usi product ka row dikhna chahiye (quantity 0).  

**Run:**  
`python scripts/phase3_e2e.py`  
ya  
`python scripts/phase3_e2e.py http://localhost:8080`  

Agar sab steps pass ho jayein to **"All Phase 3 E2E tests PASSED"** dikhega.

---

## 9. Important Points (Yaad rakhne wale)

- **Phase 3 mein purchase / sale / bill upload implement nahi hai.** Sirf category, manual product, product list, product update, aur stock list.  
- **Stock negative ho sakta hai** – design mein allowed hai (out-of-stock / credit sale ke liye).  
- **base_unit** sirf teen: PCS, GRAM, ML. Stock hamesha base unit mein.  
- **inventory_txn** table **nahi** hai – na create karo, na reference karo.  
- Response format **Phase 1/2 jaisa** – ApiResponse (success, statusCode, message, data; error pe errorCode, errors).  
- Schema **sirf Flyway** se – Hibernate ddl-auto **disabled** rehna chahiye.  
- V3 migration file **apply hone ke baad edit mat karo**; koi change = nayi migration (e.g. V4).  
- Branch: **develop_feature_phase3**.  

---

## 10. Files – Quick Reference (Kahan kya hai)

| Category | Files / Path |
|----------|----------------|
| **Migration** | `src/main/resources/db/migration/V3__category_master_product_shop_product_stock.sql` |
| **Optional migration** | `V4__purchase_sale_shop_product_id.sql` – sirf agar `purchase` / `sale_item` tables pehle se hon (shop_product_id column add) |
| **Enums** | `enums/CategoryScope.java`, `enums/BaseUnit.java` |
| **Entities** | `domain/Category.java` (scope add), `domain/MasterProduct.java`, `domain/ShopProduct.java`, `domain/Stock.java` (ShopProduct se link) |
| **Repositories** | `repository/CategoryRepository.java`, `MasterProductRepository.java`, `ShopProductRepository.java`, `StockRepository.java` |
| **DTOs** | `dto/productdto/CategoryCreateRequest.java`, `CategoryResponse.java`, `CreateManualProductRequest.java`, `ShopProductResponse.java`, `PatchShopProductRequest.java`; `dto/stockdto/StockResponse.java` |
| **Mappers** | `mapper/CategoryMapper.java` (scope map), `mapper/StockMapper.java` (shopProduct se map) |
| **Services** | `service/ICategoryService.java`, `service/impl/CategoryServiceImpl.java`; `IShopProductService.java`, `ShopProductServiceImpl.java`; `IStockService.java`, `StockServiceImpl.java` |
| **Controllers** | `controller/CategoryController.java` (/categories), `controller/ProductsController.java` (/products), `controller/StockController.java` (/stock) |
| **E2E** | `scripts/phase3_e2e.py` |

---

## 11. Flow Diagram (Simple)

```
[Shopkeeper] --> POST /categories --> [Category] (SHOP, name)
[Shopkeeper] --> POST /products/manual --> [MasterProduct (find/create)] + [ShopProduct] + [Stock qty=0]
[Shopkeeper] --> GET /products --> List of ShopProduct + stockQty
[Shopkeeper] --> PATCH /products/{id} --> Update sellingPrice / minStockInBase / isActive
[Shopkeeper] --> GET /stock --> List of (shopProductId, displayName, baseUnit, quantity)
```

Sab operations **current shop** ke andar hi – JWT se shop decide hota hai.

---

Yeh document Phase 3 implementation ko simple aur detailed dono tarah se cover karta hai. Code ya migration ki exact detail ke liye respective files dekho.
