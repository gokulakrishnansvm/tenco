# TENCO 🥥

**TEN**der **CO**conut supply-chain platform — a mobile-first app connecting **Suppliers** and
**Local Vendors** in the tender-coconut trade, with localized (multi-language) UX and UPI payments.

- **Android app** (Phase 1): Kotlin + Jetpack Compose, offline-first, local-only MVP.
- **Backend** (Phase 2): Kotlin + Spring Boot 3, JWT phone-OTP auth, REST + delta sync, Razorpay
  payment intent + signed webhook.

See the full design in **[`docs/DESIGN.md`](docs/DESIGN.md)**.

---

## Repository layout

```
tenco/
├── docs/DESIGN.md          # High-level design (architecture, data model, UPI, roadmap)
├── app/                    # Android app (Kotlin + Jetpack Compose)
├── backend/                # Spring Boot backend (Kotlin, JDK 21)
├── settings.gradle.kts     # Android Gradle build (root)
└── build.gradle.kts
```

---

## Features

### Supplier
- Dashboard: stock on hand, total earnings, dues receivable, losses, vendor-wise distribution
- Dealer management & purchases (Pollachi / Nellore / Theni)
- Vendor management, vendor-specific pricing
- Transactions ledger, Profit & Loss report
- Complaint handling → price adjustment logs the loss

### Vendor (mobile-first, low-literacy UX)
- Visual dashboard ("Received 50 @ ₹28", "Pending ₹1,200")
- One-tap delivery confirmation
- Raise complaint (reason + optional photo)
- Flexible **UPI payment** (full or partial) via `upi://pay` deep link, with success/fail capture
- Transaction history (color-coded status)
- Language toggle (English / தமிழ் / తెలుగు / हिन्दी)
- Call / WhatsApp the supplier

### Cross-cutting
- Phone-number **login** and **logout / role switch**
- Vendor login binds the session to the vendor whose phone matches the entered number

---

## Tech stack

| Layer | Choice |
|-------|--------|
| Android | Kotlin 2.0, Jetpack Compose (Material 3), Hilt, Room, Navigation-Compose, Coroutines/Flow, DataStore, Coil |
| Backend | Kotlin, Spring Boot 3.3 (Web, Data JPA, Security, Validation), JWT (jjwt), H2 (dev) / PostgreSQL (prod) |
| Payments | UPI deep link (P1) → Razorpay Orders + signed webhook (P2/P3) |
| Build | Gradle (Kotlin DSL), JDK 21 |

---

## Prerequisites

- **JDK 21** (Amazon Corretto 21 recommended)
- **Android SDK** (platform 35) + an emulator or device, `adb` on PATH — for the app
- No global Gradle needed (the Gradle wrapper is included in both `app`-root and `backend/`)

---

## Android app — build & run

```bash
# from repo root
./gradlew :app:assembleDebug          # build the debug APK
./gradlew installDebug                # install onto a running emulator/device

# APK output:
#   app/build/outputs/apk/debug/app-debug.apk
```

First launch flow: **Language → Login (name + 10-digit phone) → Role → Dashboard**.
Demo data is seeded automatically on first run.

> `local.properties` (with `sdk.dir=...`) is generated locally and git-ignored. If missing, create it:
> `echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties`

---

## Backend — build & run

```bash
cd backend
./gradlew bootJar
java -jar build/libs/tenco-backend-0.1.0.jar      # starts on http://localhost:8080 (H2 in-memory)
```

Dev mode uses in-memory **H2** (no external DB needed) and seeds the same demo data.
For production, run with the `prod` profile and Postgres:

```bash
SPRING_PROFILES_ACTIVE=prod \
DB_URL=jdbc:postgresql://localhost:5432/tenco DB_USER=tenco DB_PASSWORD=*** \
TENCO_JWT_SECRET=<32+ byte secret> \
RAZORPAY_KEY_ID=... RAZORPAY_KEY_SECRET=... RAZORPAY_WEBHOOK_SECRET=... \
java -jar build/libs/tenco-backend-0.1.0.jar
```

### API quick reference

| Method & path | Auth | Purpose |
|---------------|------|---------|
| `POST /auth/otp/request` | – | Request an OTP (dev-mode returns `devOtp`) |
| `POST /auth/otp/verify` | – | Verify OTP → `{ accessToken, refreshToken, role }` |
| `GET /api/suppliers/me/dashboard` | Bearer | Supplier dashboard aggregates |
| `GET /api/vendors/{id}/dashboard` | Bearer | Vendor dashboard aggregates |
| `GET /api/reports/pnl` | Bearer | Profit & Loss |
| `GET/POST /api/dealers`, `/api/purchases` | Bearer | Dealers & stock-in |
| `GET/POST /api/vendors`, `PUT /api/prices` | Bearer | Vendors & pricing |
| `POST /api/deliveries`, `POST /api/deliveries/{id}/confirm` | Bearer | Deliveries |
| `GET/POST /api/complaints`, `PUT /api/complaints/{id}/resolve` | Bearer | Complaints |
| `POST /api/payments/intent` | Bearer | Create payment intent (order + UPI link) |
| `POST /webhooks/pg` | HMAC sig | Gateway webhook (signature-verified) |
| `GET /api/sync/changes?since=<ms>` | Bearer | Delta sync (pass 0 for full pull) |

Example login + call:
```bash
OTP=$(curl -s -X POST localhost:8080/auth/otp/request -H 'Content-Type: application/json' -d '{"phone":"+919876543210"}')
CODE=$(echo "$OTP" | sed -E 's/.*"devOtp":"([0-9]+)".*/\1/')
TOKEN=$(curl -s -X POST localhost:8080/auth/otp/verify -H 'Content-Type: application/json' \
  -d "{\"phone\":\"+919876543210\",\"code\":\"$CODE\",\"role\":\"SUPPLIER\"}" | sed -E 's/.*"accessToken":"([^"]+)".*/\1/')
curl -s localhost:8080/api/suppliers/me/dashboard -H "Authorization: Bearer $TOKEN"
```

---

## Roadmap

| Phase | Scope | Status |
|-------|-------|--------|
| **P1** | Local-first Android MVP (Compose, Room, i18n, UPI deep link) | ✅ Done |
| **P2** | Backend: auth (OTP/JWT), REST APIs, delta sync, Razorpay intent + signed webhook | ✅ Done |
| **P3** | App ↔ backend wiring: OTP login, authenticated sync, backend payment intent; real Razorpay Orders API (with fallback) + FCM push (credential-guarded) | ✅ Done (live keys/creds pending) |
| **P4** | Voice prompts (TTS), advanced reports (date-range P&L + CSV export/share), dispute workflows (OPEN/UNDER_REVIEW/RESOLVED/REJECTED), supplier analytics/insights | ✅ Done |

### Enabling live integrations (Phase 3)

**Real Razorpay** — provide test/live keys; the backend auto-switches from the stub to the real
Orders API when the keys are not `*dummy*`:
```bash
RAZORPAY_KEY_ID=rzp_test_xxx RAZORPAY_KEY_SECRET=xxx RAZORPAY_WEBHOOK_SECRET=xxx \
java -jar backend/build/libs/tenco-backend-0.1.0.jar
```
Point your Razorpay webhook at `POST /webhooks/pg` (events `payment.captured`, `payment.failed`).
The signature is verified with `RAZORPAY_WEBHOOK_SECRET`.

**FCM push** — the backend logs notifications until credentials are supplied:
```bash
TENCO_FCM_CREDENTIALS=/path/to/firebase-service-account.json \
java -jar backend/build/libs/tenco-backend-0.1.0.jar
```
On the **app**, enable FCM by adding `app/google-services.json` and applying the plugin:
```kotlin
// settings/root build: classpath "com.google.gms:google-services"
// app/build.gradle.kts:
plugins { id("com.google.gms.google-services") }
```
The `TencoMessagingService`, device-token registration (`/api/devices/register`), and
`POST_NOTIFICATIONS` permission are already wired.

### Known limitations (by design, this stage)
- Sync is currently **pull-only** (server → client via `/api/sync/changes`); an outbox/push-up
  path (client → server) is the remaining sync work.
- Razorpay Orders API call uses a **stub** until real keys are set (auto-detected).
- FCM **logs** notifications until Firebase credentials + `google-services.json` are added.
- Dev OTP is returned in the response and OTP challenges are stored in-memory — **dev only**.

---

## License
Internal project — not for redistribution.
