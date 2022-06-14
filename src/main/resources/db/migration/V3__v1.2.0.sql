ALTER TABLE logs_receipt
    ADD batch_id UUID;

ALTER TABLE logs_receipt
    ADD processed_log_count INTEGER;

ALTER TABLE logs_receipt
    ADD status INTEGER;

ALTER TABLE logs_receipt
    ALTER COLUMN batch_id SET NOT NULL;

ALTER TABLE logs_receipt
    ALTER COLUMN processed_log_count SET NOT NULL;

ALTER TABLE logs_receipt
    ALTER COLUMN status SET NOT NULL;

ALTER TABLE applications
    DROP CONSTRAINT fk_applications_on_user;

ALTER TABLE logs_receipt
    DROP CONSTRAINT fk_logs_receipt_on_application;

DROP TABLE applications CASCADE;

ALTER TABLE logs_receipt
    DROP COLUMN application_id;

ALTER TABLE logs_receipt
    DROP COLUMN order_num;