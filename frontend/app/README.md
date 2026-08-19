# Inquiry Frontend (React + TypeScript)

This frontend hosts both inquiry experiences in one React/Vite application:

- INQCUST customer inquiry
- INQACC account inquiry
- INQACCCU customer-account relationship inquiry
- INQTRAN transaction inquiry
- INQSTMT statement inquiry

## Prerequisites

- Node.js 20+
- npm 10+

## Run Locally

```bash
npm install
npm run dev
```

App runs by default on http://localhost:5173.

## Routes

- http://localhost:5173/customers for INQCUST
- http://localhost:5173/accounts for INQACC
- http://localhost:5173/customer-accounts for INQACCCU
- http://localhost:5173/transactions for INQTRAN
- http://localhost:5173/statements for INQSTMT

## Configuration

The UI is backend-only at runtime and always calls the backend API.

- VITE_API_BASE_URL: backend base URL (default: empty, uses Vite dev proxy)
- VITE_API_TIMEOUT_MS: optional request timeout in milliseconds
- VITE_INQACC_BEARER_TOKEN: development bearer token attached transparently to INQACC requests

Example .env.local:

```bash
# Optional for cross-origin mode. If omitted, local dev uses Vite proxy.
# VITE_API_BASE_URL=http://localhost:8080
VITE_API_TIMEOUT_MS=7000
VITE_INQACC_BEARER_TOKEN=valid-inqacc-inquirer-token
```

Local development behavior:
- `npm run dev` proxies `/api/*` to `http://localhost:8080`.
- `npm run dev` also proxies `/v1/*` to `http://localhost:8080`.
- This avoids browser CORS issues for common localhost development.
- For explicit cross-origin testing, set `VITE_API_BASE_URL` and keep backend CORS enabled.

Port summary:

- Frontend dev server: `5173`
- Backend API target: `8080`

## Test and Build

```bash
npm run test
npm run test:e2e
npm run build
```

Directory and startup commands:

- Frontend directory: `frontend/app`
  - `npm install`
  - `npm run dev`
- Backend directory: `backend/api`
  - `mvn spring-boot:run`

## Notes

- Inputs are textboxes only. No lookup-mode dropdown is exposed.
- Response message rendering is backend-driven (legacyStatus.message or backend error payload message/code).
- Special values are passed to backend unchanged:
  - 0000000000 for random customer lookup
  - 9999999999 for latest customer lookup
- INQACC reserved value:
  - 99999999 requests highest-account-number lookup for the entered sortcode
- INQACC authorization:
  - configured development token `valid-inqacc-inquirer-token` is accepted for inquiry access
  - `valid-inqacc-limited-token` is authenticated but forbidden (403) for inquiry role
  - this is a deterministic development authentication adapter for bearer-header and role-boundary behavior; it is not production OAuth2/JWT identity validation
- INQSTMT inquiry:
  - uses sort code, account number, and statement period (YYYYMM)
  - calls `GET /api/v1/accounts/{sortCode}/{accountNumber}/statements/{period}`
  - uses the same development bearer token conventions as INQACC for 401/403 behavior

- INQACCCU inquiry:
	- uses customer-number input only
	- calls `GET /api/v1/customers/{customerNumber}/accounts`
	- supports subsequent inquiry in-page by updating customer number and resubmitting

INQACCCU browser-level E2E evidence:

- `e2e/inqacccu.e2e.spec.ts`
- Executed command: `npm run test:e2e`
