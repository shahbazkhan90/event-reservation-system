create table dead_letters(
    id BIGSERIAL PRIMARY KEY ,
    reservation_id BIGINT NOT NULL ,
    payload TEXT NOT NULL,
    failed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);