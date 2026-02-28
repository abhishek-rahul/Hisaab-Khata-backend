# Phase 1 – Extensibility Risks (Baad mein problem na ho isliye)

Yeh document un cheezon ko likhta hai jo abhi theek lagti hain, lekin jab app badega (naye roles, naye APIs, multi-shop user) tab **bada refactor** ya **problem** create kar sakti hain. Simple terms mein.

---

## 1. **IAuthService mein do kaam ek saath (Auth + Staff)**

**Kya hai:**  
`IAuthService` / `AuthServiceImpl` mein abhi:
- Auth: `register`, `login`, `refreshToken`
- User/Staff: `createStaff`

**Problem:**  
Jab aage add karoge: list staff, deactivate staff, change password, forgot password, invite staff, edit staff — sab isi service mein jayega. Ek hi service **bahut moti** ho jayegi aur **auth** aur **user management** mix ho jayega. Testing aur changes dono mushkil.

**Extensible banane ke liye:**  
- **AuthService** – sirf login, register, refresh, (optional: logout).
- **UserService** ya **StaffService** – createStaff, list, update, deactivate, etc.  
Abhi split kar do to baad mein aasaan rahega.

---

## 2. **Role check har controller mein alag (OWNER / STAFF)**

**Kya hai:**  
`StaffController` mein:
- `if (shopContext.getCurrentRole() != UserRole.OWNER)`  
- `if (!shopId.equals(shopContext.getCurrentShopId()))`

**Problem:**  
Jab naye roles aayenge (e.g. MANAGER bhi staff create kar sake) ya 20 endpoints par alag-alag permission chahiye hogi, to har controller mein yehi `if` duplicate honge. Ek jagah change bhool gaye to security hole ban sakta hai.

**Extensible banane ke liye:**  
- Method-level security use karo: `@PreAuthorize("hasRole('OWNER')")` ya custom annotation.
- Ya ek chhota **authorization helper** (e.g. `requireRole(UserRole.OWNER)`, `requireShop(shopId))` jisko sab controllers use karein.  
Isse “kaun kya kar sakta hai” ek jagah define hoga.

---

## 3. **Mobile = poore system mein unique**

**Kya hai:**  
`userRepository.findByMobile(mobile)` — mobile se user dhoondha jata hai, aur duplicate mobile pe conflict.

**Problem:**  
Agar baad mein yeh rule change karna ho: “same mobile number do alag shops mein (owner ek jagah, staff doosri jagah)” — to **mobile uniqueness** ab “per shop” ya “per account” karni padegi. Uske liye DB schema, validations, aur existing data sab change. **Bada change.**

**Extensible banane ke liye:**  
Domain pehle se soch lo: mobile **global** unique rahega ya **per shop**? Agar future mein multi-shop / multi-role same mobile chahiye, to abhi hi uniqueness rule document kar do aur agar possible ho to design (e.g. unique constraint) aise rakho ki baad mein scope change karna easy ho.

---

## 4. **Refresh token: ek user = ek hi token (purane delete)**

**Kya hai:**  
`RefreshTokenService.create(userId)` pehle `repo.deleteByUserId(userId)` karta hai — matlab har naya login/register purane saare refresh tokens delete kar deta hai.

**Problem:**  
Agar baad mein chahiye: “multiple devices / sessions” ya “logout other devices” alag se control karna — to abhi wala “single token per user” design change karna padega. Token rotation, family, ya multiple tokens per user — sab logic change.

**Extensible banane ke liye:**  
Agar multi-session / multi-device plan hai to jaldi soch lena: either abhi multiple tokens allow karo (and optional “logout others” API) ya document karo ki “single session per user” intentional hai, taaki baad mein refactor predictable ho.

---

## 5. **SecurityConfig: sirf path, role nahi**

**Kya hai:**  
`/auth/**`, `/health` = public; `/shops/**`, `/me` = sirf “authenticated”. Kis **role** ko kaun sa path chalana hai, yeh SecurityConfig mein define nahi hai.

**Problem:**  
Role-based rules ab controllers ke andar `if (role != OWNER)` se ho rahi hain. Jab paths zyada honge, to “OWNER can X”, “STAFF can Y” sab jagah repeat hoga. Mistake se koi endpoint role check bhool sakta hai.

**Extensible banane ke liye:**  
Ya to `@PreAuthorize` se har endpoint par role/resource fix karo, ya ek chhota matrix (kon si role kon se path access kar sakti hai) ek jagah rakho. SecurityConfig ko “authenticated vs public” tak limit karo; role logic service/annotation side rahe.

---

## 6. **Controller se direct Repository (MeController)**

**Kya hai:**  
`MeController` seedha `UserRepository` use karta hai: `userRepository.findById(shopContext.getCurrentUserId())`.

**Problem:**  
Chhota lagta hai abhi. Jab /me extend hoga (e.g. preferences, last login, linked shops) to controller mein repository calls badhengi. Business logic controller mein ghus jayegi. Test karna aur reuse karna mushkil.

**Extensible banane ke liye:**  
`MeService` ya `UserQueryService` bana ke “current user ki info / profile” wahi se lao. Controller sirf service ko call kare. Baad mein /me complex hone par sab logic ek jagah rahegi.

---

## 7. **Refresh flow mein RuntimeException**

**Kya hai:**  
`AuthServiceImpl.refreshToken()` mein:  
`if (optUser.isEmpty()) throw new RuntimeException("User not found");`

**Problem:**  
`RuntimeException` → GlobalExceptionHandler ise generic `Exception` se pakad kar **500** bhejta hai. Client ko “User not found” 500 milna sahi nahi; 401/404 aur proper `errorCode` dena chahiye.

**Extensible banane ke liye:**  
`UnauthorizedException` ya `ResourceNotFoundException` with proper `errorCode` throw karo, taaki API response consistent rahe aur client 4xx ko sahi handle kar sake.

---

## Short summary

| Risk | Kya badalna padega baad mein |
|------|-----------------------------|
| Auth + Staff ek service | Auth vs User/Staff services alag karna |
| Role check controller mein | @PreAuthorize ya central authorization |
| Mobile global unique | Uniqueness per-shop / multi-tenant |
| Single refresh token per user | Multi-session / token family design |
| Role SecurityConfig mein nahi | Role–path matrix ya method security |
| MeController → Repository | MeService / UserQueryService |
| RuntimeException in refresh | Proper 4xx exception + errorCode |

In points ko abhi document kar lena (aur jahan easy ho wahan thoda refactor) baad mein code **extensible** rakhne mein kaam aayega.
