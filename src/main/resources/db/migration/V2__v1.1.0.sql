ALTER TABLE applications
    ADD display_name VARCHAR(255);

ALTER TABLE applications
    ADD index VARCHAR(255);

ALTER TABLE applications
    ADD CONSTRAINT uc_applications_index UNIQUE (index);

ALTER TABLE flush
    DROP CONSTRAINT FK_FLUSH_ON_LOGS_RECEIPT;

DROP TABLE flush CASCADE;

ALTER TABLE logs_receipt
    DROP COLUMN source;