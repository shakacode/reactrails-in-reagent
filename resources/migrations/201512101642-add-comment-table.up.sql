CREATE TABLE comments
(id BIGINT AUTO_INCREMENT,
 name VARCHAR(40),
 content VARCHAR(2000),
 created_at DATETIME);

INSERT INTO comments (name, content, created_at) VALUES ('Guest', 'My first comment', CURRENT_TIMESTAMP);
INSERT INTO comments (name, content, created_at) VALUES ('User', 'This is **bold** content', CURRENT_TIMESTAMP);
