-- =============================================
-- Center Operation System Database Schema
-- Database: MySQL 8+
-- =============================================


CREATE
DATABASE IF NOT EXISTS center_operation
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;


USE
center_operation;


-- =============================================
-- 1. Person Table
-- 學員資料
-- =============================================

CREATE TABLE persons
(

    id         BIGINT AUTO_INCREMENT PRIMARY KEY,

    name       VARCHAR(50)  NOT NULL,

    email      VARCHAR(100) NOT NULL UNIQUE,

    phone      VARCHAR(20),

    status     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',

    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,


    CONSTRAINT chk_person_status
        CHECK (status IN ('ACTIVE', 'INACTIVE'))

);


CREATE INDEX idx_person_email
    ON persons (email);



-- =============================================
-- 2. Course Table
-- 課程資料
-- =============================================

CREATE TABLE courses
(

    id          BIGINT AUTO_INCREMENT PRIMARY KEY,

    code        VARCHAR(50)  NOT NULL UNIQUE,

    name        VARCHAR(100) NOT NULL,

    description TEXT,

    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP


);


CREATE INDEX idx_course_code
    ON courses (code);



-- =============================================
-- 3. Enrollment Table
-- 學員課程註冊紀錄
-- =============================================

CREATE TABLE enrollments
(

    id            BIGINT AUTO_INCREMENT PRIMARY KEY,


    person_id     BIGINT      NOT NULL,

    course_id     BIGINT      NOT NULL,


    status        VARCHAR(30) NOT NULL DEFAULT 'NOT_STARTED',


    start_date    DATE,

    complete_date DATE,


    created_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,


    CONSTRAINT fk_enrollment_person
        FOREIGN KEY (person_id)
            REFERENCES persons (id)
            ON DELETE CASCADE,


    CONSTRAINT fk_enrollment_course
        FOREIGN KEY (course_id)
            REFERENCES courses (id)
            ON DELETE CASCADE,


    CONSTRAINT uq_person_course
        UNIQUE (person_id, course_id),


    CONSTRAINT chk_enrollment_status
        CHECK (
            status IN
            (
             'NOT_STARTED',
             'IN_PROGRESS',
             'COMPLETED'
                )
            )

);


CREATE INDEX idx_enrollment_person
    ON enrollments (person_id);


CREATE INDEX idx_enrollment_course
    ON enrollments (course_id);



-- =============================================
-- 4. Course Prerequisite Table
-- 課程先修關係
--
-- Example:
--
-- Data Structure
-- requires
-- OOP
--
-- 用於 Course Graph
-- =============================================

CREATE TABLE course_prerequisites
(

    id              BIGINT AUTO_INCREMENT PRIMARY KEY,


    course_id       BIGINT   NOT NULL,


    prerequisite_id BIGINT   NOT NULL,


    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,


    CONSTRAINT fk_cp_course
        FOREIGN KEY (course_id)
            REFERENCES courses (id)
            ON DELETE CASCADE,


    CONSTRAINT fk_cp_prerequisite
        FOREIGN KEY (prerequisite_id)
            REFERENCES courses (id)
            ON DELETE CASCADE,


    CONSTRAINT uq_course_prerequisite
        UNIQUE (course_id, prerequisite_id),


    CONSTRAINT chk_not_self_prerequisite
        CHECK (course_id <> prerequisite_id)

);


CREATE INDEX idx_cp_course
    ON course_prerequisites (course_id);


CREATE INDEX idx_cp_prerequisite
    ON course_prerequisites (prerequisite_id);



-- =============================================
-- 5. Alert Table
-- 課程警示資料
--
-- Heap Priority Queue 使用來源
-- =============================================

CREATE TABLE alerts
(

    id          BIGINT AUTO_INCREMENT PRIMARY KEY,


    person_id   BIGINT       NOT NULL,


    course_id   BIGINT,


    priority    INT          NOT NULL DEFAULT 1,


    message     VARCHAR(255) NOT NULL,


    is_resolved BOOLEAN      NOT NULL DEFAULT FALSE,


    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,


    CONSTRAINT fk_alert_person
        FOREIGN KEY (person_id)
            REFERENCES persons (id)
            ON DELETE CASCADE,


    CONSTRAINT fk_alert_course
        FOREIGN KEY (course_id)
            REFERENCES courses (id)
            ON DELETE SET NULL,


    CONSTRAINT chk_alert_priority
        CHECK (priority BETWEEN 1 AND 3)

);


CREATE INDEX idx_alert_priority
    ON alerts (priority);


CREATE INDEX idx_alert_person
    ON alerts (person_id);



-- =============================================
-- End of Schema
-- =============================================