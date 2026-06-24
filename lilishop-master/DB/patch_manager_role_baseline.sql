SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 平台角色权限基线
-- 作用：
-- 1. 修复 li_role 为空、li_user_role 存在孤儿 role_id 的最小可用问题
-- 2. 为当前 admin 账号已有 role_ids 中的首个角色补建角色记录
-- 3. 额外提供一个固定 ID 的“平台全权限测试角色”，便于新增后台账号后直接分配
-- 4. 将上述角色授予当前 li_menu 中全部菜单，保证非 super 账号可走现有权限链
--
-- 注意：
-- 1. 本脚本不删除历史孤儿角色绑定，只补当前最小可用基线
-- 2. 若后续执行批发商城菜单基线脚本，建议随后再执行一次本脚本，补齐新增菜单授权

SET @admin_user_id = (
  SELECT CAST(id AS CHAR)
  FROM li_admin_user
  WHERE username = 'admin'
  ORDER BY id
  LIMIT 1
);

SET @admin_role_ids = (
  SELECT COALESCE(role_ids, '')
  FROM li_admin_user
  WHERE username = 'admin'
  ORDER BY id
  LIMIT 1
);

SET @admin_primary_role_id = NULLIF(SUBSTRING_INDEX(@admin_role_ids, ',', 1), '');
SET @admin_primary_role_id = COALESCE(@admin_primary_role_id, '3062200000000020000');
SET @full_access_role_id = '3062200000000020001';

INSERT INTO li_role (
  id, create_by, create_time, delete_flag, update_by, update_time,
  default_role, description, name
)
SELECT
  @admin_primary_role_id, 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP,
  b'0', '平台管理员基线角色（兼容当前 admin 绑定）', '平台管理员'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM li_role WHERE CAST(id AS CHAR) = @admin_primary_role_id
);

INSERT INTO li_role (
  id, create_by, create_time, delete_flag, update_by, update_time,
  default_role, description, name
)
SELECT
  @full_access_role_id, 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP,
  b'0', '平台全权限测试角色（新增后台账号可直接分配）', '平台全权限测试角色'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM li_role WHERE CAST(id AS CHAR) = @full_access_role_id
);

UPDATE li_admin_user
SET role_ids = @admin_primary_role_id
WHERE username = 'admin'
  AND (role_ids IS NULL OR role_ids = '');

INSERT INTO li_user_role (id, user_id, role_id)
SELECT UUID_SHORT(), @admin_user_id, @admin_primary_role_id
FROM DUAL
WHERE @admin_user_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM li_user_role
    WHERE user_id = @admin_user_id
      AND role_id = @admin_primary_role_id
  );

INSERT INTO li_role_menu (
  id, create_by, create_time, delete_flag, update_by, update_time,
  is_super, menu_id, role_id
)
SELECT
  UUID_SHORT(), 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP,
  b'1', CAST(m.id AS CHAR), @admin_primary_role_id
FROM li_menu m
WHERE NOT EXISTS (
  SELECT 1
  FROM li_role_menu rm
  WHERE rm.role_id = @admin_primary_role_id
    AND rm.menu_id = CAST(m.id AS CHAR)
);

INSERT INTO li_role_menu (
  id, create_by, create_time, delete_flag, update_by, update_time,
  is_super, menu_id, role_id
)
SELECT
  UUID_SHORT(), 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP,
  b'1', CAST(m.id AS CHAR), @full_access_role_id
FROM li_menu m
WHERE NOT EXISTS (
  SELECT 1
  FROM li_role_menu rm
  WHERE rm.role_id = @full_access_role_id
    AND rm.menu_id = CAST(m.id AS CHAR)
);

SELECT CAST(id AS CHAR) AS role_id, name, description
FROM li_role
WHERE CAST(id AS CHAR) IN (@admin_primary_role_id, @full_access_role_id)
ORDER BY id;

SELECT role_id, COUNT(*) AS menu_count
FROM li_role_menu
WHERE role_id IN (@admin_primary_role_id, @full_access_role_id)
GROUP BY role_id
ORDER BY role_id;

SET FOREIGN_KEY_CHECKS = 1;
