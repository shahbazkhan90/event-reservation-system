-- V1__init_schema.sql
-- Core Logistics and Reservation Tables

CREATE TABLE events (
                        id BIGSERIAL PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        total_capacity INT NOT NULL,
    -- In a real system, you'd have event start times, locations, etc.
                        CONSTRAINT capacity_positive CHECK (total_capacity >= 0)
);

CREATE TABLE reservations (
                              id BIGSERIAL PRIMARY KEY,
                              user_id BIGINT NOT NULL,
                              event_id BIGINT NOT NULL,
                              seats_booked INT NOT NULL,
                              status VARCHAR(50) NOT NULL,
                              created_at TIMESTAMP NOT NULL DEFAULT NOW(),

                              CONSTRAINT fk_event FOREIGN KEY (event_id) REFERENCES events (id),
                              CONSTRAINT seats_positive CHECK (seats_booked > 0)
);

CREATE TABLE outbox_events (
                               id BIGSERIAL PRIMARY KEY,
                               aggregate_id VARCHAR(255) NOT NULL,
                               event_type VARCHAR(255) NOT NULL,
                               payload TEXT NOT NULL,
                               status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                               created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ==========================================
-- PERFORMANCE INDEXES (The Survival Layer)
-- ==========================================

-- 1. The TTL Sweeper Index
-- Your PaymentTTLExecutor searches for 'PENDING' rows older than 10 minutes every 60 seconds.
-- Without this index, PostgreSQL performs a full table scan. With it, it instantly grabs the expired rows.
CREATE INDEX idx_reservations_status_created ON reservations (status, created_at);

-- 2. The Capacity & Waitlist Index
-- Your ReservationService constantly sums seats and finds the oldest waitlisted user by event.
CREATE INDEX idx_reservations_event_status ON reservations (event_id, status);

-- 3. The Outbox Relay Index
-- Your OutboxRelayScheduler polls for 'PENDING' events every 5 seconds.
CREATE INDEX idx_outbox_status_created ON outbox_events (status, created_at);