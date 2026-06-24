SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for li_wx_channel_category
-- ----------------------------
DROP TABLE IF EXISTS `li_wx_channel_category`;
CREATE TABLE `li_wx_channel_category`  (
  `id` bigint NOT NULL COMMENT 'ID',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '创建者',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `delete_flag` bit(1) NULL DEFAULT b'0' COMMENT '删除标志 true/false 删除/未删除',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '更新者',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `wx_category_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '微信类目ID',
  `wx_category_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '微信类目名称',
  `platform_category_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '平台类目ID',
  `platform_category_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '平台类目名称',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '状态 APPROVED,PENDING,REJECTED',
  `materials` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '资质材料JSON',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_li_wx_channel_category_status` (`status`) USING BTREE,
  KEY `idx_li_wx_channel_category_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='微信视频号类目申请';

-- ----------------------------
-- Table structure for li_wx_channel_goods
-- ----------------------------
DROP TABLE IF EXISTS `li_wx_channel_goods`;
CREATE TABLE `li_wx_channel_goods`  (
  `id` bigint NOT NULL COMMENT 'ID',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '创建者',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `delete_flag` bit(1) NULL DEFAULT b'0' COMMENT '删除标志 true/false 删除/未删除',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '更新者',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `goods_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '平台商品ID',
  `sku_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '平台SKU ID',
  `goods_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '商品名称',
  `goods_image` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '商品图片',
  `category_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '分类ID',
  `category_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '分类名称',
  `store_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '店铺ID',
  `store_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '店铺名称',
  `cost_price` decimal(10,2) NULL DEFAULT NULL COMMENT '销售价/成本价',
  `channel_price` decimal(10,2) NULL DEFAULT NULL COMMENT '视频号销售价',
  `stock` int NULL DEFAULT NULL COMMENT '库存',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '状态 APPROVED,PENDING,REJECTED',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_li_wx_channel_goods_goods_id` (`goods_id`) USING BTREE,
  KEY `idx_li_wx_channel_goods_sku_id` (`sku_id`) USING BTREE,
  KEY `idx_li_wx_channel_goods_status` (`status`) USING BTREE,
  KEY `idx_li_wx_channel_goods_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='微信视频号商品镜像';

-- ----------------------------
-- Table structure for li_wx_channel_order
-- ----------------------------
DROP TABLE IF EXISTS `li_wx_channel_order`;
CREATE TABLE `li_wx_channel_order`  (
  `id` bigint NOT NULL COMMENT 'ID',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '创建者',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `delete_flag` bit(1) NULL DEFAULT b'0' COMMENT '删除标志 true/false 删除/未删除',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '更新者',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `channel_order_sn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '视频号订单编号',
  `order_sn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '平台订单编号',
  `member_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '客户ID',
  `member_nick_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '客户昵称',
  `goods_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '商品名称',
  `amount` decimal(10,2) NULL DEFAULT NULL COMMENT '订单金额',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '订单状态',
  `channel_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '带货视频号名称',
  `scene` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '下单场景 LIVE,WINDOW',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_li_wx_channel_order_sn` (`channel_order_sn`) USING BTREE,
  KEY `idx_li_wx_channel_order_member` (`member_nick_name`) USING BTREE,
  KEY `idx_li_wx_channel_order_status` (`status`) USING BTREE,
  KEY `idx_li_wx_channel_order_scene` (`scene`) USING BTREE,
  KEY `idx_li_wx_channel_order_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='微信视频号订单';

-- ----------------------------
-- Table structure for li_wx_channel_refund
-- ----------------------------
DROP TABLE IF EXISTS `li_wx_channel_refund`;
CREATE TABLE `li_wx_channel_refund`  (
  `id` bigint NOT NULL COMMENT 'ID',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '创建者',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `delete_flag` bit(1) NULL DEFAULT b'0' COMMENT '删除标志 true/false 删除/未删除',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '更新者',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `channel_refund_sn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '视频号退单编号',
  `channel_order_sn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '视频号订单编号',
  `member_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '客户ID',
  `member_nick_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '客户昵称',
  `goods_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '商品名称',
  `amount` decimal(10,2) NULL DEFAULT NULL COMMENT '退款金额',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '退单状态',
  `scene` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '下单场景 LIVE,WINDOW',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_li_wx_channel_refund_sn` (`channel_refund_sn`) USING BTREE,
  KEY `idx_li_wx_channel_refund_order_sn` (`channel_order_sn`) USING BTREE,
  KEY `idx_li_wx_channel_refund_status` (`status`) USING BTREE,
  KEY `idx_li_wx_channel_refund_scene` (`scene`) USING BTREE,
  KEY `idx_li_wx_channel_refund_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='微信视频号退单';

-- ----------------------------
-- Optional: initialize WX_CHANNELS setting (empty JSON as placeholder)
-- ----------------------------
-- INSERT INTO `li_setting`(`id`, `create_by`, `create_time`, `delete_flag`, `update_by`, `update_time`, `setting_value`)
-- VALUES ('WX_CHANNELS', 'admin', NOW(6), b'0', 'admin', NOW(6), '{}')
-- ON DUPLICATE KEY UPDATE `update_time` = VALUES(`update_time`);

SET FOREIGN_KEY_CHECKS = 1;
