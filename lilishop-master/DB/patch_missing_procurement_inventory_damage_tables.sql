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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='出入库原因';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='盘点单';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='盘点单明细';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='报损单';

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
