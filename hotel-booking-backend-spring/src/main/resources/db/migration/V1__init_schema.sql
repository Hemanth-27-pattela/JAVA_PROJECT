-- ===================================================================
-- Hotel Booking Management System - Flyway Initial Schema (V1)
-- Target: MySQL 8.0+
-- Safe Execution: Uses CREATE TABLE IF NOT EXISTS (No drop / delete)
-- ===================================================================

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    image VARCHAR(1024) NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'user',
    phone VARCHAR(50) NULL,
    street VARCHAR(255) NULL,
    city VARCHAR(100) NULL,
    state VARCHAR(100) NULL,
    country VARCHAR(100) NULL,
    zip_code VARCHAR(20) NULL,
    budget_min DECIMAL(10, 2) NULL,
    budget_max DECIMAL(10, 2) NULL,
    total_bookings INT NOT NULL DEFAULT 0,
    total_spent DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    last_login TIMESTAMP NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email),
    INDEX idx_users_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. User Preferred Destinations
CREATE TABLE IF NOT EXISTS user_preferred_destinations (
    user_id VARCHAR(36) NOT NULL,
    destination VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, destination),
    CONSTRAINT fk_user_destinations_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. User Preferred Hotel Types
CREATE TABLE IF NOT EXISTS user_preferred_hotel_types (
    user_id VARCHAR(36) NOT NULL,
    hotel_type VARCHAR(100) NOT NULL,
    PRIMARY KEY (user_id, hotel_type),
    CONSTRAINT fk_user_types_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Hotels Table
CREATE TABLE IF NOT EXISTS hotels (
    id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    adult_count INT NOT NULL DEFAULT 1,
    child_count INT NOT NULL DEFAULT 0,
    price_per_night DECIMAL(10, 2) NOT NULL,
    star_rating INT NOT NULL DEFAULT 1,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    loc_latitude DOUBLE NULL,
    loc_longitude DOUBLE NULL,
    loc_street VARCHAR(255) NULL,
    loc_city VARCHAR(100) NULL,
    loc_state VARCHAR(100) NULL,
    loc_country VARCHAR(100) NULL,
    loc_zip_code VARCHAR(20) NULL,
    contact_phone VARCHAR(50) NULL,
    contact_email VARCHAR(255) NULL,
    contact_website VARCHAR(255) NULL,
    policy_check_in_time VARCHAR(50) NULL,
    policy_check_out_time VARCHAR(50) NULL,
    policy_cancellation TEXT NULL,
    policy_pet VARCHAR(255) NULL,
    policy_smoking VARCHAR(255) NULL,
    amenity_parking BOOLEAN NOT NULL DEFAULT FALSE,
    amenity_wifi BOOLEAN NOT NULL DEFAULT FALSE,
    amenity_pool BOOLEAN NOT NULL DEFAULT FALSE,
    amenity_gym BOOLEAN NOT NULL DEFAULT FALSE,
    amenity_spa BOOLEAN NOT NULL DEFAULT FALSE,
    amenity_restaurant BOOLEAN NOT NULL DEFAULT FALSE,
    amenity_bar BOOLEAN NOT NULL DEFAULT FALSE,
    amenity_airport_shuttle BOOLEAN NOT NULL DEFAULT FALSE,
    amenity_business_center BOOLEAN NOT NULL DEFAULT FALSE,
    total_bookings INT NOT NULL DEFAULT 0,
    total_revenue DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    average_rating DOUBLE NOT NULL DEFAULT 0.0,
    review_count INT NOT NULL DEFAULT 0,
    occupancy_rate DOUBLE NOT NULL DEFAULT 0.0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_hotels_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_hotels_user_id (user_id),
    INDEX idx_hotels_city_star (city, star_rating),
    INDEX idx_hotels_price_star (price_per_night, star_rating),
    INDEX idx_hotels_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Hotel Types Collection Table
CREATE TABLE IF NOT EXISTS hotel_types (
    hotel_id VARCHAR(36) NOT NULL,
    type VARCHAR(100) NOT NULL,
    PRIMARY KEY (hotel_id, type),
    CONSTRAINT fk_hotel_types_hotel FOREIGN KEY (hotel_id) REFERENCES hotels (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Hotel Facilities Collection Table
CREATE TABLE IF NOT EXISTS hotel_facilities (
    hotel_id VARCHAR(36) NOT NULL,
    facility VARCHAR(100) NOT NULL,
    PRIMARY KEY (hotel_id, facility),
    CONSTRAINT fk_hotel_facilities_hotel FOREIGN KEY (hotel_id) REFERENCES hotels (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Hotel Images Collection Table
CREATE TABLE IF NOT EXISTS hotel_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hotel_id VARCHAR(36) NOT NULL,
    image_url VARCHAR(1024) NOT NULL,
    CONSTRAINT fk_hotel_images_hotel FOREIGN KEY (hotel_id) REFERENCES hotels (id) ON DELETE CASCADE,
    INDEX idx_hotel_images_hotel_id (hotel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Bookings Table
CREATE TABLE IF NOT EXISTS bookings (
    id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    hotel_id VARCHAR(36) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NULL,
    adult_count INT NOT NULL DEFAULT 1,
    child_count INT NOT NULL DEFAULT 0,
    check_in TIMESTAMP NOT NULL,
    check_out TIMESTAMP NOT NULL,
    total_cost DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    payment_status VARCHAR(20) NOT NULL DEFAULT 'pending',
    payment_method VARCHAR(50) NULL,
    special_requests TEXT NULL,
    cancellation_reason TEXT NULL,
    refund_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    stripe_payment_intent_id VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_bookings_hotel FOREIGN KEY (hotel_id) REFERENCES hotels (id) ON DELETE CASCADE,
    INDEX idx_bookings_user_created (user_id, created_at),
    INDEX idx_bookings_hotel_check_in (hotel_id, check_in),
    INDEX idx_bookings_status_created (status, created_at),
    INDEX idx_bookings_payment_status (payment_status, created_at),
    INDEX idx_bookings_stripe_intent (stripe_payment_intent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Reviews Table
CREATE TABLE IF NOT EXISTS reviews (
    id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    hotel_id VARCHAR(36) NOT NULL,
    booking_id VARCHAR(36) NOT NULL,
    rating INT NOT NULL DEFAULT 5,
    comment TEXT NOT NULL,
    cat_cleanliness INT NOT NULL DEFAULT 5,
    cat_service INT NOT NULL DEFAULT 5,
    cat_location INT NOT NULL DEFAULT 5,
    cat_value INT NOT NULL DEFAULT 5,
    cat_amenities INT NOT NULL DEFAULT 5,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    helpful_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_reviews_booking_id (booking_id),
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_hotel FOREIGN KEY (hotel_id) REFERENCES hotels (id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE,
    INDEX idx_reviews_hotel_rating (hotel_id, rating),
    INDEX idx_reviews_user_created (user_id, created_at),
    INDEX idx_reviews_hotel_created (hotel_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Analytics Snapshots Table
CREATE TABLE IF NOT EXISTS analytics_snapshots (
    id VARCHAR(36) NOT NULL,
    date TIMESTAMP NOT NULL,
    total_bookings BIGINT NOT NULL DEFAULT 0,
    total_revenue DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    total_users BIGINT NOT NULL DEFAULT 0,
    total_hotels BIGINT NOT NULL DEFAULT 0,
    average_booking_value DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    conversion_rate DOUBLE NOT NULL DEFAULT 0.0,
    cancellation_rate DOUBLE NOT NULL DEFAULT 0.0,
    average_rating DOUBLE NOT NULL DEFAULT 0.0,
    breakdown_json LONGTEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_analytics_date (date),
    INDEX idx_analytics_revenue (total_revenue)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

