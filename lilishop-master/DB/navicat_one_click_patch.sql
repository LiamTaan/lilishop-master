-- Lilishop one-click patch for Navicat
-- Includes:
-- 1) goods group tables
-- 2) goods schedule columns
-- 3) 设置-系统设置下「消息模版」菜单（li_menu，可重复执行）

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------
-- 1) 商品分组主表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `li_goods_group` (
  `id` bigint NOT NULL COMMENT 'ID',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '创建者',
  `create_time` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `delete_flag` bit(1) DEFAULT b'0' COMMENT '删除标志 true/false 删除/未删除',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '更新者',
  `update_time` datetime(6) DEFAULT NULL COMMENT '更新时间',
  `group_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '分组名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '分组描述',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_li_goods_group_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='商品分组';

-- ------------------------------------------------------------
-- 2) 商品分组-商品关联表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `li_goods_group_goods` (
  `id` bigint NOT NULL COMMENT 'ID',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '创建者',
  `create_time` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `delete_flag` bit(1) DEFAULT b'0' COMMENT '删除标志 true/false 删除/未删除',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '更新者',
  `update_time` datetime(6) DEFAULT NULL COMMENT '更新时间',
  `group_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '分组ID',
  `goods_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '商品ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_li_goods_group_goods` (`group_id`,`goods_id`) USING BTREE,
  KEY `idx_li_goods_group_goods_group_id` (`group_id`) USING BTREE,
  KEY `idx_li_goods_group_goods_goods_id` (`goods_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='商品分组商品关联';

-- ------------------------------------------------------------
-- 3) 商品定时上下架字段补齐（兼容重复执行）
-- ------------------------------------------------------------
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

SET FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------
-- 4) 可选验证（执行后查看结果）
-- ------------------------------------------------------------
SELECT COLUMN_NAME
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'li_goods'
  AND COLUMN_NAME IN (
    'scheduled_upper_time',
    'scheduled_upper_reason',
    'scheduled_down_time',
    'scheduled_down_reason'
  );

SHOW TABLES LIKE 'li_goods_group';
SHOW TABLES LIKE 'li_goods_group_goods';

-- ------------------------------------------------------------
-- 5) 消息模版菜单：挂在「系统设置」节点下（parent_id 与标准库一致）
-- 执行后请在管理端「角色权限」为角色勾选「消息模版」并重新登录
-- ------------------------------------------------------------
SET FOREIGN_KEY_CHECKS = 0;

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

-- 校验：标准库「系统设置」(path=setting) 的 id 应为 1349246048900243456；若非此值请先改补丁中的 parent_id 再执行
SELECT 'expect_system_setting_id_1349246048900243456' AS check_step, id, title, path, parent_id
FROM `li_menu`
WHERE `path` = 'setting' AND `title` = '系统设置'
LIMIT 5;

SELECT 'message_template_menu' AS check_step, id, title, path, parent_id, front_route
FROM `li_menu`
WHERE `path` = 'message-template' AND `title` = '消息模版'
LIMIT 5;

