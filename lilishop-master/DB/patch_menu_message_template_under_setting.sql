SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 在 设置 -> 系统设置 下新增子菜单：消息模版
-- parent_id = 1349246048900243456 （系统设置）
-- 若已存在同名同路径节点，则不会重复插入
INSERT INTO `li_menu` (
  `id`,
  `create_by`,
  `create_time`,
  `delete_flag`,
  `update_by`,
  `update_time`,
  `description`,
  `front_route`,
  `icon`,
  `level`,
  `name`,
  `parent_id`,
  `path`,
  `sort_order`,
  `title`,
  `front_component`,
  `permission`
)
SELECT
  200401010001000001,
  'admin',
  NOW(),
  b'0',
  'admin',
  NOW(),
  '系统设置子菜单-消息模版',
  'sys/message/messageTemplate',
  'ios-chatbubbles',
  3,
  'setting-message-template',
  '1349246048900243456',
  'message-template',
  99.00,
  '消息模版',
  NULL,
  '/manager/setting/noticeMessage*,/manager/setting/messageTemplate*,/manager/wechat/wechatMessage*,/manager/wechat/wechatMPMessage*'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1
  FROM `li_menu`
  WHERE `parent_id` = '1349246048900243456'
    AND `path` = 'message-template'
    AND `title` = '消息模版'
);

SET FOREIGN_KEY_CHECKS = 1;

-- 校验（可选）：确认「系统设置」主键与补丁 parent_id 一致后再赋权
-- 标准库：id=1349246048900243456，path=setting，title=系统设置
SELECT id, title, path, parent_id FROM `li_menu` WHERE `path` = 'setting' AND `title` = '系统设置' LIMIT 1;
SELECT id, title, path, parent_id, front_route FROM `li_menu` WHERE `path` = 'message-template' AND `title` = '消息模版' LIMIT 1;

-- 赋权：在管理端「角色权限」中为相关角色勾选「消息模版」，保存后重新登录

