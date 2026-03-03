#!/usr/bin/env python3
"""
Phase 4B E2E test: Register -> Category -> Supplier party -> Upload -> Create draft (with supplier) ->
Run auto-resolve -> Get review -> Resolve one line (CREATE_NEW_PRODUCT) -> Get review again.
Run with: python scripts/phase4b_e2e.py [BASE_URL]
Default BASE_URL: http://localhost:8080
"""
import json
import sys
import uuid
import urllib.request
import urllib.error

BASE_URL = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080"

SHOP_NAME = "Phase4B Test Shop"
OWNER_NAME = "Phase4B Owner"
OWNER_MOBILE = "9876998504"
OWNER_PASSWORD = "pass123"
CATEGORY_NAME = "Phase4B Category"
SUPPLIER_NAME = "Phase4B Supplier"


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
    print("Phase 4B E2E Test")
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

    # 3. Create category (SHOP)
    print("\n3. POST /categories")
    status, resp = req("POST", "/categories", {"name": CATEGORY_NAME}, token=token)
    assert status == 201, f"Expected 201, got {status}: {resp}"
    cat_data = resp.get("data") or resp
    category_id = cat_data.get("id")
    assert category_id, f"Missing category id: {cat_data}"
    print(f"   OK: categoryId={category_id}")

    # 4. Create supplier party
    print("\n4. POST /parties (supplier)")
    status, resp = req("POST", "/parties", {"name": SUPPLIER_NAME, "type": "SUPPLIER"}, token=token)
    assert status == 201, f"Expected 201, got {status}: {resp}"
    party_data = resp.get("data") or resp
    supplier_party_id = party_data.get("id")
    assert supplier_party_id, f"Missing party id: {party_data}"
    print(f"   OK: supplierPartyId={supplier_party_id}")

    # 5. Upload
    print("\n5. POST /purchase/upload")
    dummy_pdf = b"%PDF-1.4 dummy for phase4b"
    status, resp = upload_req(dummy_pdf, token)
    assert status == 201, f"Expected 201, got {status}: {resp}"
    parsed = resp.get("data")
    assert parsed and "uploadId" in parsed, f"Missing uploadId: {resp}"
    upload_id = parsed["uploadId"]
    print(f"   OK: uploadId={upload_id}")

    # 6. Create draft with supplier
    print("\n6. POST /purchase/drafts (with supplierPartyId)")
    status, resp = req("POST", "/purchase/drafts", {
        "uploadId": upload_id,
        "supplierPartyId": supplier_party_id
    }, token=token)
    assert status == 201, f"Expected 201, got {status}: {resp}"
    draft_data = resp.get("data")
    assert draft_data and draft_data.get("draftId"), f"Missing draftId: {resp}"
    draft_id = draft_data["draftId"]
    assert draft_data.get("supplierPartyId") == supplier_party_id
    print(f"   OK: draftId={draft_id}")

    # 7. Run auto-resolve
    print("\n7. POST /purchase/drafts/{draftId}/resolve/auto")
    status, resp = req("POST", f"/purchase/drafts/{draft_id}/resolve/auto", token=token)
    assert status == 200, f"Expected 200, got {status}: {resp}"
    review = resp.get("data")
    assert review is not None, f"Missing data: {resp}"
    assert "lines" in review, f"Missing lines: {review}"
    assert "needsReview" in review or "readyToPost" in review
    lines = review["lines"]
    assert len(lines) >= 1, f"Expected at least 1 line: {lines}"
    print(f"   OK: lines={len(lines)}, needsReview={review.get('needsReview')}, readyToPost={review.get('readyToPost')}")

    # 8. GET review
    print("\n8. GET /purchase/drafts/{draftId}/review")
    status, resp = req("GET", f"/purchase/drafts/{draft_id}/review", token=token)
    assert status == 200, f"Expected 200, got {status}: {resp}"
    review2 = resp.get("data")
    assert review2 and "lines" in review2
    first_line = review2["lines"][0]
    line_id = first_line.get("lineId")
    assert line_id, f"Missing lineId in review: {first_line}"
    print(f"   OK: first lineId={line_id}")

    # 9. Resolve one line (CREATE_NEW_PRODUCT)
    print("\n9. PUT /purchase/drafts/{draftId}/lines/{lineId}/resolve (CREATE_NEW_PRODUCT)")
    status, resp = req("PUT", f"/purchase/drafts/{draft_id}/lines/{line_id}/resolve", {
        "action": "CREATE_NEW_PRODUCT",
        "canonicalName": "Phase4B Test Product",
        "baseUnit": "PCS",
        "categoryId": category_id
    }, token=token)
    assert status == 200, f"Expected 200, got {status}: {resp}"
    review3 = resp.get("data")
    assert review3, f"Missing data: {resp}"
    resolved_line = next((l for l in review3["lines"] if l.get("lineId") == line_id), None)
    assert resolved_line, f"Line {line_id} not in response"
    assert resolved_line.get("resolutionStatus") == "RESOLVED", f"Expected RESOLVED: {resolved_line}"
    assert resolved_line.get("resolvedShopProductId"), f"Expected resolvedShopProductId: {resolved_line}"
    print(f"   OK: line resolved, resolvedShopProductId={resolved_line.get('resolvedShopProductId')}")

    # 10. GET review again
    print("\n10. GET /purchase/drafts/{draftId}/review")
    status, resp = req("GET", f"/purchase/drafts/{draft_id}/review", token=token)
    assert status == 200, f"Expected 200, got {status}: {resp}"
    review4 = resp.get("data")
    resolved_count = sum(1 for l in review4["lines"] if l.get("resolutionStatus") == "RESOLVED")
    assert resolved_count >= 1, f"Expected at least 1 RESOLVED line: {review4}"
    print(f"   OK: resolved lines={resolved_count}")

    print("\n" + "=" * 50)
    print("All Phase 4B E2E tests PASSED")


if __name__ == "__main__":
    main()
