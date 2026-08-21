-- Public demo data only. Do not put credentials or tokens in this file.

SET NAMES utf8mb4;

INSERT INTO store (store_code, store_name)
VALUES ('TEST-001', 'MCM 청담 HAUS')
ON DUPLICATE KEY UPDATE
    store_name = VALUES(store_name);
