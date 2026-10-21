# Payments Service

A checkout and payment orchestration service for a multi-seller
marketplace. A buyer builds a cart, checks out, and is redirected to a
hosted payment page. The service never handles card data: it creates a
payment session with a Stripe-shaped [payment service provider](https://github.com/shnk185/payment-service-provider) 
, then learns
the outcome from signed webhooks. MySQL is the source of truth.

The payment provider is a separate service. This repository contains the
merchant side only: carts, orders, payments, and the webhook receiver.

## Stack

- Java 17, Spring Boot 4
- MySQL 8
- Resilience4j (retry + circuit breaker on the provider call)
- JWT bearer auth

## Requirements

* java 17+
* docker 27+ (for MySQL)
* a running payment service provider on `http://localhost:8085`

## Setup instructions

```
git clone <this-repo>
cd payments
docker compose up -d
./mvnw spring-boot:run
```

The app starts on **http://localhost:8081**.

The webhook receiver signs nothing; it verifies. Its shared secret must match
the provider's:

```
payments.webhook.secret=whsec_local_dev_secret
payments.webhook.tolerance-seconds=300
```

## API summary

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/users` | Public | Create a buyer or seller |
| POST | `/users/sessions` | Public | Log in, returns a bearer token |
| POST | `/items` | Seller | Create an item and its inventory row |
| GET | `/items?name=&page=&size=` | Logged in | Paginated item listing with availability |
| POST | `/carts` | Buyer | Create a cart |
| PUT | `/carts/{cartId}/items` | Buyer | Add an item or set its quantity |
| GET | `/carts/{cartId}/items` | Buyer | Fetch the cart with line totals |
| POST | `/carts/{cartId}/checkout` | Buyer, `Idempotency-Key` | Create the payment and per-seller orders, reserve inventory |
| PUT | `/payments/{paymentUniqueId}` | Buyer | Create the provider checkout session, return its URL |
| POST | `/webhooks/psp` | Public, HMAC signature | Receive `completed` / `payment_failed` events |

Typical flow:

```
→ POST /users, POST /users/sessions             (get bearer token)
→ POST /items                                   (seller)
→ POST /carts
→ PUT /carts/{cartId}/items
→ POST /carts/{cartId}/checkout                 (Idempotency-Key)
→ PUT /payments/{paymentUniqueId}               (returns hosted page URL)
→ buyer pays on the provider page
→ provider webhook → payment SUCCESS
```

## Design decisions

* Payment is split into payment → order → order item: One payment per
  checkout, one order per seller, one order item per purchased line. Each
  line stores a snapshot of the item name, unit cost, quantity, and line
  total, so a later catalog or cart change cannot alter what was bought.
  Checkout does not mutate or consume the cart.

* Idempotency is enforced by the database: `POST /carts/{cartId}/checkout`
  requires an `Idempotency-Key`, stored in a unique column on `payments`.
  Concurrent requests race on that index; the losing request catches the
  constraint violation and returns the payment created by the winner. The
  executor runs outside the service transaction so the violation surfaces
  instead of being swallowed by a rollback.

* Inventory is reserved atomically: Checkout decrements available quantity
  with a conditional `UPDATE ... WHERE available_quantity >= ?`. A zero-row
  result aborts the entire checkout, including the payment and orders.

* The provider session is created once and reused: `PUT /payments/{id}`
  sends the payment UUID as the provider's idempotency key, then persists
  the returned session id and hosted URL. A repeat call — double click or
  retry — returns the stored URL without a second provider request.

* Card data never reaches this service: The buyer enters card details on
  the provider's hosted page, which tokenizes them. This service handles
  only amounts, identifiers, and signed events.

* Authorization is asynchronous: `PUT /payments/{id}` returns the hosted
  page URL immediately. The provider authorizes in the background and
  notifies via webhook; this service never polls for status.

* Webhooks are verified before they are trusted: The raw body is read
  as bytes, HMAC-SHA256 is recomputed over `<timestamp>.<body>` with the
  shared secret, and the comparison is constant time. Timestamps outside a
  five-minute window are rejected. Deserialization happens only after the
  signature passes.

* A decline is not terminal: `payment_failed` records the error code and
  message on the payment but leaves it `EXECUTING`, because the buyer can
  retry on the same provider session. A later `completed` clears the error
  and settles the payment. `SUCCESS` is terminal, so a late failure cannot
  overwrite it.

* Abandoned payments are swept: A scheduled job moves `EXECUTING` payments
  past `expires_at`, plus a grace period for webhook lag, to `FAILED`,
  together with their orders, so a buyer who never completes does not leave
  a payment in flight forever.

* The provider call is protected: Resilience4j retries transient failures
  and opens the circuit when the provider is unhealthy, so checkout fails
  fast rather than hanging on a dead dependency.

* Soft delete preserves history: Rows are flagged `deleted` rather than
  removed, so payments, orders, and their line snapshots remain auditable.
