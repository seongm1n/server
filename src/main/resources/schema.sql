-- 콘서트 예약 서비스 DB 스키마

-- 사용자 잔액 테이블
CREATE TABLE user_balance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL UNIQUE,
    balance INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
);

-- 대기열 토큰 테이블
CREATE TABLE queue_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    position_num INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activated_at TIMESTAMP NULL,
    expires_at TIMESTAMP NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_token (token),
    INDEX idx_status_position (status, position_num)
);

-- 좌석 테이블 (concert_schedule_id는 외래키 제약 없이 단순 참조)
CREATE TABLE seat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    concert_schedule_id BIGINT NOT NULL,
    seat_number INT NOT NULL,
    price INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    reserved_by VARCHAR(255) NULL,
    reserved_at TIMESTAMP NULL,
    UNIQUE KEY uk_schedule_seat (concert_schedule_id, seat_number),
    INDEX idx_status (status),
    INDEX idx_reserved_by (reserved_by)
);

-- 예약 테이블
CREATE TABLE reservation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    seat_id BIGINT NOT NULL,
    price INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (seat_id) REFERENCES seat(id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
);

-- 결제 테이블
CREATE TABLE payment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    reservation_id BIGINT NOT NULL,
    amount INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    failure_reason VARCHAR(500) NULL,
    FOREIGN KEY (reservation_id) REFERENCES reservation(id),
    INDEX idx_user_id (user_id),
    INDEX idx_reservation_id (reservation_id),
    INDEX idx_status (status)
);
