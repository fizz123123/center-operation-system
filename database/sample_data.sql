-- =============================================
-- Center Operation System
-- Sample Data
-- MySQL 8+
-- =============================================

USE center_operation;


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


-- Additional demo persons (IDs 11-200)

INSERT INTO persons
    (name, email, phone, status)
VALUES
       ('Demo Student 011', 'student011@example.com', '0900000011', 'ACTIVE'),
       ('Demo Student 012', 'student012@example.com', '0900000012', 'ACTIVE'),
       ('Demo Student 013', 'student013@example.com', '0900000013', 'ACTIVE'),
       ('Demo Student 014', 'student014@example.com', '0900000014', 'ACTIVE'),
       ('Demo Student 015', 'student015@example.com', '0900000015', 'ACTIVE'),
       ('Demo Student 016', 'student016@example.com', '0900000016', 'ACTIVE'),
       ('Demo Student 017', 'student017@example.com', '0900000017', 'ACTIVE'),
       ('Demo Student 018', 'student018@example.com', '0900000018', 'ACTIVE'),
       ('Demo Student 019', 'student019@example.com', '0900000019', 'ACTIVE'),
       ('Demo Student 020', 'student020@example.com', '0900000020', 'INACTIVE'),
       ('Demo Student 021', 'student021@example.com', '0900000021', 'ACTIVE'),
       ('Demo Student 022', 'student022@example.com', '0900000022', 'ACTIVE'),
       ('Demo Student 023', 'student023@example.com', '0900000023', 'ACTIVE'),
       ('Demo Student 024', 'student024@example.com', '0900000024', 'ACTIVE'),
       ('Demo Student 025', 'student025@example.com', '0900000025', 'ACTIVE'),
       ('Demo Student 026', 'student026@example.com', '0900000026', 'ACTIVE'),
       ('Demo Student 027', 'student027@example.com', '0900000027', 'ACTIVE'),
       ('Demo Student 028', 'student028@example.com', '0900000028', 'ACTIVE'),
       ('Demo Student 029', 'student029@example.com', '0900000029', 'ACTIVE'),
       ('Demo Student 030', 'student030@example.com', '0900000030', 'INACTIVE'),
       ('Demo Student 031', 'student031@example.com', '0900000031', 'ACTIVE'),
       ('Demo Student 032', 'student032@example.com', '0900000032', 'ACTIVE'),
       ('Demo Student 033', 'student033@example.com', '0900000033', 'ACTIVE'),
       ('Demo Student 034', 'student034@example.com', '0900000034', 'ACTIVE'),
       ('Demo Student 035', 'student035@example.com', '0900000035', 'ACTIVE'),
       ('Demo Student 036', 'student036@example.com', '0900000036', 'ACTIVE'),
       ('Demo Student 037', 'student037@example.com', '0900000037', 'ACTIVE'),
       ('Demo Student 038', 'student038@example.com', '0900000038', 'ACTIVE'),
       ('Demo Student 039', 'student039@example.com', '0900000039', 'ACTIVE'),
       ('Demo Student 040', 'student040@example.com', '0900000040', 'INACTIVE'),
       ('Demo Student 041', 'student041@example.com', '0900000041', 'ACTIVE'),
       ('Demo Student 042', 'student042@example.com', '0900000042', 'ACTIVE'),
       ('Demo Student 043', 'student043@example.com', '0900000043', 'ACTIVE'),
       ('Demo Student 044', 'student044@example.com', '0900000044', 'ACTIVE'),
       ('Demo Student 045', 'student045@example.com', '0900000045', 'ACTIVE'),
       ('Demo Student 046', 'student046@example.com', '0900000046', 'ACTIVE'),
       ('Demo Student 047', 'student047@example.com', '0900000047', 'ACTIVE'),
       ('Demo Student 048', 'student048@example.com', '0900000048', 'ACTIVE'),
       ('Demo Student 049', 'student049@example.com', '0900000049', 'ACTIVE'),
       ('Demo Student 050', 'student050@example.com', '0900000050', 'INACTIVE'),
       ('Demo Student 051', 'student051@example.com', '0900000051', 'ACTIVE'),
       ('Demo Student 052', 'student052@example.com', '0900000052', 'ACTIVE'),
       ('Demo Student 053', 'student053@example.com', '0900000053', 'ACTIVE'),
       ('Demo Student 054', 'student054@example.com', '0900000054', 'ACTIVE'),
       ('Demo Student 055', 'student055@example.com', '0900000055', 'ACTIVE'),
       ('Demo Student 056', 'student056@example.com', '0900000056', 'ACTIVE'),
       ('Demo Student 057', 'student057@example.com', '0900000057', 'ACTIVE'),
       ('Demo Student 058', 'student058@example.com', '0900000058', 'ACTIVE'),
       ('Demo Student 059', 'student059@example.com', '0900000059', 'ACTIVE'),
       ('Demo Student 060', 'student060@example.com', '0900000060', 'INACTIVE'),
       ('Demo Student 061', 'student061@example.com', '0900000061', 'ACTIVE'),
       ('Demo Student 062', 'student062@example.com', '0900000062', 'ACTIVE'),
       ('Demo Student 063', 'student063@example.com', '0900000063', 'ACTIVE'),
       ('Demo Student 064', 'student064@example.com', '0900000064', 'ACTIVE'),
       ('Demo Student 065', 'student065@example.com', '0900000065', 'ACTIVE'),
       ('Demo Student 066', 'student066@example.com', '0900000066', 'ACTIVE'),
       ('Demo Student 067', 'student067@example.com', '0900000067', 'ACTIVE'),
       ('Demo Student 068', 'student068@example.com', '0900000068', 'ACTIVE'),
       ('Demo Student 069', 'student069@example.com', '0900000069', 'ACTIVE'),
       ('Demo Student 070', 'student070@example.com', '0900000070', 'INACTIVE'),
       ('Demo Student 071', 'student071@example.com', '0900000071', 'ACTIVE'),
       ('Demo Student 072', 'student072@example.com', '0900000072', 'ACTIVE'),
       ('Demo Student 073', 'student073@example.com', '0900000073', 'ACTIVE'),
       ('Demo Student 074', 'student074@example.com', '0900000074', 'ACTIVE'),
       ('Demo Student 075', 'student075@example.com', '0900000075', 'ACTIVE'),
       ('Demo Student 076', 'student076@example.com', '0900000076', 'ACTIVE'),
       ('Demo Student 077', 'student077@example.com', '0900000077', 'ACTIVE'),
       ('Demo Student 078', 'student078@example.com', '0900000078', 'ACTIVE'),
       ('Demo Student 079', 'student079@example.com', '0900000079', 'ACTIVE'),
       ('Demo Student 080', 'student080@example.com', '0900000080', 'INACTIVE'),
       ('Demo Student 081', 'student081@example.com', '0900000081', 'ACTIVE'),
       ('Demo Student 082', 'student082@example.com', '0900000082', 'ACTIVE'),
       ('Demo Student 083', 'student083@example.com', '0900000083', 'ACTIVE'),
       ('Demo Student 084', 'student084@example.com', '0900000084', 'ACTIVE'),
       ('Demo Student 085', 'student085@example.com', '0900000085', 'ACTIVE'),
       ('Demo Student 086', 'student086@example.com', '0900000086', 'ACTIVE'),
       ('Demo Student 087', 'student087@example.com', '0900000087', 'ACTIVE'),
       ('Demo Student 088', 'student088@example.com', '0900000088', 'ACTIVE'),
       ('Demo Student 089', 'student089@example.com', '0900000089', 'ACTIVE'),
       ('Demo Student 090', 'student090@example.com', '0900000090', 'INACTIVE'),
       ('Demo Student 091', 'student091@example.com', '0900000091', 'ACTIVE'),
       ('Demo Student 092', 'student092@example.com', '0900000092', 'ACTIVE'),
       ('Demo Student 093', 'student093@example.com', '0900000093', 'ACTIVE'),
       ('Demo Student 094', 'student094@example.com', '0900000094', 'ACTIVE'),
       ('Demo Student 095', 'student095@example.com', '0900000095', 'ACTIVE'),
       ('Demo Student 096', 'student096@example.com', '0900000096', 'ACTIVE'),
       ('Demo Student 097', 'student097@example.com', '0900000097', 'ACTIVE'),
       ('Demo Student 098', 'student098@example.com', '0900000098', 'ACTIVE'),
       ('Demo Student 099', 'student099@example.com', '0900000099', 'ACTIVE'),
       ('Demo Student 100', 'student100@example.com', '0900000100', 'INACTIVE'),
       ('Demo Student 101', 'student101@example.com', '0900000101', 'ACTIVE'),
       ('Demo Student 102', 'student102@example.com', '0900000102', 'ACTIVE'),
       ('Demo Student 103', 'student103@example.com', '0900000103', 'ACTIVE'),
       ('Demo Student 104', 'student104@example.com', '0900000104', 'ACTIVE'),
       ('Demo Student 105', 'student105@example.com', '0900000105', 'ACTIVE'),
       ('Demo Student 106', 'student106@example.com', '0900000106', 'ACTIVE'),
       ('Demo Student 107', 'student107@example.com', '0900000107', 'ACTIVE'),
       ('Demo Student 108', 'student108@example.com', '0900000108', 'ACTIVE'),
       ('Demo Student 109', 'student109@example.com', '0900000109', 'ACTIVE'),
       ('Demo Student 110', 'student110@example.com', '0900000110', 'INACTIVE'),
       ('Demo Student 111', 'student111@example.com', '0900000111', 'ACTIVE'),
       ('Demo Student 112', 'student112@example.com', '0900000112', 'ACTIVE'),
       ('Demo Student 113', 'student113@example.com', '0900000113', 'ACTIVE'),
       ('Demo Student 114', 'student114@example.com', '0900000114', 'ACTIVE'),
       ('Demo Student 115', 'student115@example.com', '0900000115', 'ACTIVE'),
       ('Demo Student 116', 'student116@example.com', '0900000116', 'ACTIVE'),
       ('Demo Student 117', 'student117@example.com', '0900000117', 'ACTIVE'),
       ('Demo Student 118', 'student118@example.com', '0900000118', 'ACTIVE'),
       ('Demo Student 119', 'student119@example.com', '0900000119', 'ACTIVE'),
       ('Demo Student 120', 'student120@example.com', '0900000120', 'INACTIVE'),
       ('Demo Student 121', 'student121@example.com', '0900000121', 'ACTIVE'),
       ('Demo Student 122', 'student122@example.com', '0900000122', 'ACTIVE'),
       ('Demo Student 123', 'student123@example.com', '0900000123', 'ACTIVE'),
       ('Demo Student 124', 'student124@example.com', '0900000124', 'ACTIVE'),
       ('Demo Student 125', 'student125@example.com', '0900000125', 'ACTIVE'),
       ('Demo Student 126', 'student126@example.com', '0900000126', 'ACTIVE'),
       ('Demo Student 127', 'student127@example.com', '0900000127', 'ACTIVE'),
       ('Demo Student 128', 'student128@example.com', '0900000128', 'ACTIVE'),
       ('Demo Student 129', 'student129@example.com', '0900000129', 'ACTIVE'),
       ('Demo Student 130', 'student130@example.com', '0900000130', 'INACTIVE'),
       ('Demo Student 131', 'student131@example.com', '0900000131', 'ACTIVE'),
       ('Demo Student 132', 'student132@example.com', '0900000132', 'ACTIVE'),
       ('Demo Student 133', 'student133@example.com', '0900000133', 'ACTIVE'),
       ('Demo Student 134', 'student134@example.com', '0900000134', 'ACTIVE'),
       ('Demo Student 135', 'student135@example.com', '0900000135', 'ACTIVE'),
       ('Demo Student 136', 'student136@example.com', '0900000136', 'ACTIVE'),
       ('Demo Student 137', 'student137@example.com', '0900000137', 'ACTIVE'),
       ('Demo Student 138', 'student138@example.com', '0900000138', 'ACTIVE'),
       ('Demo Student 139', 'student139@example.com', '0900000139', 'ACTIVE'),
       ('Demo Student 140', 'student140@example.com', '0900000140', 'INACTIVE'),
       ('Demo Student 141', 'student141@example.com', '0900000141', 'ACTIVE'),
       ('Demo Student 142', 'student142@example.com', '0900000142', 'ACTIVE'),
       ('Demo Student 143', 'student143@example.com', '0900000143', 'ACTIVE'),
       ('Demo Student 144', 'student144@example.com', '0900000144', 'ACTIVE'),
       ('Demo Student 145', 'student145@example.com', '0900000145', 'ACTIVE'),
       ('Demo Student 146', 'student146@example.com', '0900000146', 'ACTIVE'),
       ('Demo Student 147', 'student147@example.com', '0900000147', 'ACTIVE'),
       ('Demo Student 148', 'student148@example.com', '0900000148', 'ACTIVE'),
       ('Demo Student 149', 'student149@example.com', '0900000149', 'ACTIVE'),
       ('Demo Student 150', 'student150@example.com', '0900000150', 'INACTIVE'),
       ('Demo Student 151', 'student151@example.com', '0900000151', 'ACTIVE'),
       ('Demo Student 152', 'student152@example.com', '0900000152', 'ACTIVE'),
       ('Demo Student 153', 'student153@example.com', '0900000153', 'ACTIVE'),
       ('Demo Student 154', 'student154@example.com', '0900000154', 'ACTIVE'),
       ('Demo Student 155', 'student155@example.com', '0900000155', 'ACTIVE'),
       ('Demo Student 156', 'student156@example.com', '0900000156', 'ACTIVE'),
       ('Demo Student 157', 'student157@example.com', '0900000157', 'ACTIVE'),
       ('Demo Student 158', 'student158@example.com', '0900000158', 'ACTIVE'),
       ('Demo Student 159', 'student159@example.com', '0900000159', 'ACTIVE'),
       ('Demo Student 160', 'student160@example.com', '0900000160', 'INACTIVE'),
       ('Demo Student 161', 'student161@example.com', '0900000161', 'ACTIVE'),
       ('Demo Student 162', 'student162@example.com', '0900000162', 'ACTIVE'),
       ('Demo Student 163', 'student163@example.com', '0900000163', 'ACTIVE'),
       ('Demo Student 164', 'student164@example.com', '0900000164', 'ACTIVE'),
       ('Demo Student 165', 'student165@example.com', '0900000165', 'ACTIVE'),
       ('Demo Student 166', 'student166@example.com', '0900000166', 'ACTIVE'),
       ('Demo Student 167', 'student167@example.com', '0900000167', 'ACTIVE'),
       ('Demo Student 168', 'student168@example.com', '0900000168', 'ACTIVE'),
       ('Demo Student 169', 'student169@example.com', '0900000169', 'ACTIVE'),
       ('Demo Student 170', 'student170@example.com', '0900000170', 'INACTIVE'),
       ('Demo Student 171', 'student171@example.com', '0900000171', 'ACTIVE'),
       ('Demo Student 172', 'student172@example.com', '0900000172', 'ACTIVE'),
       ('Demo Student 173', 'student173@example.com', '0900000173', 'ACTIVE'),
       ('Demo Student 174', 'student174@example.com', '0900000174', 'ACTIVE'),
       ('Demo Student 175', 'student175@example.com', '0900000175', 'ACTIVE'),
       ('Demo Student 176', 'student176@example.com', '0900000176', 'ACTIVE'),
       ('Demo Student 177', 'student177@example.com', '0900000177', 'ACTIVE'),
       ('Demo Student 178', 'student178@example.com', '0900000178', 'ACTIVE'),
       ('Demo Student 179', 'student179@example.com', '0900000179', 'ACTIVE'),
       ('Demo Student 180', 'student180@example.com', '0900000180', 'INACTIVE'),
       ('Demo Student 181', 'student181@example.com', '0900000181', 'ACTIVE'),
       ('Demo Student 182', 'student182@example.com', '0900000182', 'ACTIVE'),
       ('Demo Student 183', 'student183@example.com', '0900000183', 'ACTIVE'),
       ('Demo Student 184', 'student184@example.com', '0900000184', 'ACTIVE'),
       ('Demo Student 185', 'student185@example.com', '0900000185', 'ACTIVE'),
       ('Demo Student 186', 'student186@example.com', '0900000186', 'ACTIVE'),
       ('Demo Student 187', 'student187@example.com', '0900000187', 'ACTIVE'),
       ('Demo Student 188', 'student188@example.com', '0900000188', 'ACTIVE'),
       ('Demo Student 189', 'student189@example.com', '0900000189', 'ACTIVE'),
       ('Demo Student 190', 'student190@example.com', '0900000190', 'INACTIVE'),
       ('Demo Student 191', 'student191@example.com', '0900000191', 'ACTIVE'),
       ('Demo Student 192', 'student192@example.com', '0900000192', 'ACTIVE'),
       ('Demo Student 193', 'student193@example.com', '0900000193', 'ACTIVE'),
       ('Demo Student 194', 'student194@example.com', '0900000194', 'ACTIVE'),
       ('Demo Student 195', 'student195@example.com', '0900000195', 'ACTIVE'),
       ('Demo Student 196', 'student196@example.com', '0900000196', 'ACTIVE'),
       ('Demo Student 197', 'student197@example.com', '0900000197', 'ACTIVE'),
       ('Demo Student 198', 'student198@example.com', '0900000198', 'ACTIVE'),
       ('Demo Student 199', 'student199@example.com', '0900000199', 'ACTIVE'),
       ('Demo Student 200', 'student200@example.com', '0900000200', 'INACTIVE');



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


-- Additional demo courses (IDs 11-20)

INSERT INTO courses
    (code, name, description)
VALUES
       ('SEC-001', 'Application Security', 'Authentication, authorization and secure coding'),
       ('TEST-001', 'Software Testing', 'Unit, integration and API testing'),
       ('DEVOPS-001', 'DevOps Fundamentals', 'CI/CD and delivery workflow'),
       ('API-001', 'REST API Design', 'RESTful conventions and API contracts'),
       ('DESIGN-001', 'System Design', 'Scalable system design fundamentals'),
       ('GIT-001', 'Git Collaboration', 'Branching, review and team workflow'),
       ('LINUX-001', 'Linux Fundamentals', 'Command line and server administration'),
       ('NETWORK-001', 'Computer Networks', 'Network protocols and web communication'),
       ('PYTHON-001', 'Python Basic', 'Python programming fundamentals'),
       ('PROJECT-001', 'Software Project', 'End-to-end team project practice');



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
WHERE p.id BETWEEN 1 AND 200;



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


-- Additional demo alerts: 8 per priority level

INSERT INTO alerts
    (person_id, course_id, priority, message, is_resolved)
VALUES
       (11, 11, 1, 'Course general reminder', FALSE),
       (12, 12, 2, 'Course progress reminder', FALSE),
       (13, 13, 3, 'Course requires immediate attention', FALSE),
       (14, 14, 1, 'Course general reminder', FALSE),
       (15, 15, 2, 'Course progress reminder', FALSE),
       (16, 16, 3, 'Course requires immediate attention', FALSE),
       (17, 17, 1, 'Course general reminder', FALSE),
       (18, 18, 2, 'Course progress reminder', FALSE),
       (19, 19, 3, 'Course requires immediate attention', FALSE),
       (20, 20, 1, 'Course general reminder', FALSE),
       (21, 1, 2, 'Course progress reminder', FALSE),
       (22, 2, 3, 'Course requires immediate attention', FALSE),
       (23, 3, 1, 'Course general reminder', FALSE),
       (24, 4, 2, 'Course progress reminder', FALSE),
       (25, 5, 3, 'Course requires immediate attention', FALSE),
       (26, 6, 1, 'Course general reminder', FALSE),
       (27, 7, 2, 'Course progress reminder', FALSE),
       (28, 8, 3, 'Course requires immediate attention', FALSE),
       (29, 9, 1, 'Course general reminder', FALSE),
       (30, 10, 2, 'Course progress reminder', FALSE),
       (31, 11, 3, 'Course requires immediate attention', FALSE),
       (32, 12, 1, 'Course general reminder', FALSE),
       (33, 13, 2, 'Course progress reminder', FALSE),
       (34, 14, 3, 'Course requires immediate attention', FALSE);



-- =============================================
-- End of Sample Data
-- =============================================
