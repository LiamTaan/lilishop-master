SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 管理端超管收口补丁
-- 目标：
-- 1. 空库初始化后只保留默认唯一超级管理员 admin
-- 2. 不预置平台管理员、测试角色或其他后台角色
-- 3. 清空历史种子里残留的后台角色、角色菜单、用户角色关系
-- 4. 后续业务管理员与角色完全由管理端菜单自行维护

UPDATE li_admin_user
SET is_super = b'1',
    status = b'1',
    role_ids = NULL,
    update_by = 'admin',
    update_time = CURRENT_TIMESTAMP
WHERE username = 'admin';

DELETE FROM li_user_role;
DELETE FROM li_role_menu;
DELETE FROM li_role;

SELECT CAST(id AS CHAR) AS admin_user_id, username, is_super, status, role_ids
FROM li_admin_user
WHERE username = 'admin'
ORDER BY id
LIMIT 1;

SET FOREIGN_KEY_CHECKS = 1;
