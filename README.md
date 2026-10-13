# eHarvest

eHarvest is a Spring Boot API for a farm-to-market platform, connecting buyers, farmers, and logistics providers through produce listings, orders, payments, delivery workflows, subscriptions, messaging, notifications, and operational reports.

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Architecture and Patterns](#architecture-and-patterns)
- [Data Models](#data-models)
- [Services and API Layer](#services-and-api-layer)
- [Navigation and Routing](#navigation-and-routing)
- [Admin Panel](#admin-panel)
- [Configuration](#configuration)
- [Getting Started](#getting-started)
- [Backend Requirements](#backend-requirements)
- [Deployment](#deployment)
- [Testing](#testing)

## Features

### Buyers

- Register and manage buyer profiles.
- Browse and filter produce listings and market locations.
- Create and manage orders and order items.
- Propose, accept, or reject transport fees.
- Initiate Paynow payments and track transaction history.
- Hold and release order escrow through the order lifecycle.
- Create, pause, resume, cancel, and process recurring subscriptions.
- Review farmers and logistics providers after orders.
- File and view personal dispute reports.
- Use conversations and read notifications.
- Download role-authorized PDF reports.

### Farmers

- Register and manage farmer profiles.
- Create, update, search, and delete produce listings.
- Upload produce images and expose produce location data.
- Accept or reject orders and manage order status.
- View subscriptions, reviews, disputes, and notifications.
- Use trust scores supplied by the external trust-score service.
- Download role-authorized supplier, SKU, review, and sales reports.

### Logistics providers

- Register and manage logistics-provider profiles.
- Manage vehicles and vehicle images.
- Accept, reject, start, and complete logistics requests.
- Update delivery locations while an order is in transit.
- View the last known delivery location.
- Hold or release logistics escrow.
- Use STOMP/WebSocket destinations for live tracking and chat.
- Download delivery and fulfillment reports.

### Platform and operations

- JWT access tokens plus persisted refresh tokens.
- User verification through configured notification channels.
- Firebase Cloud Messaging device-token registration and push delivery.
- Vonage REST SMS delivery.
- Thymeleaf and OpenHTMLtoPDF PDF report generation.
- Scheduled processing of due subscriptions at 06:00 server time.
- PostgreSQL persistence with JPA/Hibernate and Flyway migration support.

## Tech Stack

| Concern | Package/Tool | Version |
| --- | --- | --- |
| Language | Java | 21 |
| Application framework | Spring Boot | 3.5.7 |
| HTTP API | `spring-boot-starter-web` | Managed by Spring Boot |
| Persistence | Spring Data JPA / Hibernate | Managed by Spring Boot |
| Database | PostgreSQL JDBC driver | Managed by Spring Boot |
| Security | Spring Security | Managed by Spring Boot |
| Authentication | JJWT (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`) | 0.11.5 |
| Schema migrations | Flyway core and PostgreSQL support | 10.22.0 for PostgreSQL module |
| Real-time transport | Spring WebSocket with STOMP and SockJS | Managed by Spring Boot |
| Templates | Thymeleaf | Managed by Spring Boot |
| PDF rendering | OpenHTMLtoPDF PDFBox and SLF4J | 1.0.10 |
| Push notifications | Firebase Admin SDK | 9.2.0 |
| Boilerplate reduction | Lombok | Managed by Spring Boot |
| Build | Maven Wrapper | Included in repository |
| Runtime image | Eclipse Temurin JDK/JRE | 21, Jammy |
| CI/CD | GitHub Actions and Docker Hub | Workflow in `.github/workflows/deploy.yml` |

## Project Structure

```text
.
├── pom.xml                         # Maven dependencies, Java version, and build plugins
├── mvnw / mvnw.cmd                 # Unix and Windows Maven wrapper entry points
├── Dockerfile                      # Multi-stage Java 21 container build
├── .env.production.example         # Production environment template; currently empty
├── .github/workflows/deploy.yml    # Build, publish, and VM restart workflow
├── config/
│   ├── firebase-service-account.json.template # Firebase credential shape/template
│   └── firebase-service-account.json          # Local Firebase service-account file
├── src/main/java/com/munashechipanga/eharvest/
│   ├── EharvestApplication.java    # Spring Boot entry point and scheduling enablement
│   ├── configs/                    # Security, CORS, WebSocket, Firebase, and app config
│   ├── controllers/                # REST and STOMP endpoint adapters
│   ├── dtos/                       # Request, response, filter, chat, and location DTOs
│   ├── entities/                   # JPA domain models
│   ├── enums/                      # Order, logistics, notification, currency, and review states
│   ├── exceptions/                 # Resource errors and global exception handling
│   ├── heatmap/                    # Supply heatmap domain and endpoint
│   ├── market/                     # Market entity, repository, and seed data
│   ├── repositories/               # Spring Data repositories
│   ├── reports/                    # Report registry, PDF service, DTOs, and endpoints
│   ├── security/                   # JWT creation, parsing, and request filtering
│   ├── services/                   # Business services and external clients
│   │   ├── notifications/          # Notification sender abstractions
│   │   └── payments/               # Paynow client and payment response types
│   ├── specs/                      # JPA Specifications for filtered searches
│   └── utils/                      # Mapping, paging, and HTTP request examples
├── src/main/resources/
│   ├── application.properties      # Database, integration, storage, and server settings
│   ├── firebase.json               # Firebase resource configuration
│   └── templates/reports/          # Thymeleaf report templates and shared CSS
├── src/test/java/                  # Unit and integration-style service/controller tests
└── target/                         # Maven build output; generated and not source code
```

## Architecture and Patterns

- **Layered Spring architecture:** controllers translate HTTP or STOMP messages, services own business workflows, repositories persist JPA entities, and DTOs define API payloads.
- **Joined user inheritance:** `Buyer`, `Farmer`, and `LogisticsProvider` extend `User` using JPA joined inheritance.
- **Stateless JWT security:** `/auth/**` issues access and refresh tokens. The `JwtFilter` validates bearer tokens and reloads authorities through the local `UserDetailsService`. An `X-API-KEY` matching `ai.api.key` is also accepted as an admin-like identity.
- **Role-based access:** public registration, authentication, heatmap, WebSocket handshakes, and Paynow callbacks are explicitly permitted; admin and user management routes require admin authority; remaining routes require authentication.
- **Order and escrow lifecycle:** orders move through status transitions while order and logistics records track `escrowHeld`, `escrowAmount`, and release state. Logistics endpoints expose accept, reject, in-transit, delivered, hold-escrow, and release-escrow operations.
- **Real-time transport:** STOMP uses `/ws` and `/ws/tracking` SockJS endpoints, `/app` application destinations, `/topic` and `/queue` brokers, and `/user` destinations. Tracking updates are broadcast to `/topic/tracking/{orderId}`.
- **Scheduled work:** `SubscriptionScheduler` runs daily at 06:00 and processes active subscriptions whose next delivery date has passed.
- **External service adapters:** Paynow uses form-encoded HTTPS REST calls with response hash validation; trust scores use Spring `RestClient`; SMS uses the Vonage REST API; push notifications use Firebase Admin SDK.
- **Reporting pipeline:** report descriptors define allowed roles and parameters, services query the domain, Thymeleaf renders HTML, and OpenHTMLtoPDF returns a PDF download.
- **File storage:** uploaded files are sanitized, assigned UUID-prefixed names, stored under `storage.base-dir`, and returned as `/uploads/{filename}` paths.

## Data Models

| Model | File | Key fields |
| --- | --- | --- |
| User | `entities/User.java` | Identity, contact, role, active/verified flags, trust score, USD/ZiG balances |
| Buyer | `entities/Buyer.java` | Company name, successful and unsuccessful buys |
| Farmer | `entities/Farmer.java` | Farm name/location, successful and unsuccessful sales |
| LogisticsProvider | `entities/LogisticsProvider.java` | License number, defensive ID |
| Produce | `entities/Produce.java` | Name, category, quality, quantity, price, location, dates, farmer, market |
| ProduceImage | `entities/ProduceImage.java` | Produce image association and URL |
| Order | `entities/Order.java` | Buyer, farmer, date, amount, status, currency, logistics, escrow state |
| OrderItem | `entities/OrderItem.java` | Order, produce, quantity, price |
| LogisticsRequest | `entities/LogisticsRequest.java` | Pickup/delivery locations, status, cost, provider, escrow state |
| Vehicle | `entities/Vehicle.java` | Plate number, type, colour, owner, images |
| VehicleImage | `entities/VehicleImage.java` | Vehicle image association and URL |
| DeliveryLocation | `entities/DeliveryLocation.java` | Order, provider, latitude, longitude, heading, speed, update time |
| TransactionHistory | `entities/TransactionHistory.java` | Amount, status, reference, type, currency, provider, order and user links |
| Subscription | `entities/Subscription.java` | Buyer, farmer, status, frequency, dates, logistics requirement, currency, items |
| SubscriptionItem | `entities/SubscriptionItem.java` | Subscription and produce quantity/price details |
| Review | `entities/Review.java` | Rating, comment, status, order, reviewer, reviewee |
| DisputeReport | `entities/DisputeReport.java` | Description, filed-by/against users, attended flag, creation time |
| Conversation | `entities/Conversation.java` | Name, group flag, members, messages, creation time |
| ConversationMember | `entities/ConversationMember.java` | Conversation and participating user |
| Message | `entities/Message.java` | Conversation, sender, content, creation/deletion time |
| Notification | `entities/Notification.java` | User, title/message, type, channel, status, read time |
| DeviceToken | `entities/DeviceToken.java` | User device token and active state |
| RefreshToken | `entities/RefreshToken.java` | User, token, expiry, revoked flag, creation time |
| VerificationToken | `entities/VerificationToken.java` | User, verification code, channel, expiry/usage state |
| Market | `market/Market.java` | City, latitude, longitude |

## Services and API Layer

| Service | Transport | Purpose |
| --- | --- | --- |
| Auth and JWT | JSON REST plus bearer JWT | Registration, login, refresh, logout, and verification |
| Buyer, farmer, provider, user services | JSON REST | Profile and role-specific CRUD/search workflows |
| Produce and vehicle services | JSON REST and multipart upload | Listings, filters, and image storage |
| Order and order-item services | JSON REST | Commerce lifecycle, order items, transport fee, and escrow transitions |
| Logistics and tracking | JSON REST plus STOMP/WebSocket | Delivery assignment, state changes, and live location updates |
| Payment and transactions | JSON REST plus Paynow HTTPS REST | Deposit initiation, callbacks, and transaction history |
| Subscriptions | JSON REST plus scheduled processing | Recurring produce orders and delivery processing |
| Chat | STOMP/WebSocket plus JSON REST | Conversations, messages, read state, and deletion |
| Notifications | JSON REST, Firebase, and Vonage REST | In-app notifications, device tokens, push, SMS, and verification |
| Trust score | Outbound JSON REST | Reads `/trust-score/{userId}` from the configured trust-score API |
| Reports | JSON REST returning PDF | Role-filtered report discovery and PDF generation |
| Heatmap | JSON REST | Public supply heatmap data |

## Navigation and Routing

This is an API backend, so navigation is represented by resource and real-time routes rather than browser pages.

```text
/auth
├── POST /register
├── POST /login
├── POST /refresh
├── POST /logout
└── POST /verification/{request,confirm}

/api/v1
├── /buyers                  # buyer CRUD and search
├── /farmers                 # farmer CRUD and search
├── /logistics-providers     # provider CRUD and search
├── /produce                 # listings, filters, and image uploads
├── /orders                  # order lifecycle, transport fees, and escrow
├── /order_items             # order-item CRUD and order lookup
├── /payments                # Paynow initiation, return, and webhook
├── /transactions            # transaction-history CRUD
├── /logistics               # delivery request lifecycle and escrow
├── /vehicles                # vehicle CRUD and image operations
├── /tracking                # location update and last-known location
├── /subscriptions           # recurring order lifecycle
├── /reviews                 # ratings and order reviews
├── /dispute-reports         # create and list own reports
├── /notifications            # list and mark read
├── /chat                    # conversations, messages, and STOMP chat
├── /heatmap/supply          # public supply heatmap
├── /users                   # admin user management
└── /admin                   # admin dashboard and cross-domain management

/api/reports
├── GET /available
└── GET /generate/{reportName}?from=YYYY-MM-DD&to=YYYY-MM-DD

/ws
├── /ws                    # SockJS/STOMP chat endpoint
└── /ws/tracking           # SockJS/STOMP tracking endpoint
		├── /app/chat.send
		├── /app/location
		└── /topic/tracking/{orderId}
```

## Admin Panel

There is no separate frontend admin panel in this repository. The backend exposes the following admin-only API scope under `/api/v1/admin`:

| Admin area | Scope |
| --- | --- |
| Dashboard | Aggregate dashboard statistics |
| Users | List, inspect, create, update, delete, activate, and verify users |
| Farmers | Farmer CRUD and filters |
| Buyers | Buyer CRUD and filters |
| Logistics providers | Provider CRUD and filters |
| Produce | Produce CRUD and filters |
| Vehicles | Vehicle CRUD and filters |
| Orders and order items | Order and item CRUD and lookup |
| Reviews | Review CRUD and moderation data |
| Logistics | Logistics request CRUD |
| Subscriptions | Subscription lookup, create, update, and cancellation |
| Dispute reports | Review, mark attended, and delete reports |
| Transactions | Transaction listing |

## Configuration

The default configuration is in `src/main/resources/application.properties`. Set production values through Spring properties or environment-variable-backed properties before deployment. Do not commit real credentials; the checked-in defaults include development credentials and should be rotated.

| Property / environment variable | Purpose | Example |
| --- | --- | --- |
| `SPRING_DATASOURCE_URL` / `spring.datasource.url` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/eharvest` |
| `SPRING_DATASOURCE_USERNAME` / `spring.datasource.username` | Database user | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` / `spring.datasource.password` | Database password | `change-me` |
| `AI_API_KEY` / `ai.api.key` | AI-service header key accepted by `JwtFilter` | `replace-me` |
| `TRUST_SCORE_API_BASE_URL` / `trust-score.api.base-url` | Trust-score service base URL | `http://localhost:8000` |
| `STORAGE_BASE_DIR` / `storage.base-dir` | Upload directory | `uploads` |
| `PAYNOW_INTEGRATION_ID` / `paynow.integration.id` | Paynow integration ID | `24589` |
| `PAYNOW_INTEGRATION_KEY` / `paynow.integration.key` | Paynow integration key | `replace-me` |
| `PAYNOW_RETURN_URL` / `paynow.return-url` | Paynow browser return callback | `http://localhost:8080/api/v1/payments/return?reference={reference}` |
| `PAYNOW_RESULT_URL` / `paynow.result-url` | Paynow server webhook | `http://localhost:8080/api/v1/payments/webhook` |
| `VONAGE_API_KEY` / `vonage.api-key` | Vonage SMS API key | `replace-me` |
| `VONAGE_API_SECRET` / `vonage.api-secret` | Vonage SMS API secret | `replace-me` |
| `VONAGE_FROM_NUMBER` / `vonage.from-number` | SMS sender number | `+263...` |
| `FIREBASE_SERVICE_ACCOUNT_PATH` / `firebase.service-account-path` | Firebase JSON credential path | `config/firebase-service-account.json` |
| `COOKIE_SECURE` / `server.servlet.session.cookie.secure` | Secure session-cookie flag | `true` |
| `COOKIE_SAME_SITE` / `server.servlet.session.cookie.same-site` | SameSite cookie policy | `none` |
| `JAVA_OPTS` | JVM options in the Docker image | `-Xmx512m -Xms256m` |

Example PowerShell setup:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/eharvest"
$env:SPRING_DATASOURCE_USERNAME = "postgres"
$env:SPRING_DATASOURCE_PASSWORD = "change-me"
$env:FIREBASE_SERVICE_ACCOUNT_PATH = "config/firebase-service-account.json"
```

Copy a valid Firebase service-account JSON to `config/firebase-service-account.json` when push notifications are required. Firebase initialization is skipped when the configured file is absent.

## Getting Started

1. Install Java 21 and ensure `java -version` reports the expected JDK.
2. Install or run PostgreSQL and create the `eharvest` database.
3. Clone the repository and enter the project directory.
4. Configure the database and external service properties described above. For a minimal local start, PostgreSQL is required; Paynow, Firebase, Vonage, and trust-score integrations are only needed for the workflows that use them.
5. Start the API with the Maven wrapper:

```powershell
.\mvnw.cmd spring-boot:run
```

6. The HTTP and WebSocket server listens on port `8080` by default.
7. Register or create a user, authenticate through `/auth/login`, and send the returned access token as `Authorization: Bearer <token>` on protected routes.
8. Build and test locally with:

```powershell
.\mvnw.cmd clean test
```

The repository contains no frontend asset pipeline or asset-generation command. Report templates and CSS are already committed under `src/main/resources/templates/reports/`.

## Backend Requirements

| Area | Methods and paths |
| --- | --- |
| Authentication | `POST /auth/register`, `/auth/login`, `/auth/refresh`, `/auth/logout`, `/auth/verification/request`, `/auth/verification/confirm` |
| Buyers | `GET/POST /api/v1/buyers`, `GET/PUT/DELETE /api/v1/buyers/{id}` |
| Farmers | `GET/POST /api/v1/farmers`, `GET/PUT/DELETE /api/v1/farmers/{id}` |
| Logistics providers | `GET/POST /api/v1/logistics-providers`, `GET/PUT/DELETE /api/v1/logistics-providers/{id}` |
| Users | `GET/POST /api/v1/users`, `GET/PUT/DELETE /api/v1/users/{id}` |
| Produce | `GET/POST /api/v1/produce`, `GET/PUT/DELETE /api/v1/produce/{id}`, `POST /api/v1/produce/images`, `POST /api/v1/produce/{id}/images` |
| Orders | `GET /api/v1/orders`, `/orders/{id}`, `/orders/farmer/{farmerId}`, `/orders/buyer/{buyerId}`; `POST /api/v1/orders`, `/orders/{id}/{accept,reject,cancel,hold-escrow,delivery-started,delivery-confirmed,propose-transport-fee,accept-transport-fee,reject-transport-fee}`; `PUT/DELETE /api/v1/orders/{id}` |
| Order items | `GET /api/v1/order_items`, `/order_items/{id}`, `/order_items/order/{id}`; `POST /api/v1/order_items`; `PUT/DELETE /api/v1/order_items/{id}` |
| Payments | `POST /api/v1/payments/init`, `POST /api/v1/payments/webhook`, `GET /api/v1/payments/return` |
| Transactions | `GET/POST /api/v1/transactions`, `GET/PUT/DELETE /api/v1/transactions/{id}` |
| Logistics | `GET/POST /api/v1/logistics`, `GET/PUT/DELETE /api/v1/logistics/{id}`, `GET /api/v1/logistics/order/{orderId}`, `POST /api/v1/logistics/{id}/{accept,reject,in-transit,delivered,hold-escrow,release-escrow}` |
| Vehicles | `GET/POST /api/v1/vehicles`, `GET/PUT/DELETE /api/v1/vehicles/{id}` |
| Tracking | `POST /api/v1/tracking/location`, `GET /api/v1/tracking/{orderId}`, STOMP `/app/location` |
| Subscriptions | `POST /api/v1/subscriptions`, `GET /api/v1/subscriptions/{id}`, `/subscriptions/buyer/{buyerId}`, `/subscriptions/farmer/{farmerId}`, `PUT /api/v1/subscriptions/{id}`, `POST /api/v1/subscriptions/{id}/{cancel,pause,resume,process}` |
| Reviews | `GET /api/v1/reviews`, `/reviews/{id}`, `/reviews/reviewer/{reviewerId}`, `/reviews/reviewee/{revieweeId}`, `/reviews/pending/reviewer/{reviewerId}`, `/reviews/order/{id}`; `POST /api/v1/reviews`; `PUT/DELETE /api/v1/reviews/{id}` |
| Disputes | `POST /api/v1/dispute-reports`, `GET /api/v1/dispute-reports/mine` |
| Notifications | `GET /api/v1/notifications`, `POST /api/v1/notifications/{id}/read`, `POST /api/notifications/register-token`, `DELETE /api/notifications/deactivate-token` |
| Chat | `POST /api/v1/chat/conversations`, `GET /api/v1/chat/conversations`, `GET /api/v1/chat/conversations/{conversationId}/messages`, `POST /api/v1/chat/conversations/{conversationId}/read`, `DELETE /api/v1/chat/messages/{messageId}`, STOMP `/app/chat.send` |
| Heatmap | `GET /api/v1/heatmap/supply` |
| Reports | `GET /api/reports/available`, `GET /api/reports/generate/{reportName}?from=YYYY-MM-DD&to=YYYY-MM-DD` |
| Admin | `/api/v1/admin/dashboard/stats` plus admin CRUD routes for users, farmers, buyers, logistics providers, produce, vehicles, orders, order items, reviews, logistics, subscriptions, disputes, and transactions |

## Deployment

Build and run the container locally:

```bash
docker build -t eharvest:local .
docker run --rm -p 8080:8080 \
	-e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/eharvest \
	-e SPRING_DATASOURCE_USERNAME=postgres \
	-e SPRING_DATASOURCE_PASSWORD=change-me \
	eharvest:local
```

The Dockerfile builds with Eclipse Temurin 21, skips tests during image creation, creates `/app/uploads`, exposes port `8080`, and checks `/actuator/health`. The project does not contain a `docker-compose.yml`; the GitHub deployment workflow expects one to already exist on the target VM.

On pushes to `main`, `.github/workflows/deploy.yml` checks out the code, logs in to Docker Hub, publishes `munacips/eharvest:latest` and a SHA tag, then SSHes to an AWS VM and recreates the `backend` Compose service. Configure these GitHub secrets: `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`, `AWS_HOST`, `AWS_USERNAME`, and `SSH_PRIVATE_KEY`.

For production, provide PostgreSQL separately, mount persistent storage for `/app/uploads`, supply the Firebase credential securely, use HTTPS for callbacks and clients, rotate all integration keys, and make the Paynow return/result URLs reachable by Paynow.

## Testing

Run the complete Maven test suite:

```powershell
.\mvnw.cmd test
```

The current tests cover application startup, order, logistics, tracking, review, notification/SMS, Paynow client, Paynow Ecocash integration, and report controller behavior under `src/test/java/com/munashechipanga/eharvest/`. Paynow integration tests may require external credentials or network access; keep provider secrets out of source control.
