SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 批发商城管理端菜单收口补丁
-- 作用：
-- 1. 新增采购管理顶级菜单及 5 个子菜单
-- 2. 将新增菜单授权给当前全部角色，避免只补菜单不补权限
-- 3. 修正隐藏治理页 front_route / path，使 memberMenu + menu-sync 可稳定命中

DROP TEMPORARY TABLE IF EXISTS `tmp_wholesale_manager_menu_finalize_seed`;

CREATE TEMPORARY TABLE `tmp_wholesale_manager_menu_finalize_seed` (
  `id` bigint NOT NULL,
  `create_by` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `delete_flag` bit(1) DEFAULT NULL,
  `update_by` varchar(255) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `front_route` varchar(255) DEFAULT NULL,
  `icon` varchar(255) DEFAULT NULL,
  `level` int DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `parent_id` varchar(255) DEFAULT NULL,
  `path` varchar(255) DEFAULT NULL,
  `sort_order` decimal(10,2) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `front_component` varchar(255) DEFAULT NULL,
  `permission` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
);

INSERT INTO `tmp_wholesale_manager_menu_finalize_seed` (
  `id`,`create_by`,`create_time`,`delete_flag`,`update_by`,`update_time`,
  `description`,`front_route`,`icon`,`level`,`name`,`parent_id`,`path`,
  `sort_order`,`title`,`front_component`,`permission`
) VALUES
  ('3062200000000000015', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-menu-finalize', 'procurement-governance', 'ep:box', '0', 'ProcurementGovernance', '0', 'procurement-governance', 75.00, '采购管理', NULL, NULL),
  ('3062200000000010098', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-menu-finalize', 'procurement/order/index', '', '1', 'procurement-order', '3062200000000000015', 'procurement-order', 1.00, '采购订单管理', NULL, NULL),
  ('3062200000000010099', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-menu-finalize', 'procurement/inbound/index', '', '1', 'procurement-inbound', '3062200000000000015', 'procurement-inbound', 2.00, '采购入库管理', NULL, NULL),
  ('3062200000000010100', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-menu-finalize', 'procurement/inventory-count/index', '', '1', 'procurement-inventory-count', '3062200000000000015', 'procurement-inventory-count', 3.00, '盘点管理', NULL, NULL),
  ('3062200000000010101', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-menu-finalize', 'procurement/damage-report/index', '', '1', 'procurement-damage-report', '3062200000000000015', 'procurement-damage-report', 4.00, '报损管理', NULL, NULL),
  ('3062200000000010102', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-menu-finalize', 'procurement/reason/index', '', '1', 'procurement-reason', '3062200000000000015', 'procurement-reason', 5.00, '库存原因管理', NULL, NULL);

INSERT INTO `li_menu` (
  `id`,`create_by`,`create_time`,`delete_flag`,`update_by`,`update_time`,
  `description`,`front_route`,`icon`,`level`,`name`,`parent_id`,`path`,
  `sort_order`,`title`,`front_component`,`permission`
)
SELECT
  `id`,`create_by`,`create_time`,`delete_flag`,`update_by`,`update_time`,
  `description`,`front_route`,`icon`,`level`,`name`,`parent_id`,`path`,
  `sort_order`,`title`,`front_component`,`permission`
FROM `tmp_wholesale_manager_menu_finalize_seed` seed
WHERE NOT EXISTS (
  SELECT 1 FROM `li_menu` m WHERE m.`id` = seed.`id`
);

UPDATE `li_menu`
SET `front_route` = 'finance/profitsharing-balance/index',
    `path` = 'profitsharing-balance',
    `name` = 'profitsharing-balance',
    `update_by` = 'admin',
    `update_time` = CURRENT_TIMESTAMP
WHERE `id` = '3062200000000010025';

UPDATE `li_menu`
SET `front_route` = 'finance/wallet-account/index',
    `path` = 'wallet-account',
    `name` = 'wallet-account',
    `update_by` = 'admin',
    `update_time` = CURRENT_TIMESTAMP
WHERE `id` = '3062200000000010026';

UPDATE `li_menu`
SET `front_route` = 'finance/withdraw-apply/index',
    `path` = 'withdraw-apply',
    `name` = 'withdraw-apply',
    `update_by` = 'admin',
    `update_time` = CURRENT_TIMESTAMP
WHERE `id` = '3062200000000010027';

UPDATE `li_menu`
SET `front_route` = 'finance/procurement-reconciliation/index',
    `path` = 'procurement-reconciliation',
    `name` = 'procurement-reconciliation',
    `update_by` = 'admin',
    `update_time` = CURRENT_TIMESTAMP
WHERE `id` = '3062200000000010028';

UPDATE `li_menu`
SET `front_route` = 'finance/fund-reconciliation/index',
    `path` = 'fund-reconciliation',
    `name` = 'fund-reconciliation',
    `update_by` = 'admin',
    `update_time` = CURRENT_TIMESTAMP
WHERE `id` = '3062200000000010029';

UPDATE `li_menu`
SET `front_route` = 'seller/agent/agent-manage',
    `path` = 'agent-manage',
    `name` = 'agent-manage',
    `update_by` = 'admin',
    `update_time` = CURRENT_TIMESTAMP
WHERE `id` = '3062200000000010009';

UPDATE `li_menu`
SET `front_route` = 'seller/store/store-apply/index',
    `path` = 'store-apply',
    `name` = 'store-apply',
    `update_by` = 'admin',
    `update_time` = CURRENT_TIMESTAMP
WHERE `id` = '3062200000000010007';

INSERT INTO `li_role_menu` (
  `id`,`create_by`,`create_time`,`delete_flag`,`update_by`,`update_time`,
  `is_super`,`menu_id`,`role_id`
)
SELECT
  UUID_SHORT(), 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP,
  b'1', CAST(seed.`id` AS CHAR), CAST(role.`id` AS CHAR)
FROM `tmp_wholesale_manager_menu_finalize_seed` seed
JOIN `li_role` role
WHERE NOT EXISTS (
  SELECT 1
  FROM `li_role_menu` rm
  WHERE rm.`menu_id` = CAST(seed.`id` AS CHAR)
    AND rm.`role_id` = CAST(role.`id` AS CHAR)
);

SELECT `id`, `parent_id`, `title`, `front_route`, `path`
FROM `li_menu`
WHERE `id` IN (
  '3062200000000000015',
  '3062200000000010098',
  '3062200000000010099',
  '3062200000000010100',
  '3062200000000010101',
  '3062200000000010102',
  '3062200000000010007',
  '3062200000000010009',
  '3062200000000010025',
  '3062200000000010026',
  '3062200000000010027',
  '3062200000000010028',
  '3062200000000010029'
)
ORDER BY `id`;

SET FOREIGN_KEY_CHECKS = 1;
