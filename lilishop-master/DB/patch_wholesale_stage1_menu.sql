SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 批发商城阶段一：管理端菜单与权限初始化
-- 约束：
-- 1. 仅插入缺失菜单，不重复写入
-- 2. 仅为已拥有「店铺管理」菜单的角色补授新菜单

-- 店铺主菜单下：代理商治理
INSERT INTO `li_menu` (
  `id`,`create_by`,`create_time`,`delete_flag`,`update_by`,`update_time`,
  `description`,`front_route`,`icon`,`level`,`name`,`parent_id`,`path`,`sort_order`,`title`,`front_component`,`permission`
)
SELECT
  2061700000000000001,'admin',NOW(),b'0','admin',NOW(),
  '批发商城阶段一-代理商治理','seller/agent/agent-manage','ios-people',2,'agent-manage','1367048684339986432','agent-manage',10.00,'代理商治理',NULL,
  '/manager/agent/role*,/manager/agent/store*'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `li_menu` WHERE `id` = 2061700000000000001
);

-- 店铺主菜单下：店铺审核历史
INSERT INTO `li_menu` (
  `id`,`create_by`,`create_time`,`delete_flag`,`update_by`,`update_time`,
  `description`,`front_route`,`icon`,`level`,`name`,`parent_id`,`path`,`sort_order`,`title`,`front_component`,`permission`
)
SELECT
  2061700000000000002,'admin',NOW(),b'0','admin',NOW(),
  '批发商城阶段一-店铺审核历史','seller/shop/store-audit-log','ios-document',2,'store-audit-log','1367048684339986432','store-audit-log',11.00,'店铺审核历史',NULL,
  '/manager/store/store/audit/log*'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `li_menu` WHERE `id` = 2061700000000000002
);

-- 自动将新增菜单授予已拥有店铺列表菜单的角色
INSERT INTO `li_role_menu` (
  `id`,`create_by`,`create_time`,`delete_flag`,`update_by`,`update_time`,`role_id`,`menu_id`,`is_super`
)
SELECT
  UUID_SHORT(),COALESCE(rm.`create_by`,'admin'),NOW(),b'0',COALESCE(rm.`update_by`,'admin'),NOW(),rm.`role_id`,'2061700000000000001',rm.`is_super`
FROM `li_role_menu` rm
WHERE rm.`menu_id` = '1367048832210173952'
  AND NOT EXISTS (
    SELECT 1 FROM `li_role_menu` x WHERE x.`role_id` = rm.`role_id` AND x.`menu_id` = '2061700000000000001'
  );

INSERT INTO `li_role_menu` (
  `id`,`create_by`,`create_time`,`delete_flag`,`update_by`,`update_time`,`role_id`,`menu_id`,`is_super`
)
SELECT
  UUID_SHORT(),COALESCE(rm.`create_by`,'admin'),NOW(),b'0',COALESCE(rm.`update_by`,'admin'),NOW(),rm.`role_id`,'2061700000000000002',rm.`is_super`
FROM `li_role_menu` rm
WHERE rm.`menu_id` = '1367048832210173952'
  AND NOT EXISTS (
    SELECT 1 FROM `li_role_menu` x WHERE x.`role_id` = rm.`role_id` AND x.`menu_id` = '2061700000000000002'
  );

SET FOREIGN_KEY_CHECKS = 1;

SELECT id, title, path, parent_id, permission
FROM `li_menu`
WHERE id IN ('2061700000000000001','2061700000000000002');
