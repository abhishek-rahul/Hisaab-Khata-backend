import requests
import sys

BASE = "http://localhost:8080"

def main():
    # 1) Health
    r = requests.get(f"{BASE}/health", timeout=5)
    if r.status_code != 200 or r.text.strip() != "OK":
        print("❌ Health failed:", r.status_code, r.text)
        sys.exit(1)
    print("✅ Health OK")

    print("\nPhase 0 E2E done ✅")
    print("Next: check app logs for Flyway migration success.")

if __name__ == "__main__":
    main()