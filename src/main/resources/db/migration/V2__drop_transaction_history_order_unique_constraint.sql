ALTER TABLE transaction_history
    DROP CONSTRAINT IF EXISTS uk2fswed84s47mlvryh9ure65nc;

DROP INDEX IF EXISTS uk2fswed84s47mlvryh9ure65nc;

CREATE INDEX IF NOT EXISTS idx_transaction_history_order_id
    ON transaction_history (order_id);
