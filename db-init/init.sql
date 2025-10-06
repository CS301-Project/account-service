CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TYPE account_type AS ENUM('SAVINGS', 'CHECKING', 'INVESTMENT', 'BUSINESS');
CREATE TYPE account_status AS ENUM('ACTIVE', 'INACTIVE', 'PENDING');


CREATE TABLE account (
    account_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID NOT NULL,
    account_type account_type NOT NULL,
    account_status account_status NOT NULL,
    opening_date TIMESTAMP NOT NULL,
    initial_deposit NUMERIC(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    branch_id INT NOT NULL
);
