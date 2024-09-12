CREATE TABLE conversation
(
    id bigint auto_increment PRIMARY KEY,
    title VARCHAR(255) NOT NULL
);

CREATE TABLE conversation_items
(
    id bigint auto_increment PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    send_by         VARCHAR(5) CHECK (send_by IN ('USER', 'GPT')),
    prompt          TEXT,
    content         TEXT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (conversation_id) REFERENCES conversation (Id)
);