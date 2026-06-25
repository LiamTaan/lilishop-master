SET NAMES utf8mb4;

ALTER TABLE `li_operation_shortcut_nav`
  COMMENT='首页分类宫格配置',
  MODIFY COLUMN `id` varchar(64) NOT NULL COMMENT '主键ID',
  MODIFY COLUMN `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  MODIFY COLUMN `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  MODIFY COLUMN `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  MODIFY COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  MODIFY COLUMN `delete_flag` tinyint(1) DEFAULT 0 COMMENT '删除标记',
  MODIFY COLUMN `title` varchar(64) NOT NULL COMMENT '入口标题',
  MODIFY COLUMN `subtitle` varchar(128) DEFAULT NULL COMMENT '入口副标题',
  MODIFY COLUMN `image` varchar(255) DEFAULT NULL COMMENT '图标地址',
  MODIFY COLUMN `link_type` varchar(32) NOT NULL COMMENT '跳转类型',
  MODIFY COLUMN `link_value` varchar(255) NOT NULL COMMENT '跳转值',
  MODIFY COLUMN `client_type` varchar(32) NOT NULL COMMENT '客户端类型',
  MODIFY COLUMN `sort_order` int DEFAULT 0 COMMENT '排序值',
  MODIFY COLUMN `display_status` varchar(16) NOT NULL DEFAULT 'OPEN' COMMENT '显示状态',
  MODIFY COLUMN `remark` varchar(255) DEFAULT NULL COMMENT '备注';

