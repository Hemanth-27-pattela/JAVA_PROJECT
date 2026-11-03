# Hotel Booking Management System — Java Spring Boot Backend

A modern, robust **Java Spring Boot 4 / MySQL** backend replacement for the Hotel Booking Management System API.

This backend serves the exact same REST API endpoints, JSON contracts, and authentication protocols expected by the existing `hotel-booking-frontend` Vite/React application, ensuring **100% frontend preservation** with zero changes to frontend code, CSS, or components.

---

## Prerequisites

1. **Java Development Kit (JDK):** JDK 21+ (Tested and verified with Oracle JDK 26 / JDK 21 release target)
2. **Apache Maven:** 3.9+ (Or run with the included `mvnw.cmd`)
3. **MySQL Database Server:** MySQL 8.0+ running on port `3306` (Local or cloud instance)

---

## Environment Variables Configuration

The backend uses environment variables for all sensitive configuration settings, with sensible defaults for local development:

| Variable | Default Value | Description |
|---|---|---|
| `PORT` or `SERVER_PORT` | `5001` | Server listening port (Must match frontend's target) |
| `MYSQL_URL` | `jdbc:mysql://localhost:3306/hotel_booking?...` | Full JDBC database URL |
| `MYSQL_HOST` | `localhost` | MySQL host address |
| `MYSQL_PORT` | `3306` | MySQL port |
| `MYSQL_DATABASE` | `hotel_booking` | Target database name |
| `MYSQL_USERNAME` | `root` | Database username |
| `MYSQL_PASSWORD` | *(empty)* | Database password |
| `JWT_SECRET_KEY` | *(Required in Prod)* | Strong secret key (min 32 characters / 256 bits) for HMAC-SHA256 JWT tokens. Application fails securely at startup if missing or < 32 characters. |
| `FRONTEND_URL` | `http://localhost:5174` | Allowed frontend origin for CORS |
| `CLOUDINARY_CLOUD_NAME` | *(empty)* | Cloudinary cloud name for hotel image uploads |
| `CLOUDINARY_API_KEY` | *(empty)* | Cloudinary API key |
| `CLOUDINARY_API_SECRET` | *(empty)* | Cloudinary API secret |

---

## How to Configure and Run

### 1. Configure MySQL Database
Before starting the application, ensure MySQL is running and create the database:

```sql
CREATE DATABASE IF NOT EXISTS hotel_booking;
```

If your MySQL user has a password, set it in your environment or PowerShell session:

```powershell
$env:MYSQL_USERNAME="root"
$env:MYSQL_PASSWORD="your_password"
```

### 2. Build and Test the Application

Run the build and automated test suite:

```powershell
cd hotel-booking-backend-spring
.\mvnw.cmd clean test
```

*Note: The test suite runs against an isolated in-memory H2 database (`application-test.properties`), meaning unit and integration tests execute successfully without needing an active MySQL instance.*

### 3. Run the Backend Server

Start the Spring Boot application on port `5001`:

```powershell
.\mvnw.cmd spring-boot:run
```

Once started, verify:
* **Root API check:** [http://localhost:5001/](http://localhost:5001/)
* **Health Check Probe:** [http://localhost:5001/api/health](http://localhost:5001/api/health)
* **Detailed Health Probe:** [http://localhost:5001/api/health/detailed](http://localhost:5001/api/health/detailed)
* **Interactive Swagger API Docs:** [http://localhost:5001/api-docs](http://localhost:5001/api-docs)

---

## Architecture & Security Implementation (Phase 2 & Phase 3)

* **Domain & Persistence (Phase 2):**
  * JPA entities: `User`, `Hotel`, `Booking`, `Review`, `AnalyticsSnapshot`.
  * Automatic schema migration via Flyway (`V1__init_schema.sql`) and Spring Data JPA repositories.
  * Serializes primary keys as `_id` to maintain 100% compatibility with frontend React TypeScript definitions (`shared/types.ts`).

* **Authentication & Authorization (Phase 3):**
  * **Stateless JWT Security:** Generates HMAC-SHA256 tokens using `io.jsonwebtoken` (jjwt 0.12.6). Fails securely on startup if `JWT_SECRET_KEY` is absent or shorter than 256 bits.
  * **Role-Based Access Control:** Distinguishes `USER`, `HOTEL_OWNER`, and `ADMIN`. Public registration strictly assigns `USER`.
  * **Ownership Authorization:** `HotelSecurityService` verifies hotel ownership for management operations.
  * **Unified Exception & Security Handling:** Returns consistent JSON errors:
    * 401 Unauthorized: `{"message": "unauthorized"}`
    * 403 Forbidden: `{"message": "Access denied"}`
    * 400 Bad Request: `{"message": "Invalid Credentials"}`
  * **Logout Semantics:** Clearing the client-side token (and clearing the `auth_token` cookie) discards credentials locally. In a pure stateless JWT model, tokens remain cryptographically valid until expiration (`app.jwt.expiration-ms`). If real-time revocation is required, a Redis-backed token blacklist or short-lived tokens with refresh token rotation can be added.
  * **Strict CORS:** Restricts origin access strictly to `http://localhost:5174` and `http://localhost:5173` with credentials enabled (no wildcard origins).

* **Hotel Management, Search & Cloudinary Upload (Phase 4):**
  * **Public Search & Discovery:** `GET /api/hotels/search` supports multi-attribute filtering (destination match across city/country, capacity for adults/children, price ceilings, star ratings, hotel types, and all-facilities requirements) with dynamic JPA specifications, multi-mode sorting (`starRating`, `pricePerNightAsc`, `pricePerNightDesc`, `lastUpdated`), and pagination (page size = 5).
  * **Hotel Management (My Hotels):** `POST /api/my-hotels` and `PUT /api/my-hotels/:id` handle multipart form uploads and complex indexed parameters (`type[0]`, `facilities[0]`, `contact.phone`, `policies.checkInTime`, `imageFiles`).
  * **Ownership Protection:** Ensures operations verify that `authenticatedUserId == hotel.userId` (or `ROLE_ADMIN`). Non-owners receive HTTP 403.
  * **Owner Dashboard Enrichment:** `GET /api/my-hotels` enriches hotels with live booking counters (`upcomingBookings`, `completedBookings`, `cancelledBookings`) and review ratings (`averageRating`, `reviewCount`).
  * **Cloudinary Image Upload:** `CloudinaryImageUploadService` uploads up to 6 images per hotel with auto-scaling (800x600 fill, auto quality) and validates image type and 5MB size limits. Falls back safely when unconfigured for testing.

* **Booking Management (Phase 5):**
  * **Room Booking Creation:** `POST /api/hotels/:hotelId/bookings` handles reservation creation with strict date checking (`checkIn < checkOut`), guest capacity enforcement, and overlapping booking prevention (rejects concurrent duplicate reservations for the same hotel by the same user).
  * **Payment Intent & Checkout:** `POST /api/hotels/:hotelId/bookings/payment-intent` calculates total stay cost based on nightly rates and prepares client secrets for frontend checkout.
  * **Guest Bookings:** `GET /api/my-bookings` returns the authenticated user's reservations wrapped in `HotelWithBookingsDto` with full hotel details and nested booking objects.
  * **Hotel Owner & Admin Management:**
    * `GET /api/bookings` lists all system bookings (Admin only).
    * `GET /api/bookings/hotel/:hotelId` lists bookings for a specific hotel (Hotel Owner or Admin).
    * `GET /api/bookings/:id` allows the guest owner, hotel owner, or admin to inspect details.
    * `PATCH /api/bookings/:id/status` & `PATCH /api/bookings/:id/payment` allow owners and admins to update operational and payment statuses.
    * `DELETE /api/bookings/:id` allows admins to remove bookings and adjusts business analytics.
  * **Cancellation & Refunds:** `POST /api/bookings/:id/cancel` enables cancellation for upcoming stays (`checkIn > now`), computes refunds when paid, sets status to `cancelled`/`refunded`, and automatically decrements hotel and user revenue and booking statistics.

