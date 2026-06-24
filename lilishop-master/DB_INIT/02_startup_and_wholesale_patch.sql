-- Execute this file after 01_full_base_schema.sql
USE `lilishop`;

-- Official startup patches required by current codebase

-- 商品定时上下架字段补齐。
-- 当前代码实体会读取这些字段，旧初始化 SQL 未包含时会导致商品列表 SELECT 失败。
SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'li_goods'
    AND COLUMN_NAME = 'scheduled_upper_time'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_goods` ADD COLUMN `scheduled_upper_time` DATETIME NULL COMMENT ''计划上架时间''',
  'SELECT ''scheduled_upper_time exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'li_goods'
    AND COLUMN_NAME = 'scheduled_upper_reason'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_goods` ADD COLUMN `scheduled_upper_reason` VARCHAR(255) NULL COMMENT ''计划上架原因''',
  'SELECT ''scheduled_upper_reason exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'li_goods'
    AND COLUMN_NAME = 'scheduled_down_time'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_goods` ADD COLUMN `scheduled_down_time` DATETIME NULL COMMENT ''计划下架时间''',
  'SELECT ''scheduled_down_time exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'li_goods'
    AND COLUMN_NAME = 'scheduled_down_reason'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_goods` ADD COLUMN `scheduled_down_reason` VARCHAR(255) NULL COMMENT ''计划下架原因''',
  'SELECT ''scheduled_down_reason exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Official bootstrap patch

-- 首次启动补齐商品参数关联表，并让默认上传走本地文件存储。
CREATE TABLE IF NOT EXISTS `li_category_parameter` (
  `id` bigint NOT NULL COMMENT 'ID',
  `create_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '创建者',
  `create_time` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `delete_flag` bit(1) DEFAULT NULL COMMENT '删除标志 true/false 删除/未删除',
  `update_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '更新者',
  `update_time` datetime(6) DEFAULT NULL COMMENT '更新时间',
  `category_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '分类ID',
  `parameter_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '参数ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_li_category_parameter_category_parameter` (`category_id`,`parameter_id`) USING BTREE,
  KEY `idx_li_category_parameter_category_id` (`category_id`) USING BTREE,
  KEY `idx_li_category_parameter_parameter_id` (`parameter_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='分类-参数关联';

INSERT IGNORE INTO `li_category_parameter` (`id`, `category_id`, `parameter_id`)
SELECT `id`, `category_id`, `id`
FROM `li_parameters`
WHERE `category_id` IS NOT NULL;

INSERT INTO `li_setting` (
  `id`,
  `create_by`,
  `create_time`,
  `delete_flag`,
  `update_by`,
  `update_time`,
  `setting_value`
) VALUES (
  'OSS_SETTING',
  'admin',
  NOW(6),
  b'0',
  'admin',
  NOW(6),
  '{"type":"LOCAL","localFilePath":"data/uploads","localFileUrlPrefix":"/files"}'
) ON DUPLICATE KEY UPDATE
  `update_by` = 'admin',
  `update_time` = NOW(6),
  `setting_value` = '{"type":"LOCAL","localFilePath":"data/uploads","localFileUrlPrefix":"/files"}';

-- Official after-sale schema patch

-- 首次启动补齐商家售后确认收货退款链路依赖的表和消息模板字段。
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `li_order_gift_card_record` (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `delete_flag` bit(1) DEFAULT b'0',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  `order_sn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '订单号',
  `trade_sn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '交易号',
  `member_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '会员ID',
  `credential_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '礼品卡凭证ID',
  `card_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '礼品卡卡号',
  `deduct_amount` decimal(12,2) DEFAULT 0.00 COMMENT '本次抵扣金额',
  `change_amount` decimal(12,2) DEFAULT 0.00 COMMENT '变动金额',
  `balance_after` decimal(12,2) DEFAULT 0.00 COMMENT '变动后余额',
  `flow_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '流水方向：INCREASE/DECREASE',
  `biz_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '业务类型',
  `biz_sn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '业务单号',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_li_order_gift_card_order` (`order_sn`),
  KEY `idx_li_order_gift_card_trade` (`trade_sn`),
  KEY `idx_li_order_gift_card_member` (`member_id`),
  KEY `idx_li_order_gift_card_credential` (`credential_id`),
  KEY `idx_li_order_gift_card_biz` (`biz_type`,`biz_sn`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='订单礼品卡使用流水';

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_notice_message' AND COLUMN_NAME = 'scene_code');
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_notice_message` ADD COLUMN `scene_code` varchar(64) NULL COMMENT ''业务场景编码，与 NoticeMessageNodeEnum.name 一致'' AFTER `variable`',
  'SELECT ''li_notice_message.scene_code exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_notice_message' AND COLUMN_NAME = 'email_status');
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_notice_message` ADD COLUMN `email_status` varchar(16) NOT NULL DEFAULT ''CLOSE'' COMMENT ''邮箱渠道 OPEN/CLOSE'' AFTER `scene_code`',
  'SELECT ''li_notice_message.email_status exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_notice_message' AND COLUMN_NAME = 'email_content');
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_notice_message` ADD COLUMN `email_content` varchar(2000) NULL COMMENT ''邮箱正文，空则与站内信同文'' AFTER `email_status`',
  'SELECT ''li_notice_message.email_content exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_wechat_message' AND COLUMN_NAME = 'scene_code');
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_wechat_message` ADD COLUMN `scene_code` varchar(64) NULL COMMENT ''关联站内信场景'' AFTER `remark`',
  'SELECT ''li_wechat_message.scene_code exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_wechat_mp_message' AND COLUMN_NAME = 'scene_code');
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_wechat_mp_message` ADD COLUMN `scene_code` varchar(64) NULL COMMENT ''关联站内信场景'' AFTER `order_status`',
  'SELECT ''li_wechat_mp_message.scene_code exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE `li_notice_message` SET `scene_code` = CASE TRIM(REPLACE(REPLACE(`notice_node`, CHAR(10), ''), CHAR(13), ''))
  WHEN '订单提交成功通知' THEN 'ORDER_CREATE_SUCCESS'
  WHEN '订单取消成功通知' THEN 'ORDER_CANCEL_SUCCESS'
  WHEN '订单支付成功通知' THEN 'ORDER_PAY_SUCCESS'
  WHEN '支付失败自动退款通知' THEN 'ORDER_PAY_ERROR'
  WHEN '订单发货通知' THEN 'ORDER_DELIVER'
  WHEN '订单完成通知' THEN 'ORDER_COMPLETE'
  WHEN '订单评价提醒' THEN 'ORDER_EVALUATION'
  WHEN '售后提交成功通知' THEN 'AFTER_SALE_CREATE_SUCCESS'
  WHEN '退货审核通过通知' THEN 'RETURN_GOODS_PASS'
  WHEN '退款审核通过通知' THEN 'RETURN_MONEY_PASS'
  WHEN '退货审核未通过通知' THEN 'RETURN_GOODS_REFUSE'
  WHEN '退款审核未通过通知' THEN 'RETURN_MONEY_REFUSE'
  WHEN '退货物品签收通知' THEN 'AFTER_SALE_ROG_PASS'
  WHEN '退货物品拒收通知' THEN 'AFTER_SALE_ROG_REFUSE'
  WHEN '售后完成通知' THEN 'AFTER_SALE_COMPLETE'
  WHEN '开团成功通知' THEN 'PINTUAN_CREATE'
  WHEN '拼团失败通知' THEN 'PINTUAN_ERROR'
  WHEN '拼团成功通知' THEN 'PINTUAN_SUCCESS'
  WHEN '积分变更通知' THEN 'POINT_CHANGE'
  WHEN '余额账户变更通知' THEN 'WALLET_CHANGE'
  WHEN '余额提现申请提交成功通知' THEN 'WALLET_WITHDRAWAL_CREATE'
  WHEN '余额提现成功通知' THEN 'WALLET_WITHDRAWAL_SUCCESS'
  WHEN '余额提现申请失败通知' THEN 'WALLET_WITHDRAWAL_ERROR'
  WHEN '余额提现申请驳回通知' THEN 'WALLET_WITHDRAWAL_AUDIT_ERROR'
  WHEN '余额提现申请通过通知' THEN 'WALLET_WITHDRAWAL_AUDIT_SUCCESS'
  WHEN '微信提现成功通知' THEN 'WALLET_WITHDRAWAL_SUCCESS'
  WHEN '提现申请提交成功通知' THEN 'WALLET_WITHDRAWAL_CREATE'
  WHEN '提现申请驳回通知' THEN 'WALLET_WITHDRAWAL_AUDIT_ERROR'
  ELSE `scene_code`
END
WHERE (`scene_code` IS NULL OR `scene_code` = '');

UPDATE `li_wechat_message` w
INNER JOIN `li_notice_message` n ON n.`scene_code` IS NOT NULL AND n.`scene_code` != ''
  AND TRIM(w.`name`) = TRIM(REPLACE(REPLACE(n.`notice_node`, CHAR(10), ''), CHAR(13), ''))
SET w.`scene_code` = n.`scene_code`
WHERE w.`scene_code` IS NULL OR w.`scene_code` = '';

UPDATE `li_wechat_message` SET `scene_code` = 'ORDER_PAY_SUCCESS' WHERE (`scene_code` IS NULL OR `scene_code` = '') AND `name` = '订单支付成功通知';
UPDATE `li_wechat_message` SET `scene_code` = 'ORDER_DELIVER' WHERE (`scene_code` IS NULL OR `scene_code` = '') AND `name` = '订单发货';
UPDATE `li_wechat_message` SET `scene_code` = 'ORDER_COMPLETE' WHERE (`scene_code` IS NULL OR `scene_code` = '') AND `name` = '订单完成';

UPDATE `li_wechat_mp_message` w
INNER JOIN `li_notice_message` n ON n.`scene_code` IS NOT NULL AND n.`scene_code` != ''
  AND TRIM(w.`name`) = TRIM(REPLACE(REPLACE(n.`notice_node`, CHAR(10), ''), CHAR(13), ''))
SET w.`scene_code` = n.`scene_code`
WHERE w.`scene_code` IS NULL OR w.`scene_code` = '';

UPDATE `li_wechat_mp_message` SET `scene_code` = 'ORDER_PAY_SUCCESS' WHERE (`scene_code` IS NULL OR `scene_code` = '') AND `name` LIKE '%订单支付%';
UPDATE `li_wechat_mp_message` SET `scene_code` = 'ORDER_DELIVER' WHERE (`scene_code` IS NULL OR `scene_code` = '') AND `name` LIKE '%发货%';
UPDATE `li_wechat_mp_message` SET `scene_code` = 'ORDER_COMPLETE' WHERE (`scene_code` IS NULL OR `scene_code` = '') AND `name` LIKE '%订单完成%';

-- Wholesale project patches

-- 批发商城阶段一：代理商、店铺申请与审核扩展

ALTER TABLE `li_store`
  ADD COLUMN `apply_type` varchar(32) DEFAULT NULL COMMENT '申请主体类型：PERSONAL个人，INDIVIDUAL个体户，COMPANY_LEGAL企业法人，COMPANY_NON_LEGAL企业非法人' AFTER `store_name`,
  ADD COLUMN `store_type` varchar(32) DEFAULT NULL COMMENT '店铺类型' AFTER `apply_type`,
  ADD COLUMN `audit_status` varchar(32) NOT NULL DEFAULT 'DRAFT' COMMENT '审核状态：DRAFT草稿，SUBMITTED待审核，APPROVED通过，REJECTED驳回，FROZEN冻结' AFTER `store_disable`,
  ADD COLUMN `audit_remark` varchar(255) DEFAULT NULL COMMENT '审核备注' AFTER `audit_status`;

ALTER TABLE `li_store_detail`
  ADD COLUMN `apply_type` varchar(32) DEFAULT NULL COMMENT '申请主体类型' AFTER `store_name`,
  ADD COLUMN `store_type` varchar(32) DEFAULT NULL COMMENT '店铺类型' AFTER `apply_type`,
  ADD COLUMN `business_license_url` varchar(255) DEFAULT NULL COMMENT '营业执照图片' AFTER `scope`,
  ADD COLUMN `credit_code` varchar(64) DEFAULT NULL COMMENT '统一社会信用代码' AFTER `business_license_url`,
  ADD COLUMN `business_license_region_id` varchar(64) DEFAULT NULL COMMENT '营业执照所在地区ID' AFTER `credit_code`,
  ADD COLUMN `business_license_expire_type` varchar(32) DEFAULT NULL COMMENT '营业执照有效期类型' AFTER `business_license_region_id`,
  ADD COLUMN `business_license_expire_date` varchar(32) DEFAULT NULL COMMENT '营业执照有效期截止时间' AFTER `business_license_expire_type`,
  ADD COLUMN `facade_image_url` varchar(255) DEFAULT NULL COMMENT '门头照' AFTER `business_license_expire_date`,
  ADD COLUMN `indoor_image_urls` text DEFAULT NULL COMMENT '店内照，英文逗号分隔' AFTER `facade_image_url`,
  ADD COLUMN `store_category_id` varchar(64) DEFAULT NULL COMMENT '店铺分类ID' AFTER `indoor_image_urls`,
  ADD COLUMN `business_hours_start` varchar(16) DEFAULT NULL COMMENT '营业开始时间' AFTER `store_category_id`,
  ADD COLUMN `business_hours_end` varchar(16) DEFAULT NULL COMMENT '营业结束时间' AFTER `business_hours_start`,
  ADD COLUMN `real_name` varchar(64) DEFAULT NULL COMMENT '个人主体姓名' AFTER `legal_photo`,
  ADD COLUMN `id_card_no` varchar(64) DEFAULT NULL COMMENT '个人身份证号' AFTER `real_name`,
  ADD COLUMN `id_card_front_url` varchar(255) DEFAULT NULL COMMENT '身份证正面' AFTER `id_card_no`,
  ADD COLUMN `id_card_back_url` varchar(255) DEFAULT NULL COMMENT '身份证反面' AFTER `id_card_front_url`,
  ADD COLUMN `legal_mobile` varchar(32) DEFAULT NULL COMMENT '法人手机号' AFTER `id_card_back_url`,
  ADD COLUMN `authorization_url` varchar(255) DEFAULT NULL COMMENT '企业非法人授权书' AFTER `legal_mobile`;

CREATE TABLE `agent_role_relation` (
  `id` varchar(64) NOT NULL COMMENT '主键ID',
  `member_id` varchar(64) NOT NULL COMMENT '会员ID',
  `role_code` varchar(32) NOT NULL COMMENT '角色编码，固定为ROLE_AGENT',
  `region_id` varchar(64) NOT NULL COMMENT '所属区域ID',
  `region_name` varchar(128) NOT NULL COMMENT '所属区域名称',
  `status` varchar(32) NOT NULL DEFAULT 'ENABLE' COMMENT '状态：ENABLE启用，DISABLE停用',
  `effective_time` datetime DEFAULT NULL COMMENT '生效时间',
  `expire_time` datetime DEFAULT NULL COMMENT '失效时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0正常，1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_member_region` (`member_id`, `region_id`) COMMENT '同一代理商在同一区域唯一',
  KEY `idx_region_status` (`region_id`, `status`) COMMENT '按区域和状态查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代理商角色关系表';

CREATE TABLE `agent_store_bind` (
  `id` varchar(64) NOT NULL COMMENT '主键ID',
  `agent_member_id` varchar(64) NOT NULL COMMENT '代理商会员ID',
  `store_id` varchar(64) NOT NULL COMMENT '店铺ID',
  `store_name` varchar(128) NOT NULL COMMENT '店铺名称',
  `region_id` varchar(64) NOT NULL COMMENT '所属区域ID',
  `bind_status` varchar(32) NOT NULL DEFAULT 'BOUND' COMMENT '绑定状态：UNBOUND未绑定，BOUND已绑定，DISABLED已停用',
  `bind_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  `unbind_time` datetime DEFAULT NULL COMMENT '解绑时间',
  `audit_status` varchar(32) NOT NULL DEFAULT 'SUBMITTED' COMMENT '审核状态：SUBMITTED待审核，APPROVED通过，REJECTED驳回',
  `audit_remark` varchar(255) DEFAULT NULL COMMENT '审核备注',
  `remark` varchar(255) DEFAULT NULL COMMENT '业务备注',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0正常，1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_agent_store` (`agent_member_id`, `store_id`) COMMENT '同一代理商和店铺唯一绑定',
  KEY `idx_store_id` (`store_id`) COMMENT '按店铺查询绑定关系'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代理商店铺绑定表';

CREATE TABLE `li_store_audit_log` (
  `id` varchar(64) NOT NULL COMMENT '主键ID',
  `store_id` varchar(64) NOT NULL COMMENT '店铺ID',
  `from_audit_status` varchar(32) DEFAULT NULL COMMENT '审核前状态',
  `to_audit_status` varchar(32) NOT NULL COMMENT '审核后状态',
  `audit_remark` varchar(255) DEFAULT NULL COMMENT '审核备注',
  `operator_id` varchar(64) DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(128) DEFAULT NULL COMMENT '操作人名称',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0正常，1删除',
  PRIMARY KEY (`id`),
  KEY `idx_store_create_time` (`store_id`, `create_time`) COMMENT '按店铺和时间查询审核记录'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺审核历史表';

-- Wholesale stage1 menu

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

-- Wholesale stage2

CREATE TABLE IF NOT EXISTS `li_operation_shortcut_nav` (
    `id` varchar(64) NOT NULL COMMENT '主键ID',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `delete_flag` tinyint(1) DEFAULT 0 COMMENT '删除标记',
    `title` varchar(64) NOT NULL COMMENT '入口标题',
    `subtitle` varchar(128) DEFAULT NULL COMMENT '入口副标题',
    `image` varchar(255) DEFAULT NULL COMMENT '图标地址',
    `link_type` varchar(32) NOT NULL COMMENT '跳转类型',
    `link_value` varchar(255) NOT NULL COMMENT '跳转值',
    `client_type` varchar(32) NOT NULL COMMENT '客户端类型',
    `sort_order` int DEFAULT 0 COMMENT '排序值',
    `display_status` varchar(16) NOT NULL DEFAULT 'OPEN' COMMENT '显示状态',
    `remark` varchar(255) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_shortcut_nav_client_status` (`client_type`, `display_status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页分类宫格配置';

UPDATE `li_setting`
SET `setting_value` = '{"apply":true,"minPrice":0,"fee":0,"type":"ALIPAY"}'
WHERE `id` = 'WITHDRAWAL_SETTING'
  AND (
    `setting_value` IS NULL
    OR `setting_value` = ''
    OR `setting_value` = '{"apply":true}'
  );

CREATE TABLE IF NOT EXISTS `li_card_coupon_goods` (
    `id` varchar(64) NOT NULL COMMENT '主键ID',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `delete_flag` tinyint(1) DEFAULT 0 COMMENT '删除标记',
    `coupon_id` varchar(64) NOT NULL COMMENT '优惠券ID',
    `coupon_name` varchar(128) NOT NULL COMMENT '优惠券名称',
    `goods_id` varchar(64) NOT NULL COMMENT '商品ID',
    `sku_id` varchar(64) NOT NULL COMMENT '商品SKU ID',
    `goods_name` varchar(255) NOT NULL COMMENT '商品名称',
    `thumbnail` varchar(255) DEFAULT NULL COMMENT '缩略图',
    `original_price` decimal(12,2) DEFAULT NULL COMMENT '商品原价',
    `store_id` varchar(64) DEFAULT NULL COMMENT '店铺ID',
    `store_name` varchar(255) DEFAULT NULL COMMENT '店铺名称',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_card_coupon_goods_coupon_sku` (`coupon_id`, `sku_id`),
    KEY `idx_card_coupon_goods_sku` (`sku_id`),
    KEY `idx_card_coupon_goods_goods` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='卡券商品关联';

-- Wholesale stage3

CREATE TABLE IF NOT EXISTS `li_verification_record` (
    `id` varchar(64) NOT NULL COMMENT '主键ID',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `delete_flag` tinyint(1) DEFAULT 0 COMMENT '删除标记',
    `order_sn` varchar(64) NOT NULL COMMENT '订单编号',
    `store_id` varchar(64) DEFAULT NULL COMMENT '店铺ID',
    `store_name` varchar(255) DEFAULT NULL COMMENT '店铺名称',
    `member_id` varchar(64) DEFAULT NULL COMMENT '会员ID',
    `member_name` varchar(255) DEFAULT NULL COMMENT '会员名称',
    `verification_code` varchar(64) NOT NULL COMMENT '核销码',
    `operator_id` varchar(64) DEFAULT NULL COMMENT '核销人ID',
    `operator_name` varchar(255) DEFAULT NULL COMMENT '核销人名称',
    `source_type` varchar(32) NOT NULL COMMENT '核销来源',
    `result_type` varchar(32) NOT NULL COMMENT '核销结果',
    `remark` varchar(255) DEFAULT NULL COMMENT '核销备注',
    `verify_time` datetime DEFAULT NULL COMMENT '核销时间',
    PRIMARY KEY (`id`),
    KEY `idx_verification_record_order_sn` (`order_sn`),
    KEY `idx_verification_record_store_id` (`store_id`),
    KEY `idx_verification_record_code` (`verification_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='核销记录';

-- Wholesale stage5 profitsharing rule

CREATE TABLE IF NOT EXISTS `li_profit_sharing_rule` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `delete_flag` tinyint(1) DEFAULT 0 COMMENT '删除标志',
  `rule_name` varchar(128) NOT NULL COMMENT '规则名称',
  `role_type` varchar(32) NOT NULL COMMENT '角色类型',
  `region_id` varchar(32) DEFAULT NULL COMMENT '区域ID',
  `category_id` varchar(32) DEFAULT NULL COMMENT '品类ID',
  `ratio` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '分账比例',
  `status` varchar(32) NOT NULL DEFAULT 'ENABLE' COMMENT '状态',
  PRIMARY KEY (`id`),
  KEY `idx_role_type` (`role_type`),
  KEY `idx_region_category` (`region_id`,`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台分账规则表';

-- Wholesale manager menu stage2 to stage6

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

-- Recommended indexes

-- 针对WHERE条件、排序和分组的组合索引
CREATE INDEX idx_order_delete_flag_create_time_id_sn ON li_order (delete_flag, create_time DESC, id DESC, sn);

-- 针对WHERE条件和连接条件的组合索引
CREATE INDEX idx_order_status_delete_flag_sn ON li_order (order_status, delete_flag, sn);

-- 针对连接条件的索引
CREATE INDEX idx_order_item_order_sn ON li_order_item (order_sn);

-- 针对过滤条件、排序字段的组合索引
CREATE INDEX idx_li_member_disabled_create_time ON li_member (disabled, create_time DESC);

-- 针对过滤条件、排序字段的组合索引
CREATE INDEX idx_li_goods_delete_flag_create_time ON li_goods (delete_flag, create_time DESC);

CREATE TABLE IF NOT EXISTS `li_stock_reason` (
  `id` BIGINT NOT NULL COMMENT 'ID',
  `create_by` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '创建者',
  `create_time` DATETIME(6) DEFAULT NULL COMMENT '创建时间',
  `delete_flag` BIT(1) DEFAULT NULL COMMENT '删除标志 true/false 删除/未删除',
  `update_by` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '更新者',
  `update_time` DATETIME(6) DEFAULT NULL COMMENT '更新时间',
  `reason` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '出入库原因',
  `category` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '类别',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_li_stock_reason_category` (`category`) USING BTREE,
  KEY `idx_li_stock_reason_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='出入库原因';

CREATE TABLE IF NOT EXISTS `li_inventory_count` (
  `id` BIGINT NOT NULL COMMENT 'ID',
  `create_by` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '创建者',
  `create_time` DATETIME(6) DEFAULT NULL COMMENT '创建时间',
  `delete_flag` BIT(1) DEFAULT NULL COMMENT '删除标志 true/false 删除/未删除',
  `update_by` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '更新者',
  `update_time` DATETIME(6) DEFAULT NULL COMMENT '更新时间',
  `sn` VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '盘点单号',
  `store_id` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '店铺ID',
  `maker_id` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '制单人ID',
  `maker_name` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '制单人名称',
  `item_total` INT DEFAULT NULL COMMENT '商品总数',
  `count_time` DATETIME(6) DEFAULT NULL COMMENT '盘点时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_li_inventory_count_sn` (`sn`) USING BTREE,
  KEY `idx_li_inventory_count_store_id` (`store_id`) USING BTREE,
  KEY `idx_li_inventory_count_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='盘点单';

CREATE TABLE IF NOT EXISTS `li_inventory_count_item` (
  `id` BIGINT NOT NULL COMMENT 'ID',
  `create_by` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '创建者',
  `create_time` DATETIME(6) DEFAULT NULL COMMENT '创建时间',
  `delete_flag` BIT(1) DEFAULT NULL COMMENT '删除标志 true/false 删除/未删除',
  `update_by` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '更新者',
  `update_time` DATETIME(6) DEFAULT NULL COMMENT '更新时间',
  `count_id` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '盘点单ID',
  `goods_id` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '商品ID',
  `sku_id` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'SKU ID',
  `goods_name` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '商品名称',
  `sku_name` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '规格名称（多规格用/拼接）',
  `market_enable` VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '商品上架状态',
  `quantity` INT DEFAULT NULL COMMENT '库存数量',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_li_inventory_count_item_count_id` (`count_id`) USING BTREE,
  KEY `idx_li_inventory_count_item_sku_id` (`sku_id`) USING BTREE,
  KEY `idx_li_inventory_count_item_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='盘点单明细';

CREATE TABLE IF NOT EXISTS `li_damage_report` (
  `id` BIGINT NOT NULL COMMENT 'ID',
  `create_by` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '创建者',
  `create_time` DATETIME(6) DEFAULT NULL COMMENT '创建时间',
  `delete_flag` BIT(1) DEFAULT NULL COMMENT '删除标志 true/false 删除/未删除',
  `update_by` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '更新者',
  `update_time` DATETIME(6) DEFAULT NULL COMMENT '更新时间',
  `sn` VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '报损单号',
  `store_id` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '店铺ID',
  `status` VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '状态',
  `damage_date` DATE DEFAULT NULL COMMENT '报损日期',
  `damage_reason_id` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '报损原因ID',
  `remark` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '备注',
  `evidence` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '凭证图片',
  `total_quantity` INT DEFAULT NULL COMMENT '报损总数量',
  `total_amount` DECIMAL(20,2) DEFAULT NULL COMMENT '报损总金额',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_li_damage_report_sn` (`sn`) USING BTREE,
  KEY `idx_li_damage_report_store_id` (`store_id`) USING BTREE,
  KEY `idx_li_damage_report_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='报损单';

CREATE TABLE IF NOT EXISTS `li_damage_report_item` (
  `id` BIGINT NOT NULL COMMENT 'ID',
  `create_by` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '创建者',
  `create_time` DATETIME(6) DEFAULT NULL COMMENT '创建时间',
  `delete_flag` BIT(1) DEFAULT NULL COMMENT '删除标志 true/false 删除/未删除',
  `update_by` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '更新者',
  `update_time` DATETIME(6) DEFAULT NULL COMMENT '更新时间',
  `report_id` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '报损单ID',
  `goods_id` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '商品ID',
  `sku_id` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'SKU ID',
  `quantity` INT DEFAULT NULL COMMENT '报损数量',
  `unit_price` DECIMAL(20,2) DEFAULT NULL COMMENT '单价',
  `amount` DECIMAL(20,2) DEFAULT NULL COMMENT '金额',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_li_damage_report_item_report_id` (`report_id`) USING BTREE,
  KEY `idx_li_damage_report_item_sku_id` (`sku_id`) USING BTREE,
  KEY `idx_li_damage_report_item_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='报损单明细';

CREATE TABLE IF NOT EXISTS `li_seat` (
  `id` BIGINT NOT NULL COMMENT 'ID',
  `create_by` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '创建者',
  `create_time` DATETIME(6) DEFAULT NULL COMMENT '创建时间',
  `delete_flag` BIT(1) DEFAULT NULL COMMENT '删除标志 true/false 删除/未删除',
  `update_by` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '更新者',
  `update_time` DATETIME(6) DEFAULT NULL COMMENT '更新时间',
  `tenant_id` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '租户ID',
  `username` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '坐席用户名',
  `face` VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '客户头像',
  `password` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '坐席密码',
  `nick_name` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '昵称',
  `disabled` BIT(1) DEFAULT NULL COMMENT '坐席状态',
  `mobile` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '手机号码',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_li_seat_username` (`username`) USING BTREE,
  KEY `idx_li_seat_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_li_seat_mobile` (`mobile`) USING BTREE,
  KEY `idx_li_seat_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='坐席';

CREATE TABLE IF NOT EXISTS `li_seat_setting` (
  `id` BIGINT NOT NULL COMMENT 'ID',
  `create_by` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '创建者',
  `create_time` DATETIME(6) DEFAULT NULL COMMENT '创建时间',
  `delete_flag` BIT(1) DEFAULT NULL COMMENT '删除标志 true/false 删除/未删除',
  `update_by` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '更新者',
  `update_time` DATETIME(6) DEFAULT NULL COMMENT '更新时间',
  `tenant_id` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '租户ID',
  `welcome` VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '欢迎语',
  `out_line_auto_reply` VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '离线自动回复',
  `long_term_auto_reply` VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '长时间自动回复',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_li_seat_setting_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_li_seat_setting_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='坐席设置';

CREATE TABLE IF NOT EXISTS `li_qa` (
  `id` BIGINT NOT NULL COMMENT 'ID',
  `create_by` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '创建者',
  `create_time` DATETIME(6) DEFAULT NULL COMMENT '创建时间',
  `delete_flag` BIT(1) DEFAULT NULL COMMENT '删除标志 true/false 删除/未删除',
  `update_by` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '更新者',
  `update_time` DATETIME(6) DEFAULT NULL COMMENT '更新时间',
  `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
  `question` VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '问题',
  `answer` VARCHAR(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '答案',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_li_qa_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_li_qa_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='租户问答';
