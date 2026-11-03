# Phase 8 Integration & Deployment Checklist

> **Repository Root:** `D:\HOTEL_PROJECT\Hotel-Booking-Management-System--React-MERN-FullStack`  
> **Status:** In Progress — Stages 1–4 Complete, Stage 5 Audit Complete

---

## Staged Execution Checklist

### [x] Stage 1: MySQL 8 Service & Port 3306 Verification (Completed)
- [x] Test connectivity to `localhost:3306` using `Test-NetConnection`.
- [x] Started MySQL service (`MySQL80`).
- [x] Confirmed port 3306 is open and accepting TCP connections.

### [x] Stage 2: Database Initialization & Flyway Schema Migration (Completed)
- [x] Database `hotel_booking` created.
- [x] Configured `application.properties`: Flyway enabled, baseline=false, ddl-auto=validate.
- [x] Verified `V1__init_schema.sql` applied successfully (`SUCCESS` in `flyway_schema_history`).
- [x] Inspected and confirmed all 10 application domain tables in MySQL.

### [x] Stage 3: Spring Boot Backend Startup (Port 5001) (Completed & Running)
- [x] Set session environment variables (`MYSQL_PASSWORD`, `JWT_SECRET_KEY`, `PAYMENT_MODE=simulation`).
- [x] Launched backend: `.\mvnw.cmd spring-boot:run`.
- [x] Verified health probe: `GET http://localhost:5001/api/health` returns `healthy` and `database: connected`.

### [x] Stage 4: React Frontend Startup (Port 5174) & Handshake (Completed & Running)
- [x] Checked API base URL configuration (defaults to `http://localhost:5001`).
- [x] Launched frontend: `npm run dev` on `http://localhost:5174`.
- [x] Verified CORS preflight handling and API client request configuration.
- [x] Completed Frontend-Backend Integration Audit (42/42 endpoint parity).

### [ ] Stage 5: Core End-to-End Persona Workflows (Tomorrow Morning's Task)
- [ ] **Resolution:** Decide hotel creation policy (`@PreAuthorize("hasAnyRole('USER', 'HOTEL_OWNER', 'ADMIN')")` vs manual user role elevation to `HOTEL_OWNER`).
- [ ] **E2E-01:** Guest registration (token saved, cookie set, redirected to home).
- [ ] **E2E-02:** Guest login & session persistence (`/api/auth/validate-token`).
- [ ] **E2E-03:** Hotel search & filters (destination, dates, price, stars, facilities).
- [ ] **E2E-04:** Hotel detail view (descriptions, pricing, review summary).
- [ ] **E2E-05:** Owner property creation (`/add-hotel`).
- [ ] **E2E-06:** Owner hotel status toggle (`/my-hotels` active switch).
- [ ] **E2E-07:** Booking checkout (stay calculation, simulation payment, confirmed status).
- [ ] **E2E-08:** Guest my-bookings list (`/my-bookings`).
- [ ] **E2E-09:** Booking cancellation & refund calculation.
- [ ] **E2E-10:** Verified stay review submission & rating aggregation recalculation.
- [ ] **E2E-11:** Admin dashboard & manual analytics snapshot capture.
- [ ] **E2E-12:** Analytics dashboard charts & AI copy assist.

### [ ] Stage 6: Live Stripe Test-Mode Verification
- [ ] Switch `$env:PAYMENT_MODE="stripe"` with `STRIPE_API_KEY=sk_test_...`.
- [ ] Configure `VITE_STRIPE_PUB_KEY=pk_test_...` in frontend.
- [ ] Test real Stripe checkout with standard test card `4242 4242 4242 4242`.
- [ ] Test cancellation and verified refund via Stripe API.

### [ ] Stage 7: Live Cloudinary Image Upload Verification
- [ ] Configure `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`.
- [ ] Upload 2–3 photos during hotel creation/editing.
- [ ] Confirm images load via Cloudinary CDN URLs in search and detail pages.

### [ ] Stage 8: Defect Triaging & Final Documentation
- [ ] Log any defects found; resolve exclusively on Spring Boot backend.
- [ ] Confirm frontend and original Node.js backend remain untouched.
- [ ] Complete final deployment checklist.
