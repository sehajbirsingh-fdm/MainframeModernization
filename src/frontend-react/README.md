# Customer Inquiry Frontend (React + TypeScript)

This frontend implements the INQCUST customer inquiry workflow with textbox-only input for sort code and customer number and API-driven responses.

## Prerequisites

- Node.js 20+
- npm 10+

## Run Locally

```bash
npm install
npm run dev
```

App runs by default on http://localhost:5173.

## Configuration

The UI is backend-only at runtime and always calls the backend API.

- VITE_API_BASE_URL: backend base URL (default: empty, uses Vite dev proxy)
- VITE_API_TIMEOUT_MS: optional request timeout in milliseconds

Example .env.local:

```bash
# Optional for cross-origin mode. If omitted, local dev uses Vite proxy.
# VITE_API_BASE_URL=http://localhost:8080
VITE_API_TIMEOUT_MS=7000
```

Local development behavior:
- `npm run dev` proxies `/api/*` to `http://localhost:8080`.
- This avoids browser CORS issues for common localhost development.
- For explicit cross-origin testing, set `VITE_API_BASE_URL` and keep backend CORS enabled.

## Test and Build

```bash
npm run test
npm run build
```

## Notes

- Inputs are textboxes only. No lookup-mode dropdown is exposed.
- Response message rendering is backend-driven (legacyStatus.message or backend error payload message/code).
- Special values are passed to backend unchanged:
  - 0000000000 for random customer lookup
  - 9999999999 for latest customer lookup
