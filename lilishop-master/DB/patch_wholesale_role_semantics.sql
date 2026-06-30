SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store' AND COLUMN_NAME = 'agent_level'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store` ADD COLUMN `agent_level` VARCHAR(32) NULL COMMENT ''代理等级'' AFTER `store_type`',
  'SELECT ''li_store.agent_level exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store' AND COLUMN_NAME = 'agent_region_id'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store` ADD COLUMN `agent_region_id` VARCHAR(64) NULL COMMENT ''代理区域ID'' AFTER `agent_level`',
  'SELECT ''li_store.agent_region_id exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store' AND COLUMN_NAME = 'agent_region_name'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store` ADD COLUMN `agent_region_name` VARCHAR(255) NULL COMMENT ''代理区域名称'' AFTER `agent_region_id`',
  'SELECT ''li_store.agent_region_name exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'agent_level'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `agent_level` VARCHAR(32) NULL COMMENT ''代理等级'' AFTER `store_type`',
  'SELECT ''li_store_detail.agent_level exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'agent_region_id'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `agent_region_id` VARCHAR(64) NULL COMMENT ''代理区域ID'' AFTER `agent_level`',
  'SELECT ''li_store_detail.agent_region_id exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_store_detail' AND COLUMN_NAME = 'agent_region_name'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_store_detail` ADD COLUMN `agent_region_name` VARCHAR(255) NULL COMMENT ''代理区域名称'' AFTER `agent_region_id`',
  'SELECT ''li_store_detail.agent_region_name exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'agent_role_relation' AND COLUMN_NAME = 'agent_level'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `agent_role_relation` ADD COLUMN `agent_level` VARCHAR(32) NULL COMMENT ''代理等级'' AFTER `region_name`',
  'SELECT ''agent_role_relation.agent_level exists''');
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

UPDATE agent_role_relation
SET agent_level = 'WHOLESALER'
WHERE agent_level IS NULL OR agent_level = '';
