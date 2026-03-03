#!/usr/bin/env python3
"""
Phase 5 E2E: Sales draft + post.
- Walk-in full payment: success (create draft -> post).
- Customer partial mixed payment: success (draft with customer, cash+upi < total -> post).
- Walk-in partial payment: fail with 400 (paid < total).
Run with: python scripts/phase5_e2e.py [BASE_URL]
Default BASE_URL: http://localhost:8080
"""
import json
import sys
import urllib.request
import urllib.error

BASE_URL = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080"

SHOP_NAME = "Phase5 Test Shop"
OWNER_NAME = "Phase5 Owner"
OWNER_MOBILE = "9871698603"
OWNER_PASSWORD = "pass123"


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
    print("Phase 5 E2E Test (Sales Draft + Post)")
    print("=" * 50)

    # 1. Health
    print("\n1. GET /health")
    status, resp = req("GET", "/health")
    assert status == 200, f"Expected 200, got {status}: {resp}"
    print("   OK")

    # 2. Register shop
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

    # 3. Get shopId from /me
    print("\n3. GET /me")
    status, resp = req("GET", "/me", token=token)
    assert status == 200, f"Expected 200, got {status}: {resp}"
    shop_id = (resp.get("data") or resp).get("shopId")
    assert shop_id is not None, f"Missing shopId: {resp}"
    print(f"   OK: shopId={shop_id}")

    # 4. Create one product (manual) for sale items
    print("\n4. POST /products/manual")
    status, resp = req("POST", "/products/manual", {
        "canonicalName": "Phase5 Product",
        "normalizedName": "phase5product",
        "baseUnit": "PCS",
        "displayName": "Phase5 Product",
        "displayUnit": "PCS",
        "conversionToBase": 1,
        "sellingPrice": 10,
        "minStockInBase": 0
    }, token=token)
    assert status == 201, f"Expected 201, got {status}: {resp}"
    prod = resp.get("data") or resp
    shop_product_id = prod.get("id")
    assert shop_product_id, f"Missing product id: {prod}"
    print(f"   OK: shop_product_id={shop_product_id}")

    # 5. Create customer party (for partial mixed test)
    print("\n5. POST /parties (customer)")
    status, resp = req("POST", "/parties", {
        "name": "Phase5 Customer",
        "phone": "9999999999",
        "type": "CUSTOMER"
    }, token=token)
    assert status == 201, f"Expected 201, got {status}: {resp}"
    party_data = resp.get("data") or resp
    customer_party_id = party_data.get("id")
    assert customer_party_id, f"Missing party id: {party_data}"
    print(f"   OK: customer_party_id={customer_party_id}")

    # 6. Walk-in full payment: draft then post
    print("\n6. POST /sales/draft (walk-in, full payment)")
    gross = 100.0  # 10 * 10
    draft_body = {
        "shopId": shop_id,
        "customerPartyId": None,
        "discountAmount": 0,
        "cashPaidAmount": 100,
        "upiPaidAmount": 0,
        "items": [
            {"shopProductId": shop_product_id, "quantityInBase": 10, "unitPrice": 10}
        ]
    }
    status, resp = req("POST", "/sales/draft", draft_body, token=token)
    assert status == 201, f"Expected 201, got {status}: {resp}"
    assert resp.get("success") is True, f"Expected success=True: {resp}"
    draft = resp.get("data")
    assert draft, f"Missing data: {resp}"
    assert draft.get("status") == "DRAFT"
    assert draft.get("totalAmount") == gross
    assert draft.get("paidAmount") == 100
    draft_id = draft["id"]
    print(f"   OK: draft id={draft_id}")

    print("   POST /sales/{id}/post (walk-in full)")
    status, resp = req("POST", f"/sales/{draft_id}/post", None, token=token)
    assert status == 200, f"Expected 200, got {status}: {resp}"
    assert resp.get("success") is True
    posted = resp.get("data")
    assert posted and posted.get("status") == "POSTED"
    print("   OK: posted")

    # 7. Customer partial mixed payment: draft then post
    print("\n7. POST /sales/draft (customer, partial mixed)")
    draft_body2 = {
        "shopId": shop_id,
        "customerPartyId": customer_party_id,
        "discountAmount": 0,
        "cashPaidAmount": 30,
        "upiPaidAmount": 40,
        "items": [
            {"shopProductId": shop_product_id, "quantityInBase": 10, "unitPrice": 10}
        ]
    }
    status, resp = req("POST", "/sales/draft", draft_body2, token=token)
    assert status == 201, f"Expected 201, got {status}: {resp}"
    assert resp.get("success") is True
    draft2 = resp.get("data")
    assert draft2.get("totalAmount") == 100
    assert draft2.get("paidAmount") == 70
    draft_id2 = draft2["id"]
    print(f"   OK: draft id={draft_id2} (partial 70)")

    print("   POST /sales/{id}/post (customer partial)")
    status, resp = req("POST", f"/sales/{draft_id2}/post", None, token=token)
    assert status == 200, f"Expected 200, got {status}: {resp}"
    assert resp.get("data", {}).get("status") == "POSTED"
    print("   OK: posted")

    # 8. Walk-in partial: must fail with 400 (paid < total not allowed for walk-in)
    print("\n8. POST /sales/draft (walk-in partial) -> expect 400")
    draft_body3 = {
        "shopId": shop_id,
        "customerPartyId": None,
        "discountAmount": 0,
        "cashPaidAmount": 30,
        "upiPaidAmount": 0,
        "items": [
            {"shopProductId": shop_product_id, "quantityInBase": 5, "unitPrice": 10}
        ]
    }
    status, resp = req("POST", "/sales/draft", draft_body3, token=token)
    assert status == 400, f"Expected 400 for walk-in partial, got {status}: {resp}"
    assert resp.get("success") is False
    assert "errorCode" in resp or "message" in resp
    print("   OK: walk-in partial rejected (400)")

    print("\n" + "=" * 50)
    print("All Phase 5 E2E tests PASSED")


if __name__ == "__main__":
    main()
