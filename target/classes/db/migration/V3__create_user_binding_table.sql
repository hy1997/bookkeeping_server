CREATE TABLE user_binding (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    bound_user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_bound_user FOREIGN KEY (bound_user_id) REFERENCES users(id),
    CONSTRAINT unique_user_binding UNIQUE (user_id, bound_user_id)
); 