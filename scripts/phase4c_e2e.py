#!/usr/bin/env python3
"""
Phase 4C E2E test: Upload -> Create draft (with supplier) -> Resolve all lines (CREATE_NEW_PRODUCT) ->
Post -> Double post 409 -> Edit blocked (PUT save returns 409 when POSTED).
Run with: python scripts/phase4c_e2e.py [BASE_URL]
Default BASE_URL: http://localhost:8080
"""
import json
import sys
import uuid
import urllib.request
import urllib.error

BASE_URL = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080"

SHOP_NAME = "Phase4C Test Shop"
OWNER_NAME = "Phase4C Owner"
OWNER_MOBILE = "9876598505"
OWNER_PASSWORD = "pass123"
CATEGORY_NAME = "Phase4C Category"
SUPPLIER_NAME = "Phase4C Supplier"


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
    print("Phase 4C E2E Test")
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

    # 3. Create category
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
    dummy_pdf = b"%PDF-1.4 dummy for phase4c"
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
    print(f"   OK: draftId={draft_id}")

    # 7. Resolve all lines via CREATE_NEW_PRODUCT (mock has 3 lines)
    print("\n7. GET review then resolve each line (CREATE_NEW_PRODUCT)")
    status, resp = req("GET", f"/purchase/drafts/{draft_id}/review", token=token)
    assert status == 200, f"Expected 200, got {status}: {resp}"
    review = resp.get("data")
    lines = review.get("lines", [])
    assert len(lines) >= 1, f"Expected at least 1 line: {lines}"
    for i, line in enumerate(lines):
        line_id = line.get("lineId")
        if line.get("resolutionStatus") == "RESOLVED":
            continue
        status, resp = req("PUT", f"/purchase/drafts/{draft_id}/lines/{line_id}/resolve", {
            "action": "CREATE_NEW_PRODUCT",
            "canonicalName": f"Phase4C Product {i+1}",
            "baseUnit": "PCS",
            "categoryId": category_id
        }, token=token)
        assert status == 200, f"Expected 200 resolving line {line_id}, got {status}: {resp}"
    print(f"   OK: all {len(lines)} lines resolved")

    # 8. Post draft
    print("\n8. POST /purchase/drafts/{draftId}/post")
    status, resp = req("POST", f"/purchase/drafts/{draft_id}/post", token=token)
    assert status == 200, f"Expected 200, got {status}: {resp}"
    posted = resp.get("data")
    assert posted, f"Missing data: {resp}"
    assert posted.get("status") == "POSTED", f"Expected status POSTED: {posted}"
    assert posted.get("postedAt"), f"Missing postedAt: {posted}"
    print(f"   OK: status=POSTED, postedAt={posted.get('postedAt')}")

    # 9. Double post => 409
    print("\n9. POST post again (expect 409)")
    status, resp = req("POST", f"/purchase/drafts/{draft_id}/post", token=token)
    assert status == 409, f"Expected 409 for already posted, got {status}: {resp}"
    assert resp.get("success") is False
    assert resp.get("errorCode") == "ALREADY_POSTED" or "posted" in (resp.get("message") or "").lower()
    print("   OK: 409 ALREADY_POSTED")

    # 10. Edit blocked (PUT save draft when POSTED => 409)
    print("\n10. PUT save draft (expect 409 DRAFT_NOT_EDITABLE)")
    status, resp = req("GET", f"/purchase/drafts/{draft_id}", token=token)
    assert status == 200, f"Expected 200, got {status}: {resp}"
    draft = resp.get("data")
    save_lines = [{"lineNo": l["lineNo"], "rawName": l["rawName"], "quantity": float(l.get("quantity", 1)),
                  "unit": l.get("unit", "PCS"), "unitPrice": float(l.get("unitPrice", 0)),
                  "lineAmount": float(l.get("lineAmount", 0))} for l in draft.get("lines", [])]
    status, resp = req("PUT", f"/purchase/drafts/{draft_id}", {
        "version": draft.get("version"),
        "notes": "Try edit after post",
        "lines": save_lines
    }, token=token)
    assert status == 409, f"Expected 409 when editing posted draft, got {status}: {resp}"
    assert resp.get("success") is False
    assert resp.get("errorCode") == "DRAFT_NOT_EDITABLE" or "draft" in (resp.get("message") or "").lower()
    print("   OK: 409 edit blocked")

    print("\n" + "=" * 50)
    print("All Phase 4C E2E tests PASSED")


if __name__ == "__main__":
    main()
