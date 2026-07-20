# TENCO Web (Supplier Dashboard)

A React + TypeScript web dashboard for suppliers, talking to the existing TENCO
Spring Boot backend. Login (OTP), KPIs + P&L, Vendors (by city), Dealers, and Complaints.

## Run (dev)
1. Start the backend (from repo root):
   ```
   java -jar backend/build/libs/tenco-backend-0.1.0.jar   # serves :8080
   ```
2. Start the web app:
   ```
   cd web
   npm install --registry=https://registry.npmjs.org/
   npm run dev            # http://localhost:5173
   ```
   The Vite dev server proxies `/api` and `/auth` to `http://localhost:8080`, so there
   are no CORS issues in development.

3. Sign in with any phone number; in dev the backend returns the OTP (`devOtp`) and it's
   prefilled automatically. Log in as SUPPLIER.

## Build
```
npm run build     # tsc + vite build -> dist/
npm run preview   # serve the production build
```

## Notes / scope
- Uses the real backend endpoints: `/auth/otp/*`, `/api/suppliers/me/dashboard`,
  `/api/reports/pnl`, `/api/vendors`, `/api/dealers`, `/api/complaints`, `/api/payments`.
- Orders and cash-approvals are currently Android-local features (no backend endpoints yet),
  so they aren't in the web dashboard. Once those get backend routes, they can be added here.
- For production, host the backend (Dockerfile + CDK exist under `infra/`) with Postgres and
  point the web app at it (replace the dev proxy with the real base URL / configure CORS).

## Structure
```
src/
  lib/api.ts      fetch wrapper (JWT + refresh)
  lib/tenco.ts    typed endpoint helpers + money()
  components/Shell.tsx   sidebar layout
  pages/          Login, Dashboard, Vendors, Dealers, Complaints
```
