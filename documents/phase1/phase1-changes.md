# Phase 1 – Changes Summary

Yeh document Phase 1 mein kiye gaye changes ko simple aur easy terms mein batata hai.

---

## 1. Auth (Login / Register / Refresh)

- **Register:** Naya shop + owner user ban sakta hai. Response mein `shopId`, `userId`, `token`, aur `refreshToken` aate hain.
- **Login:** Mobile + password se login. Response mein `token` aur `refreshToken` milte hain.
- **Refresh:** Purana refresh token bhejne par naya access token milta hai. Refresh token **Authorization: Bearer &lt;refreshToken&gt;** header mein bhejna hota hai.
- **Note:** Har naya login/register us user ke purane refresh tokens ko replace kar deta hai (ek user ka ek hi valid refresh token).

---

## 2. User & Shop

- **User:** `User` entity mein `role` (OWNER/STAFF), `status` (e.g. ACTIVE), `active` flag, `shopId` link.
- **Shop:** `Shop` entity – shop name, phone, owner name.
- **RefreshToken:** Alag table – token string, userId, expiry, revoked. Ek user ke liye naya token banate waqt purane delete ho jate hain.

---

## 3. Security

- **SecurityConfig:** Public routes – register, login, refresh, health. Baaki routes ke liye JWT (Bearer token) zaroori.
- JWT se user identify hota hai; role (OWNER/STAFF) bhi token se aata hai.

---

## 4. New APIs

- **GET /me:** Logged-in user ki details (id, shopId, role, name, mobile, etc.) – JWT se user resolve hota hai.
- **POST /shops/{shopId}/staff:** Sirf OWNER staff create kar sakta hai. Body: name, mobile, password. Staff ki role STAFF hoti hai.
- **GET /health (ya similar):** Server health check ke liye.

---

## 5. Responses & Errors

- **ApiResponse:** Sab APIs ek common structure use karti hain – `success`, `statusCode`, `message`, `data`, `meta`, `errorCode` (errors ke liye).
- **GlobalExceptionHandler:** Unauthorized, Conflict (e.g. mobile already exists), validation errors ko same format mein return karta hai.
- **ConflictException:** Jaise mobile pehle se registered ho to conflict throw hota hai.

---

## 6. E2E Script

- **scripts/phase1_e2e.py:**  
  Register → Owner login → Staff create (owner) → Staff login → GET /me (staff) → Staff se staff create (403 expected) → Refresh token (owner ka **login** wala refresh token use karna zaroori, register wala nahi).

---

## 7. Database

- **db/:** Migration ya schema scripts (e.g. Flyway/Liquibase) agar use kiye ho.
- **domain/support:** Koi support classes/embeddables agar add kiye ho.

---

## 8. DTOs & Enums

- **Auth:** RegisterRequest, LoginRequest, AuthResponse, MeResponse, StaffCreateRequest, StaffResponse, RefreshToken (domain).
- **Enums:** UserRole (OWNER, STAFF), UserStatus (e.g. ACTIVE).
