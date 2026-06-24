SET NAMES utf8mb4;

-- 将批发商城管理端新菜单基线授权给当前 admin 账号已绑定的平台角色
-- 说明：
-- 1. 仅补齐 li_role_menu，不改动现有 li_user_role / li_role
-- 2. 默认按 is_super = 1 授权，确保当前平台管理员可完整联调
-- 3. 依赖 patch_wholesale_manager_menu_route_baseline.sql 先执行

INSERT INTO li_role_menu (
  id,
  create_by,
  create_time,
  delete_flag,
  update_by,
  update_time,
  is_super,
  menu_id,
  role_id
)
WITH admin_roles AS (
  SELECT DISTINCT ur.role_id
  FROM li_admin_user au
  INNER JOIN li_user_role ur
    ON ur.user_id = CAST(au.id AS CHAR)
  WHERE au.username = 'admin'
),
baseline_menus AS (
  SELECT CAST(id AS CHAR) AS menu_id
  FROM li_menu
  WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110
),
missing_bindings AS (
  SELECT
    ar.role_id,
    bm.menu_id,
    ROW_NUMBER() OVER (ORDER BY ar.role_id, bm.menu_id) AS rn
  FROM admin_roles ar
  CROSS JOIN baseline_menus bm
  LEFT JOIN li_role_menu rm
    ON rm.role_id = ar.role_id
   AND rm.menu_id = bm.menu_id
  WHERE rm.id IS NULL
)
SELECT
  4062200000000000000 + rn AS id,
  'codex',
  CURRENT_TIMESTAMP(6),
  b'0',
  'codex',
  CURRENT_TIMESTAMP(6),
  b'1',
  menu_id,
  role_id
FROM missing_bindings;

SELECT
  role_id,
  COUNT(*) AS baseline_menu_count
FROM li_role_menu
WHERE menu_id BETWEEN '3062200000000000001' AND '3062200000000010110'
GROUP BY role_id
ORDER BY role_id;
