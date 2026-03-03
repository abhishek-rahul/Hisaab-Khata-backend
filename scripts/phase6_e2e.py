#!/usr/bin/env python3
"""
Phase 6 E2E: Ledger read + Daily summary reports + Purchase updates daily_summary.
1) Post SALE (customer partial) -> ledger entries exist in party_ledger
2) Post PURCHASE -> daily_summary.total_purchase increases
3) GET /ledger/parties/{partyId}/statement (from=today, to=today) -> SALE and PAYMENT_IN entries
4) GET /report/daily?date=today -> total_sales > 0 and total_purchase > 0
Run with: python scripts/phase6_e2e.py [BASE_URL]
Default BASE_URL: http://localhost:8080
"""
import json
import sys
import uuid
from datetime import date
import urllib.request
import urllib.error

BASE_URL = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080"

SHOP_NAME = "Phase6 Test Shop"
OWNER_NAME = "Phase6 Owner"
OWNER_MOBILE = "9877698706"
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


def upload_req(file_bytes, token, filename="bill.pdf"):
    boundary = uuid.uuid4().hex
    body = b""
    body += f"--{boundary}\r\n".encode()
    body += f'Content-Disposition: form-data; name="file"; filename="{filename}"\r\n'.encode()
    body += b"Content-Type: application/pdf\r\n\r\n"
    body += file_bytes
    body += b"\r\n"
    body += f"--{boundary}--\r\n".encode()
    url = f"{BASE_URL}/purchase/upload"
    headers = {
        "Content-Type": f"multipart/form-data; boundary={boundary}",
        "Content-Length": str(len(body)),
        "Authorization": f"Bearer {token}",
    }
    req_obj = urllib.request.Request(url, data=body, headers=headers, method="POST")
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
    today = date.today().isoformat()
    print("Phase 6 E2E Test (Ledger + Daily Summary Reports)")
    print("=" * 50)

    # 1. Health
    print("\n1. GET /health")
    status, resp = req("GET", "/health")
    assert status == 200, f"Expected 200, got {status}: {resp}"
    print("   OK")

    # 2. Register
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

    # 3. /me -> shopId
    print("\n3. GET /me")
    status, resp = req("GET", "/me", token=token)
    assert status == 200
    shop_id = (resp.get("data") or resp).get("shopId")
    assert shop_id is not None
    print(f"   OK: shopId={shop_id}")

    # 4. Product (manual)
    print("\n4. POST /products/manual")
    status, resp = req("POST", "/products/manual", {
        "canonicalName": "Phase6 Product",
        "normalizedName": "phase6product",
        "baseUnit": "PCS",
        "displayName": "Phase6 Product",
        "displayUnit": "PCS",
        "conversionToBase": 1,
        "sellingPrice": 10,
        "minStockInBase": 0
    }, token=token)
    assert status == 201
    shop_product_id = (resp.get("data") or resp).get("id")
    print(f"   OK: shop_product_id={shop_product_id}")

    # 5. Customer party
    print("\n5. POST /parties (customer)")
    status, resp = req("POST", "/parties", {"name": "Phase6 Customer", "phone": "8888888888", "type": "CUSTOMER"}, token=token)
    assert status == 201
    customer_party_id = (resp.get("data") or resp).get("id")
    print(f"   OK: customer_party_id={customer_party_id}")

    # 6. Supplier party
    print("\n6. POST /parties (supplier)")
    status, resp = req("POST", "/parties", {"name": "Phase6 Supplier", "phone": "7777777777", "type": "SUPPLIER"}, token=token)
    assert status == 201
    supplier_party_id = (resp.get("data") or resp).get("id")
    print(f"   OK: supplier_party_id={supplier_party_id}")

    # 7. Post SALE (customer partial payment)
    print("\n7. POST /sales/draft (customer partial) -> POST /sales/{id}/post")
    status, resp = req("POST", "/sales/draft", {
        "shopId": shop_id,
        "customerPartyId": customer_party_id,
        "discountAmount": 0,
        "cashPaidAmount": 30,
        "upiPaidAmount": 40,
        "items": [{"shopProductId": shop_product_id, "quantityInBase": 10, "unitPrice": 10}]
    }, token=token)
    assert status == 201, f"Expected 201, got {status}: {resp}"
    draft = resp.get("data") or resp
    sale_id = draft.get("id")
    status, resp = req("POST", f"/sales/{sale_id}/post", None, token=token)
    assert status == 200
    print("   OK: sale posted (ledger entries created)")

    # 8. Post PURCHASE (upload -> draft -> save with supplier -> resolve lines -> post)
    print("\n8. POST /purchase/upload")
    status, resp = upload_req(b"%PDF-1.4 phase6 dummy", token)
    assert status == 201
    upload_id = (resp.get("data") or resp).get("uploadId")
    print(f"   OK: uploadId={upload_id}")

    print("   POST /purchase/drafts")
    status, resp = req("POST", "/purchase/drafts", {"uploadId": upload_id}, token=token)
    assert status == 201
    draft_data = resp.get("data") or resp
    draft_id = draft_data.get("draftId")
    version = draft_data.get("version", 0)

    print("   PUT /purchase/drafts/{id} (supplier)")
    status, resp = req("PUT", f"/purchase/drafts/{draft_id}", {"version": version, "supplierPartyId": supplier_party_id}, token=token)
    assert status == 200
    draft_data = resp.get("data") or resp
    version = draft_data.get("version", 0)

    print("   GET /purchase/drafts/{id}/review")
    status, resp = req("GET", f"/purchase/drafts/{draft_id}/review", token=token)
    assert status == 200
    review = resp.get("data") or resp
    lines = review.get("lines") or []
    for line in lines:
        line_id = line.get("lineId")
        if line_id and (line.get("resolutionStatus") == "UNRESOLVED" or line.get("resolutionStatus") == "REVIEW_REQUIRED"):
            status, resp = req("PUT", f"/purchase/drafts/{draft_id}/lines/{line_id}/resolve", {
                "action": "CHOOSE_EXISTING",
                "shopProductId": shop_product_id
            }, token=token)
            assert status == 200

    print("   POST /purchase/drafts/{id}/post")
    status, resp = req("POST", f"/purchase/drafts/{draft_id}/post", None, token=token)
    assert status == 200, f"Expected 200, got {status}: {resp}"
    print("   OK: purchase posted (daily_summary.total_purchase updated)")

    # 9. GET /ledger/parties/{id}/statement?from=today&to=today
    print("\n9. GET /ledger/parties/{id}/statement")
    status, resp = req("GET", f"/ledger/parties/{customer_party_id}/statement?from={today}&to={today}&limit=50&offset=0", token=token)
    assert status == 200, f"Expected 200, got {status}: {resp}"
    payload = resp.get("data") if resp.get("success") else resp
    if payload is None:
        payload = resp
    entries = payload.get("entries") or []
    entry_types = [e.get("entryType") for e in entries]
    assert "SALE" in entry_types, f"Expected SALE in statement entries: {entry_types}"
    assert "PAYMENT_IN" in entry_types, f"Expected PAYMENT_IN in statement entries: {entry_types}"
    print("   OK: statement contains SALE and PAYMENT_IN")

    # 10. GET /report/daily?date=today
    print("\n10. GET /report/daily")
    status, resp = req("GET", f"/report/daily?date={today}", token=token)
    assert status == 200
    payload = resp.get("data") if resp.get("success") else resp
    if payload is None:
        payload = resp
    total_sales = payload.get("totalSales") or 0
    total_purchase = payload.get("totalPurchase") or 0
    assert total_sales > 0, f"Expected total_sales > 0, got {total_sales}"
    assert total_purchase > 0, f"Expected total_purchase > 0, got {total_purchase}"
    print(f"   OK: total_sales={total_sales}, total_purchase={total_purchase}")

    print("\n" + "=" * 50)
    print("All Phase 6 E2E tests PASSED")


if __name__ == "__main__":
    main()
