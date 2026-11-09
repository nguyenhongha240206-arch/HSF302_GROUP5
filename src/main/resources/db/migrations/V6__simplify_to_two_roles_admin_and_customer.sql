-- =====================================================================
-- DATABASE MIGRATION SCRIPT: V6__simplify_to_two_roles_admin_and_customer.sql
-- PURPOSE: Simplify system roles to only 2 roles:
--          1. ROLE_ADMIN (Chủ shop / Quản trị viên)
--          2. ROLE_CUSTOMER (Khách hàng)
-- =====================================================================

-- 1. Remove user_roles linking to ROLE_STAFF
DELETE FROM dbo.user_roles 
WHERE role_id IN (SELECT id FROM dbo.roles WHERE name = 'ROLE_STAFF');
GO

-- 2. Delete ROLE_STAFF from roles table
DELETE FROM dbo.roles 
WHERE name = 'ROLE_STAFF';
GO

-- 3. Remove staff accounts
DELETE FROM dbo.users 
WHERE email IN ('staff@fruitstore.com', 'staff.demo@fruitstore.com');
GO

-- 4. Update descriptions for the two remaining roles
UPDATE dbo.roles 
SET description = N'Chủ shop (Quản trị viên)' 
WHERE name = 'ROLE_ADMIN';

UPDATE dbo.roles 
SET description = N'Khách hàng' 
WHERE name = 'ROLE_CUSTOMER';
GO
