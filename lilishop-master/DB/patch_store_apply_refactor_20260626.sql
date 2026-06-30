USE `lilishop`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 店铺入驻流程重构：主体类型字段、结算资料拆表、旧字段清理

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

SET @index_exists := (
  SELECT COUNT(1) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_settlement_profile' AND INDEX_NAME = 'uk_li_store_settlement_profile_store_id'
);
SET @sql := IF(@index_exists = 0,
  'ALTER TABLE `li_store_settlement_profile` ADD UNIQUE KEY `uk_li_store_settlement_profile_store_id` (`store_id`)',
  'SELECT ''li_store_settlement_profile.uk_li_store_settlement_profile_store_id exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

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

SET FOREIGN_KEY_CHECKS = 1;
