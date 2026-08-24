-- =============================================
-- Center Operation System
-- Demo/Test Data Reset
-- MySQL 8+
--
-- WARNING: This script deletes all application data before rebuilding a
-- deterministic demo dataset. Use it only in local, demo, or test databases.
-- It may be executed in full repeatedly and produces the same baseline.
-- =============================================

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

USE center_operation;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE alerts;
TRUNCATE TABLE course_prerequisites;
TRUNCATE TABLE enrollments;
TRUNCATE TABLE courses;
TRUNCATE TABLE persons;
SET FOREIGN_KEY_CHECKS = 1;

START TRANSACTION;


-- =============================================
-- 1. Persons (200 seed rows)
--
-- IDs 1-10 use named demo people.
-- IDs 11-200 use deterministic three-character Chinese names.
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
        ELSE CONCAT(
            SUBSTRING('王李張劉陳楊黃趙吳周徐孫馬朱胡郭何高林鄭', MOD(person_id - 1, 20) + 1, 1),
            SUBSTRING('子宇俊雅家思承雨冠欣', MOD(FLOOR((person_id - 1) / 20), 10) + 1, 1),
            SUBSTRING('豪婷軒涵傑妤恩維蓉凱', MOD(person_id + FLOOR((person_id - 1) / 20), 10) + 1, 1)
        )
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
-- Foundations branch into programming, backend, infrastructure, AI,
-- testing and security paths. Multiple advanced paths converge on the
-- Software Project capstone.
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
-- 3. Course prerequisites (26 seed rows)
--
-- A directed acyclic graph with branches, merges and long paths for
-- CourseGraph, DFS cycle detection and Topological Sort demonstrations.
-- =============================================


INSERT INTO course_prerequisites
    (course_id, prerequisite_id)
VALUES
    (2, 1),    -- OOP requires Java Basic
    (3, 2),    -- Data Structure requires OOP
    (4, 3),    -- Algorithm requires Data Structure
    (7, 1),    -- Spring Boot requires Java Basic
    (14, 6),   -- REST API Design requires Web Programming
    (14, 2),   -- REST API Design requires OOP
    (8, 7),    -- Backend Engineering requires Spring Boot
    (8, 5),    -- Backend Engineering requires Database
    (8, 14),   -- Backend Engineering requires REST API Design
    (9, 19),   -- Artificial Intelligence requires Python Basic
    (9, 4),    -- Artificial Intelligence requires Algorithm
    (13, 16),  -- DevOps Fundamentals requires Git Collaboration
    (13, 17),  -- DevOps Fundamentals requires Linux Fundamentals
    (10, 8),   -- Cloud Computing requires Backend Engineering
    (10, 13),  -- Cloud Computing requires DevOps Fundamentals
    (10, 18),  -- Cloud Computing requires Computer Networks
    (11, 8),   -- Application Security requires Backend Engineering
    (11, 18),  -- Application Security requires Computer Networks
    (12, 2),   -- Software Testing requires OOP
    (12, 7),   -- Software Testing requires Spring Boot
    (12, 16),  -- Software Testing requires Git Collaboration
    (15, 8),   -- System Design requires Backend Engineering
    (20, 12),  -- Software Project requires Software Testing
    (20, 13),  -- Software Project requires DevOps Fundamentals
    (20, 15),  -- Software Project requires System Design
    (20, 11)   -- Software Project requires Application Security
ON DUPLICATE KEY UPDATE
    prerequisite_id = VALUES(prerequisite_id);



-- =============================================
-- 4. Enrollments (1,000 generated seed rows)
--
-- Learning Progress Data
--
-- Each person has one target course plus every direct and transitive
-- prerequisite required by that target. All prerequisite enrollments are
-- COMPLETED before the target course starts.
--
-- The target-course cohort sizes are chosen so the prerequisite closures
-- produce exactly 1,000 enrollment rows across 200 persons.
-- Distribution:
-- 85% COMPLETED (all prerequisites plus 50 completed targets)
--  5% IN_PROGRESS for 45 days (MEDIUM alert candidates)
--  5% IN_PROGRESS for 120 days (HIGH alert candidates)
--  5% NOT_STARTED (LOW alert candidates)
-- =============================================


INSERT INTO enrollments
    (person_id, course_id, status, start_date, complete_date)
WITH RECURSIVE learning_plans AS (
    SELECT 1 AS first_person_id, 3 AS last_person_id, 20 AS target_course_id
    UNION ALL SELECT 4, 11, 10
    UNION ALL SELECT 12, 23, 11
    UNION ALL SELECT 24, 41, 15
    UNION ALL SELECT 42, 57, 9
    UNION ALL SELECT 58, 83, 8
    UNION ALL SELECT 84, 104, 12
    UNION ALL SELECT 105, 116, 14
    UNION ALL SELECT 117, 128, 4
    UNION ALL SELECT 129, 138, 13
    UNION ALL SELECT 139, 148, 3
    UNION ALL SELECT 149, 156, 2
    UNION ALL SELECT 157, 164, 7
    UNION ALL SELECT 165, 170, 1
    UNION ALL SELECT 171, 175, 5
    UNION ALL SELECT 176, 180, 6
    UNION ALL SELECT 181, 185, 16
    UNION ALL SELECT 186, 190, 17
    UNION ALL SELECT 191, 195, 18
    UNION ALL SELECT 196, 200, 19
), course_stages AS (
    SELECT 1 AS course_id, 1 AS stage
    UNION ALL SELECT 5, 1
    UNION ALL SELECT 6, 1
    UNION ALL SELECT 16, 1
    UNION ALL SELECT 17, 1
    UNION ALL SELECT 18, 1
    UNION ALL SELECT 19, 1
    UNION ALL SELECT 2, 2
    UNION ALL SELECT 7, 2
    UNION ALL SELECT 13, 2
    UNION ALL SELECT 3, 3
    UNION ALL SELECT 12, 3
    UNION ALL SELECT 14, 3
    UNION ALL SELECT 4, 4
    UNION ALL SELECT 8, 4
    UNION ALL SELECT 9, 5
    UNION ALL SELECT 10, 5
    UNION ALL SELECT 11, 5
    UNION ALL SELECT 15, 5
    UNION ALL SELECT 20, 6
), course_requirements AS (
    SELECT id AS target_course_id, id AS required_course_id
    FROM courses

    UNION DISTINCT

    SELECT
        requirements.target_course_id,
        relation.prerequisite_id
    FROM course_requirements requirements
    JOIN course_prerequisites relation
        ON relation.course_id = requirements.required_course_id
)
SELECT
    p.id,
    requirements.required_course_id AS course_id,
    CASE
        WHEN requirements.required_course_id <> plan.target_course_id THEN 'COMPLETED'
        WHEN p.id <= 50 THEN 'COMPLETED'
        WHEN p.id <= 150 THEN 'IN_PROGRESS'
        ELSE 'NOT_STARTED'
    END AS status,
    CASE
        WHEN requirements.required_course_id <> plan.target_course_id OR p.id <= 50
            THEN DATE_SUB(CURRENT_DATE, INTERVAL (400 - (stage.stage * 40)) DAY)
        WHEN p.id <= 100 THEN DATE_SUB(CURRENT_DATE, INTERVAL 120 DAY)
        WHEN p.id <= 150 THEN DATE_SUB(CURRENT_DATE, INTERVAL 45 DAY)
        ELSE NULL
    END AS start_date,
    CASE
        WHEN requirements.required_course_id <> plan.target_course_id OR p.id <= 50
            THEN DATE_SUB(CURRENT_DATE, INTERVAL (380 - (stage.stage * 40)) DAY)
        ELSE NULL
    END AS complete_date
FROM persons p
JOIN learning_plans plan
    ON p.id BETWEEN plan.first_person_id AND plan.last_person_id
JOIN course_requirements requirements
    ON requirements.target_course_id = plan.target_course_id
JOIN course_stages stage
    ON stage.course_id = requirements.required_course_id
ON DUPLICATE KEY UPDATE
    status = VALUES(status),
    start_date = VALUES(start_date),
    complete_date = VALUES(complete_date);



-- =============================================
-- 5. Alerts (150 enrollment-derived seed rows)
--
-- Uses the same status/date thresholds as AlertGenerator.
-- Every qualifying Enrollment gets one [AUTO] Alert so the seed data and
-- runtime business rules remain consistent.
-- One enrollment has at most one seed alert; a person may have alerts
-- for multiple courses.
-- =============================================


INSERT INTO alerts
    (id, person_id, course_id, priority, message, is_resolved)
WITH alert_candidates AS (
    SELECT
        e.person_id,
        e.course_id,
        CASE
            WHEN e.status = 'NOT_STARTED' THEN 1
            WHEN DATEDIFF(CURRENT_DATE, e.start_date) >= 90 THEN 3
            ELSE 2
        END AS priority,
        CASE
            WHEN e.status = 'NOT_STARTED' THEN '[AUTO] 尚未開始課程'
            WHEN DATEDIFF(CURRENT_DATE, e.start_date) >= 90
                THEN CONCAT(
                    '[AUTO] 已進行 ',
                    DATEDIFF(CURRENT_DATE, e.start_date),
                    ' 天，可能需要立即協助'
                )
            ELSE CONCAT(
                '[AUTO] 已進行 ',
                DATEDIFF(CURRENT_DATE, e.start_date),
                ' 天，請關注學習進度'
            )
        END AS message
    FROM enrollments e
    WHERE e.status = 'NOT_STARTED'
       OR (
           e.status = 'IN_PROGRESS'
           AND DATEDIFF(CURRENT_DATE, e.start_date) >= 30
       )
), ranked_alerts AS (
    SELECT
        person_id,
        course_id,
        priority,
        message,
        ROW_NUMBER() OVER (
            ORDER BY priority DESC, person_id, course_id
        ) AS alert_id
    FROM alert_candidates
)
SELECT
    alert_id AS id,
    person_id,
    course_id,
    priority,
    message,
    FALSE AS is_resolved
FROM ranked_alerts
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
