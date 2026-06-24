SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 清理平台用户角色孤儿绑定
-- 作用：
-- 1. 删除 user_id 为空的 li_user_role 记录
-- 2. 删除 user_id 在 li_admin_user 中不存在的 li_user_role 记录
-- 3. 删除 role_id 在 li_role 中不存在的 li_user_role 记录
--
-- 说明：
-- 当前本地库仅有 1 个真实后台账号 admin，其他角色绑定均指向已不存在的历史用户

DELETE ur
FROM li_user_role ur
LEFT JOIN li_admin_user u ON CAST(u.id AS CHAR) = ur.user_id
LEFT JOIN li_role r ON CAST(r.id AS CHAR) = ur.role_id
WHERE ur.user_id IS NULL
   OR u.id IS NULL
   OR r.id IS NULL;

SELECT COUNT(*) AS remain_user_role_count
FROM li_user_role;

SELECT role_id, user_id
FROM li_user_role
ORDER BY role_id, user_id;

SET FOREIGN_KEY_CHECKS = 1;
