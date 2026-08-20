-- =============================================
-- Center Operation System
-- Sample Data
-- MySQL 8+
-- =============================================

USE
center_operation;


-- =============================================
-- 1. Insert Persons
-- =============================================

INSERT INTO persons
    (name, email, phone, status)
VALUES ('王小明', 'ming@example.com', '0912000001', 'ACTIVE'),

       ('陳小華', 'hua@example.com', '0912000002', 'ACTIVE'),

       ('林志偉', 'wei@example.com', '0912000003', 'ACTIVE'),

       ('張雅婷', 'ting@example.com', '0912000004', 'ACTIVE'),

       ('李冠宇', 'guan@example.com', '0912000005', 'ACTIVE'),

       ('黃俊傑', 'jun@example.com', '0912000006', 'ACTIVE'),

       ('吳佳穎', 'ying@example.com', '0912000007', 'ACTIVE'),

       ('劉家豪', 'hao@example.com', '0912000008', 'ACTIVE'),

       ('蔡佩珊', 'shan@example.com', '0912000009', 'ACTIVE'),

       ('周建宏', 'hong@example.com', '0912000010', 'INACTIVE');



-- =============================================
-- 2. Insert Courses
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
VALUES ('JAVA-001',
        'Java Basic',
        'Java programming fundamental course'),


       ('JAVA-002',
        'Object Oriented Programming',
        'OOP concepts including class, object, inheritance'),


       ('DS-001',
        'Data Structure',
        'Array, Linked List, Tree, Graph and Hash Table'),


       ('ALG-001',
        'Algorithm',
        'Sorting, searching and algorithm analysis'),


       ('DB-001',
        'Database',
        'SQL and relational database design'),


       ('WEB-001',
        'Web Programming',
        'HTML CSS JavaScript and Web fundamentals'),


       ('SPRING-001',
        'Spring Boot',
        'Backend development using Spring Boot'),


       ('BACKEND-001',
        'Backend Engineering',
        'REST API and system architecture'),


       ('AI-001',
        'Artificial Intelligence',
        'Machine learning and AI introduction'),


       ('CLOUD-001',
        'Cloud Computing',
        'Cloud deployment and container technology');



-- =============================================
-- 3. Insert Course Prerequisite
--
-- Used by CourseGraph
-- =============================================


-- OOP requires Java Basic

INSERT INTO course_prerequisites
    (course_id, prerequisite_id)
VALUES (2, 1);


-- Data Structure requires OOP

INSERT INTO course_prerequisites
    (course_id, prerequisite_id)
VALUES (3, 2);


-- Algorithm requires Data Structure

INSERT INTO course_prerequisites
    (course_id, prerequisite_id)
VALUES (4, 3);


-- Spring Boot requires Java

INSERT INTO course_prerequisites
    (course_id, prerequisite_id)
VALUES (7, 1);


-- Backend requires Spring Boot

INSERT INTO course_prerequisites
    (course_id, prerequisite_id)
VALUES (8, 7);


-- Backend requires Database

INSERT INTO course_prerequisites
    (course_id, prerequisite_id)
VALUES (8, 5);


-- Cloud requires Backend

INSERT INTO course_prerequisites
    (course_id, prerequisite_id)
VALUES (10, 8);



-- =============================================
-- 4. Insert Enrollment
--
-- Learning Progress Data
-- =============================================


INSERT INTO enrollments
    (person_id, course_id, status, start_date, complete_date)
VALUES


-- Person 1

(1, 1, 'COMPLETED', '2026-01-05', '2026-01-20'),

(1, 2, 'COMPLETED', '2026-02-01', '2026-02-20'),

(1, 3, 'IN_PROGRESS', '2026-03-01', NULL),


-- Person 2

(2, 1, 'COMPLETED', '2026-01-10', '2026-01-25'),

(2, 2, 'IN_PROGRESS', '2026-02-10', NULL),

(2, 5, 'COMPLETED', '2026-01-15', '2026-02-01'),


-- Person 3

(3, 1, 'COMPLETED', '2026-01-03', '2026-01-18'),

(3, 2, 'COMPLETED', '2026-02-01', '2026-02-18'),

(3, 3, 'COMPLETED', '2026-03-01', '2026-03-25'),

(3, 4, 'IN_PROGRESS', '2026-04-01', NULL),


-- Person 4

(4, 6, 'COMPLETED', '2026-01-05', '2026-01-30'),

(4, 7, 'IN_PROGRESS', '2026-03-01', NULL),


-- Person 5

(5, 1, 'COMPLETED', '2026-01-01', '2026-01-15'),

(5, 5, 'COMPLETED', '2026-02-01', '2026-02-15'),

(5, 7, 'COMPLETED', '2026-03-01', '2026-03-30'),

(5, 8, 'IN_PROGRESS', '2026-04-01', NULL),


-- Person 6

(6, 1, 'COMPLETED', '2026-01-01', '2026-01-20'),

(6, 2, 'IN_PROGRESS', '2026-02-01', NULL),


-- Person 7

(7, 5, 'COMPLETED', '2026-01-10', '2026-01-30'),

(7, 6, 'IN_PROGRESS', '2026-02-01', NULL),


-- Person 8

(8, 7, 'NOT_STARTED', NULL, NULL),

(8, 8, 'NOT_STARTED', NULL, NULL),


-- Person 9

(9, 1, 'COMPLETED', '2026-01-05', '2026-01-25'),

(9, 9, 'IN_PROGRESS', '2026-04-01', NULL),


-- Person 10

(10, 10, 'NOT_STARTED', NULL, NULL);



-- =============================================
-- 5. Insert Alerts
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
    (person_id, course_id, priority, message, is_resolved)
VALUES (1,
        3,
        3,
        'Data Structure progress is overdue',
        FALSE),


       (2,
        2,
        2,
        'OOP course progress reminder',
        FALSE),


       (5,
        8,
        3,
        'Backend course requires attention',
        FALSE),


       (7,
        6,
        1,
        'Web course weekly reminder',
        FALSE),


       (8,
        7,
        2,
        'Spring Boot course not started',
        FALSE),


       (10,
        10,
        1,
        'Cloud Computing course available',
        FALSE);



-- =============================================
-- End of Sample Data
-- =============================================