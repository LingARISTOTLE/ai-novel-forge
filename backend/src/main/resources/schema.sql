CREATE TABLE IF NOT EXISTS novels (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    created_at DATETIME,
    updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS chapters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE
);
