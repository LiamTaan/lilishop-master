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

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store' AND COLUMN_NAME = 'apply_type'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store` ADD COLUMN `apply_type` varchar(32) DEFAULT NULL COMMENT ''申请主体类型：PERSONAL个人，INDIVIDUAL个体户，COMPANY_LEGAL企业法人，COMPANY_NON_LEGAL企业非法人'' AFTER `store_name`',
  'SELECT ''li_store.apply_type exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store' AND COLUMN_NAME = 'store_type'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store` ADD COLUMN `store_type` varchar(32) DEFAULT NULL COMMENT ''店铺类型'' AFTER `apply_type`',
  'SELECT ''li_store.store_type exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store' AND COLUMN_NAME = 'agent_level'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store` ADD COLUMN `agent_level` varchar(32) DEFAULT NULL COMMENT ''代理等级'' AFTER `store_type`',
  'SELECT ''li_store.agent_level exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store' AND COLUMN_NAME = 'agent_region_id'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store` ADD COLUMN `agent_region_id` varchar(64) DEFAULT NULL COMMENT ''代理区域ID'' AFTER `agent_level`',
  'SELECT ''li_store.agent_region_id exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store' AND COLUMN_NAME = 'agent_region_name'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store` ADD COLUMN `agent_region_name` varchar(255) DEFAULT NULL COMMENT ''代理区域名称'' AFTER `agent_region_id`',
  'SELECT ''li_store.agent_region_name exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store' AND COLUMN_NAME = 'audit_status'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store` ADD COLUMN `audit_status` varchar(32) NOT NULL DEFAULT ''DRAFT'' COMMENT ''审核状态：DRAFT草稿，SUBMITTED待审核，APPROVED通过，REJECTED驳回，FROZEN冻结'' AFTER `store_disable`',
  'SELECT ''li_store.audit_status exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store' AND COLUMN_NAME = 'audit_remark'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store` ADD COLUMN `audit_remark` varchar(255) DEFAULT NULL COMMENT ''审核备注'' AFTER `audit_status`',
  'SELECT ''li_store.audit_remark exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'apply_type'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `apply_type` varchar(32) DEFAULT NULL COMMENT ''申请主体类型'' AFTER `store_name`',
  'SELECT ''li_store_detail.apply_type exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'store_type'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `store_type` varchar(32) DEFAULT NULL COMMENT ''店铺类型'' AFTER `apply_type`',
  'SELECT ''li_store_detail.store_type exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'agent_level'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `agent_level` varchar(32) DEFAULT NULL COMMENT ''代理等级'' AFTER `store_type`',
  'SELECT ''li_store_detail.agent_level exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'agent_region_id'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `agent_region_id` varchar(64) DEFAULT NULL COMMENT ''代理区域ID'' AFTER `agent_level`',
  'SELECT ''li_store_detail.agent_region_id exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'agent_region_name'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `agent_region_name` varchar(255) DEFAULT NULL COMMENT ''代理区域名称'' AFTER `agent_region_id`',
  'SELECT ''li_store_detail.agent_region_name exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'business_license_url'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `business_license_url` varchar(255) DEFAULT NULL COMMENT ''营业执照图片'' AFTER `scope`',
  'SELECT ''li_store_detail.business_license_url exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'credit_code'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `credit_code` varchar(64) DEFAULT NULL COMMENT ''统一社会信用代码'' AFTER `business_license_url`',
  'SELECT ''li_store_detail.credit_code exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'business_license_region_id'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `business_license_region_id` varchar(64) DEFAULT NULL COMMENT ''营业执照所在地区ID'' AFTER `credit_code`',
  'SELECT ''li_store_detail.business_license_region_id exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'business_license_expire_type'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `business_license_expire_type` varchar(32) DEFAULT NULL COMMENT ''营业执照有效期类型'' AFTER `business_license_region_id`',
  'SELECT ''li_store_detail.business_license_expire_type exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'business_license_expire_date'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `business_license_expire_date` varchar(32) DEFAULT NULL COMMENT ''营业执照有效期截止时间'' AFTER `business_license_expire_type`',
  'SELECT ''li_store_detail.business_license_expire_date exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'facade_image_url'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `facade_image_url` varchar(255) DEFAULT NULL COMMENT ''门头照'' AFTER `business_license_expire_date`',
  'SELECT ''li_store_detail.facade_image_url exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'indoor_image_urls'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `indoor_image_urls` text DEFAULT NULL COMMENT ''店内照，英文逗号分隔'' AFTER `facade_image_url`',
  'SELECT ''li_store_detail.indoor_image_urls exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'store_category_id'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `store_category_id` varchar(64) DEFAULT NULL COMMENT ''店铺分类ID'' AFTER `indoor_image_urls`',
  'SELECT ''li_store_detail.store_category_id exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'business_hours_start'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `business_hours_start` varchar(16) DEFAULT NULL COMMENT ''营业开始时间'' AFTER `store_category_id`',
  'SELECT ''li_store_detail.business_hours_start exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'business_hours_end'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `business_hours_end` varchar(16) DEFAULT NULL COMMENT ''营业结束时间'' AFTER `business_hours_start`',
  'SELECT ''li_store_detail.business_hours_end exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'real_name'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `real_name` varchar(64) DEFAULT NULL COMMENT ''个人主体姓名'' AFTER `legal_photo`',
  'SELECT ''li_store_detail.real_name exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'id_card_no'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `id_card_no` varchar(64) DEFAULT NULL COMMENT ''个人身份证号'' AFTER `real_name`',
  'SELECT ''li_store_detail.id_card_no exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'id_card_front_url'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `id_card_front_url` varchar(255) DEFAULT NULL COMMENT ''身份证正面'' AFTER `id_card_no`',
  'SELECT ''li_store_detail.id_card_front_url exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'id_card_back_url'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `id_card_back_url` varchar(255) DEFAULT NULL COMMENT ''身份证反面'' AFTER `id_card_front_url`',
  'SELECT ''li_store_detail.id_card_back_url exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'legal_mobile'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `legal_mobile` varchar(32) DEFAULT NULL COMMENT ''法人手机号'' AFTER `id_card_back_url`',
  'SELECT ''li_store_detail.legal_mobile exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'authorization_url'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `authorization_url` varchar(255) DEFAULT NULL COMMENT ''企业非法人授权书'' AFTER `legal_mobile`',
  'SELECT ''li_store_detail.authorization_url exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE li_store
SET store_type = 'SUPPLIER'
WHERE store_type IS NULL OR store_type = '';

UPDATE li_store_detail d
INNER JOIN li_store s ON s.id = d.store_id
SET d.store_type = s.store_type
WHERE d.store_type IS NULL OR d.store_type = '';

UPDATE li_store_detail d
INNER JOIN li_store s ON s.id = d.store_id
SET d.agent_level = s.agent_level,
    d.agent_region_id = s.agent_region_id,
    d.agent_region_name = s.agent_region_name
WHERE (d.agent_level IS NULL OR d.agent_level = '')
   OR (d.agent_region_id IS NULL OR d.agent_region_id = '')
   OR (d.agent_region_name IS NULL OR d.agent_region_name = '');

CREATE TABLE IF NOT EXISTS `agent_role_relation` (
  `id` varchar(64) NOT NULL COMMENT '主键ID',
  `member_id` varchar(64) NOT NULL COMMENT '会员ID',
  `role_code` varchar(32) NOT NULL COMMENT '角色编码，固定为ROLE_AGENT',
  `region_id` varchar(64) NOT NULL COMMENT '所属区域ID',
  `region_name` varchar(128) NOT NULL COMMENT '所属区域名称',
  `agent_level` varchar(32) NOT NULL DEFAULT 'CITY' COMMENT '代理等级：CITY市级，COUNTY区县级，TOWNSHIP乡镇级，WHOLESALER批发商',
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

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'agent_role_relation' AND COLUMN_NAME = 'agent_level'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `agent_role_relation` ADD COLUMN `agent_level` varchar(32) NOT NULL DEFAULT ''CITY'' COMMENT ''代理等级：CITY市级，COUNTY区县级，TOWNSHIP乡镇级，WHOLESALER批发商'' AFTER `region_name`',
  'SELECT ''agent_role_relation.agent_level exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE agent_role_relation
SET agent_level = 'WHOLESALER'
WHERE agent_level IS NULL OR agent_level = '';

CREATE TABLE IF NOT EXISTS `agent_store_bind` (
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

CREATE TABLE IF NOT EXISTS `li_store_audit_log` (
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

-- Recommended indexes

-- 针对WHERE条件、排序和分组的组合索引
SET @idx_exists := (
  SELECT COUNT(1) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'li_order' AND index_name = 'idx_order_delete_flag_create_time_id_sn'
);
SET @sql := IF(@idx_exists = 0,
  'CREATE INDEX idx_order_delete_flag_create_time_id_sn ON li_order (delete_flag, create_time DESC, id DESC, sn)',
  'SELECT ''idx_order_delete_flag_create_time_id_sn exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 针对WHERE条件和连接条件的组合索引
SET @idx_exists := (
  SELECT COUNT(1) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'li_order' AND index_name = 'idx_order_status_delete_flag_sn'
);
SET @sql := IF(@idx_exists = 0,
  'CREATE INDEX idx_order_status_delete_flag_sn ON li_order (order_status, delete_flag, sn)',
  'SELECT ''idx_order_status_delete_flag_sn exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 针对连接条件的索引
SET @idx_exists := (
  SELECT COUNT(1) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'li_order_item' AND index_name = 'idx_order_item_order_sn'
);
SET @sql := IF(@idx_exists = 0,
  'CREATE INDEX idx_order_item_order_sn ON li_order_item (order_sn)',
  'SELECT ''idx_order_item_order_sn exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 针对过滤条件、排序字段的组合索引
SET @idx_exists := (
  SELECT COUNT(1) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'li_member' AND index_name = 'idx_li_member_disabled_create_time'
);
SET @sql := IF(@idx_exists = 0,
  'CREATE INDEX idx_li_member_disabled_create_time ON li_member (disabled, create_time DESC)',
  'SELECT ''idx_li_member_disabled_create_time exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 针对过滤条件、排序字段的组合索引
SET @idx_exists := (
  SELECT COUNT(1) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'li_goods' AND index_name = 'idx_li_goods_delete_flag_create_time'
);
SET @sql := IF(@idx_exists = 0,
  'CREATE INDEX idx_li_goods_delete_flag_create_time ON li_goods (delete_flag, create_time DESC)',
  'SELECT ''idx_li_goods_delete_flag_create_time exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

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

-- 采购治理相关表补齐
CREATE TABLE IF NOT EXISTS `li_procurement_order` (
  `id` BIGINT NOT NULL COMMENT 'ID',
  `create_by` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '创建者',
  `create_time` DATETIME(6) DEFAULT NULL COMMENT '创建时间',
  `delete_flag` BIT(1) DEFAULT NULL COMMENT '删除标志 true/false 删除/未删除',
  `update_by` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '更新者',
  `update_time` DATETIME(6) DEFAULT NULL COMMENT '更新时间',
  `order_sn` VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '采购订单编号',
  `store_id` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '店铺ID',
  `store_name` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '店铺名称',
  `total_amount` DECIMAL(20,2) DEFAULT NULL COMMENT '采购总金额',
  `total_quantity` INT DEFAULT NULL COMMENT '采购总数量',
  `maker_id` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '制单人ID',
  `maker_name` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '制单人',
  `audit_time` DATETIME(6) DEFAULT NULL COMMENT '审核时间',
  `remark` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '备注',
  `status` VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '状态',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_li_procurement_order_order_sn` (`order_sn`) USING BTREE,
  KEY `idx_li_procurement_order_store_id` (`store_id`) USING BTREE,
  KEY `idx_li_procurement_order_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='采购单';

CREATE TABLE IF NOT EXISTS `li_procurement_order_item` (
  `id` BIGINT NOT NULL COMMENT 'ID',
  `create_time` DATETIME(6) DEFAULT NULL COMMENT '创建时间',
  `procurement_order_id` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '采购订单ID',
  `goods_id` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '商品ID',
  `sku_id` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'SKU ID',
  `goods_name` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '商品名称',
  `retail_price` DECIMAL(20,2) DEFAULT NULL COMMENT '零售价',
  `quantity` INT DEFAULT NULL COMMENT '采购数量',
  `tax_rate` INT DEFAULT NULL COMMENT '税率(百分比整数)',
  `unit_price_with_tax` DECIMAL(20,2) DEFAULT NULL COMMENT '含税单价',
  `unit_price_without_tax` DECIMAL(20,2) DEFAULT NULL COMMENT '不含税单价',
  `subtotal_without_tax` DECIMAL(20,2) DEFAULT NULL COMMENT '不含税小计',
  `subtotal_with_tax` DECIMAL(20,2) DEFAULT NULL COMMENT '含税小计',
  `received_quantity` INT DEFAULT NULL COMMENT '已入库数量',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_li_procurement_order_item_order_id` (`procurement_order_id`) USING BTREE,
  KEY `idx_li_procurement_order_item_sku_id` (`sku_id`) USING BTREE,
  KEY `idx_li_procurement_order_item_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='采购单明细';

CREATE TABLE IF NOT EXISTS `li_procurement_inbound` (
  `id` BIGINT NOT NULL COMMENT 'ID',
  `create_by` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '创建者',
  `create_time` DATETIME(6) DEFAULT NULL COMMENT '创建时间',
  `delete_flag` BIT(1) DEFAULT NULL COMMENT '删除标志 true/false 删除/未删除',
  `update_by` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '更新者',
  `update_time` DATETIME(6) DEFAULT NULL COMMENT '更新时间',
  `inbound_sn` VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '入库单号',
  `store_id` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '店铺ID',
  `procurement_order_id` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '采购订单ID',
  `expected_quantity` INT DEFAULT NULL COMMENT '预计入库量',
  `confirmed_quantity` INT DEFAULT NULL COMMENT '已确认入库量',
  `pending_quantity` INT DEFAULT NULL COMMENT '待确认入库量',
  `total_cost` DECIMAL(20,2) DEFAULT NULL COMMENT '合计入库成本',
  `total_retail_amount` DECIMAL(20,2) DEFAULT NULL COMMENT '合计零售金额',
  `inbound_time` DATETIME(6) DEFAULT NULL COMMENT '入库时间',
  `certificate_images` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '入库凭证',
  `operator_id` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '制单人ID',
  `operator_name` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '制单人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_li_procurement_inbound_inbound_sn` (`inbound_sn`) USING BTREE,
  KEY `idx_li_procurement_inbound_store_id` (`store_id`) USING BTREE,
  KEY `idx_li_procurement_inbound_order_id` (`procurement_order_id`) USING BTREE,
  KEY `idx_li_procurement_inbound_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='采购入库单';

CREATE TABLE IF NOT EXISTS `li_procurement_inbound_item` (
  `id` BIGINT NOT NULL COMMENT 'ID',
  `inbound_id` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '入库单ID',
  `procurement_order_item_id` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '采购订单子项ID',
  `goods_id` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '商品ID',
  `sku_id` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'SKU ID',
  `goods_name` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '商品名称',
  `expected_quantity` INT DEFAULT NULL COMMENT '预计入库量',
  `inbound_quantity` INT DEFAULT NULL COMMENT '实际入库量',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_li_procurement_inbound_item_inbound_id` (`inbound_id`) USING BTREE,
  KEY `idx_li_procurement_inbound_item_order_item_id` (`procurement_order_item_id`) USING BTREE,
  KEY `idx_li_procurement_inbound_item_sku_id` (`sku_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='采购入库明细';

-- 批发管理端菜单、权限、条码补丁在空库初始化阶段一并完成
-- 这里直接内联 SQL，避免 Navicat/DataGrip 等客户端执行 SOURCE 时报 1064。

-- BEGIN patch_wholesale_manager_menu_route_baseline.sql
DROP TEMPORARY TABLE IF EXISTS `tmp_wholesale_manager_menu_seed`;

CREATE TEMPORARY TABLE `tmp_wholesale_manager_menu_seed` (
  `id` bigint NOT NULL,
  `create_by` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `delete_flag` bit(1) DEFAULT NULL,
  `update_by` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `description` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `front_route` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `icon` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `level` int DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `parent_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `path` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `sort_order` decimal(10,2) DEFAULT NULL,
  `title` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `front_component` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `permission` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;

INSERT IGNORE INTO `tmp_wholesale_manager_menu_seed` (
  `id`,`create_by`,`create_time`,`delete_flag`,`update_by`,`update_time`,
  `description`,`front_route`,`icon`,`level`,`name`,`parent_id`,`path`,
  `sort_order`,`title`,`front_component`,`permission`
) VALUES
  ('3062200000000000001', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/', 'ep:data-board', '0', 'Home', '0', '/', 10.00, '工作台', NULL, NULL),
  ('3062200000000010001', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/welcome', '', '1', 'WholesaleDashboardHome', '3062200000000000001', '/welcome', 1.00, '工作台', NULL, NULL),
  ('3062200000000000002', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/dashboard', 'ep:data-board', '0', 'WholesaleDashboard', '0', '/dashboard', 20.00, '数据概览', NULL, NULL),
  ('3062200000000010002', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/dashboard/wholesale', '', '1', 'WholesaleDashboard', '3062200000000000002', '/dashboard/wholesale', 1.00, '工作台', NULL, NULL),
  ('3062200000000010003', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/dashboard/overview', '', '1', 'PlatformOverview', '3062200000000000002', '/dashboard/overview', 2.00, '数据概览', NULL, NULL),
  ('3062200000000010004', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/dashboard/store-rank', '', '1', 'StoreRank', '3062200000000000002', '/dashboard/store-rank', 3.00, '门店排行', NULL, NULL),
  ('3062200000000010005', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/dashboard/goods-rank', '', '1', 'GoodsRank', '3062200000000000002', '/dashboard/goods-rank', 4.00, '商品/品类排行', NULL, NULL),
  ('3062200000000010006', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/dashboard/order-overview', '', '1', 'OrderOverview', '3062200000000000002', '/dashboard/order-overview', 5.00, '订单概览', NULL, NULL),
  ('3062200000000000003', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/store-governance', 'ep:shop', '0', 'StoreApplyAudit', '0', '/store-governance', 30.00, '店铺治理', NULL, NULL),
  ('3062200000000010007', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/store-governance/store-apply', '', '1', 'StoreApplyAudit', '3062200000000000003', '/store-governance/store-apply', 1.00, '入驻审核', NULL, NULL),
  ('3062200000000010008', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/store-governance/store-manage', '', '1', 'StoreManage', '3062200000000000003', '/store-governance/store-manage', 2.00, '店铺管理', NULL, NULL),
  ('3062200000000010009', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/store-governance/agent-manage', '', '1', 'AgentManage', '3062200000000000003', '/store-governance/agent-manage', 3.00, '代理商治理', NULL, NULL),
  ('3062200000000010010', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/store-governance/store-audit-log', '', '1', 'StoreAuditLog', '3062200000000000003', '/store-governance/store-audit-log', 4.00, '店铺审核历史', NULL, NULL),
  ('3062200000000000004', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/goods-governance', 'ep:goods', '0', 'GoodsManage', '0', '/goods-governance', 40.00, '商品管理', NULL, NULL),
  ('3062200000000010011', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/goods-governance/goods-manage', '', '1', 'GoodsManage', '3062200000000000004', '/goods-governance/goods-manage', 1.00, '商品列表', NULL, NULL),
  ('3062200000000010093', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/goods-governance/brand-manage', '', '1', 'BrandManage', '3062200000000000004', '/goods-governance/brand-manage', 2.00, '品牌管理', NULL, NULL),
  ('3062200000000010094', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/goods-governance/parameter-manage', '', '1', 'ParameterManage', '3062200000000000004', '/goods-governance/parameter-manage', 3.00, '参数管理', NULL, NULL),
  ('3062200000000010012', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/goods-governance/category-manage', '', '1', 'CategoryManage', '3062200000000000004', '/goods-governance/category-manage', 4.00, '分类管理', NULL, NULL),
  ('3062200000000010095', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/goods-governance/goods-group-manage', '', '1', 'GoodsGroupManage', '3062200000000000004', '/goods-governance/goods-group-manage', 5.00, '商品分组', NULL, NULL),
  ('3062200000000010014', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/goods-governance/points-goods', '', '1', 'PointsGoodsManage', '3062200000000000004', '/goods-governance/points-goods', 7.00, '积分商品治理', NULL, NULL),
  ('3062200000000010015', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/goods-governance/card-coupon-goods', '', '1', 'CardCouponGoodsManage', '3062200000000000004', '/goods-governance/card-coupon-goods', 8.00, '卡券商品治理', NULL, NULL),
  ('3062200000000000005', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/order-governance', 'ep:document', '0', 'OrderManage', '0', '/order-governance', 50.00, '订单管理', NULL, NULL),
  ('3062200000000010016', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/order-governance/order-manage', '', '1', 'OrderManage', '3062200000000000005', '/order-governance/order-manage', 1.00, '订单列表', NULL, NULL),
  ('3062200000000010096', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/order-governance/order-complaint', '', '1', 'OrderComplaintManage', '3062200000000000005', '/order-governance/order-complaint', 2.00, '订单投诉', NULL, NULL),
  ('3062200000000010017', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/order-governance/verification-rule', '', '1', 'VerificationRuleManage', '3062200000000000005', '/order-governance/verification-rule', 3.00, '核销规则', NULL, NULL),
  ('3062200000000010107', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/order-governance/refund-log', '', '1', 'RefundLogManage', '3062200000000000005', '/order-governance/refund-log', 4.00, '退款日志', NULL, NULL),
  ('3062200000000010108', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/order-governance/payment-log', '', '1', 'PaymentLogManage', '3062200000000000005', '/order-governance/payment-log', 5.00, '支付日志', NULL, NULL),
  ('3062200000000010109', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/order-governance/receipt-manage', '', '1', 'ReceiptManage', '3062200000000000005', '/order-governance/receipt-manage', 6.00, '发票管理', NULL, NULL),
  ('3062200000000010110', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/order-governance/order-log', '', '1', 'OrderLogManage', '3062200000000000005', '/order-governance/order-log', 7.00, '订单日志', NULL, NULL),
  ('3062200000000010018', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/order-governance/after-sale', '', '1', 'AfterSaleManage', '3062200000000000005', '/order-governance/after-sale', 8.00, '售后治理', NULL, NULL),
  ('3062200000000010019', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/order-governance/verification-record', '', '1', 'VerificationRecordManage', '3062200000000000005', '/order-governance/verification-record', 9.00, '核销记录', NULL, NULL),
  ('3062200000000000006', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/marketing-governance', 'ep:sell', '0', 'MarketingGovernance', '0', '/marketing-governance', 60.00, '营销管理', NULL, NULL),
  ('3062200000000010101', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/marketing-governance/coupon-manage', '', '1', 'CouponManage', '3062200000000000006', '/marketing-governance/coupon-manage', 1.00, '优惠券管理', NULL, NULL),
  ('3062200000000010102', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/marketing-governance/coupon-activity', '', '1', 'CouponActivityManage', '3062200000000000006', '/marketing-governance/coupon-activity', 2.00, '券活动管理', NULL, NULL),
  ('3062200000000010103', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/marketing-governance/full-discount', '', '1', 'FullDiscountManage', '3062200000000000006', '/marketing-governance/full-discount', 3.00, '满减活动管理', NULL, NULL),
  ('3062200000000010104', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/marketing-governance/seckill-manage', '', '1', 'SeckillManage', '3062200000000000006', '/marketing-governance/seckill-manage', 4.00, '秒杀活动管理', NULL, NULL),
  ('3062200000000010105', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/marketing-governance/kanjia-manage', '', '1', 'KanjiaManage', '3062200000000000006', '/marketing-governance/kanjia-manage', 5.00, '砍价活动管理', NULL, NULL),
  ('3062200000000010020', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/marketing-governance/pintuan-rule', '', '1', 'PintuanRule', '3062200000000000006', '/marketing-governance/pintuan-rule', 6.00, '拼团规则', NULL, NULL),
  ('3062200000000010021', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/marketing-governance/pintuan-record', '', '1', 'PintuanRecord', '3062200000000000006', '/marketing-governance/pintuan-record', 7.00, '拼团记录', NULL, NULL),
  ('3062200000000000007', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/fund-governance', 'ep:money', '0', 'ProfitSharingRule', '0', '/fund-governance', 70.00, '分账管理', NULL, NULL),
  ('3062200000000010022', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/fund-governance/profitsharing-rule', '', '1', 'ProfitSharingRule', '3062200000000000007', '/fund-governance/profitsharing-rule', 1.00, '分账规则', NULL, NULL),
  ('3062200000000010106', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/fund-governance/bill-manage', '', '1', 'BillManage', '3062200000000000007', '/fund-governance/bill-manage', 2.00, '结算单', NULL, NULL),
  ('3062200000000010023', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/fund-governance/profitsharing-record', '', '1', 'ProfitSharingRecord', '3062200000000000007', '/fund-governance/profitsharing-record', 3.00, '分账明细', NULL, NULL),
  ('3062200000000010024', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/fund-governance/wallet-log', '', '1', 'WalletLog', '3062200000000000007', '/fund-governance/wallet-log', 4.00, '余额记录', NULL, NULL),
  ('3062200000000010025', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/fund-governance/profitsharing-balance', '', '1', 'ProfitSharingBalance', '3062200000000000007', '/fund-governance/profitsharing-balance', 5.00, '分账治理概览', NULL, NULL),
  ('3062200000000010026', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/fund-governance/wallet-account', '', '1', 'WalletAccount', '3062200000000000007', '/fund-governance/wallet-account', 6.00, '钱包账户台账', NULL, NULL),
  ('3062200000000010027', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/fund-governance/withdraw-apply', '', '1', 'WithdrawApply', '3062200000000000007', '/fund-governance/withdraw-apply', 7.00, '提现审核', NULL, NULL),
  ('3062200000000010028', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/fund-governance/procurement-reconciliation', '', '1', 'ProcurementReconciliation', '3062200000000000007', '/fund-governance/procurement-reconciliation', 8.00, '采购对账', NULL, NULL),
  ('3062200000000010029', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/fund-governance/fund-reconciliation', '', '1', 'FundReconciliation', '3062200000000000007', '/fund-governance/fund-reconciliation', 9.00, '资金对账', NULL, NULL),
  ('3062200000000000008', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config', 'ep:tools', '0', 'PlatformBaseSetting', '0', '/platform-config', 80.00, '平台配置', NULL, NULL),
  ('3062200000000010030', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/base-setting', '', '1', 'PlatformBaseSetting', '3062200000000000008', '/platform-config/base-setting', 1.00, '基础设置', NULL, NULL),
  ('3062200000000010031', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/payment-setting', '', '1', 'PlatformPaymentSetting', '3062200000000000008', '/platform-config/payment-setting', 2.00, '支付配置', NULL, NULL),
  ('3062200000000010032', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/withdraw-setting', '', '1', 'PlatformWithdrawSetting', '3062200000000000008', '/platform-config/withdraw-setting', 3.00, '提现设置', NULL, NULL),
  ('3062200000000010033', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/oss-setting', '', '1', 'PlatformOssSetting', '3062200000000000008', '/platform-config/oss-setting', 4.00, '对象存储配置', NULL, NULL),
  ('3062200000000010034', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/sms-setting', '', '1', 'PlatformSmsSetting', '3062200000000000008', '/platform-config/sms-setting', 5.00, '短信配置', NULL, NULL),
  ('3062200000000010035', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/email-setting', '', '1', 'PlatformEmailSetting', '3062200000000000008', '/platform-config/email-setting', 6.00, '邮件配置', NULL, NULL),
  ('3062200000000010036', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/goods-setting', '', '1', 'PlatformGoodsSetting', '3062200000000000008', '/platform-config/goods-setting', 7.00, '商品设置', NULL, NULL),
  ('3062200000000010037', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/order-setting', '', '1', 'PlatformOrderSetting', '3062200000000000008', '/platform-config/order-setting', 8.00, '订单设置', NULL, NULL),
  ('3062200000000010038', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/logistics-setting', '', '1', 'PlatformLogisticsSetting', '3062200000000000008', '/platform-config/logistics-setting', 9.00, '物流设置', NULL, NULL),
  ('3062200000000010039', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/point-setting', '', '1', 'PlatformPointSetting', '3062200000000000008', '/platform-config/point-setting', 10.00, '积分设置', NULL, NULL),
  ('3062200000000010041', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/seckill-setting', '', '1', 'PlatformSeckillSetting', '3062200000000000008', '/platform-config/seckill-setting', 12.00, '秒杀设置', NULL, NULL),
  ('3062200000000010042', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/im-setting', '', '1', 'PlatformImSetting', '3062200000000000008', '/platform-config/im-setting', 13.00, 'IM配置', NULL, NULL),
  ('3062200000000010043', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/connect-setting', '', '1', 'PlatformConnectSetting', '3062200000000000008', '/platform-config/connect-setting', 14.00, '登录设置', NULL, NULL),
  ('3062200000000010044', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/distribution-setting', '', '1', 'PlatformDistributionSetting', '3062200000000000008', '/platform-config/distribution-setting', 15.00, '分销设置', NULL, NULL),
  ('3062200000000010045', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/alipay-setting', '', '1', 'PlatformAlipaySetting', '3062200000000000008', '/platform-config/alipay-setting', 16.00, '支付宝配置', NULL, NULL),
  ('3062200000000010046', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/wechat-payment-setting', '', '1', 'PlatformWechatPaymentSetting', '3062200000000000008', '/platform-config/wechat-payment-setting', 17.00, '微信支付配置', NULL, NULL),
  ('3062200000000010047', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/unionpay-setting', '', '1', 'PlatformUnionpaySetting', '3062200000000000008', '/platform-config/unionpay-setting', 18.00, '银联支付配置', NULL, NULL),
  ('3062200000000010051', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/wx-channels-setting', '', '1', 'PlatformWxChannelsSetting', '3062200000000000008', '/platform-config/wx-channels-setting', 22.00, '视频号配置', NULL, NULL),
  ('3062200000000000009', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/member-center', 'ep:user', '0', 'MemberList', '0', '/member-center', 90.00, '用户中心', NULL, NULL),
  ('3062200000000010052', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/member-center/member-list', '', '1', 'MemberList', '3062200000000000009', '/member-center/member-list', 1.00, '前台用户管理', NULL, NULL),
  ('3062200000000010054', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/member-center/member-group', '', '1', 'MemberGroup', '3062200000000000009', '/member-center/member-group', 3.00, '会员分组', NULL, NULL),
  ('3062200000000010055', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/member-center/member-benefit', '', '1', 'MemberBenefit', '3062200000000000009', '/member-center/member-benefit', 4.00, '会员权益', NULL, NULL),
  ('3062200000000010056', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/member-center/member-points-history', '', '1', 'MemberPointsHistory', '3062200000000000009', '/member-center/member-points-history', 5.00, '积分记录', NULL, NULL),
  ('3062200000000010057', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/member-center/member-evaluation', '', '1', 'MemberEvaluation', '3062200000000000009', '/member-center/member-evaluation', 6.00, '用户评价', NULL, NULL),
  ('3062200000000010058', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/member-center/member-address', '', '1', 'MemberAddress', '3062200000000000009', '/member-center/member-address', 7.00, '用户地址', NULL, NULL),
  ('3062200000000000010', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/message-center', 'ep:chat-dot-round', '0', 'MemberNotice', '0', '/message-center', 100.00, '通知与消息', NULL, NULL),
  ('3062200000000010059', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/message-center/member-notice', '', '1', 'MemberNotice', '3062200000000000010', '/message-center/member-notice', 1.00, '站内通知', NULL, NULL),
  ('3062200000000010060', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/message-center/member-notice-log', '', '1', 'MemberNoticeLog', '3062200000000000010', '/message-center/member-notice-log', 2.00, '通知日志', NULL, NULL),
  ('3062200000000010061', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/message-center/sms-sign', '', '1', 'SmsSign', '3062200000000000010', '/message-center/sms-sign', 3.00, '短信签名', NULL, NULL),
  ('3062200000000010062', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/message-center/sms-template', '', '1', 'SmsTemplate', '3062200000000000010', '/message-center/sms-template', 4.00, '短信模板', NULL, NULL),
  ('3062200000000010063', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/message-center/member-notice-sender', '', '1', 'MemberNoticeSender', '3062200000000000010', '/message-center/member-notice-sender', 5.00, '发送任务', NULL, NULL),
  ('3062200000000010064', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/message-center/service-notice', '', '1', 'ServiceNotice', '3062200000000000010', '/message-center/service-notice', 6.00, '服务通知', NULL, NULL),
  ('3062200000000010065', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/message-center/message-channel', '', '1', 'MessageChannel', '3062200000000000010', '/message-center/message-channel', 7.00, '消息发送管理', NULL, NULL),
  ('3062200000000010066', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/message-center/member-message', '', '1', 'MemberMessage', '3062200000000000010', '/message-center/member-message', 8.00, '客户消息', NULL, NULL),
  ('3062200000000010067', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/message-center/store-message', '', '1', 'StoreMessage', '3062200000000000010', '/message-center/store-message', 9.00, '店铺消息', NULL, NULL),
  ('3062200000000000011', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/content-center', 'ep:document', '0', 'ContentArticle', '0', '/content-center', 110.00, '内容与运营', NULL, NULL),
  ('3062200000000010069', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/content-center/advertisement', '', '1', 'ContentAdvertisement', '3062200000000000011', '/content-center/advertisement', 1.00, '广告位管理', NULL, NULL),
  ('3062200000000010070', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/content-center/special-manage', '', '1', 'ContentSpecialManage', '3062200000000000011', '/content-center/special-manage', 2.00, '专题管理', NULL, NULL),
  ('3062200000000010071', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/content-center/recommendation-strategy', '', '1', 'ContentRecommendationStrategy', '3062200000000000011', '/content-center/recommendation-strategy', 3.00, '推荐策略', NULL, NULL),
  ('3062200000000010068', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/content-center/article', '', '1', 'ContentArticle', '3062200000000000011', '/content-center/article', 4.00, '帮助与公告', NULL, NULL),
  ('3062200000000010072', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/content-center/article-category', '', '1', 'ContentArticleCategory', '3062200000000000011', '/content-center/article-category', 5.00, '文章分类', NULL, NULL),
  ('3062200000000010073', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/content-center/sensitive-words', '', '1', 'ContentSensitiveWords', '3062200000000000011', '/content-center/sensitive-words', 6.00, '敏感词管理', NULL, NULL),
  ('3062200000000010074', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/content-center/custom-words', '', '1', 'ContentCustomWords', '3062200000000000011', '/content-center/custom-words', 7.00, '自定义词库', NULL, NULL),
  ('3062200000000010077', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/content-center/app-version', '', '1', 'ContentAppVersion', '3062200000000000011', '/content-center/app-version', 8.00, 'APP版本', NULL, NULL),
  ('3062200000000000012', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/support-center', 'ep:setting', '0', 'SupportVerificationSource', '0', '/support-center', 120.00, '系统支撑', NULL, NULL),
  ('3062200000000010078', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/support-center/verification-source', '', '1', 'SupportVerificationSource', '3062200000000000012', '/support-center/verification-source', 1.00, '验证码资源', NULL, NULL),
  ('3062200000000010079', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/support-center/region-manage', '', '1', 'SupportRegionManage', '3062200000000000012', '/support-center/region-manage', 2.00, '区域管理', NULL, NULL),
  ('3062200000000010080', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/support-center/logistics-manage', '', '1', 'SupportLogisticsManage', '3062200000000000012', '/support-center/logistics-manage', 3.00, '物流公司', NULL, NULL),
  ('3062200000000010113', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/support-center/freight-template-manage', '', '1', 'SupportFreightTemplateManage', '3062200000000000012', '/support-center/freight-template-manage', 3.50, '运费模板', NULL, NULL),
  ('3062200000000010081', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/support-center/after-sale-reason', '', '1', 'SupportAfterSaleReason', '3062200000000000012', '/support-center/after-sale-reason', 4.00, '售后原因', NULL, NULL),
  ('3062200000000010082', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/support-center/feedback-manage', '', '1', 'SupportFeedbackManage', '3062200000000000012', '/support-center/feedback-manage', 5.00, '意见反馈', NULL, NULL),
  ('3062200000000010083', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/support-center/setting-log', '', '1', 'SupportSettingLog', '3062200000000000012', '/support-center/setting-log', 6.00, '配置日志', NULL, NULL),
  ('3062200000000000013', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/merchant-grant', 'ep:key', '0', 'MerchantGrantMenuPermission', '0', '/merchant-grant', 130.00, '系统与权限', NULL, NULL),
  ('3062200000000010086', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/merchant-grant/menu-permission', '', '1', 'MerchantGrantMenuPermission', '3062200000000000013', '/merchant-grant/menu-permission', 1.00, '平台菜单权限', NULL, NULL),
  ('3062200000000010087', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/merchant-grant/role-permission', '', '1', 'MerchantGrantRolePermission', '3062200000000000013', '/merchant-grant/role-permission', 2.00, '平台角色权限', NULL, NULL),
  ('3062200000000010088', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/merchant-grant/department-manage', '', '1', 'MerchantGrantDepartmentManage', '3062200000000000013', '/merchant-grant/department-manage', 3.00, '组织架构配置', NULL, NULL),
  ('3062200000000010089', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/merchant-grant/backend-account', '', '1', 'MerchantGrantBackendAccount', '3062200000000000013', '/merchant-grant/backend-account', 4.00, '平台后台账号', NULL, NULL),
  ('3062200000000010097', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/merchant-grant/store-menu', '', '1', 'MerchantGrantStoreMenu', '3062200000000000013', '/merchant-grant/store-menu', 5.00, '店铺菜单资源', NULL, NULL),
  ('3062200000000000014', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/system', 'ep:setting', '0', 'SystemWarningSetting', '0', '/system', 140.00, '系统设置', NULL, NULL),
  ('3062200000000010090', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/system/warning-setting', '', '1', 'SystemWarningSetting', '3062200000000000014', '/system/warning-setting', 1.00, '预警设置', NULL, NULL),
  ('3062200000000010091', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/system/maintenance-log', '', '1', 'SystemMaintenanceLog', '3062200000000000014', '/system/maintenance-log', 2.00, '系统维护记录', NULL, NULL),
  ('3062200000000010092', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/system/operation-log', '', '1', 'SystemOperationLog', '3062200000000000014', '/system/operation-log', 3.00, '操作日志', NULL, NULL);

INSERT INTO `li_menu` (
  `id`,`create_by`,`create_time`,`delete_flag`,`update_by`,`update_time`,
  `description`,`front_route`,`icon`,`level`,`name`,`parent_id`,`path`,
  `sort_order`,`title`,`front_component`,`permission`
)
SELECT
  `id`,`create_by`,`create_time`,`delete_flag`,`update_by`,`update_time`,
  `description`,`front_route`,`icon`,`level`,`name`,`parent_id`,`path`,
  `sort_order`,`title`,`front_component`,`permission`
FROM `tmp_wholesale_manager_menu_seed` seed
WHERE NOT EXISTS (
  SELECT 1 FROM `li_menu` m WHERE m.`id` = seed.`id`
);

UPDATE `li_menu` m
JOIN `tmp_wholesale_manager_menu_seed` seed ON m.`id` = seed.`id`
SET
  m.`update_by` = seed.`update_by`,
  m.`update_time` = CURRENT_TIMESTAMP,
  m.`delete_flag` = seed.`delete_flag`,
  m.`description` = seed.`description`,
  m.`front_route` = seed.`front_route`,
  m.`icon` = seed.`icon`,
  m.`level` = seed.`level`,
  m.`name` = seed.`name`,
  m.`parent_id` = seed.`parent_id`,
  m.`path` = seed.`path`,
  m.`sort_order` = seed.`sort_order`,
  m.`title` = seed.`title`,
  m.`front_component` = seed.`front_component`;

DROP TEMPORARY TABLE IF EXISTS `tmp_wholesale_manager_menu_seed`;

DELETE FROM `li_role_menu`
WHERE `menu_id` IN ('3062200000000010071');

DELETE FROM `li_menu`
WHERE `id` = 3062200000000010071
   OR `front_route` IN ('/content-center/message-monitor', '/content-center/shortcut-nav');

-- BEGIN patch_wholesale_manager_menu_permission_fill.sql
UPDATE li_menu SET permission = '/manager/dashboard/wholesale*,/manager/statistics/index/notice*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/welcome';
UPDATE li_menu SET permission = '/manager/dashboard/wholesale*,/manager/statistics/index/notice*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/dashboard/wholesale';
UPDATE li_menu SET permission = '/manager/dashboard/wholesale*,/manager/statistics/order/overview*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/dashboard/overview';
UPDATE li_menu SET permission = '/manager/statistics/index/storeStatistics*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/dashboard/store-rank';
UPDATE li_menu SET permission = '/manager/statistics/index/goodsStatistics*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/dashboard/goods-rank';
UPDATE li_menu SET permission = '/manager/statistics/order/overview*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/dashboard/order-overview';
UPDATE li_menu SET permission = '/manager/store/store*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/store-governance/store-apply';
UPDATE li_menu SET permission = '/manager/store/store*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/store-governance/store-manage';
UPDATE li_menu SET permission = '/manager/agent/role*,/manager/agent/store*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/store-governance/agent-manage';
UPDATE li_menu SET permission = '/manager/store/store*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/store-governance/store-audit-log';
UPDATE li_menu
SET permission = '/manager/goods/goods*,/manager/goods/goodsUnit*,/manager/goods/categoryParameters*,/manager/goods/freightTemplate/store*,/manager/goods/category/allChildren*,/manager/goods/brand/all*,/manager/store/store/all*,/common/common/upload/file*'
WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110
  AND path = '/goods-governance/goods-manage';
UPDATE li_menu SET permission = '/manager/goods/brand*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/goods-governance/brand-manage';
UPDATE li_menu SET permission = '/manager/goods/parameters*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/goods-governance/parameter-manage';
UPDATE li_menu SET permission = '/manager/goods/category*,/manager/goods/brand/all*,/manager/goods/categoryBrand*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/goods-governance/category-manage';
UPDATE li_menu SET permission = '/manager/goods/goodsGroup*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/goods-governance/goods-group-manage';
UPDATE li_menu SET permission = '/manager/promotion/pointsGoods*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/goods-governance/points-goods';
UPDATE li_menu SET permission = '/manager/promotion/cardCouponGoods*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/goods-governance/card-coupon-goods';
UPDATE li_menu SET permission = '/manager/order/order*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/order-governance/order-manage';
UPDATE li_menu SET permission = '/manager/order/complain*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/order-governance/order-complaint';
UPDATE li_menu SET permission = '/manager/setting/setting/get/VERIFICATION_RULE_SETTING*,/manager/setting/setting/put/VERIFICATION_RULE_SETTING*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/order-governance/verification-rule';
UPDATE li_menu SET permission = '/manager/order/refundLog*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/order-governance/refund-log';
UPDATE li_menu SET permission = '/manager/order/paymentLog*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/order-governance/payment-log';
UPDATE li_menu SET permission = '/manager/trade/receipt*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/order-governance/receipt-manage';
UPDATE li_menu SET permission = '/manager/order/orderLog*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/order-governance/order-log';
UPDATE li_menu SET permission = '/manager/order/afterSale/page*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/order-governance/after-sale';
UPDATE li_menu SET permission = '/manager/other/verificationRecord*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/order-governance/verification-record';
UPDATE li_menu SET permission = '/manager/promotion/coupon*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/marketing-governance/coupon-manage';
UPDATE li_menu SET permission = '/manager/promotion/couponActivity*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/marketing-governance/coupon-activity';
UPDATE li_menu SET permission = '/manager/promotion/fullDiscount*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/marketing-governance/full-discount';
UPDATE li_menu SET permission = '/manager/promotion/seckill*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/marketing-governance/seckill-manage';
UPDATE li_menu SET permission = '/manager/promotion/kanJiaGoods*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/marketing-governance/kanjia-manage';
UPDATE li_menu SET permission = '/manager/promotion/pintuan*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/marketing-governance/pintuan-rule';
UPDATE li_menu SET permission = '/manager/promotion/pintuan*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/marketing-governance/pintuan-record';
UPDATE li_menu SET permission = '/manager/profitsharing/rule*,/common/common/region/allCity*,/manager/goods/category/allChildren*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/fund-governance/profitsharing-rule';
UPDATE li_menu SET permission = '/manager/order/bill*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/fund-governance/bill-manage';
UPDATE li_menu SET permission = '/manager/profitsharing/record*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/fund-governance/profitsharing-record';
UPDATE li_menu SET permission = '/manager/wallet/log*,/manager/wallet/wallet*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/fund-governance/wallet-log';
UPDATE li_menu SET permission = '/manager/profitsharing/balance*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/fund-governance/profitsharing-balance';
UPDATE li_menu SET permission = '/manager/wallet/wallet/page*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/fund-governance/wallet-account';
UPDATE li_menu SET permission = '/manager/wallet/withdrawApply*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/fund-governance/withdraw-apply';
UPDATE li_menu SET permission = '/manager/reconciliation/purchase*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/fund-governance/procurement-reconciliation';
UPDATE li_menu SET permission = '/manager/reconciliation/fund*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/fund-governance/fund-reconciliation';
UPDATE li_menu SET permission = '/manager/setting/setting/get*,/manager/setting/setting/put*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path IN ('/platform-config/base-setting','/platform-config/payment-setting','/platform-config/withdraw-setting','/platform-config/oss-setting','/platform-config/sms-setting','/platform-config/email-setting','/platform-config/goods-setting','/platform-config/order-setting','/platform-config/logistics-setting','/platform-config/point-setting','/platform-config/seckill-setting','/platform-config/im-setting','/platform-config/connect-setting','/platform-config/distribution-setting','/platform-config/alipay-setting','/platform-config/wechat-payment-setting','/platform-config/unionpay-setting','/platform-config/wx-channels-setting');
UPDATE li_menu SET permission = '/manager/passport/member*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/member-center/member-list';
UPDATE li_menu SET permission = '/manager/passport/member*,/manager/member/memberGroup*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/member-center/member-group';
UPDATE li_menu SET permission = '/manager/member/benefit*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/member-center/member-benefit';
UPDATE li_menu SET permission = '/manager/member/memberPointsHistory/getByPage*,/manager/member/memberPointsHistory/getMemberPointsHistoryVO*,/manager/member/memberPointsHistory/queryMemberPointsStatistics*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/member-center/member-points-history';
UPDATE li_menu SET permission = '/manager/member/evaluation/get*,/manager/member/evaluation/delete*,/manager/member/evaluation/updateTop*,/manager/member/evaluation/updateStatus*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/member-center/member-evaluation';
UPDATE li_menu SET permission = '/manager/member/address*,/manager/passport/member*,/manager/setting/region/item*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/member-center/member-address';
UPDATE li_menu SET permission = '/manager/message/memberNotice*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/message-center/member-notice';
UPDATE li_menu SET permission = '/manager/message/memberNoticeLog/get*,/manager/message/memberNoticeLog/delByIds*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/message-center/member-notice-log';
UPDATE li_menu SET permission = '/manager/sms/sign*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/message-center/sms-sign';
UPDATE li_menu SET permission = '/manager/sms/template*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/message-center/sms-template';
UPDATE li_menu SET permission = '/manager/message/memberNoticeSenter/get*,/manager/message/memberNoticeSenter/delByIds*,/manager/message/memberNoticeSenter/insertOrUpdate*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/message-center/member-notice-sender';
UPDATE li_menu SET permission = '/manager/message/serviceNotice*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/message-center/service-notice';
UPDATE li_menu SET permission = '/manager/other/message*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/message-center/message-channel';
UPDATE li_menu SET permission = '/manager/other/memberMessage*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/message-center/member-message';
UPDATE li_menu SET permission = '/manager/other/storeMessage*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/message-center/store-message';
UPDATE li_menu SET permission = '/manager/other/homeAdvertisement*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/content-center/advertisement';
UPDATE li_menu SET permission = '/manager/other/special*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/content-center/special-manage';
UPDATE li_menu SET permission = '/manager/other/homeRecommendationStrategy*,/manager/goods/category/allChildren*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/content-center/recommendation-strategy';
UPDATE li_menu SET permission = '/manager/other/article*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/content-center/article';
UPDATE li_menu SET permission = '/manager/other/articleCategory*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/content-center/article-category';
UPDATE li_menu SET permission = '/manager/other/sensitiveWords*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/content-center/sensitive-words';
UPDATE li_menu SET permission = '/manager/other/customWords*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/content-center/custom-words';
UPDATE li_menu SET permission = '/manager/other/appVersion*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/content-center/app-version';
UPDATE li_menu SET permission = '/manager/other/verificationSource*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/support-center/verification-source';
UPDATE li_menu SET permission = '/manager/setting/region*,/common/common/region/allCity*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/support-center/region-manage';
UPDATE li_menu SET permission = '/manager/other/logistics*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/support-center/logistics-manage';
UPDATE li_menu SET permission = '/manager/goods/freightTemplate*,/manager/store/store/all*,/common/common/region/allCity*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010113 AND path = '/support-center/freight-template-manage';
UPDATE li_menu SET permission = '/manager/order/afterSaleReason*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/support-center/after-sale-reason';
UPDATE li_menu SET permission = '/manager/other/feedback*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/support-center/feedback-manage';
UPDATE li_menu SET permission = '/manager/setting/log*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path IN ('/support-center/setting-log','/system/maintenance-log','/system/operation-log');
UPDATE li_menu SET permission = '/manager/permission/menu*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path IN ('/merchant-grant/menu-permission','/system/menu');
UPDATE li_menu SET permission = '/manager/permission/role*,/manager/permission/menu/tree*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path IN ('/merchant-grant/role-permission','/system/role');
UPDATE li_menu SET permission = '/manager/permission/department*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path IN ('/merchant-grant/department-manage','/system/dept');
UPDATE li_menu SET permission = '/manager/passport/user*,/manager/permission/role*,/common/common/upload/file*,/manager/permission/department*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path IN ('/merchant-grant/backend-account','/system/user');
UPDATE li_menu SET permission = '/manager/permission/storeMenu*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/merchant-grant/store-menu';
UPDATE li_menu SET permission = '/manager/message/serviceNotice*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/system/warning-setting';

-- BEGIN patch_wholesale_manager_legacy_menu_archive.sql
DROP TEMPORARY TABLE IF EXISTS `tmp_wholesale_legacy_menu_ids`;
CREATE TEMPORARY TABLE `tmp_wholesale_legacy_menu_ids` (
  `id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;

INSERT INTO `tmp_wholesale_legacy_menu_ids` (`id`)
SELECT DISTINCT legacy_seed.id_str
FROM (
  SELECT CAST(root.`id` AS CHAR) AS id_str
  FROM `li_menu` root
  WHERE root.`parent_id` = '0'
    AND root.`id` NOT BETWEEN 3062200000000000001 AND 3062200000000010097
    AND root.`title` IN ('会员', '订单', '商品', '促销', '店铺', '运营', '统计', '设置', '日志')

  UNION ALL

  SELECT CAST(level1.`id` AS CHAR) AS id_str
  FROM `li_menu` level1
  INNER JOIN `li_menu` root
    ON level1.`parent_id` = CAST(root.`id` AS CHAR)
  WHERE root.`parent_id` = '0'
    AND root.`id` NOT BETWEEN 3062200000000000001 AND 3062200000000010097
    AND root.`title` IN ('会员', '订单', '商品', '促销', '店铺', '运营', '统计', '设置', '日志')

  UNION ALL

  SELECT CAST(level2.`id` AS CHAR) AS id_str
  FROM `li_menu` level2
  INNER JOIN `li_menu` level1
    ON level2.`parent_id` = CAST(level1.`id` AS CHAR)
  INNER JOIN `li_menu` root
    ON level1.`parent_id` = CAST(root.`id` AS CHAR)
  WHERE root.`parent_id` = '0'
    AND root.`id` NOT BETWEEN 3062200000000000001 AND 3062200000000010097
    AND root.`title` IN ('会员', '订单', '商品', '促销', '店铺', '运营', '统计', '设置', '日志')

  UNION ALL

  SELECT CAST(level3.`id` AS CHAR) AS id_str
  FROM `li_menu` level3
  INNER JOIN `li_menu` level2
    ON level3.`parent_id` = CAST(level2.`id` AS CHAR)
  INNER JOIN `li_menu` level1
    ON level2.`parent_id` = CAST(level1.`id` AS CHAR)
  INNER JOIN `li_menu` root
    ON level1.`parent_id` = CAST(root.`id` AS CHAR)
  WHERE root.`parent_id` = '0'
    AND root.`id` NOT BETWEEN 3062200000000000001 AND 3062200000000010097
    AND root.`title` IN ('会员', '订单', '商品', '促销', '店铺', '运营', '统计', '设置', '日志')
) legacy_seed;

UPDATE li_menu target
INNER JOIN `tmp_wholesale_legacy_menu_ids` archived
  ON CAST(target.id AS CHAR) = archived.id
SET
  target.delete_flag = b'1',
  target.update_by = 'codex',
  target.update_time = CURRENT_TIMESTAMP(6)
WHERE target.id NOT BETWEEN 3062200000000000001 AND 3062200000000010097;

DROP TEMPORARY TABLE IF EXISTS `tmp_wholesale_legacy_menu_ids`;

-- BEGIN patch_wholesale_manager_archived_menu_purge.sql
DELETE FROM li_role_menu
WHERE menu_id IN (
  SELECT archived_menu.id_str
  FROM (
    SELECT CAST(id AS CHAR) AS id_str
    FROM li_menu
    WHERE delete_flag = b'1'
      AND update_by = 'codex'
  ) AS archived_menu
);

DELETE FROM li_menu
WHERE delete_flag = b'1'
  AND update_by = 'codex';

-- BEGIN patch_wholesale_manager_delivery_cleanup.sql
UPDATE li_menu
SET
  delete_flag = b'1',
  update_by = 'codex',
  update_time = CURRENT_TIMESTAMP(6)
WHERE delete_flag = b'0'
  AND (
    front_route IN (
      '/goods-governance/home-category',
      '/content-center/page-data',
      '/content-center/shortcut-nav',
      '/content-center/special'
    )
    OR path IN (
      '/goods-governance/home-category',
      '/content-center/page-data',
      '/content-center/shortcut-nav',
      '/content-center/special'
    )
    OR permission LIKE '%/manager/other/shortcutNav%'
    OR permission LIKE '%/manager/distribution/%'
    OR permission LIKE '%/manager/wechat/%'
    OR permission LIKE '%/manager/wxchannels/%'
    OR permission LIKE '%/manager/broadcast/%'
    OR permission LIKE '%/manager/other/elasticsearch%'
    OR permission LIKE '%/manager/setting/messageTemplate%'
    OR permission LIKE '%/manager/setting/settingx%'
  );

DELETE FROM li_role_menu
WHERE menu_id IN (
  SELECT id
  FROM li_menu
  WHERE delete_flag = b'1'
    AND (
      front_route IN (
        '/goods-governance/home-category',
        '/content-center/page-data',
        '/content-center/shortcut-nav',
        '/content-center/special'
      )
      OR path IN (
        '/goods-governance/home-category',
        '/content-center/page-data',
        '/content-center/shortcut-nav',
        '/content-center/special'
      )
      OR permission LIKE '%/manager/other/shortcutNav%'
      OR permission LIKE '%/manager/distribution/%'
      OR permission LIKE '%/manager/wechat/%'
      OR permission LIKE '%/manager/wxchannels/%'
      OR permission LIKE '%/manager/broadcast/%'
      OR permission LIKE '%/manager/other/elasticsearch%'
      OR permission LIKE '%/manager/setting/messageTemplate%'
      OR permission LIKE '%/manager/setting/settingx%'
    )
);

-- BEGIN patch_manager_role_baseline.sql
UPDATE li_admin_user
SET is_super = b'1',
    status = b'1',
    role_ids = NULL,
    update_by = 'admin',
    update_time = CURRENT_TIMESTAMP
WHERE username = 'admin';

DELETE FROM li_user_role;
DELETE FROM li_role_menu;
DELETE FROM li_role;

-- BEGIN patch_goods_barcode_columns.sql
SET @goods_barcode_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'li_goods'
    AND COLUMN_NAME = 'barcode'
);

SET @goods_barcode_sql = IF(
  @goods_barcode_exists = 0,
  'ALTER TABLE `li_goods` ADD COLUMN `barcode` varchar(64) DEFAULT NULL COMMENT ''商品主条码'' AFTER `sn`',
  'SELECT ''li_goods.barcode already exists'' AS message'
);
PREPARE goods_barcode_stmt FROM @goods_barcode_sql;
EXECUTE goods_barcode_stmt;
DEALLOCATE PREPARE goods_barcode_stmt;

SET @goods_sku_barcode_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'li_goods_sku'
    AND COLUMN_NAME = 'barcode'
);

SET @goods_sku_barcode_sql = IF(
  @goods_sku_barcode_exists = 0,
  'ALTER TABLE `li_goods_sku` ADD COLUMN `barcode` varchar(64) DEFAULT NULL COMMENT ''商品规格条码'' AFTER `sn`',
  'SELECT ''li_goods_sku.barcode already exists'' AS message'
);
PREPARE goods_sku_barcode_stmt FROM @goods_sku_barcode_sql;
EXECUTE goods_sku_barcode_stmt;
DEALLOCATE PREPARE goods_sku_barcode_stmt;

UPDATE `li_goods_sku` sku
JOIN `li_goods` goods ON goods.`id` = sku.`goods_id`
SET sku.`barcode` = goods.`barcode`
WHERE (sku.`barcode` IS NULL OR sku.`barcode` = '')
  AND goods.`barcode` IS NOT NULL
  AND goods.`barcode` <> '';

-- BEGIN patch_wholesale_manager_menu_finalize.sql
DROP TEMPORARY TABLE IF EXISTS `tmp_wholesale_manager_menu_finalize_seed`;

UPDATE `li_store_settlement_profile` p
INNER JOIN `li_store_detail` d ON d.`store_id` = p.`store_id`
SET
  p.`bank_account_name` = COALESCE(NULLIF(p.`bank_account_name`, ''), d.`settlement_bank_account_name`),
  p.`bank_account_number` = COALESCE(NULLIF(p.`bank_account_number`, ''), d.`settlement_bank_account_num`),
  p.`bank_branch_name` = COALESCE(NULLIF(p.`bank_branch_name`, ''), d.`settlement_bank_branch_name`),
  p.`bank_joint_code` = COALESCE(NULLIF(p.`bank_joint_code`, ''), d.`settlement_bank_joint_name`),
  p.`settlement_cycle` = COALESCE(NULLIF(p.`settlement_cycle`, ''), d.`settlement_cycle`),
  p.`settlement_day` = COALESCE(p.`settlement_day`, d.`settlement_day`);

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store' AND COLUMN_NAME = 'apply_type'
);
SET @sql := IF(@col_exists = 1,
  'ALTER TABLE `li_store` DROP COLUMN `apply_type`',
  'SELECT ''li_store.apply_type missing''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @drop_col := 'apply_type';
SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = @drop_col
);
SET @sql := IF(@col_exists = 1, CONCAT('ALTER TABLE `li_store_detail` DROP COLUMN `', @drop_col, '`'), 'SELECT ''li_store_detail.apply_type missing''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @drop_col := 'company_name';
SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = @drop_col
);
SET @sql := IF(@col_exists = 1, CONCAT('ALTER TABLE `li_store_detail` DROP COLUMN `', @drop_col, '`'), 'SELECT ''li_store_detail.company_name missing''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @drop_col := 'company_address';
SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = @drop_col
);
SET @sql := IF(@col_exists = 1, CONCAT('ALTER TABLE `li_store_detail` DROP COLUMN `', @drop_col, '`'), 'SELECT ''li_store_detail.company_address missing''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @drop_col := 'company_address_id_path';
SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = @drop_col
);
SET @sql := IF(@col_exists = 1, CONCAT('ALTER TABLE `li_store_detail` DROP COLUMN `', @drop_col, '`'), 'SELECT ''li_store_detail.company_address_id_path missing''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @drop_col := 'company_address_path';
SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = @drop_col
);
SET @sql := IF(@col_exists = 1, CONCAT('ALTER TABLE `li_store_detail` DROP COLUMN `', @drop_col, '`'), 'SELECT ''li_store_detail.company_address_path missing''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @drop_col := 'company_phone';
SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = @drop_col
);
SET @sql := IF(@col_exists = 1, CONCAT('ALTER TABLE `li_store_detail` DROP COLUMN `', @drop_col, '`'), 'SELECT ''li_store_detail.company_phone missing''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @drop_col := 'company_email';
SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = @drop_col
);
SET @sql := IF(@col_exists = 1, CONCAT('ALTER TABLE `li_store_detail` DROP COLUMN `', @drop_col, '`'), 'SELECT ''li_store_detail.company_email missing''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @drop_col := 'employee_num';
SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = @drop_col
);
SET @sql := IF(@col_exists = 1, CONCAT('ALTER TABLE `li_store_detail` DROP COLUMN `', @drop_col, '`'), 'SELECT ''li_store_detail.employee_num missing''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @drop_col := 'registered_capital';
SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = @drop_col
);
SET @sql := IF(@col_exists = 1, CONCAT('ALTER TABLE `li_store_detail` DROP COLUMN `', @drop_col, '`'), 'SELECT ''li_store_detail.registered_capital missing''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @drop_col := 'link_name';
SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = @drop_col
);
SET @sql := IF(@col_exists = 1, CONCAT('ALTER TABLE `li_store_detail` DROP COLUMN `', @drop_col, '`'), 'SELECT ''li_store_detail.link_name missing''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @drop_col := 'link_phone';
SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = @drop_col
);
SET @sql := IF(@col_exists = 1, CONCAT('ALTER TABLE `li_store_detail` DROP COLUMN `', @drop_col, '`'), 'SELECT ''li_store_detail.link_phone missing''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @drop_col := 'license_num';
SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = @drop_col
);
SET @sql := IF(@col_exists = 1, CONCAT('ALTER TABLE `li_store_detail` DROP COLUMN `', @drop_col, '`'), 'SELECT ''li_store_detail.license_num missing''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @drop_col := 'scope';
SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = @drop_col
);
SET @sql := IF(@col_exists = 1, CONCAT('ALTER TABLE `li_store_detail` DROP COLUMN `', @drop_col, '`'), 'SELECT ''li_store_detail.scope missing''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @drop_col := 'licence_photo';
SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = @drop_col
);
SET @sql := IF(@col_exists = 1, CONCAT('ALTER TABLE `li_store_detail` DROP COLUMN `', @drop_col, '`'), 'SELECT ''li_store_detail.licence_photo missing''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @drop_col := 'legal_photo';
SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = @drop_col
);
SET @sql := IF(@col_exists = 1, CONCAT('ALTER TABLE `li_store_detail` DROP COLUMN `', @drop_col, '`'), 'SELECT ''li_store_detail.legal_photo missing''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @drop_col := 'id_card_front_url';
SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = @drop_col
);
SET @sql := IF(@col_exists = 1, CONCAT('ALTER TABLE `li_store_detail` DROP COLUMN `', @drop_col, '`'), 'SELECT ''li_store_detail.id_card_front_url missing''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @drop_col := 'id_card_back_url';
SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = @drop_col
);
SET @sql := IF(@col_exists = 1, CONCAT('ALTER TABLE `li_store_detail` DROP COLUMN `', @drop_col, '`'), 'SELECT ''li_store_detail.id_card_back_url missing''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @drop_col := 'settlement_bank_account_name';
SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = @drop_col
);
SET @sql := IF(@col_exists = 1, CONCAT('ALTER TABLE `li_store_detail` DROP COLUMN `', @drop_col, '`'), 'SELECT ''li_store_detail.settlement_bank_account_name missing''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @drop_col := 'settlement_bank_account_num';
SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = @drop_col
);
SET @sql := IF(@col_exists = 1, CONCAT('ALTER TABLE `li_store_detail` DROP COLUMN `', @drop_col, '`'), 'SELECT ''li_store_detail.settlement_bank_account_num missing''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @drop_col := 'settlement_bank_branch_name';
SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = @drop_col
);
SET @sql := IF(@col_exists = 1, CONCAT('ALTER TABLE `li_store_detail` DROP COLUMN `', @drop_col, '`'), 'SELECT ''li_store_detail.settlement_bank_branch_name missing''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @drop_col := 'settlement_bank_joint_name';
SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = @drop_col
);
SET @sql := IF(@col_exists = 1, CONCAT('ALTER TABLE `li_store_detail` DROP COLUMN `', @drop_col, '`'), 'SELECT ''li_store_detail.settlement_bank_joint_name missing''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @drop_col := 'settlement_cycle';
SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = @drop_col
);
SET @sql := IF(@col_exists = 1, CONCAT('ALTER TABLE `li_store_detail` DROP COLUMN `', @drop_col, '`'), 'SELECT ''li_store_detail.settlement_cycle missing''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @drop_col := 'settlement_day';
SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = @drop_col
);
SET @sql := IF(@col_exists = 1, CONCAT('ALTER TABLE `li_store_detail` DROP COLUMN `', @drop_col, '`'), 'SELECT ''li_store_detail.settlement_day missing''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Store apply refactor patch
-- 将批发阶段一老入驻结构收敛到新三步流程结构，并拆分结算资料表。

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store' AND COLUMN_NAME = 'subject_type'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store` ADD COLUMN `subject_type` varchar(32) DEFAULT NULL COMMENT ''入驻主体类型'' AFTER `store_name`',
  'SELECT ''li_store.subject_type exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store' AND COLUMN_NAME = 'company_identity_type'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store` ADD COLUMN `company_identity_type` varchar(32) DEFAULT NULL COMMENT ''企业身份类型'' AFTER `subject_type`',
  'SELECT ''li_store.company_identity_type exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'store_logo'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `store_logo` varchar(255) DEFAULT NULL COMMENT ''店铺logo'' AFTER `store_name`',
  'SELECT ''li_store_detail.store_logo exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'store_desc'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `store_desc` varchar(255) DEFAULT NULL COMMENT ''店铺简介'' AFTER `store_logo`',
  'SELECT ''li_store_detail.store_desc exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'store_center'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `store_center` varchar(255) DEFAULT NULL COMMENT ''经纬度'' AFTER `store_desc`',
  'SELECT ''li_store_detail.store_center exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'store_address_path'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `store_address_path` varchar(255) DEFAULT NULL COMMENT ''店铺地址名称'' AFTER `store_center`',
  'SELECT ''li_store_detail.store_address_path exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'store_address_id_path'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `store_address_id_path` varchar(255) DEFAULT NULL COMMENT ''店铺地址ID路径'' AFTER `store_address_path`',
  'SELECT ''li_store_detail.store_address_id_path exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'store_address_detail'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `store_address_detail` varchar(255) DEFAULT NULL COMMENT ''店铺详细地址'' AFTER `store_address_id_path`',
  'SELECT ''li_store_detail.store_address_detail exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'subject_type'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `subject_type` varchar(32) DEFAULT NULL COMMENT ''主体类型'' AFTER `store_address_detail`',
  'SELECT ''li_store_detail.subject_type exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'company_identity_type'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `company_identity_type` varchar(32) DEFAULT NULL COMMENT ''企业身份类型'' AFTER `subject_type`',
  'SELECT ''li_store_detail.company_identity_type exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'real_name'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `real_name` varchar(64) DEFAULT NULL COMMENT ''个人/经营者/被授权人姓名'' AFTER `indoor_image_urls`',
  'SELECT ''li_store_detail.real_name exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'id_card_no'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `id_card_no` varchar(64) DEFAULT NULL COMMENT ''个人/经营者/被授权人身份证号'' AFTER `real_name`',
  'SELECT ''li_store_detail.id_card_no exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'legal_mobile'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `legal_mobile` varchar(32) DEFAULT NULL COMMENT ''主体手机号'' AFTER `legal_id`',
  'SELECT ''li_store_detail.legal_mobile exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'authorization_url'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `authorization_url` varchar(255) DEFAULT NULL COMMENT ''授权书'' AFTER `legal_mobile`',
  'SELECT ''li_store_detail.authorization_url exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `li_store_settlement_profile` (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'ID',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '创建者',
  `create_time` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '更新者',
  `update_time` datetime(6) DEFAULT NULL COMMENT '更新时间',
  `delete_flag` bit(1) DEFAULT b'0' COMMENT '删除标志 true/false 删除/未删除',
  `store_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '店铺ID',
  `bank_account_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '银行开户名',
  `bank_account_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '银行账号',
  `bank_branch_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '开户支行',
  `bank_joint_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '联行号',
  `settlement_cycle` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '结算周期',
  `settlement_day` datetime(6) DEFAULT NULL COMMENT '结算日',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_li_store_settlement_profile_store_id` (`store_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='店铺结算资料';

UPDATE `li_store`
SET
  `subject_type` = CASE `apply_type`
    WHEN 'PERSONAL' THEN 'PERSONAL'
    WHEN 'INDIVIDUAL' THEN 'INDIVIDUAL'
    WHEN 'COMPANY_LEGAL' THEN 'COMPANY'
    WHEN 'COMPANY_NON_LEGAL' THEN 'COMPANY'
    ELSE `subject_type`
  END,
  `company_identity_type` = CASE `apply_type`
    WHEN 'COMPANY_LEGAL' THEN 'LEGAL'
    WHEN 'COMPANY_NON_LEGAL' THEN 'AUTHORIZED'
    ELSE `company_identity_type`
  END
WHERE `apply_type` IS NOT NULL AND (`subject_type` IS NULL OR `subject_type` = '');

UPDATE `li_store_detail`
SET
  `subject_type` = CASE `apply_type`
    WHEN 'PERSONAL' THEN 'PERSONAL'
    WHEN 'INDIVIDUAL' THEN 'INDIVIDUAL'
    WHEN 'COMPANY_LEGAL' THEN 'COMPANY'
    WHEN 'COMPANY_NON_LEGAL' THEN 'COMPANY'
    ELSE `subject_type`
  END,
  `company_identity_type` = CASE `apply_type`
    WHEN 'COMPANY_LEGAL' THEN 'LEGAL'
    WHEN 'COMPANY_NON_LEGAL' THEN 'AUTHORIZED'
    ELSE `company_identity_type`
  END
WHERE `apply_type` IS NOT NULL AND (`subject_type` IS NULL OR `subject_type` = '');

UPDATE `li_store_detail` d
INNER JOIN `li_store` s ON s.`id` = d.`store_id`
SET
  d.`store_name` = COALESCE(NULLIF(d.`store_name`, ''), s.`store_name`),
  d.`store_logo` = COALESCE(NULLIF(d.`store_logo`, ''), s.`store_logo`),
  d.`store_desc` = COALESCE(NULLIF(d.`store_desc`, ''), s.`store_desc`),
  d.`store_center` = COALESCE(NULLIF(d.`store_center`, ''), s.`store_center`),
  d.`store_address_path` = COALESCE(NULLIF(d.`store_address_path`, ''), s.`store_address_path`),
  d.`store_address_id_path` = COALESCE(NULLIF(d.`store_address_id_path`, ''), s.`store_address_id_path`),
  d.`store_address_detail` = COALESCE(NULLIF(d.`store_address_detail`, ''), s.`store_address_detail`),
  d.`subject_type` = COALESCE(NULLIF(d.`subject_type`, ''), s.`subject_type`),
  d.`company_identity_type` = COALESCE(NULLIF(d.`company_identity_type`, ''), s.`company_identity_type`),
  d.`store_type` = COALESCE(NULLIF(d.`store_type`, ''), s.`store_type`),
  d.`agent_level` = COALESCE(NULLIF(d.`agent_level`, ''), s.`agent_level`),
  d.`agent_region_id` = COALESCE(NULLIF(d.`agent_region_id`, ''), s.`agent_region_id`),
  d.`agent_region_name` = COALESCE(NULLIF(d.`agent_region_name`, ''), s.`agent_region_name`);

UPDATE `li_store_detail`
SET `business_license_url` = `licence_photo`
WHERE (`business_license_url` IS NULL OR `business_license_url` = '')
  AND `licence_photo` IS NOT NULL
  AND `licence_photo` != '';

INSERT INTO `li_store_settlement_profile` (
  `id`,
  `create_by`,
  `create_time`,
  `update_by`,
  `update_time`,
  `delete_flag`,
  `store_id`,
  `bank_account_name`,
  `bank_account_number`,
  `bank_branch_name`,
  `bank_joint_code`,
  `settlement_cycle`,
  `settlement_day`
)
SELECT
  CONCAT('SSP_', d.`store_id`),
  COALESCE(d.`create_by`, 'admin'),
  COALESCE(d.`create_time`, NOW(6)),
  d.`update_by`,
  d.`update_time`,
  COALESCE(d.`delete_flag`, b'0'),
  d.`store_id`,
  d.`settlement_bank_account_name`,
  d.`settlement_bank_account_num`,
  d.`settlement_bank_branch_name`,
  d.`settlement_bank_joint_name`,
  d.`settlement_cycle`,
  d.`settlement_day`
FROM `li_store_detail` d
LEFT JOIN `li_store_settlement_profile` p ON p.`store_id` = d.`store_id`
WHERE p.`store_id` IS NULL
  AND (
    NULLIF(d.`settlement_bank_account_name`, '') IS NOT NULL
    OR NULLIF(d.`settlement_bank_account_num`, '') IS NOT NULL
    OR NULLIF(d.`settlement_bank_branch_name`, '') IS NOT NULL
    OR NULLIF(d.`settlement_bank_joint_name`, '') IS NOT NULL
    OR NULLIF(d.`settlement_cycle`, '') IS NOT NULL
    OR d.`settlement_day` IS NOT NULL
  );

CREATE TEMPORARY TABLE `tmp_wholesale_manager_menu_finalize_seed` (
  `id` bigint NOT NULL,
  `create_by` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `delete_flag` bit(1) DEFAULT NULL,
  `update_by` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `description` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `front_route` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `icon` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `level` int DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `parent_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `path` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `sort_order` decimal(10,2) DEFAULT NULL,
  `title` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `front_component` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `permission` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;

INSERT IGNORE INTO `tmp_wholesale_manager_menu_finalize_seed` (
  `id`,`create_by`,`create_time`,`delete_flag`,`update_by`,`update_time`,
  `description`,`front_route`,`icon`,`level`,`name`,`parent_id`,`path`,
  `sort_order`,`title`,`front_component`,`permission`
) VALUES
  ('3062200000000000015', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-menu-finalize', 'procurement-governance', 'ep:box', '0', 'ProcurementGovernance', '0', 'procurement-governance', 75.00, '采购管理', NULL, NULL),
  ('3062200000000010098', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-menu-finalize', 'procurement/order/index', '', '1', 'procurement-order', '3062200000000000015', 'procurement-order', 1.00, '采购订单管理', NULL, NULL),
  ('3062200000000010099', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-menu-finalize', 'procurement/inbound/index', '', '1', 'procurement-inbound', '3062200000000000015', 'procurement-inbound', 2.00, '采购入库管理', NULL, NULL),
  ('3062200000000010100', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-menu-finalize', 'procurement/inventory-count/index', '', '1', 'procurement-inventory-count', '3062200000000000015', 'procurement-inventory-count', 3.00, '盘点管理', NULL, NULL),
  ('3062200000000010111', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-menu-finalize', 'procurement/damage-report/index', '', '1', 'procurement-damage-report', '3062200000000000015', 'procurement-damage-report', 4.00, '报损管理', NULL, NULL),
  ('3062200000000010112', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-menu-finalize', 'procurement/reason/index', '', '1', 'procurement-reason', '3062200000000000015', 'procurement-reason', 5.00, '库存原因管理', NULL, NULL);

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

DROP TEMPORARY TABLE IF EXISTS `tmp_wholesale_manager_menu_finalize_seed`;
