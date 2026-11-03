-- ===================================================================
-- Migration: V2__hotel_owner_requests.sql
-- Description: Hotel owner access request and admin approval workflow
-- Target Engine: MySQL 8.0+
-- ===================================================================

CREATE TABLE IF NOT EXISTS hotel_owner_requests (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    business_name VARCHAR(255) NOT NULL,
    business_email VARCHAR(255) NOT NULL,
    business_phone VARCHAR(50),
    business_address TEXT,
    justification TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    is_pending TINYINT(1) GENERATED ALWAYS AS (CASE WHEN status = 'PENDING' THEN 1 ELSE NULL END) STORED,
    admin_notes TEXT,
    rejection_reason TEXT,
    reviewed_by VARCHAR(36),
    reviewed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_owner_requests_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_owner_requests_admin FOREIGN KEY (reviewed_by) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT uk_owner_requests_user_pending UNIQUE (user_id, is_pending),
    INDEX idx_owner_requests_user_id (user_id),
    INDEX idx_owner_requests_status (status),
    INDEX idx_owner_requests_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

