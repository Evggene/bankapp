CREATE SCHEMA IF NOT EXISTS exchange;

CREATE TABLE IF NOT EXISTS exchange.conversion_operation (
    id                UUID PRIMARY KEY,
    ts                TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    action            VARCHAR(10) NOT NULL,
    from_currency     VARCHAR(10) NOT NULL,
    to_currency       VARCHAR(10) NOT NULL,
    amount            NUMERIC(19, 2) NOT NULL,
    rate_from_rub     NUMERIC(19, 6) NOT NULL,
    rate_to_rub       NUMERIC(19, 6) NOT NULL,
    conversion_rate   NUMERIC(19, 6) NOT NULL,
    result_amount     NUMERIC(19, 2) NOT NULL
);
