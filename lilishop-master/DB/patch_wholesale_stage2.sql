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
