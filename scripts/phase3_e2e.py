#!/usr/bin/env python3
"""
Phase 3 E2E test: Health -> Register shop -> Create Category (SHOP) -> Create Manual Product ->
List Products (stock qty=0) -> Update Shop Product -> List Stock.
Run with: python scripts/phase3_e2e.py [BASE_URL]
Default BASE_URL: http://localhost:8080
"""
import json
import sys
import urllib.request
import urllib.error

BASE_URL = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080"

SHOP_NAME = "Phase3 Test Shop"
OWNER_NAME = "Phase3 Owner"
OWNER_MOBILE = "9876598501"
OWNER_PASSWORD = "pass123"

CATEGORY_NAME = "Phase3 Category"
CANONICAL_NAME = "Test Rice"
NORMALIZED_NAME = "test rice"
BASE_UNIT = "GRAM"
DISPLAY_NAME = "Rice 1kg"
DISPLAY_UNIT = "KG"
CONVERSION_TO_BASE = 1000
SELLING_PRICE = 55.0
MIN_STOCK = 10.0


def req(method, path, body=None, token=None):
    url = f"{BASE_URL}{path}"
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    data = json.dumps(body).encode() if body else None
    req_obj = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req_obj) as r:
            return r.status, json.loads(r.read().decode())
    except urllib.error.HTTPError as e:
        raw_body = e.read().decode()
        try:
            return e.code, json.loads(raw_body)
        except json.JSONDecodeError:
            return e.code, {"raw": raw_body}


def main():
    print("Phase 3 E2E Test")
    print("=" * 50)

    # 1. Health
    print("\n1. GET /health")
    status, resp = req("GET", "/health")
    assert status == 200, f"Expected 200, got {status}: {resp}"
    if resp.get("data"):
        assert resp.get("data", {}).get("status") == "UP" or resp.get("success") is True
    print("   OK")

    # 2. Register shop (Phase 1)
    print("\n2. POST /auth/register")
    status, resp = req("POST", "/auth/register", {
        "shopName": SHOP_NAME,
        "ownerName": OWNER_NAME,
        "mobile": OWNER_MOBILE,
        "password": OWNER_PASSWORD
    })
    assert status == 201, f"Expected 201, got {status}: {resp}"
    data = resp.get("data") or resp
    token = data.get("token")
    assert token, f"Missing token: {data}"
    print("   OK: token obtained")

    # 3. Create Category (SHOP)
    print("\n3. POST /categories")
    status, resp = req("POST", "/categories", {"name": CATEGORY_NAME}, token=token)
    assert status == 201, f"Expected 201, got {status}: {resp}"
    assert resp.get("success") is True
    cat_data = resp.get("data") or resp
    category_id = cat_data.get("id")
    assert category_id, f"Missing category id: {cat_data}"
    assert cat_data.get("scope") == "SHOP" or cat_data.get("name") == CATEGORY_NAME
    print(f"   OK: categoryId={category_id}")

    # 4. GET /categories?scope=SHOP
    print("\n4. GET /categories?scope=SHOP")
    status, resp = req("GET", "/categories?scope=SHOP", token=token)
    assert status == 200, f"Expected 200, got {status}: {resp}"
    assert resp.get("success") is True
    cats = resp.get("data")
    assert isinstance(cats, list), f"Expected list: {cats}"
    assert any(c.get("id") == category_id for c in cats)
    print(f"   OK: list length={len(cats)}")

    # 5. POST /products/manual
    print("\n5. POST /products/manual")
    status, resp = req("POST", "/products/manual", {
        "canonicalName": CANONICAL_NAME,
        "normalizedName": NORMALIZED_NAME,
        "baseUnit": BASE_UNIT,
        "categoryId": category_id,
        "displayName": DISPLAY_NAME,
        "displayUnit": DISPLAY_UNIT,
        "conversionToBase": CONVERSION_TO_BASE,
        "sellingPrice": None,
        "minStockInBase": None
    }, token=token)
    assert status == 201, f"Expected 201, got {status}: {resp}"
    assert resp.get("success") is True
    prod_data = resp.get("data") or resp
    shop_product_id = prod_data.get("id")
    assert shop_product_id, f"Missing shop product id: {prod_data}"
    assert prod_data.get("stockQty") in (0, 0.0), f"Expected stockQty=0: {prod_data.get('stockQty')}"
    print(f"   OK: shopProductId={shop_product_id}, stockQty=0")

    # 6. GET /products
    print("\n6. GET /products")
    status, resp = req("GET", "/products", token=token)
    assert status == 200, f"Expected 200, got {status}: {resp}"
    assert resp.get("success") is True
    products = resp.get("data")
    assert isinstance(products, list), f"Expected list: {products}"
    found = next((p for p in products if p.get("id") == shop_product_id), None)
    assert found, f"Shop product {shop_product_id} not in list: {products}"
    assert found.get("stockQty") in (0, 0.0), f"Expected stockQty=0: {found.get('stockQty')}"
    print(f"   OK: list length={len(products)}, product has stockQty=0")

    # 7. PATCH /products/{shopProductId}
    print("\n7. PATCH /products/{shopProductId}")
    status, resp = req("PATCH", f"/products/{shop_product_id}", {
        "sellingPrice": SELLING_PRICE,
        "minStockInBase": MIN_STOCK,
        "isActive": True
    }, token=token)
    assert status == 200, f"Expected 200, got {status}: {resp}"
    assert resp.get("success") is True
    patched = resp.get("data") or resp
    assert patched.get("sellingPrice") == SELLING_PRICE or abs((patched.get("sellingPrice") or 0) - SELLING_PRICE) < 0.01
    assert patched.get("minStockInBase") == MIN_STOCK or abs((patched.get("minStockInBase") or 0) - MIN_STOCK) < 0.01
    print("   OK: sellingPrice and minStockInBase updated")

    # 8. GET /stock
    print("\n8. GET /stock")
    status, resp = req("GET", "/stock", token=token)
    assert status == 200, f"Expected 200, got {status}: {resp}"
    assert resp.get("success") is True
    stock_list = resp.get("data")
    assert isinstance(stock_list, list), f"Expected list: {stock_list}"
    stock_row = next((s for s in stock_list if s.get("shopProductId") == shop_product_id), None)
    assert stock_row, f"Stock row for shopProductId={shop_product_id} not found: {stock_list}"
    assert "quantity" in stock_row or "displayName" in stock_row
    print(f"   OK: stock list length={len(stock_list)}, same product row present")

    print("\n" + "=" * 50)
    print("All Phase 3 E2E tests PASSED")


if __name__ == "__main__":
    main()
