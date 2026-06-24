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
