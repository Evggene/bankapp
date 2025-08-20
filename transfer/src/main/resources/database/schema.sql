CREATE SCHEMA IF NOT EXISTS transfer;

CREATE TABLE IF NOT EXISTS transfer.operation (
    id               UUID PRIMARY KEY,
    from_user        VARCHAR(50)    NOT NULL,
    to_user          VARCHAR(50)    NOT NULL,
    from_currency    VARCHAR(10)    NOT NULL,
    to_currency      VARCHAR(10)    NOT NULL,
    amount           NUMERIC(19,2)  NOT NULL,
    converted_amount NUMERIC(19,2)  NOT NULL,
    status           VARCHAR(20)    NOT NULL,
    blocker_reason   VARCHAR(255),
    ts               TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS transfer_operation_ts_idx ON transfer.operation(ts DESC);
