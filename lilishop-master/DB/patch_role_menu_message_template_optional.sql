-- 可选：为已绑定「系统设置」菜单（id=1349246048900243456）的角色自动勾选「消息模版」
-- 前提：已执行 patch_menu_message_template_under_setting.sql（菜单 id 200401010001000001）
-- 若贵司角色未直接关联该父菜单，请改 WHERE 条件或在管理端手工赋权
-- id 使用 UUID_SHORT()，需 MySQL 支持

SET NAMES utf8mb4;

INSERT INTO `li_role_menu` (
  `id`,
  `create_by`,
  `create_time`,
  `delete_flag`,
  `update_by`,
  `update_time`,
  `role_id`,
  `menu_id`,
  `is_super`
)
SELECT
  UUID_SHORT(),
  COALESCE(rm.`create_by`, 'admin'),
  NOW(),
  b'0',
  COALESCE(rm.`update_by`, 'admin'),
  NOW(),
  rm.`role_id`,
  '200401010001000001',
  rm.`is_super`
FROM `li_role_menu` rm
WHERE rm.`menu_id` = '1349246048900243456'
  AND NOT EXISTS (
    SELECT 1
    FROM `li_role_menu` x
    WHERE x.`role_id` = rm.`role_id` AND x.`menu_id` = '200401010001000001'
  );
