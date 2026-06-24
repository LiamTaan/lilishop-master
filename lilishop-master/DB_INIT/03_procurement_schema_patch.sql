DROP TABLE IF EXISTS `li_procurement_order`;
CREATE TABLE `li_procurement_order` (
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

DROP TABLE IF EXISTS `li_procurement_order_item`;
CREATE TABLE `li_procurement_order_item` (
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

DROP TABLE IF EXISTS `li_procurement_inbound`;
CREATE TABLE `li_procurement_inbound` (
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

DROP TABLE IF EXISTS `li_procurement_inbound_item`;
CREATE TABLE `li_procurement_inbound_item` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='采购入库明细';
