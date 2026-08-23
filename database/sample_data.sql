-- =============================================
-- Center Operation System
-- Sample Data
-- MySQL 8+
--
-- This file is idempotent and may be executed in full repeatedly.
-- Existing seed rows are updated through their unique keys; rows created
-- outside this seed set are preserved.
-- =============================================

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

USE center_operation;

START TRANSACTION;


-- =============================================
-- 1. Persons (200 seed rows)
--
-- IDs 1-10 use named demo people.
-- IDs 11-200 are generated demo students.
-- Every tenth person is INACTIVE.
-- =============================================

INSERT INTO persons
    (id, name, email, phone, status)
WITH RECURSIVE person_numbers AS (
    SELECT 1 AS person_id
    UNION ALL
    SELECT person_id + 1
    FROM person_numbers
    WHERE person_id < 200
)
SELECT
    person_id,
    CASE person_id
        WHEN 1 THEN '王小明'
        WHEN 2 THEN '陳小華'
        WHEN 3 THEN '林志偉'
        WHEN 4 THEN '張雅婷'
        WHEN 5 THEN '李冠宇'
        WHEN 6 THEN '黃俊傑'
        WHEN 7 THEN '吳佳穎'
        WHEN 8 THEN '劉家豪'
        WHEN 9 THEN '蔡佩珊'
        WHEN 10 THEN '周建宏'
        ELSE CONCAT('Demo Student ', LPAD(person_id, 3, '0'))
    END AS name,
    CASE person_id
        WHEN 1 THEN 'ming@example.com'
        WHEN 2 THEN 'hua@example.com'
        WHEN 3 THEN 'wei@example.com'
        WHEN 4 THEN 'ting@example.com'
        WHEN 5 THEN 'guan@example.com'
        WHEN 6 THEN 'jun@example.com'
        WHEN 7 THEN 'ying@example.com'
        WHEN 8 THEN 'hao@example.com'
        WHEN 9 THEN 'shan@example.com'
        WHEN 10 THEN 'hong@example.com'
        ELSE CONCAT('student', LPAD(person_id, 3, '0'), '@example.com')
    END AS email,
    CASE
        WHEN person_id <= 10
            THEN CONCAT('09120000', LPAD(person_id, 2, '0'))
        ELSE CONCAT('0900000', LPAD(person_id, 3, '0'))
    END AS phone,
    CASE
        WHEN MOD(person_id, 10) = 0 THEN 'INACTIVE'
        ELSE 'ACTIVE'
    END AS status
FROM person_numbers
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    email = VALUES(email),
    phone = VALUES(phone),
    status = VALUES(status);


-- =============================================
-- 2. Courses (20 seed rows)
--
-- Course Graph:
--
-- Java Basic
--      |
--      v
-- Object Oriented Programming
--      |
--      v
-- Data Structure
--      |
--      v
-- Algorithm
--
-- Database / Web / Spring / Backend
-- =============================================


INSERT INTO courses
    (code, name, description)
VALUES
       ('JAVA-001', 'Java Basic', 'Java programming fundamental course'),
       ('JAVA-002', 'Object Oriented Programming', 'OOP concepts including class, object, inheritance'),
       ('DS-001', 'Data Structure', 'Array, Linked List, Tree, Graph and Hash Table'),
       ('ALG-001', 'Algorithm', 'Sorting, searching and algorithm analysis'),
       ('DB-001', 'Database', 'SQL and relational database design'),
       ('WEB-001', 'Web Programming', 'HTML CSS JavaScript and Web fundamentals'),
       ('SPRING-001', 'Spring Boot', 'Backend development using Spring Boot'),
       ('BACKEND-001', 'Backend Engineering', 'REST API and system architecture'),
       ('AI-001', 'Artificial Intelligence', 'Machine learning and AI introduction'),
       ('CLOUD-001', 'Cloud Computing', 'Cloud deployment and container technology'),
       ('SEC-001', 'Application Security', 'Authentication, authorization and secure coding'),
       ('TEST-001', 'Software Testing', 'Unit, integration and API testing'),
       ('DEVOPS-001', 'DevOps Fundamentals', 'CI/CD and delivery workflow'),
       ('API-001', 'REST API Design', 'RESTful conventions and API contracts'),
       ('DESIGN-001', 'System Design', 'Scalable system design fundamentals'),
       ('GIT-001', 'Git Collaboration', 'Branching, review and team workflow'),
       ('LINUX-001', 'Linux Fundamentals', 'Command line and server administration'),
       ('NETWORK-001', 'Computer Networks', 'Network protocols and web communication'),
       ('PYTHON-001', 'Python Basic', 'Python programming fundamentals'),
       ('PROJECT-001', 'Software Project', 'End-to-end team project practice')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    description = VALUES(description);



-- =============================================
-- 3. Course prerequisites (7 seed rows)
--
-- Used by CourseGraph
-- =============================================


INSERT INTO course_prerequisites
    (course_id, prerequisite_id)
VALUES
    (2, 1),  -- OOP requires Java Basic
    (3, 2),  -- Data Structure requires OOP
    (4, 3),  -- Algorithm requires Data Structure
    (7, 1),  -- Spring Boot requires Java
    (8, 7),  -- Backend requires Spring Boot
    (8, 5),  -- Backend requires Database
    (10, 8)  -- Cloud requires Backend
ON DUPLICATE KEY UPDATE
    prerequisite_id = VALUES(prerequisite_id);



-- =============================================
-- 4. Enrollments (1,000 generated seed rows)
--
-- Learning Progress Data
--
-- 200 persons x 5 unique courses = 1,000 enrollments
-- Status distribution: 45% COMPLETED, 35% IN_PROGRESS,
-- and 20% NOT_STARTED
-- =============================================


INSERT INTO enrollments
    (person_id, course_id, status, start_date, complete_date)
SELECT
    p.id,
    MOD((p.id - 1) + (slots.slot * 3), 20) + 1 AS course_id,
    CASE
        WHEN MOD(((p.id - 1) * 5) + slots.slot, 20) < 9 THEN 'COMPLETED'
        WHEN MOD(((p.id - 1) * 5) + slots.slot, 20) < 16 THEN 'IN_PROGRESS'
        ELSE 'NOT_STARTED'
    END AS status,
    CASE
        WHEN MOD(((p.id - 1) * 5) + slots.slot, 20) < 16
            THEN DATE_ADD(
                '2025-01-01',
                INTERVAL MOD(((p.id - 1) * 5) + slots.slot, 500) DAY
            )
        ELSE NULL
    END AS start_date,
    CASE
        WHEN MOD(((p.id - 1) * 5) + slots.slot, 20) < 9
            THEN DATE_ADD(
                DATE_ADD(
                    '2025-01-01',
                    INTERVAL MOD(((p.id - 1) * 5) + slots.slot, 500) DAY
                ),
                INTERVAL (14 + MOD(((p.id - 1) * 5) + slots.slot, 45)) DAY
            )
        ELSE NULL
    END AS complete_date
FROM persons p
CROSS JOIN (
    SELECT 0 AS slot
    UNION ALL SELECT 1
    UNION ALL SELECT 2
    UNION ALL SELECT 3
    UNION ALL SELECT 4
) slots
WHERE p.id BETWEEN 1 AND 200
ON DUPLICATE KEY UPDATE
    status = VALUES(status),
    start_date = VALUES(start_date),
    complete_date = VALUES(complete_date);



-- =============================================
-- 5. Alerts (30 seed rows)
--
-- Priority:
--
-- 3 HIGH
-- 2 MEDIUM
-- 1 LOW
--
-- Used by Max Heap
-- =============================================


INSERT INTO alerts
    (id, person_id, course_id, priority, message, is_resolved)
VALUES
       (1, 1, 3, 3, 'Data Structure progress is overdue', FALSE),
       (2, 2, 2, 2, 'OOP course progress reminder', FALSE),
       (3, 5, 8, 3, 'Backend course requires attention', FALSE),
       (4, 7, 6, 1, 'Web course weekly reminder', FALSE),
       (5, 8, 7, 2, 'Spring Boot course not started', FALSE),
       (6, 10, 10, 1, 'Cloud Computing course available', FALSE),
       (7, 11, 11, 1, 'Course general reminder', FALSE),
       (8, 12, 12, 2, 'Course progress reminder', FALSE),
       (9, 13, 13, 3, 'Course requires immediate attention', FALSE),
       (10, 14, 14, 1, 'Course general reminder', FALSE),
       (11, 15, 15, 2, 'Course progress reminder', FALSE),
       (12, 16, 16, 3, 'Course requires immediate attention', FALSE),
       (13, 17, 17, 1, 'Course general reminder', FALSE),
       (14, 18, 18, 2, 'Course progress reminder', FALSE),
       (15, 19, 19, 3, 'Course requires immediate attention', FALSE),
       (16, 20, 20, 1, 'Course general reminder', FALSE),
       (17, 21, 1, 2, 'Course progress reminder', FALSE),
       (18, 22, 2, 3, 'Course requires immediate attention', FALSE),
       (19, 23, 3, 1, 'Course general reminder', FALSE),
       (20, 24, 4, 2, 'Course progress reminder', FALSE),
       (21, 25, 5, 3, 'Course requires immediate attention', FALSE),
       (22, 26, 6, 1, 'Course general reminder', FALSE),
       (23, 27, 7, 2, 'Course progress reminder', FALSE),
       (24, 28, 8, 3, 'Course requires immediate attention', FALSE),
       (25, 29, 9, 1, 'Course general reminder', FALSE),
       (26, 30, 10, 2, 'Course progress reminder', FALSE),
       (27, 31, 11, 3, 'Course requires immediate attention', FALSE),
       (28, 32, 12, 1, 'Course general reminder', FALSE),
       (29, 33, 13, 2, 'Course progress reminder', FALSE),
       (30, 34, 14, 3, 'Course requires immediate attention', FALSE)
ON DUPLICATE KEY UPDATE
    person_id = VALUES(person_id),
    course_id = VALUES(course_id),
    priority = VALUES(priority),
    message = VALUES(message),
    is_resolved = VALUES(is_resolved);



-- =============================================
-- End of Sample Data
-- =============================================

COMMIT;
