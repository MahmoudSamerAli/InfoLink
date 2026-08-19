USE InfoLink

DROP TABLE Group_Collections;
DROP TABLE Logs;
DROP TABLE Users;
DROP TABLE Groups;

CREATE TABLE Groups(
Group_ID INT PRIMARY KEY IDENTITY,
Group_Name NVARCHAR(100) NOT NULL,
Descriptionn NVARCHAR(255),
Is_Active BIT NOT NULL DEFAULT 1            -- 1 = Active, 0 = Inactive
);

CREATE TABLE Group_Collections(
Collection_ID INT PRIMARY KEY IDENTITY,
Group_ID INT NOT NULL REFERENCES Groups(Group_ID),
Collection_Name NVARCHAR(50) NOT NULL       -- name of the MongoDB collection granted
);

CREATE TABLE Users(
User_Idd INT IDENTITY PRIMARY KEY,
Username NVARCHAR(30) UNIQUE NOT NULL,
Password_Hash NVARCHAR(255) NOT NULL,
Full_Name NVARCHAR(150) NOT NULL,
Group_ID INT NOT NULL REFERENCES Groups(Group_ID),
Rolee TINYINT NOT NULL DEFAULT 0,           -- 0 = User, 1 = Admin, 2 = SysAdmin
IS_Active BIT NOT NULL DEFAULT 1,           -- 1 = Active, 0 = InActive
Created_date DATETIME NOT NULL DEFAULT GETDATE(),
CONSTRAINT CK_Users_Rolee CHECK (Rolee IN (0, 1, 2))
);

CREATE TABLE Logs(
Log_ID INT IDENTITY PRIMARY KEY,
User_Idd INT NOT NULL REFERENCES Users(User_Idd),
Collection_Name NVARCHAR(100),
Search_keyword NVARCHAR(100),
Search_date DATETIME DEFAULT GETDATE(),
IP_address NVARCHAR(50),
Statuss BIT NOT NULL DEFAULT 1              -- 1 = Success, 0 = Failed/Denied
);

/* =========================================================
   1. GROUPS
   ========================================================= */

INSERT INTO Groups (Group_Name, Descriptionn, Is_Active)
VALUES
('Administrators', 'System administrators with full access', 1),
('Human Resources', 'Handles hiring, payroll and employee records', 1),
('Sales', 'Manages client accounts and deal pipelines', 1),
('Finance', 'Handles budgeting, invoicing and accounting', 1),
('Marketing', 'Handles campaigns, branding and analytics', 0);


/* =========================================================
   2. GROUP_COLLECTIONS
   ========================================================= */

-- Regular Users
INSERT INTO Group_Collections (Group_ID, Collection_Name) VALUES
(4, 'Orders'),       -- Finance
(2, 'Employees'),    -- Human Resources
(3, 'Customers'),    -- Sales
(3, 'Contracts'),    -- Sales
(4, 'Payments'),     -- Finance
(4, 'Invoices'),     -- Finance
(1, 'Customers'),	 -- Administrators
(1, 'Employees'),	 -- Administrators
(1, 'Payments'),	 -- Administrators
(1, 'Contracts'),	 -- Administrators
(1, 'Orders'),		 -- Administrators
(1, 'Invoices');	 -- Administrators
 
-- ---- Users (employees) ------------------------------------------------------
-- Rolee: 0 = User, 1 = Admin, 2 = SysAdmin  |  IS_Active: 1 = Active, 0 = Inactive
-- User_Idd is IDENTITY, so it's generated automatically (starts at 1, in insert order below)
-- Hashed Password = P@$$w0rd
INSERT INTO Users (Username, Password_Hash, Full_Name, Group_ID, Rolee, IS_Active, Created_date) VALUES
('r.osei',    '$2a$10$HFUyPISlAFMi/o1ksPRFx.W0jgHDTGlYRtPvRi9MgySe/wq5P0DLi', 'Rita Osei',      1, 2, 1, '2021-04-01'),  -- IT, SysAdmin
('j.smith',   '$2a$10$HFUyPISlAFMi/o1ksPRFx.W0jgHDTGlYRtPvRi9MgySe/wq5P0DLi', 'John Smith',     1, 1, 1, '2023-01-10'),  -- IT, Admin
('e.rossi',   '$2a$10$HFUyPISlAFMi/o1ksPRFx.W0jgHDTGlYRtPvRi9MgySe/wq5P0DLi', 'Elena Rossi',    1, 1, 1, '2024-01-08'),  -- IT, User
('a.khan',    '$2a$10$HFUyPISlAFMi/o1ksPRFx.W0jgHDTGlYRtPvRi9MgySe/wq5P0DLi', 'Amira Khan',     2, 1, 1, '2022-11-05'),  -- HR, Admin
('m.diaz',    '$2a$10$HFUyPISlAFMi/o1ksPRFx.W0jgHDTGlYRtPvRi9MgySe/wq5P0DLi', 'Miguel Diaz',    2, 0, 1, '2023-03-20'),  -- HR, User
('t.brown',   '$2a$10$HFUyPISlAFMi/o1ksPRFx.W0jgHDTGlYRtPvRi9MgySe/wq5P0DLi', 'Tyler Brown',    3, 1, 1, '2021-09-18'),  -- Sales, Admin
('l.nguyen',  '$2a$10$HFUyPISlAFMi/o1ksPRFx.W0jgHDTGlYRtPvRi9MgySe/wq5P0DLi', 'Linh Nguyen',    3, 0, 1, '2023-04-02'),  -- Sales, User
('k.johnson', '$2a$10$HFUyPISlAFMi/o1ksPRFx.W0jgHDTGlYRtPvRi9MgySe/wq5P0DLi', 'Kelly Johnson',  4, 1, 0, '2020-08-11'),  -- Finance, Admin, inactive
('s.patel',   '$2a$10$HFUyPISlAFMi/o1ksPRFx.W0jgHDTGlYRtPvRi9MgySe/wq5P0DLi', 'Sanjay Patel',   4, 0, 1, '2022-06-30'),  -- Finance, User
('o.mensah',  '$2a$10$HFUyPISlAFMi/o1ksPRFx.W0jgHDTGlYRtPvRi9MgySe/wq5P0DLi', 'Ofori Mensah',   3, 0, 0, '2023-05-15');  -- Marketing, User, inactive (dept dissolved)
 
-- ---- Logs (actions/searches performed by users) -----------------------------
-- User_Idd values below assume identity seeding 1..10 in the insert order above.
-- If Users already had rows before this script ran, adjust these to match actual IDs.
INSERT INTO Logs (User_Idd, Collection_Name, Search_keyword, Search_date, IP_address, Statuss) VALUES
(1, 'Orders', 'pending delivery', '2024-06-01 08:15:00', '10.0.0.10', 1),  -- Rita, SysAdmin
(2, 'Orders', 'order backlog', '2024-06-01 08:40:00', '10.0.0.12', 1),  -- John, IT Admin
(3, 'Orders', 'my orders', '2024-06-01 09:02:00', '10.0.0.14', 1),  -- Elena, IT User
(4, 'Employees', 'new hires', '2024-06-01 09:45:00', '10.0.1.20', 1),  -- Amira, HR Admin
(5, 'Employees', 'salary review', '2024-06-01 10:10:00', '10.0.1.21', 1),  -- Miguel, HR User
(6, 'Customers', 'top clients', '2024-06-01 11:30:00', '10.0.2.31', 1),  -- Tyler, Sales Admin
(7, 'Contracts', 'renewals', '2024-06-01 11:00:00', '10.0.2.30', 1),  -- Linh, Sales User
(8, 'Invoices', 'overdue', '2024-06-01 13:05:00', '10.0.3.40', 0),  -- Kelly, inactive account, denied
(9, 'Payments', 'monthly revenue', '2024-06-01 14:22:00', '10.0.3.41', 1),  -- Sanjay, Finance User
(10, 'Customers', 'campaign leads', '2024-06-01 15:00:00', '10.0.4.50', 0),  -- Ofori, inactive account, denied
(1, 'Orders', 'shipping delays', '2024-06-02 09:12:00', '10.0.0.10', 1),  -- Rita, SysAdmin
(5, 'Invoices', 'unauthorized access', '2024-06-02 10:05:00', '10.0.1.21', 0);  -- Miguel, HR User, cross-collection denial

USE InfoLink;

SELECT * FROM Groups;
SELECT * FROM Group_Collections;
SELECT * FROM Users;
SELECT * FROM Logs;
