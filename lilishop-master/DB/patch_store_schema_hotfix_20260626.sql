-- Hotfix for store schema columns required by the 2026 store apply refactor.
-- Safe to run multiple times on MySQL 8.x.

SET @ddl = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store' AND COLUMN_NAME = 'subject_type'
  ),
  'SELECT ''li_store.subject_type exists''',
  'ALTER TABLE `li_store` ADD COLUMN `subject_type` varchar(32) DEFAULT NULL COMMENT ''store subject type'' AFTER `store_name`'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store' AND COLUMN_NAME = 'company_identity_type'
  ),
  'SELECT ''li_store.company_identity_type exists''',
  'ALTER TABLE `li_store` ADD COLUMN `company_identity_type` varchar(32) DEFAULT NULL COMMENT ''company identity type'' AFTER `subject_type`'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store' AND COLUMN_NAME = 'store_type'
  ),
  'SELECT ''li_store.store_type exists''',
  'ALTER TABLE `li_store` ADD COLUMN `store_type` varchar(32) DEFAULT NULL COMMENT ''store business type'' AFTER `apply_type`'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store' AND COLUMN_NAME = 'agent_level'
  ),
  'SELECT ''li_store.agent_level exists''',
  'ALTER TABLE `li_store` ADD COLUMN `agent_level` varchar(32) DEFAULT NULL COMMENT ''agent level'' AFTER `store_type`'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store' AND COLUMN_NAME = 'agent_region_id'
  ),
  'SELECT ''li_store.agent_region_id exists''',
  'ALTER TABLE `li_store` ADD COLUMN `agent_region_id` varchar(64) DEFAULT NULL COMMENT ''agent region id'' AFTER `agent_level`'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store' AND COLUMN_NAME = 'agent_region_name'
  ),
  'SELECT ''li_store.agent_region_name exists''',
  'ALTER TABLE `li_store` ADD COLUMN `agent_region_name` varchar(255) DEFAULT NULL COMMENT ''agent region name'' AFTER `agent_region_id`'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'subject_type'
  ),
  'SELECT ''li_store_detail.subject_type exists''',
  'ALTER TABLE `li_store_detail` ADD COLUMN `subject_type` varchar(32) DEFAULT NULL COMMENT ''store subject type'' AFTER `store_address_detail`'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'company_identity_type'
  ),
  'SELECT ''li_store_detail.company_identity_type exists''',
  'ALTER TABLE `li_store_detail` ADD COLUMN `company_identity_type` varchar(32) DEFAULT NULL COMMENT ''company identity type'' AFTER `subject_type`'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'store_type'
  ),
  'SELECT ''li_store_detail.store_type exists''',
  'ALTER TABLE `li_store_detail` ADD COLUMN `store_type` varchar(32) DEFAULT NULL COMMENT ''store business type'' AFTER `apply_type`'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'agent_level'
  ),
  'SELECT ''li_store_detail.agent_level exists''',
  'ALTER TABLE `li_store_detail` ADD COLUMN `agent_level` varchar(32) DEFAULT NULL COMMENT ''agent level'' AFTER `store_type`'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'agent_region_id'
  ),
  'SELECT ''li_store_detail.agent_region_id exists''',
  'ALTER TABLE `li_store_detail` ADD COLUMN `agent_region_id` varchar(64) DEFAULT NULL COMMENT ''agent region id'' AFTER `agent_level`'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'agent_region_name'
  ),
  'SELECT ''li_store_detail.agent_region_name exists''',
  'ALTER TABLE `li_store_detail` ADD COLUMN `agent_region_name` varchar(255) DEFAULT NULL COMMENT ''agent region name'' AFTER `agent_region_id`'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

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
WHERE `apply_type` IS NOT NULL
  AND (`subject_type` IS NULL OR `subject_type` = '');

UPDATE `li_store`
SET `store_type` = 'SUPPLIER'
WHERE `store_type` IS NULL OR `store_type` = '';

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
WHERE `apply_type` IS NOT NULL
  AND (`subject_type` IS NULL OR `subject_type` = '');

UPDATE `li_store_detail` d
INNER JOIN `li_store` s ON s.id = d.store_id
SET
  d.`subject_type` = COALESCE(NULLIF(d.`subject_type`, ''), s.`subject_type`),
  d.`company_identity_type` = COALESCE(NULLIF(d.`company_identity_type`, ''), s.`company_identity_type`),
  d.`store_type` = COALESCE(NULLIF(d.`store_type`, ''), s.`store_type`),
  d.`agent_level` = COALESCE(NULLIF(d.`agent_level`, ''), s.`agent_level`),
  d.`agent_region_id` = COALESCE(NULLIF(d.`agent_region_id`, ''), s.`agent_region_id`),
  d.`agent_region_name` = COALESCE(NULLIF(d.`agent_region_name`, ''), s.`agent_region_name`);
