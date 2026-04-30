# Chat System — Production Design

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│  Clients (WebSocket / MQTT / Netty)                                        │
└─────────────────────────────────┬───────────────────────────────────────────┘
                                  │ L7 Consistent Hash (userId)
                                  ▼
┌─────────────────────────────────┴─────────────────────────────────────────┐
│  Gateway Cluster (stateful per pod)                                         │
│                                                                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐                       │
│  │  Pod 1       │  │  Pod 2       │  │  Pod N       │                       │
│  │  ChannelMap  │  │  ChannelMap  │  │  ChannelMap  │                       │
│  │  JWT Auth    │  │  JWT Auth    │  │  JWT Auth    │                       │
│  │  Heartbeat   │  │  Heartbeat   │  │  Heartbeat   │                       │
│  │  gRPC Client │  │  gRPC Client │  │  gRPC Client │                       │
│  └──────────────┘  └──────────────┘  └──────────────┘                       │
│                                                                             │
│  Session Registry (Redis): Gateway writes, ChatService reads only          │
└─────────────────────────────────┬───────────────────────────────────────────┘
                                  │ gRPC
                                  ▼
┌─────────────────────────────────┴─────────────────────────────────────────┐
│  ChatService (stateless)                                                   │
│                                                                             │
│  Pipeline: Decode → Validate → Authorize → Enrich                          │
│                                                                             │
│  Delivery Orchestrator:                                                     │
│    1. Persist (DynamoDB messages + Message_box)                            │
│    2. Return SENT to sender                                               │
│    3. Query Session Registry → online device list                         │
│    4. gRPC push (500ms timeout, 1-2 retries) → DELIVERED                    │
│    5. Offline / failed → Kafka → retry + Notification fallback            │
│                                                                             │
│  Hybrid Fan-out:                                                           │
│    Small group (≤ threshold): write fan-out to Message_box                 │
│    Large group (> threshold): read fan-out + conversation sharding         │
│                                                                             │
│  Storage: DynamoDB (messages, message_box) | MySQL (conversation, parts)   │
└─────────────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼ Kafka (async)
                        Offline Retry / Notification / Analytics
```

## Message Flow

```
1. Client → Gateway
   WebSocket connect, JWT in handshake. Gateway validates, extracts userId,
   registers channel in per-pod ChannelMap, UPSERTs session to Redis.

2. Gateway → ChatService (gRPC)
   Message forwarded as gRPC unary call with correlation_id in metadata.

3. ChatService Pipeline
   Decode (Protobuf) → Validate (schema + required fields) →
   Authorize (conversation membership + role) → Enrich (sequence_id, timestamp)

4. Delivery Orchestrator
   a. Persist to DynamoDB (messages table + Message_box with fan-out decision)
      — Must succeed; failure returns error to sender
   b. Return SENT to sender (single checkmark)
   c. Query Session Registry → recipient online device list
   d. gRPC push to each online device:
      - Success → recipient sees DELIVERED (double checkmark)
      - Timeout / failure → bounded retry (1-2 attempts), then treat as offline
   e. Offline / retry-exhausted → Kafka retry queue → async push → Notification RPC

5. READ (on recipient open):
   a. Update Message_box.is_read (and read_count for groups)
   b. Sync READ state to recipient's other online devices
   c. Notify sender's devices → sender sees "read" / "N read"
```

---

## Design Decisions

### 1. Gateway Stateful; ChatService Stateless

Gateway holds per-pod `ChannelMap (userId → List<Channel>)` for direct channel write on push. ChatService has zero session state — all routing reads from Redis Session Registry.

**Trade-off:** Gateway pods are stateful. On scale-in, wait for heartbeat timeout (~3-4 min) before terminating pods to avoid premature session expiry. Acceptable: brief window where a disconnected user's push goes to Kafka (harmless).

### 2. Consistent Hash at L7, Not L4

L4 load balancing (TCP) cannot inspect JWT/userId — it routes randomly. L7 (Envoy, NGINX Ingress) routes by `hash(userId)` → same user sticks to same pod. Reduces cross-pod traffic for single-device users.

**Pod failover:** Pod goes unhealthy → Ingress marks it down → client reconnects to different pod → re-authenticates → UPSERT new connectionId to Redis with new gatewayInstanceId → ChatService routes to new pod → client backfills missed messages via last_message_id.

### 3. Hybrid Fan-out: Write vs Read

| | Write Fan-out | Read Fan-out |
|---|---|---|
| Read latency | O(1) pre-written | O(N) on-demand |
| Write cost | O(N) per message | O(1) |
| Best for | Small groups | Large groups |

**Our choice:** Configurable threshold. Small groups: low read latency is worth the write cost. Large groups: avoids write amplification for N=1000+ members.

### 4. DynamoDB for Messages, MySQL for Metadata

DynamoDB: high write throughput for append-only messages, limited query patterns.
MySQL: rich relational queries, ACID transactions for participant management.

**Our choice:** Performance isolation — DynamoDB handles what it's best at (message append), MySQL handles what it does best (participant joins/leaves, role management).

**Hot partition mitigation:**
- `messages` table (PK=conversation_id): high-traffic viral convs use GSI on `sender_id#create_at_month`. No salt-based PK sharding (DynamoDB doesn't support it).
- `message_box` (PK=user_id): hot read users → DynamoDB DAX (in-memory cache) or read replicas. Write fan-out naturally distributes writes.

### 5. 3-State ACK: Sent / Delivered / Read

- **SENT:** Persisted to DynamoDB. Sender sees ✓
- **DELIVERED:** gRPC push confirmed to recipient's device. Recipient sees ✓✓
- **READ:** Recipient opens / auto-read on pull. ChatService: (1) updates is_read + read_count, (2) syncs READ state to recipient's other devices, (3) notifies sender → sender sees "read" / "N read"

**Large group read receipts (v1):** read_count only. Extension (README): bitmap using immutable `sequence_id` from ConversationParticipant (stable on join/leave, no remapping).

### 6. Per-Conversation Sequence ID

Monotonically increasing per conversation. Gap detected → client sends GAP_DETECTION → ChatService returns missing range from DynamoDB.

**Why:** Solves distributed ordering without global coordination. Sequence ID is not globally unique — only unique per conversation — keeps it lightweight and naturally sortable by DynamoDB clustering key.

### 7. At-Least-Once + Idempotent Storage

- Snowflake message_id (64-bit, time-ordered) is the deduplication key
- DynamoDB conditional put: if message_id exists → skip silently
- Client can safely retry on timeout; server ignores duplicates

**Trade-off:** True exactly-once requires distributed transactions across DynamoDB + Kafka + gRPC. Significantly more complex. At-least-once + idempotent retry is sufficient for chat.

### 8. WebSocket Ping/Pong Heartbeat

WebSocket native Ping/Pong (2 bytes) + 45-60s app-level interval + grace period (miss 2-3 beats → offline) + activity-based reset on any business message.

**Trade-off:** 30s heartbeat catches offline faster but adds ~90% more traffic for active users. 45-60s + grace period keeps detection under 4 minutes while cutting heartbeat overhead by ~90%.

### 9. Session Registry: TTL + Grace Period (No Immediate Delete)

On disconnect, Gateway removes channel from in-memory map immediately but does NOT delete the Redis entry — Redis TTL = 60s handles expiration naturally.

**Why:** Immediate deletion creates a race: Gateway deletes → ChatService queries → user appears offline → Kafka offline queue triggered → but user had only a brief network blip. TTL avoids this without added implementation complexity.

### 10. Protobuf over JSON

Binary serialization, strongly typed, schema-evolution safe, compact.

**Why:** ~2-5x smaller payload vs JSON. ~10x faster serialization in benchmarks. Type safety prevents silent schema drift across client versions — critical for a long-lived system with multiple client generations.

---

## Scalability

### ChatService: Stateless Horizontal Scaling
Add/remove pods with zero coordination. All state lives in Redis/DynamoDB/MySQL.

### Gateway: Stateful but Gracefully Distributed
Per-pod ChannelMap is the only stateful part. Scale strategy:
- Distribute pods across availability zones
- Graceful drain on scale-in: wait for heartbeat timeout before terminating pod
- During drain: brief window where a disconnected user's push goes to Kafka (harmless)

### DynamoDB: Partition-Level Scaling
- conversation_id as PK distributes writes across partitions
- GSI for cross-conversation queries (scatter-gather)
- DAX for hot read users (influencers, support agents)
- On-demand or provisioned capacity mode

### Redis Session Registry
- Keys: `session:{userId}` → hash of connection entries
- Gateway writes are write-behind (batched, async) — no Redis load on message hot path
- ChatService reads only — read volume scales with DAU, not message volume

### Kafka: Partition-Driven Scaling
- Topic partitioned by conversation_id
- Consumer group: N pods each own one partition
- Scale consumers up to partition count; lag monitored and alerted

---

## Reliability

### Ordering
Per-conversation sequence ID. Gaps → backfill.

### Idempotency
Snowflake message_id + DynamoDB conditional put. Safe retry.

### Delivery Guarantee
At-least-once. DynamoDB persist is source of truth — if persist fails, nothing else happens (error returned to sender, no delivery triggered).

### Offline Delivery
Kafka retry queue. Consumer tracks retry count with exponential backoff. After N attempts → Notification Service RPC as final fallback.

### Multi-Device Consistency
Session Registry stores all connectionIds per userId (multi-device).
DELIVERED: push to all recipient devices (all see ✓✓ simultaneously).
READ: (1) sync to recipient's own other devices, (2) notify sender's devices.

---

## Observability

### Metrics

```
Business:   messages_sent_total, messages_delivered_total,
            delivery_success_ratio (delivered/sent),
            online_users, e2e_delivery_latency_seconds (T0→T2, p50/p95/p99)

Gateway:    connections_current, connection_closes_total (by reason),
            heartbeat_timeout_total, gRPC_push_latency_seconds,
            gRPC_push_errors_total

ChatService: message_processed_total (by type + status),
            pipeline_latency_seconds (p95/p99),
            delivery_GRPC_success_total, delivery_GRPC_failure_total,
            kafka_publish_latency_seconds

DynamoDB:  consumed_capacity_units (read/write), throttling_events_total,
            write_latency_p99_seconds (per message type),
            partition_variance_ratio (hot partition risk)

Redis:     command_latency_seconds (p50/p95/p99), keyspace_hits_per_second,
            evictions_total

Kafka:     consumer_lag (by topic+partition), produce_latency_seconds (p99)
```

**E2E Latency = T2 − T0:**
- T0: client-sent timestamp in message payload
- T2: client receives message + app-layer ACK arrives back at ChatService
- Target: p99 < 1s (realistic for global deployment)

Internal stage targets:
- Decode+Validate+Authorize+Enrich: p99 < 5ms
- DynamoDB persist: p99 < 100ms
- gRPC push + client ACK roundtrip: p99 < 300ms

### SLOs

```
E2E delivery latency p99 < 1s          SLO: 99.9%
Gateway push success rate > 99.5%       SLO: 99.5%
DynamoDB write latency p99 < 200ms      SLO: 99.9%
Kafka consumer lag < 10,000            SLO: 99.9%
```

### Tracing

W3C TraceContext (traceparent + Baggage). `correlation_id` propagated through every hop.

```
chat_pipeline (root)
  ├── decode
  ├── validate
  ├── authorize
  ├── enrich
  ├── dynamodb_persist
  ├── redis_session_query
  ├── gRPC_push
  └── kafka_publish
```

Sampling: head-based 5% success + tail-based 100% errors + latency > 1s. Result: ~1-2% trace volume, full coverage of what matters.

Auto-instrumentation: gRPC interceptors, AWS SDK (DynamoDB), Kafka client.
Manual spans: critical pipeline stages (decode/enrich/delivery decision).

### Logging

Structured JSON (JSON lines), correlation_id in every entry.

```
ERROR: always — push failures, DynamoDB throttling, exceptions
WARN:  always — retry triggered, heartbeat timeout, gRPC deadline
INFO:  connection lifecycle, message persisted, delivery confirmed
       (0.1%-1% sample — full logging infeasible at throughput)
DEBUG: disabled in prod
```

Trade-off: 0.1%-1% INFO sampling balances debuggability with storage/performance. ERROR/WARN always captured. correlation_id enables trace-linked retrieval. Temporarily raise sampling rate during incidents.

### Alerting

```
Critical (page immediately):
  - E2E latency p99 > 2s for 5 min
  - kafka_consumer_lag > 50,000 for 5 min
  - DynamoDB throttling > 50/min
  - gRPC push success rate < 99% for 5 min

Warning (notify, no page):
  - kafka_consumer_lag > 10,000 for 10 min
  - DynamoDB consumed capacity > 80%
  - partition_variance_ratio > 3x (hot partition risk)
  - connection_closes spike (sudden drop vs gradual)
  - Redis latency p99 > 50ms
  - heartbeat_timeout_total > 20/min
```

Alert on symptoms (user-visible latency, error rates), not causes. `connection_closes` spike distinguishes突发掉线 (sudden) from gradual scaling events.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Gateway | Netty / WebSocket / MQTT |
| Auth | JWT (HMAC-SHA256) |
| RPC | gRPC |
| Message Queue | Apache Kafka |
| Cache / Session | Redis (Redisson) |
| Relational DB | MySQL (Hibernate) |
| Message Storage | AWS DynamoDB |
| Serialization | Protocol Buffers v3 |
| ID Generation | Snowflake |
| Observability | Prometheus + OpenTelemetry + Jaeger/Tempo |
| Orchestration | Kubernetes |
