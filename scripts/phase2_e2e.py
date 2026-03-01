#!/usr/bin/env python3
"""
Phase 2 E2E test: Health -> Register shop -> JWT -> Create customer/supplier parties ->
List by type -> Get ledger (empty) -> Duplicate phone fails with PARTY_PHONE_DUPLICATE.
Run with: python scripts/phase2_e2e.py [BASE_URL]
Default BASE_URL: http://localhost:8080
"""
import json
import sys
import urllib.request
import urllib.error

BASE_URL = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080"

# Config / Test Data
SHOP_NAME = "Phase2 Test Shop"
OWNER_NAME = "Phase2 Owner"
OWNER_MOBILE = "9876543401"
OWNER_PASSWORD = "pass123"

CUSTOMER_NAME = "Test Customer"
CUSTOMER_PHONE = "9876543402"

SUPPLIER_NAME = "Test Supplier"
SUPPLIER_PHONE = "9876543403"


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
    print("Phase 2 E2E Test")
    print("=" * 50)

    # 1. Health (actuator or /health)
    print("\n1. GET /health")
    status, resp = req("GET", "/health")
    assert status == 200, f"Expected 200, got {status}: {resp}"
    # ApiResponse or simple map
    if resp.get("data"):
        assert resp.get("data", {}).get("status") == "UP" or resp.get("success") is True
    print("   OK")

    # 2. Register shop (Phase 1 API) -> get JWT token
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
    print(f"   OK: token obtained")

    # 3. Create customer party (unique phone)
    print("\n3. POST /parties (customer)")
    status, resp = req("POST", "/parties", {
        "name": CUSTOMER_NAME,
        "phone": CUSTOMER_PHONE,
        "type": "CUSTOMER"
    }, token=token)
    assert status == 201, f"Expected 201, got {status}: {resp}"
    assert resp.get("success") is True
    party_data = resp.get("data") or resp
    customer_id = party_data.get("id")
    assert customer_id, f"Missing party id: {party_data}"
    assert party_data.get("type") == "CUSTOMER"
    assert party_data.get("phone") == CUSTOMER_PHONE
    print(f"   OK: customer partyId={customer_id}")

    # 4. Create supplier party (unique phone)
    print("\n4. POST /parties (supplier)")
    status, resp = req("POST", "/parties", {
        "name": SUPPLIER_NAME,
        "phone": SUPPLIER_PHONE,
        "type": "SUPPLIER"
    }, token=token)
    assert status == 201, f"Expected 201, got {status}: {resp}"
    assert resp.get("success") is True
    supplier_data = resp.get("data") or resp
    supplier_id = supplier_data.get("id")
    assert supplier_id, f"Missing party id: {supplier_data}"
    assert supplier_data.get("type") == "SUPPLIER"
    print(f"   OK: supplier partyId={supplier_id}")

    # 5. List customers by type
    print("\n5. GET /parties?type=CUSTOMER")
    status, resp = req("GET", "/parties?type=CUSTOMER", token=token)
    assert status == 200, f"Expected 200, got {status}: {resp}"
    assert resp.get("success") is True
    list_data = resp.get("data")
    assert isinstance(list_data, list), f"Expected list: {list_data}"
    assert any(p.get("id") == customer_id and p.get("type") == "CUSTOMER" for p in list_data)
    print(f"   OK: list length={len(list_data)}")

    # 6. Get customer ledger -> entries=[], balance=0
    print("\n6. GET /parties/{partyId}/ledger")
    status, resp = req("GET", f"/parties/{customer_id}/ledger", token=token)
    assert status == 200, f"Expected 200, got {status}: {resp}"
    assert resp.get("success") is True
    ledger = resp.get("data") or resp
    assert ledger.get("partyId") == customer_id, f"Expected partyId={customer_id}: {ledger}"
    assert ledger.get("entries") == [], f"Expected entries=[], got {ledger.get('entries')}"
    assert ledger.get("balance") in (0, 0.0), f"Expected balance=0: {ledger.get('balance')}"
    print("   OK: entries=[], balance=0")

    # 7. Duplicate phone in same shop -> fail (400/409) with PARTY_PHONE_DUPLICATE
    print("\n7. POST /parties (duplicate phone) -> expect 400/409")
    status, resp = req("POST", "/parties", {
        "name": "Another Customer",
        "phone": CUSTOMER_PHONE,
        "type": "CUSTOMER"
    }, token=token)
    assert status in (400, 409), f"Expected 400 or 409, got {status}: {resp}"
    assert resp.get("success") is False
    assert resp.get("errorCode") == "PARTY_PHONE_DUPLICATE", f"Expected errorCode PARTY_PHONE_DUPLICATE: {resp}"
    print("   OK: duplicate phone rejected with PARTY_PHONE_DUPLICATE")

    print("\n" + "=" * 50)
    print("All Phase 2 E2E tests PASSED")


if __name__ == "__main__":
    main()
