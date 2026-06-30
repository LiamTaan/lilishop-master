USE `lilishop`;
SET NAMES utf8mb4;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'store_logo'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `store_logo` varchar(255) DEFAULT NULL COMMENT ''store logo'' AFTER `store_name`',
  'SELECT ''li_store_detail.store_logo exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'store_desc'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `store_desc` varchar(255) DEFAULT NULL COMMENT ''store description'' AFTER `store_logo`',
  'SELECT ''li_store_detail.store_desc exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'store_center'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `store_center` varchar(255) DEFAULT NULL COMMENT ''store geo center'' AFTER `store_desc`',
  'SELECT ''li_store_detail.store_center exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'store_address_path'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `store_address_path` varchar(255) DEFAULT NULL COMMENT ''store address path'' AFTER `store_center`',
  'SELECT ''li_store_detail.store_address_path exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'store_address_id_path'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `store_address_id_path` varchar(255) DEFAULT NULL COMMENT ''store address id path'' AFTER `store_address_path`',
  'SELECT ''li_store_detail.store_address_id_path exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'store_address_detail'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `store_address_detail` varchar(255) DEFAULT NULL COMMENT ''store address detail'' AFTER `store_address_id_path`',
  'SELECT ''li_store_detail.store_address_detail exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'subject_type'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `subject_type` varchar(32) DEFAULT NULL COMMENT ''subject type'' AFTER `store_address_detail`',
  'SELECT ''li_store_detail.subject_type exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'company_identity_type'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `company_identity_type` varchar(32) DEFAULT NULL COMMENT ''company identity type'' AFTER `subject_type`',
  'SELECT ''li_store_detail.company_identity_type exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `li_store_settlement_profile` (
  `id` varchar(64) NOT NULL COMMENT 'id',
  `create_by` varchar(64) DEFAULT NULL COMMENT 'create by',
  `create_time` datetime(6) DEFAULT NULL COMMENT 'create time',
  `update_by` varchar(64) DEFAULT NULL COMMENT 'update by',
  `update_time` datetime(6) DEFAULT NULL COMMENT 'update time',
  `delete_flag` bit(1) DEFAULT b'0' COMMENT 'delete flag',
  `store_id` varchar(64) NOT NULL COMMENT 'store id',
  `bank_account_name` varchar(255) DEFAULT NULL COMMENT 'bank account name',
  `bank_account_number` varchar(255) DEFAULT NULL COMMENT 'bank account number',
  `bank_branch_name` varchar(255) DEFAULT NULL COMMENT 'bank branch name',
  `bank_joint_code` varchar(255) DEFAULT NULL COMMENT 'bank joint code',
  `settlement_cycle` varchar(255) DEFAULT NULL COMMENT 'settlement cycle',
  `settlement_day` datetime(6) DEFAULT NULL COMMENT 'settlement day',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_li_store_settlement_profile_store_id` (`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='store settlement profile';

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
  REPLACE(UUID(), '-', ''),
  NULL,
  NOW(6),
  NULL,
  NOW(6),
  b'0',
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
    COALESCE(d.`settlement_bank_account_name`, '') <> ''
    OR COALESCE(d.`settlement_bank_account_num`, '') <> ''
    OR COALESCE(d.`settlement_bank_branch_name`, '') <> ''
    OR COALESCE(d.`settlement_bank_joint_name`, '') <> ''
    OR COALESCE(d.`settlement_cycle`, '') <> ''
    OR d.`settlement_day` IS NOT NULL
  );
