#!/usr/bin/env python3
"""
Phase 4A E2E test: Health -> Register -> Upload -> Create draft -> Get draft ->
Save (replace-all) -> Conflict test (stale version returns 409).
Run with: python scripts/phase4a_e2e.py [BASE_URL]
Default BASE_URL: http://localhost:8080
"""
import json
import sys
import uuid
import urllib.request
import urllib.error

BASE_URL = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080"

SHOP_NAME = "Phase4A Test Shop"
OWNER_NAME = "Phase4A Owner"
OWNER_MOBILE = "9875598503"
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
    print("Phase 4A E2E Test")
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

    # 3. Upload
    print("\n3. POST /purchase/upload")
    dummy_pdf = b"%PDF-1.4 dummy for phase4a"
    status, resp = upload_req(dummy_pdf, token)
    assert status == 201, f"Expected 201, got {status}: {resp}"
    parsed = resp.get("data")
    assert parsed and "uploadId" in parsed, f"Missing uploadId: {resp}"
    upload_id = parsed["uploadId"]
    lines = parsed.get("lines", [])
    assert len(lines) >= 1, f"Expected at least 1 line: {lines}"
    print(f"   OK: uploadId={upload_id}")

    # 4. Create draft (idempotent: same uploadId twice returns same draft)
    print("\n4. POST /purchase/drafts")
    status, resp = req("POST", "/purchase/drafts", {"uploadId": upload_id}, token=token)
    assert status == 201, f"Expected 201, got {status}: {resp}"
    assert resp.get("success") is True
    draft_data = resp.get("data")
    assert draft_data, f"Missing data: {resp}"
    assert draft_data.get("draftId"), f"Missing draftId: {draft_data}"
    assert draft_data.get("status") == "DRAFT"
    assert draft_data.get("version") == 0
    assert draft_data.get("uploadId") == upload_id
    draft_id = draft_data["draftId"]
    total_before = draft_data.get("totalAmount")
    assert total_before is not None, "Missing totalAmount"
    print(f"   OK: draftId={draft_id}, version=0")

    # 4b. Idempotent: create again with same uploadId -> same draft
    status2, resp2 = req("POST", "/purchase/drafts", {"uploadId": upload_id}, token=token)
    assert status2 == 201, f"Expected 201 on idempotent create, got {status2}: {resp2}"
    draft_data2 = resp2.get("data")
    assert draft_data2.get("draftId") == draft_id, "Idempotent should return same draft"

    # 5. Get draft
    print("\n5. GET /purchase/drafts/{draftId}")
    status, resp = req("GET", f"/purchase/drafts/{draft_id}", token=token)
    assert status == 200, f"Expected 200, got {status}: {resp}"
    get_draft = resp.get("data")
    assert get_draft.get("draftId") == draft_id
    assert get_draft.get("lines") is not None
    version = get_draft["version"]
    print(f"   OK: version={version}, lines={len(get_draft['lines'])}")

    # 6. Save (replace-all): change notes and one line
    print("\n6. PUT /purchase/drafts/{draftId} (replace-all)")
    save_lines = []
    for i, ln in enumerate(get_draft["lines"]):
        save_lines.append({
            "lineNo": ln["lineNo"],
            "rawName": ln["rawName"] if i > 0 else "Updated Item Name",
            "quantity": float(ln["quantity"]) if ln.get("quantity") is not None else 1,
            "unit": ln["unit"],
            "unitPrice": float(ln["unitPrice"]) if ln.get("unitPrice") is not None else 0,
            "lineAmount": float(ln["lineAmount"]) if ln.get("lineAmount") is not None else 0,
        })
    save_body = {
        "version": version,
        "notes": "Phase4A E2E save",
        "lines": save_lines
    }
    status, resp = req("PUT", f"/purchase/drafts/{draft_id}", save_body, token=token)
    assert status == 200, f"Expected 200, got {status}: {resp}"
    saved = resp.get("data")
    assert saved.get("version") == version + 1
    assert saved.get("notes") == "Phase4A E2E save"
    print(f"   OK: new version={saved['version']}")

    # 7. Conflict test: PUT with stale version -> 409
    print("\n7. PUT with stale version (expect 409)")
    stale_body = {"version": version, "notes": "Stale", "lines": save_lines}
    status, resp = req("PUT", f"/purchase/drafts/{draft_id}", stale_body, token=token)
    assert status == 409, f"Expected 409 for version conflict, got {status}: {resp}"
    assert resp.get("success") is False
    assert resp.get("errorCode") == "VERSION_CONFLICT" or "version" in (resp.get("message") or "").lower()
    print("   OK: 409 VERSION_CONFLICT")

    print("\n" + "=" * 50)
    print("All Phase 4A E2E tests PASSED")


if __name__ == "__main__":
    main()
