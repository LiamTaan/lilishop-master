SET NAMES utf8mb4;

START TRANSACTION;

-- 为平台全权限测试角色补齐当前批发管理端全部有效菜单授权
INSERT INTO li_role_menu (
  id,
  create_by,
  create_time,
  delete_flag,
  update_by,
  update_time,
  role_id,
  menu_id
)
SELECT
  UUID_SHORT(),
  'codex',
  NOW(),
  b'0',
  'codex',
  NOW(),
  '3062200000000020001',
  CAST(m.id AS CHAR)
FROM li_menu m
LEFT JOIN li_role_menu rm
  ON rm.role_id = '3062200000000020001'
 AND rm.menu_id = CAST(m.id AS CHAR)
WHERE m.delete_flag = b'0'
  AND rm.id IS NULL;

COMMIT;
