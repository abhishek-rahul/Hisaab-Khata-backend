# Collection notes – extending for Phase 7+

Use this guide to extend the Postman collection when new phases (e.g. Phase 7) add endpoints or change request/response shapes.

## Adding new endpoints

1. **Discover from code**
   - Controllers: `src/main/java/.../controller/*.java` – note `@RequestMapping` base path and each method’s path and HTTP method.
   - DTOs: `src/main/java/.../dto/` – use exact field names and types for request bodies.
   - Response: `ApiResponse<T>` vs `SuccessResponse<T>` – check which envelope the controller returns.

2. **Add to README**
   - In `postman/README.md`, add the new endpoint to the correct domain table (or a new section).
   - Include: Method, Path, Tag (core/optional/legacy), Request body/params, Response shape.

3. **Add to collection**
   - Create a new folder if the domain is new (e.g. “Payments”).
   - Add a request with:
     - **Name**: `[METHOD] /path - Short purpose`
     - **URL**: `{{baseUrl}}/path` (and query params if needed).
     - **Headers**: `Content-Type: application/json` for JSON bodies.
     - **Body**: Raw JSON matching the request DTO (use `{{variable}}` for IDs from previous steps).
     - **Auth**: Use collection auth (Bearer `{{accessToken}}`) unless the endpoint is public.
   - In **Tests**:
     - Assert status code (e.g. `pm.response.to.have.status(200)`).
     - Assert envelope (`j.success`, `j.data`).
     - Store any returned IDs with `pm.environment.set('variableName', value)` so later requests can use them.

4. **Environment variables**
   - If new IDs or config are needed, add them to both environment files:
     - `postman/HisaabKhata_local.postman_environment.json`
     - `postman/HisaabKhata_dev.postman_environment.json`
   - Add the same keys to the “Environment variables” table in README.

## Runner order

- Keep the same flow order in the collection so the Runner can run folders top-to-bottom:
  1. Health → Auth (Register or Login) → Me  
  2. Parties (Create Customer, Create Supplier)  
  3. Categories → Products → Stock  
  4. Purchase (Upload → … → Post) → Sales (Draft → Post)  
  5. Ledger → Reports → Dashboard / Khata / Search / Closing  

- New domains (e.g. Payments) should be inserted in a logical place and any new variables (e.g. `paymentId`) set in tests and documented in README.

## Exporting / updating the collection

- **From Postman**: Collection → ⋮ → Export → Export Collection v2.1 → save as `postman/HisaabKhata_Phase1_6.postman_collection.json` (overwrite).
- **Environments**: Environments → ⋮ → Export → save as `postman/HisaabKhata_local.postman_environment.json` (and `_dev`).
- **Version control**: Commit the updated JSON files and any README/notes changes so the repo stays the source of truth.

## Deprecations (Phase 7+)

- When an endpoint is deprecated (e.g. legacy purchase), keep it in the collection under a “Legacy” or “Deprecated” folder and mark it in README so it can be removed in a later phase.
- Do not remove legacy requests until the replacement flow is stable and documented.
