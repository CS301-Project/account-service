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

INSERT INTO account (client_id, account_type, account_status, opening_date, initial_deposit, currency, branch_id) VALUES
('a1b2c3d4-e5f6-4788-990a-b1c2d3e4f5a6', 'SAVINGS', 'ACTIVE', '2023-01-15 10:00:00', 5000.00, 'USD', 101),
('a1b2c3d4-e5f6-4788-990a-b1c2d3e4f5a6', 'CHECKING', 'ACTIVE', '2023-01-15 10:00:00', 5000.00, 'SGD', 101),
('a1b2c3d4-e5f6-4788-990a-b1c2d3e4f5a6', 'INVESTMENT', 'ACTIVE', '2023-01-15 10:00:00', 5000.00, 'EUR', 101),
('b2c3d4e5-f6a7-4899-801b-c2d3e4f5a6b7', 'CHECKING', 'PENDING', '2023-02-20 14:30:00', 1500.00, 'EUR', 102),
('c3d4e5f6-a7b8-4900-912c-d3e4f5a6b7c8', 'INVESTMENT', 'ACTIVE', '2023-03-10 09:15:00', 10000.00, 'GBP', 103),
('d4e5f6a7-b8c9-4011-023d-e4f5a6b7c8d9', 'BUSINESS', 'INACTIVE', '2023-04-05 11:45:00', 25000.00, 'USD', 104),
('e5f6a7b8-c9d0-4122-134e-f5a6b7c8d9e0', 'SAVINGS', 'ACTIVE', '2023-05-12 16:20:00', 3000.00, 'CAD', 105);
