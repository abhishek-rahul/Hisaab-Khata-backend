#!/usr/bin/env python3
"""
Phase 1 E2E test: register -> login owner -> create staff -> login staff -> /me -> forbidden staff create staff.
Run with: python scripts/phase1_e2e.py [BASE_URL]
Default BASE_URL: http://localhost:8080
"""
import json
import sys
import urllib.request
import urllib.error

BASE_URL = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080"


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
        body = e.read().decode()
        try:
            return e.code, json.loads(body)
        except json.JSONDecodeError:
            return e.code, {"raw": body}


def main():
    print("Phase 1 E2E Test")
    print("=" * 50)

    # 1. Register
    print("\n1. POST /auth/register")
    status, resp = req("POST", "/auth/register", {
        "shopName": "Test Shop",
        "ownerName": "Owner User",
        "mobile": "9876543267",
        "password": "pass123"
    })
    assert status == 201, f"Expected 201, got {status}: {resp}"
    assert resp.get("success") is True, f"Expected success: {resp}"
    data = resp.get("data", {})
    shop_id = data.get("shopId")
    user_id = data.get("userId")
    token = data.get("token")
    refresh_token = data.get("refreshToken")
    print(f"   resfresh token ={refresh_token}")
    assert shop_id and user_id and token and refresh_token, f"Missing fields: {data}"
    print(f"   OK: shopId={shop_id}, userId={user_id}")

    # 2. Login owner
    print("\n2. POST /auth/login (owner)")
    status, resp = req("POST", "/auth/login", {"mobile": "9876543267", "password": "pass123"})
    assert status == 200, f"Expected 200, got {status}: {resp}"
    assert resp.get("success") is True
    login_data = resp.get("data", {})
    owner_token = login_data.get("token")
    owner_refresh_token = login_data.get("refreshToken")
    assert owner_token, "No token in login response"
    assert owner_refresh_token, "No refresh token in login response"
    print("   OK")

    # 3. Create staff (owner)
    print("\n3. POST /shops/{shopId}/staff (owner)")
    status, resp = req("POST", f"/shops/{shop_id}/staff", {
        "name": "Staff User",
        "mobile": "9876543268",
        "password": "staff123"
    }, token=owner_token)
    assert status == 201, f"Expected 201, got {status}: {resp}"
    assert resp.get("success") is True
    staff_data = resp.get("data", {})
    assert staff_data.get("role") == "STAFF"
    assert staff_data.get("active") is True
    staff_id = staff_data.get("id")
    print(f"   OK: staffId={staff_id}")

    # 4. Login staff
    print("\n4. POST /auth/login (staff)")
    status, resp = req("POST", "/auth/login", {"mobile": "9876543268", "password": "staff123"})
    assert status == 200, f"Expected 200, got {status}: {resp}"
    staff_token = resp.get("data", {}).get("token")
    assert staff_token
    print("   OK")

    # 5. GET /me (staff)
    print("\n5. GET /me (staff)")
    status, resp = req("GET", "/me", token=staff_token)
    assert status == 200, f"Expected 200, got {status}: {resp}"
    me = resp.get("data", {})
    assert me.get("id") == staff_id
    assert me.get("shopId") == shop_id
    assert me.get("role") == "STAFF"
    print(f"   OK: id={me.get('id')}, role={me.get('role')}")

    # 6. Staff tries to create staff -> forbidden
    print("\n6. POST /shops/{shopId}/staff (staff) -> expect 403")
    status, resp = req("POST", f"/shops/{shop_id}/staff", {
        "name": "Another Staff",
        "mobile": "9876543268",
        "password": "staff456"
    }, token=staff_token)
    assert status == 403, f"Expected 403, got {status}: {resp}"
    assert resp.get("success") is False
    assert resp.get("statusCode") == "FORBIDDEN"
    print("   OK: Forbidden as expected")

    # 7. Refresh token (owner's refresh token from step 1)
    print("\n7. POST /auth/refresh (with owner's refresh token)")
    print(f"   resfresh token ={owner_refresh_token}")
    status, resp = req("POST", "/auth/refresh", token=owner_refresh_token)
    assert status == 200, f"Expected 200, got {status}: {resp}"
    assert resp.get("success") is True
    print("   OK")

    print("\n" + "=" * 50)
    print("All Phase 1 E2E tests PASSED")


if __name__ == "__main__":
    main()
