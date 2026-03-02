#!/usr/bin/env python3
"""
Phase 4D E2E test: Health -> Register shop -> POST /purchase/upload (PDF) ->
GET /purchase/upload/{uploadId} -> assert ParsedInvoiceResponse.
Invalid/missing file returns uniform ErrorResponse.
Run with: python scripts/phase4d_e2e.py [BASE_URL]
Default BASE_URL: http://localhost:8080
"""
import json
import sys
import urllib.request
import urllib.error

BASE_URL = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080"

SHOP_NAME = "Phase4D Test Shop"
OWNER_NAME = "Phase4D Owner"
OWNER_MOBILE = "9877698502"
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


def upload_req(file_bytes, filename="bill.pdf", token=None, supplier_party_id=None):
    import mimetypes
    import uuid
    boundary = uuid.uuid4().hex
    body = b""
    body += f"--{boundary}\r\n".encode()
    body += f'Content-Disposition: form-data; name="file"; filename="{filename}"\r\n'.encode()
    body += b"Content-Type: application/pdf\r\n\r\n"
    body += file_bytes
    body += b"\r\n"
    if supplier_party_id is not None:
        body += f"--{boundary}\r\n".encode()
        body += b'Content-Disposition: form-data; name="supplierPartyId"\r\n\r\n'
        body += str(supplier_party_id).encode()
        body += b"\r\n"
    body += f"--{boundary}--\r\n".encode()
    url = f"{BASE_URL}/purchase/upload"
    headers = {
        "Content-Type": f"multipart/form-data; boundary={boundary}",
        "Content-Length": str(len(body)),
    }
    if token:
        headers["Authorization"] = f"Bearer {token}"
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
    print("Phase 4D E2E Test")
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

    # 3. POST /purchase/upload with a dummy PDF (mock parser ignores content)
    print("\n3. POST /purchase/upload")
    dummy_pdf = b"%PDF-1.4 dummy content for phase4d e2e"
    status, resp = upload_req(dummy_pdf, token=token)
    assert status == 201, f"Expected 201, got {status}: {resp}"
    assert resp.get("success") is True, f"Expected success=True: {resp}"
    parsed = resp.get("data")
    assert parsed, f"Missing data: {resp}"
    assert "uploadId" in parsed, f"Missing uploadId: {parsed}"
    assert parsed.get("docKind") == "PDF", f"Expected docKind=PDF: {parsed}"
    assert parsed.get("parserKey") == "MOCK:v0", f"Expected parserKey=MOCK:v0: {parsed}"
    lines = parsed.get("lines")
    assert isinstance(lines, list), f"Expected lines list: {lines}"
    assert len(lines) == 3, f"Expected 3 mocked lines: {len(lines)}"
    for i, line in enumerate(lines):
        assert "lineNo" in line and "rawName" in line and "normalizedName" in line
        assert "quantity" in line and "unit" in line and "unitPrice" in line
        assert "lineAmount" in line
    upload_id = parsed["uploadId"]
    print(f"   OK: uploadId={upload_id}, lines={len(lines)}")

    # 4. GET /purchase/upload/{uploadId}
    print("\n4. GET /purchase/upload/{uploadId}")
    status, resp = req("GET", f"/purchase/upload/{upload_id}", token=token)
    assert status == 200, f"Expected 200, got {status}: {resp}"
    assert resp.get("success") is True
    get_data = resp.get("data")
    assert get_data.get("uploadId") == upload_id
    assert len(get_data.get("lines")) == 3
    print("   OK: same ParsedInvoiceResponse")

    # 5. Invalid: missing file — empty multipart triggers parse failure → 400 + ErrorResponse
    print("\n5. POST /purchase/upload without file (expect 400 + ErrorResponse)")
    empty_boundary = "boundary123"
    empty_body = f"--{empty_boundary}\r\n".encode() + f"--{empty_boundary}--\r\n".encode()
    url = f"{BASE_URL}/purchase/upload"
    headers = {
        "Content-Type": f"multipart/form-data; boundary={empty_boundary}",
        "Content-Length": str(len(empty_body)),
        "Authorization": f"Bearer {token}",
    }
    req_obj = urllib.request.Request(url, data=empty_body, headers=headers, method="POST")
    try:
        with urllib.request.urlopen(req_obj) as r:
            status, resp = r.status, json.loads(r.read().decode())
        assert status == 400, f"Expected 400 for missing/invalid file, got {status}"
    except urllib.error.HTTPError as e:
        status = e.code
        try:
            resp = json.loads(e.read().decode())
        except Exception:
            resp = {}
        assert status == 400, f"Expected 400 for missing/invalid file, got {status}: {resp}"
    assert resp.get("success") is False, f"Expected success=False in ErrorResponse: {resp}"
    assert "errorCode" in resp or "message" in resp, f"Expected errorCode/message: {resp}"
    print("   OK: uniform ErrorResponse (success=false, errorCode/message)")

    print("\n" + "=" * 50)
    print("All Phase 4D E2E tests PASSED")


if __name__ == "__main__":
    main()
