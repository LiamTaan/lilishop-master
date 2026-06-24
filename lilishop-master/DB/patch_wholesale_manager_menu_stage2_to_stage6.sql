SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 批发商城管理端菜单补充：阶段二到阶段六
-- 说明：
-- 1. 阶段一菜单已由 patch_wholesale_stage1_menu.sql 初始化
-- 2. 本脚本仅补批发商城新增治理入口，不重复覆盖框架既有菜单
-- 3. 仅插入缺失菜单，不删除、不覆盖既有菜单
-- 4. 仅为已拥有对应父菜单能力的角色补授新菜单，避免误扩权

-- 父菜单约定：
-- 店铺：1367048684339986432
-- 商品管理：1367044376391319552
-- 促销管理：1367049214198022144
-- 预存款：1367042490443497472
-- 统计：1367052616634204160

-- 阶段二：商品治理扩展
INSERT INTO `li_menu` (
  `id`,`create_by`,`create_time`,`delete_flag`,`update_by`,`update_time`,
  `description`,`front_route`,`icon`,`level`,`name`,`parent_id`,`path`,`sort_order`,`title`,`front_component`,`permission`
)
SELECT
  2061700000000000003,'admin',NOW(),b'0','admin',NOW(),
  '批发商城阶段二-首页分类配置','operation/shortcut-nav/index','ios-apps',2,'shortcut-nav','1367044376391319552','shortcut-nav',12.00,'首页分类配置',NULL,
  '/manager/other/shortcutNav*'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `li_menu` WHERE `id` = 2061700000000000003
);

INSERT INTO `li_menu` (
  `id`,`create_by`,`create_time`,`delete_flag`,`update_by`,`update_time`,
  `description`,`front_route`,`icon`,`level`,`name`,`parent_id`,`path`,`sort_order`,`title`,`front_component`,`permission`
)
SELECT
  2061700000000000004,'admin',NOW(),b'0','admin',NOW(),
  '批发商城阶段二-卡券商品治理','promotions/card-coupon-goods/index','ios-card',2,'card-coupon-goods','1367049214198022144','card-coupon-goods',8.00,'卡券商品治理',NULL,
  '/manager/promotion/cardCouponGoods*'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `li_menu` WHERE `id` = 2061700000000000004
);

-- 阶段三：订单治理扩展
INSERT INTO `li_menu` (
  `id`,`create_by`,`create_time`,`delete_flag`,`update_by`,`update_time`,
  `description`,`front_route`,`icon`,`level`,`name`,`parent_id`,`path`,`sort_order`,`title`,`front_component`,`permission`
)
SELECT
  2061700000000000005,'admin',NOW(),b'0','admin',NOW(),
  '批发商城阶段三-核销记录','order/verification-record/index','ios-checkmark-circle',2,'verification-record','1367048684339986432','verification-record',12.00,'核销记录',NULL,
  '/manager/other/verificationRecord*'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `li_menu` WHERE `id` = 2061700000000000005
);

-- 阶段五：资金治理扩展
INSERT INTO `li_menu` (
  `id`,`create_by`,`create_time`,`delete_flag`,`update_by`,`update_time`,
  `description`,`front_route`,`icon`,`level`,`name`,`parent_id`,`path`,`sort_order`,`title`,`front_component`,`permission`
)
SELECT
  2061700000000000006,'admin',NOW(),b'0','admin',NOW(),
  '批发商城阶段五-分账规则','finance/profitsharing-rule/index','logo-yen',2,'profitsharing-rule','1367042490443497472','profitsharing-rule',10.00,'分账规则',NULL,
  '/manager/profitsharing/rule*'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `li_menu` WHERE `id` = 2061700000000000006
);

INSERT INTO `li_menu` (
  `id`,`create_by`,`create_time`,`delete_flag`,`update_by`,`update_time`,
  `description`,`front_route`,`icon`,`level`,`name`,`parent_id`,`path`,`sort_order`,`title`,`front_component`,`permission`
)
SELECT
  2061700000000000007,'admin',NOW(),b'0','admin',NOW(),
  '批发商城阶段五-分账明细','finance/profitsharing-record/index','ios-paper',2,'profitsharing-record','1367042490443497472','profitsharing-record',11.00,'分账明细',NULL,
  '/manager/profitsharing/record*,/manager/profitsharing/balance*'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `li_menu` WHERE `id` = 2061700000000000007
);

INSERT INTO `li_menu` (
  `id`,`create_by`,`create_time`,`delete_flag`,`update_by`,`update_time`,
  `description`,`front_route`,`icon`,`level`,`name`,`parent_id`,`path`,`sort_order`,`title`,`front_component`,`permission`
)
SELECT
  2061700000000000008,'admin',NOW(),b'0','admin',NOW(),
  '批发商城阶段五-采购对账','finance/procurement-reconciliation/index','ios-document',2,'procurement-reconciliation','1367042490443497472','procurement-reconciliation',12.00,'采购对账',NULL,
  '/manager/reconciliation/purchase*'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `li_menu` WHERE `id` = 2061700000000000008
);

INSERT INTO `li_menu` (
  `id`,`create_by`,`create_time`,`delete_flag`,`update_by`,`update_time`,
  `description`,`front_route`,`icon`,`level`,`name`,`parent_id`,`path`,`sort_order`,`title`,`front_component`,`permission`
)
SELECT
  2061700000000000009,'admin',NOW(),b'0','admin',NOW(),
  '批发商城阶段五-资金对账','finance/fund-reconciliation/index','ios-cash',2,'fund-reconciliation','1367042490443497472','fund-reconciliation',13.00,'资金对账',NULL,
  '/manager/reconciliation/fund*'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `li_menu` WHERE `id` = 2061700000000000009
);

-- 阶段六：平台工作台扩展
INSERT INTO `li_menu` (
  `id`,`create_by`,`create_time`,`delete_flag`,`update_by`,`update_time`,
  `description`,`front_route`,`icon`,`level`,`name`,`parent_id`,`path`,`sort_order`,`title`,`front_component`,`permission`
)
SELECT
  2061700000000000010,'admin',NOW(),b'0','admin',NOW(),
  '批发商城阶段六-平台工作台','statistics/wholesale-dashboard/index','ios-podium',2,'wholesale-dashboard','1367052616634204160','wholesale-dashboard',5.00,'平台工作台',NULL,
  '/manager/dashboard/wholesale*,/manager/statistics/index*,/manager/statistics/order/overview*'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `li_menu` WHERE `id` = 2061700000000000010
);

-- 将新增菜单授权给具备对应父级菜单的角色
INSERT INTO `li_role_menu` (
  `id`,`create_by`,`create_time`,`delete_flag`,`update_by`,`update_time`,`role_id`,`menu_id`,`is_super`
)
SELECT
  UUID_SHORT(),COALESCE(rm.`create_by`,'admin'),NOW(),b'0',COALESCE(rm.`update_by`,'admin'),NOW(),rm.`role_id`,'2061700000000000003',rm.`is_super`
FROM `li_role_menu` rm
WHERE rm.`menu_id` = '1367044376391319552'
  AND NOT EXISTS (
    SELECT 1 FROM `li_role_menu` x WHERE x.`role_id` = rm.`role_id` AND x.`menu_id` = '2061700000000000003'
  );

INSERT INTO `li_role_menu` (
  `id`,`create_by`,`create_time`,`delete_flag`,`update_by`,`update_time`,`role_id`,`menu_id`,`is_super`
)
SELECT
  UUID_SHORT(),COALESCE(rm.`create_by`,'admin'),NOW(),b'0',COALESCE(rm.`update_by`,'admin'),NOW(),rm.`role_id`,'2061700000000000004',rm.`is_super`
FROM `li_role_menu` rm
WHERE rm.`menu_id` = '1367049214198022144'
  AND NOT EXISTS (
    SELECT 1 FROM `li_role_menu` x WHERE x.`role_id` = rm.`role_id` AND x.`menu_id` = '2061700000000000004'
  );

INSERT INTO `li_role_menu` (
  `id`,`create_by`,`create_time`,`delete_flag`,`update_by`,`update_time`,`role_id`,`menu_id`,`is_super`
)
SELECT
  UUID_SHORT(),COALESCE(rm.`create_by`,'admin'),NOW(),b'0',COALESCE(rm.`update_by`,'admin'),NOW(),rm.`role_id`,'2061700000000000005',rm.`is_super`
FROM `li_role_menu` rm
WHERE rm.`menu_id` = '1367048832210173952'
  AND NOT EXISTS (
    SELECT 1 FROM `li_role_menu` x WHERE x.`role_id` = rm.`role_id` AND x.`menu_id` = '2061700000000000005'
  );

INSERT INTO `li_role_menu` (
  `id`,`create_by`,`create_time`,`delete_flag`,`update_by`,`update_time`,`role_id`,`menu_id`,`is_super`
)
SELECT
  UUID_SHORT(),COALESCE(rm.`create_by`,'admin'),NOW(),b'0',COALESCE(rm.`update_by`,'admin'),NOW(),rm.`role_id`,'2061700000000000006',rm.`is_super`
FROM `li_role_menu` rm
WHERE rm.`menu_id` = '1367042490443497472'
  AND NOT EXISTS (
    SELECT 1 FROM `li_role_menu` x WHERE x.`role_id` = rm.`role_id` AND x.`menu_id` = '2061700000000000006'
  );

INSERT INTO `li_role_menu` (
  `id`,`create_by`,`create_time`,`delete_flag`,`update_by`,`update_time`,`role_id`,`menu_id`,`is_super`
)
SELECT
  UUID_SHORT(),COALESCE(rm.`create_by`,'admin'),NOW(),b'0',COALESCE(rm.`update_by`,'admin'),NOW(),rm.`role_id`,'2061700000000000007',rm.`is_super`
FROM `li_role_menu` rm
WHERE rm.`menu_id` = '1367042490443497472'
  AND NOT EXISTS (
    SELECT 1 FROM `li_role_menu` x WHERE x.`role_id` = rm.`role_id` AND x.`menu_id` = '2061700000000000007'
  );

INSERT INTO `li_role_menu` (
  `id`,`create_by`,`create_time`,`delete_flag`,`update_by`,`update_time`,`role_id`,`menu_id`,`is_super`
)
SELECT
  UUID_SHORT(),COALESCE(rm.`create_by`,'admin'),NOW(),b'0',COALESCE(rm.`update_by`,'admin'),NOW(),rm.`role_id`,'2061700000000000008',rm.`is_super`
FROM `li_role_menu` rm
WHERE rm.`menu_id` = '1367042490443497472'
  AND NOT EXISTS (
    SELECT 1 FROM `li_role_menu` x WHERE x.`role_id` = rm.`role_id` AND x.`menu_id` = '2061700000000000008'
  );

INSERT INTO `li_role_menu` (
  `id`,`create_by`,`create_time`,`delete_flag`,`update_by`,`update_time`,`role_id`,`menu_id`,`is_super`
)
SELECT
  UUID_SHORT(),COALESCE(rm.`create_by`,'admin'),NOW(),b'0',COALESCE(rm.`update_by`,'admin'),NOW(),rm.`role_id`,'2061700000000000009',rm.`is_super`
FROM `li_role_menu` rm
WHERE rm.`menu_id` = '1367042490443497472'
  AND NOT EXISTS (
    SELECT 1 FROM `li_role_menu` x WHERE x.`role_id` = rm.`role_id` AND x.`menu_id` = '2061700000000000009'
  );

INSERT INTO `li_role_menu` (
  `id`,`create_by`,`create_time`,`delete_flag`,`update_by`,`update_time`,`role_id`,`menu_id`,`is_super`
)
SELECT
  UUID_SHORT(),COALESCE(rm.`create_by`,'admin'),NOW(),b'0',COALESCE(rm.`update_by`,'admin'),NOW(),rm.`role_id`,'2061700000000000010',rm.`is_super`
FROM `li_role_menu` rm
WHERE rm.`menu_id` = '1367052616634204160'
  AND NOT EXISTS (
    SELECT 1 FROM `li_role_menu` x WHERE x.`role_id` = rm.`role_id` AND x.`menu_id` = '2061700000000000010'
  );

SET FOREIGN_KEY_CHECKS = 1;

SELECT id, title, path, parent_id, permission
FROM `li_menu`
WHERE id IN (
  '2061700000000000003',
  '2061700000000000004',
  '2061700000000000005',
  '2061700000000000006',
  '2061700000000000007',
  '2061700000000000008',
  '2061700000000000009',
  '2061700000000000010'
);
