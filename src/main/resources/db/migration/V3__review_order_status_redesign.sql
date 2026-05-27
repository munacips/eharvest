ALTER TABLE review
    ADD COLUMN IF NOT EXISTS order_id BIGINT,
    ADD COLUMN IF NOT EXISTS status VARCHAR(32);

UPDATE review
SET status = 'COMPLETED'
WHERE status IS NULL;

ALTER TABLE review
    ALTER COLUMN status SET NOT NULL;

ALTER TABLE review
    ADD CONSTRAINT fk_review_order
        FOREIGN KEY (order_id) REFERENCES orders(id);

ALTER TABLE review
    ADD CONSTRAINT uk_review_order_reviewer_reviewee
        UNIQUE (order_id, reviewer_id, reviewee_id);
