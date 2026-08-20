-- KNOQ database schema for MySQL 8.4.
-- This file only creates missing tables and indexes. It does not delete existing data.

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS account (
    id VARCHAR(64) NOT NULL,
    kakao_user_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_account_kakao_user_id UNIQUE (kakao_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS store (
    id BIGINT NOT NULL AUTO_INCREMENT,
    store_code VARCHAR(30) NOT NULL,
    store_name VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_store_store_code UNIQUE (store_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS product (
    id VARCHAR(64) NOT NULL,
    product_code VARCHAR(30) NOT NULL,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(100) NULL,
    material VARCHAR(200) NULL,
    features VARCHAR(500) NULL,
    price BIGINT NULL,
    thumbnail_url VARCHAR(500) NULL,
    brand_official_description TEXT NULL,
    ai_generated_description TEXT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_product_product_code UNIQUE (product_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS product_feature_style (
    product_id VARCHAR(64) NOT NULL,
    position INT NOT NULL,
    feature VARCHAR(100) NOT NULL,
    PRIMARY KEY (product_id, position),
    CONSTRAINT fk_product_feature_style_product
        FOREIGN KEY (product_id) REFERENCES product (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS product_feature_composition (
    product_id VARCHAR(64) NOT NULL,
    position INT NOT NULL,
    feature VARCHAR(100) NOT NULL,
    PRIMARY KEY (product_id, position),
    CONSTRAINT fk_product_feature_composition_product
        FOREIGN KEY (product_id) REFERENCES product (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS product_feature_usage (
    product_id VARCHAR(64) NOT NULL,
    position INT NOT NULL,
    feature VARCHAR(100) NOT NULL,
    PRIMARY KEY (product_id, position),
    CONSTRAINT fk_product_feature_usage_product
        FOREIGN KEY (product_id) REFERENCES product (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS product_color (
    product_id VARCHAR(64) NOT NULL,
    color VARCHAR(255) NULL,
    CONSTRAINT fk_product_color_product
        FOREIGN KEY (product_id) REFERENCES product (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS product_size (
    product_id VARCHAR(64) NOT NULL,
    size VARCHAR(255) NULL,
    CONSTRAINT fk_product_size_product
        FOREIGN KEY (product_id) REFERENCES product (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS product_embedding (
    product_id VARCHAR(64) NOT NULL,
    position INT NOT NULL,
    value DOUBLE NULL,
    PRIMARY KEY (product_id, position),
    CONSTRAINT fk_product_embedding_product
        FOREIGN KEY (product_id) REFERENCES product (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS product_reference_image (
    product_id VARCHAR(64) NOT NULL,
    position INT NOT NULL,
    image_base64 LONGTEXT NULL,
    PRIMARY KEY (product_id, position),
    CONSTRAINT fk_product_reference_image_product
        FOREIGN KEY (product_id) REFERENCES product (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `session` (
    id VARCHAR(64) NOT NULL,
    token VARCHAR(64) NOT NULL,
    store_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    terms_of_service BIT(1) NOT NULL,
    privacy_policy BIT(1) NOT NULL,
    over14 BIT(1) NOT NULL,
    marketing_opt_in BIT(1) NOT NULL,
    consented_at DATETIME(6) NULL,
    storage_scope ENUM('ACCOUNT', 'PENDING_KAKAO_LOGIN', 'PRIVATE') NOT NULL,
    account_id VARCHAR(64) NULL,
    nickname VARCHAR(10) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_session_token UNIQUE (token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS session_lifestyle_tag (
    session_id VARCHAR(64) NOT NULL,
    tag ENUM('CASUAL', 'CLASSIC', 'FORMAL', 'MINIMAL', 'STREET', 'TRENDY') NULL,
    CONSTRAINT fk_session_lifestyle_tag_session
        FOREIGN KEY (session_id) REFERENCES `session` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS saved_product (
    id BIGINT NOT NULL AUTO_INCREMENT,
    session_id VARCHAR(64) NOT NULL,
    product_id VARCHAR(64) NOT NULL,
    source ENUM('CAMERA', 'RECOMMEND') NOT NULL,
    saved_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_saved_product_session_product UNIQUE (session_id, product_id),
    INDEX idx_saved_product_session_saved_at (session_id, saved_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS recognition (
    id VARCHAR(64) NOT NULL,
    session_id VARCHAR(64) NOT NULL,
    match_type ENUM('CANDIDATES', 'SINGLE') NOT NULL,
    status ENUM('CONFIRMED', 'DISCARDED', 'PENDING') NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_recognition_session (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS recognition_candidate (
    recognition_id VARCHAR(64) NOT NULL,
    product_id VARCHAR(64) NULL,
    confidence DOUBLE NOT NULL,
    CONSTRAINT fk_recognition_candidate_recognition
        FOREIGN KEY (recognition_id) REFERENCES recognition (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS needs_analysis (
    id BIGINT NOT NULL AUTO_INCREMENT,
    session_id VARCHAR(64) NOT NULL,
    product_category VARCHAR(100) NULL,
    preferred_color VARCHAR(100) NULL,
    preferred_material VARCHAR(100) NULL,
    preferred_size VARCHAR(50) NULL,
    comment VARCHAR(500) NULL,
    analyzed_at DATETIME(6) NOT NULL,
    user_edited TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_needs_analysis_session UNIQUE (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS consultation_request (
    id VARCHAR(64) NOT NULL,
    session_id VARCHAR(64) NOT NULL,
    store_id BIGINT NOT NULL,
    help_type ENUM('PRODUCT_COMPARISON', 'PRODUCT_INFO', 'PRODUCT_RECOMMENDATION', 'STYLING_RECOMMENDATION') NOT NULL,
    status ENUM('ACCEPTED', 'COMPLETED', 'EXPIRED', 'IN_PROGRESS', 'REQUESTED') NOT NULL,
    include_needs_analysis BIT(1) NOT NULL,
    requested_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    INDEX idx_consultation_request_session_status (session_id, status),
    INDEX idx_consultation_request_store_requested_at (store_id, requested_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS consultation_request_product (
    id BIGINT NOT NULL AUTO_INCREMENT,
    consultation_request_id VARCHAR(64) NOT NULL,
    product_id VARCHAR(64) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_consultation_request_product_request
        FOREIGN KEY (consultation_request_id) REFERENCES consultation_request (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS notification (
    id VARCHAR(64) NOT NULL,
    session_id VARCHAR(64) NOT NULL,
    request_id VARCHAR(64) NOT NULL,
    status ENUM('ACCEPTED', 'COMPLETED', 'EXPIRED', 'IN_PROGRESS', 'REQUESTED') NOT NULL,
    message VARCHAR(500) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_notification_session_created_at (session_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
