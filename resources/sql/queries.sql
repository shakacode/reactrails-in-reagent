-- name: create-comment<!
-- creates a new comment record
INSERT INTO comments
(name, content, created_at)
VALUES (:name, :content, :created_at)

-- name: get-comments-after
-- retrieve a comment after time.
SELECT * FROM comments
WHERE created_at >= :after
ORDER BY created_at

-- name: delete-comment!
-- delete a comment given the id
DELETE FROM comments
WHERE id = :id
