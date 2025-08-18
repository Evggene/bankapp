CREATE SCHEMA IF NOT EXISTS cash;

CREATE TABLE IF NOT EXISTS cash.account (
    id           UUID PRIMARY KEY,
    username     VARCHAR(50) NOT NULL,
    currency     VARCHAR(10) NOT NULL,
    balance      NUMERIC(19,2) NOT NULL DEFAULT 0,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE UNIQUE INDEX IF NOT EXISTS cash_acc_username_currency_uidx
  ON cash.account(username, currency);
