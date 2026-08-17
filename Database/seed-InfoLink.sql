USE InfoLink;
GO

-- 1. Seed Groups (no identity column -> explicit IDs)
IF NOT EXISTS (SELECT 1 FROM Groups WHERE Group_ID = 1)
INSERT INTO Groups (Group_ID, Group_Name, Descriptionn, Is_Active) VALUES
(1, 'HR',                    'Human Resources',          1),
(2, 'Sales',                 'Sales department',         1),
(3, 'Finance',               'Finance department',       1),
(4, 'Management',            'Management team',          1),
(5, 'Administration',        'Administration',           1),
(6, 'Digital Marketing',     'Digital Marketing',        1),
(7, 'Operations',            'Operations',               1),
(8, 'IT Support',            'IT Support',               1);
GO

-- 2. Add Collection_Name column (does not exist yet)
IF COL_LENGTH('Group_Collections', 'Collection_Name') IS NULL
    ALTER TABLE Group_Collections ADD Collection_Name nvarchar(255) NULL;
GO

-- 3. Remove stale rows (all have NULL Group_ID, table has no identity)
DELETE FROM Group_Collections;
GO

-- 4. Seed group-collection mapping (Collection_ID is PK -> globally unique)
-- Collections: Contracts, Customers, Employees, Invoices, Orders, Payments
INSERT INTO Group_Collections (Collection_ID, Group_ID, Collection_Name) VALUES
-- Management -> all 6
(1, 4, 'Contracts'),  (2, 4, 'Customers'),  (3, 4, 'Employees'),  (4, 4, 'Invoices'),  (5, 4, 'Orders'),  (6, 4, 'Payments'),
-- Administration -> all 6
(7, 5, 'Contracts'),  (8, 5, 'Customers'),  (9, 5, 'Employees'),  (10, 5, 'Invoices'), (11, 5, 'Orders'), (12, 5, 'Payments'),
-- HR -> Employees, Contracts
(13, 1, 'Employees'), (14, 1, 'Contracts'),
-- Sales -> Customers, Orders
(15, 2, 'Customers'), (16, 2, 'Orders'),
-- Finance -> Payments, Invoices
(17, 3, 'Payments'),  (18, 3, 'Invoices'),
-- Digital Marketing -> Customers, Orders
(19, 6, 'Customers'), (20, 6, 'Orders'),
-- IT Support -> Employees
(21, 8, 'Employees');
GO

-- 5. Re-hash plain-text passwords to BCrypt (must match BCryptPasswordEncoder)
UPDATE Users SET Password_Hash = '$2a$10$Wmg1SRBfrDlFmBaMiCo3R.D4puZ9qRWPPlLZP64qHS87MPtguzLTC' WHERE Username = 'admin';      -- Admin@123
UPDATE Users SET Password_Hash = '$2a$10$N2usTfsfTMirM/7tJ3ETT.dnTACdKtM6q4Cj7Ae8jVOj4NDiPPxXi' WHERE Username = 'sara.m';    -- Sara@123
UPDATE Users SET Password_Hash = '$2a$10$6e2Ih.aaaluqon16ghgZOuRN4dKety5GoofZn/o5ULn3MG6Jr16/a' WHERE Username = 'omar.f';    -- Omar Farouk@123
UPDATE Users SET Password_Hash = '$2a$10$Di8YHOKlVjGeUiOZse.Nq.gVSgGdziB3foCg68YjDPawegXTT.f9m' WHERE Username = 'nour.s';    -- Nour@123
UPDATE Users SET Password_Hash = '$2a$10$tkxK8dF4IwU9vh8/gpke2.E39HKafFpys.r3F1m8qyWSd60AbbN9S' WHERE Username = 'mostafa.a'; -- Mostafa@123
UPDATE Users SET Password_Hash = '$2a$10$u4qCmDF1s6IfpEdRYaSr8eFncx4i40LkU1M52KVFo2tBNOWlkrHju' WHERE Username = 'heba.a';    -- Heba@123
UPDATE Users SET Password_Hash = '$2a$10$g7Vcz1ICi4qo6xwFcSzRueiRMOw.L8Ov0YmD8t94CP7w2aVIJZznm' WHERE Username = 'youssef.a'; -- Youssef@123
UPDATE Users SET Password_Hash = '$2a$10$Bdc.My/fwiZ3E5.m/iKvNuvJh07bUq4Og5vjqeNvnfHrno.u6eU5m' WHERE Username = 'laila.k';   -- Laila@123
UPDATE Users SET Password_Hash = '$2a$10$MF1QekkLrMo7ljB0LWYv.edZI/1U7qrKSCsaaw4avcn6VPYLuZsIy' WHERE Username = 'mariam.i';  -- Mariam@123
UPDATE Users SET Password_Hash = '$2a$10$xXDVvrxdWxfHpZWv1Si4fe0tlPUv2GcTI2/XbQafvsLO6HziD7lV2' WHERE Username = 'ali.s';     -- Ali@123
UPDATE Users SET Password_Hash = '$2a$10$W68sOc6u9fO/GPol8KO.jukK9DpiaI6aCO9bEJ5D1d2gYm/TcdjeO' WHERE Username = 'farah.h';   -- Farah@123
UPDATE Users SET Password_Hash = '$2a$10$enK4vAQ4hHKuDh7ASQL.zu9UUkLxOC9VKmHKtpUTuRjE9K7sN9dOG' WHERE Username = 'karim.n';   -- Karim@123
UPDATE Users SET Password_Hash = '$2a$10$chFeLWPAAu41muMUFt5bZeCW0D9WLQger6oxvYutDCbv6B9UXk7Gi' WHERE Username = 'dina.w';    -- Dina@123
UPDATE Users SET Password_Hash = '$2a$10$6i2jiUa5vc4VJeyvX9konO8/Hum9Q.yIYBrvgS4XQ4NahWuvkRdEC' WHERE Username = 'rana.a';    -- Rana@123
UPDATE Users SET Password_Hash = '$2a$10$pw3ysQfnsuu4e8yB7YoOH.x2kf4d8FYVq7SitF/5FZ14UDFuW6ryu' WHERE Username = 'eman.y';    -- Eman@123
GO
