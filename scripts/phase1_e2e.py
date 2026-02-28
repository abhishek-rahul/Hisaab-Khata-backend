"""
Phase 1 E2E: Identity + Auth (Register, Login, JWT).
Assumes app is running at BASE; Flyway V1 + V2 applied.
"""
import requests
import sys

BASE = "http://localhost:8080"

def main():
    # 1) Health (public)
    r = requests.get(f"{BASE}/health", timeout=5)
    if r.status_code != 200 or r.text.strip() != "OK":
        print("❌ Health failed:", r.status_code, r.text)
        sys.exit(1)
    print("✅ Health OK")

    # 2) Register: name, phone, password, shopName, city
    reg = requests.post(
        f"{BASE}/auth/register",
        json={
            "name": "Phase1 Owner",
            "phone": "9876543211",
            "password": "secret124",
            "shopName": "Phase2 Shop",
            "city": "Mumbai"
        },
        timeout=10
    )
    if reg.status_code != 201:
        print("❌ Register failed:", reg.status_code, reg.text)
        sys.exit(1)
    data = reg.json()
    if not data.get("success") or not data.get("data", {}).get("token"):
        print("❌ Register response missing token:", data)
        sys.exit(1)
    token = data["data"]["token"]
    shop_id = data["data"].get("shopId")
    user_id = data["data"].get("userId")
    print("✅ Register OK — shopId:", shop_id, "userId:", user_id)

    # 3) Login: phone, password
    login = requests.post(
        f"{BASE}/auth/login",
        json={"phone": "9876543211", "password": "secret124"},
        timeout=10
    )
    if login.status_code != 200:
        print("❌ Login failed:", login.status_code, login.text)
        sys.exit(1)
    login_data = login.json()
    if not login_data.get("success") or not login_data.get("data", {}).get("token"):
        print("❌ Login response missing token:", login_data)
        sys.exit(1)
    print("✅ Login OK — JWT received")

    # 4) Optional: call a protected endpoint with JWT
    me = requests.get(
        f"{BASE}/category",
        headers={"Authorization": f"Bearer {login_data['data']['token']}"},
        timeout=5
    )
    # 200 or 404/empty list is fine
    if me.status_code in (200, 404):
        print("✅ Protected endpoint (/category) OK with JWT")
    else:
        print("⚠️ Protected endpoint returned:", me.status_code, "(check if route exists)")

    print("\nPhase 1 E2E done ✅")

if __name__ == "__main__":
    main()
