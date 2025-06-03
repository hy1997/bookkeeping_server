CREATE TABLE budget (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    daily_budget DOUBLE,
    monthly_budget DOUBLE
);

CREATE TABLE fixed_expense (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255),
    amount DOUBLE,
    repeat_type VARCHAR(50),
    budget_id BIGINT,
    CONSTRAINT fk_budget FOREIGN KEY (budget_id) REFERENCES budget(id)
); 