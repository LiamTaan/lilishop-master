-- 批发商城阶段一：代理商、店铺申请与审核扩展

ALTER TABLE `li_store`
  ADD COLUMN `apply_type` varchar(32) DEFAULT NULL COMMENT '申请主体类型：PERSONAL个人，INDIVIDUAL个体户，COMPANY_LEGAL企业法人，COMPANY_NON_LEGAL企业非法人' AFTER `store_name`,
  ADD COLUMN `store_type` varchar(32) DEFAULT NULL COMMENT '店铺类型' AFTER `apply_type`,
  ADD COLUMN `audit_status` varchar(32) NOT NULL DEFAULT 'DRAFT' COMMENT '审核状态：DRAFT草稿，SUBMITTED待审核，APPROVED通过，REJECTED驳回，FROZEN冻结' AFTER `store_disable`,
  ADD COLUMN `audit_remark` varchar(255) DEFAULT NULL COMMENT '审核备注' AFTER `audit_status`;

ALTER TABLE `li_store_detail`
  ADD COLUMN `apply_type` varchar(32) DEFAULT NULL COMMENT '申请主体类型' AFTER `store_name`,
  ADD COLUMN `store_type` varchar(32) DEFAULT NULL COMMENT '店铺类型' AFTER `apply_type`,
  ADD COLUMN `business_license_url` varchar(255) DEFAULT NULL COMMENT '营业执照图片' AFTER `scope`,
  ADD COLUMN `credit_code` varchar(64) DEFAULT NULL COMMENT '统一社会信用代码' AFTER `business_license_url`,
  ADD COLUMN `business_license_region_id` varchar(64) DEFAULT NULL COMMENT '营业执照所在地区ID' AFTER `credit_code`,
  ADD COLUMN `business_license_expire_type` varchar(32) DEFAULT NULL COMMENT '营业执照有效期类型' AFTER `business_license_region_id`,
  ADD COLUMN `business_license_expire_date` varchar(32) DEFAULT NULL COMMENT '营业执照有效期截止时间' AFTER `business_license_expire_type`,
  ADD COLUMN `facade_image_url` varchar(255) DEFAULT NULL COMMENT '门头照' AFTER `business_license_expire_date`,
  ADD COLUMN `indoor_image_urls` text DEFAULT NULL COMMENT '店内照，英文逗号分隔' AFTER `facade_image_url`,
  ADD COLUMN `store_category_id` varchar(64) DEFAULT NULL COMMENT '店铺分类ID' AFTER `indoor_image_urls`,
  ADD COLUMN `business_hours_start` varchar(16) DEFAULT NULL COMMENT '营业开始时间' AFTER `store_category_id`,
  ADD COLUMN `business_hours_end` varchar(16) DEFAULT NULL COMMENT '营业结束时间' AFTER `business_hours_start`,
  ADD COLUMN `real_name` varchar(64) DEFAULT NULL COMMENT '个人主体姓名' AFTER `legal_photo`,
  ADD COLUMN `id_card_no` varchar(64) DEFAULT NULL COMMENT '个人身份证号' AFTER `real_name`,
  ADD COLUMN `id_card_front_url` varchar(255) DEFAULT NULL COMMENT '身份证正面' AFTER `id_card_no`,
  ADD COLUMN `id_card_back_url` varchar(255) DEFAULT NULL COMMENT '身份证反面' AFTER `id_card_front_url`,
  ADD COLUMN `legal_mobile` varchar(32) DEFAULT NULL COMMENT '法人手机号' AFTER `id_card_back_url`,
  ADD COLUMN `authorization_url` varchar(255) DEFAULT NULL COMMENT '企业非法人授权书' AFTER `legal_mobile`;

CREATE TABLE `agent_role_relation` (
  `id` varchar(64) NOT NULL COMMENT '主键ID',
  `member_id` varchar(64) NOT NULL COMMENT '会员ID',
  `role_code` varchar(32) NOT NULL COMMENT '角色编码，固定为ROLE_AGENT',
  `region_id` varchar(64) NOT NULL COMMENT '所属区域ID',
  `region_name` varchar(128) NOT NULL COMMENT '所属区域名称',
  `status` varchar(32) NOT NULL DEFAULT 'ENABLE' COMMENT '状态：ENABLE启用，DISABLE停用',
  `effective_time` datetime DEFAULT NULL COMMENT '生效时间',
  `expire_time` datetime DEFAULT NULL COMMENT '失效时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0正常，1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_member_region` (`member_id`, `region_id`) COMMENT '同一代理商在同一区域唯一',
  KEY `idx_region_status` (`region_id`, `status`) COMMENT '按区域和状态查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代理商角色关系表';

CREATE TABLE `agent_store_bind` (
  `id` varchar(64) NOT NULL COMMENT '主键ID',
  `agent_member_id` varchar(64) NOT NULL COMMENT '代理商会员ID',
  `store_id` varchar(64) NOT NULL COMMENT '店铺ID',
  `store_name` varchar(128) NOT NULL COMMENT '店铺名称',
  `region_id` varchar(64) NOT NULL COMMENT '所属区域ID',
  `bind_status` varchar(32) NOT NULL DEFAULT 'BOUND' COMMENT '绑定状态：UNBOUND未绑定，BOUND已绑定，DISABLED已停用',
  `bind_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  `unbind_time` datetime DEFAULT NULL COMMENT '解绑时间',
  `audit_status` varchar(32) NOT NULL DEFAULT 'SUBMITTED' COMMENT '审核状态：SUBMITTED待审核，APPROVED通过，REJECTED驳回',
  `audit_remark` varchar(255) DEFAULT NULL COMMENT '审核备注',
  `remark` varchar(255) DEFAULT NULL COMMENT '业务备注',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0正常，1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_agent_store` (`agent_member_id`, `store_id`) COMMENT '同一代理商和店铺唯一绑定',
  KEY `idx_store_id` (`store_id`) COMMENT '按店铺查询绑定关系'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代理商店铺绑定表';

CREATE TABLE `li_store_audit_log` (
  `id` varchar(64) NOT NULL COMMENT '主键ID',
  `store_id` varchar(64) NOT NULL COMMENT '店铺ID',
  `from_audit_status` varchar(32) DEFAULT NULL COMMENT '审核前状态',
  `to_audit_status` varchar(32) NOT NULL COMMENT '审核后状态',
  `audit_remark` varchar(255) DEFAULT NULL COMMENT '审核备注',
  `operator_id` varchar(64) DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(128) DEFAULT NULL COMMENT '操作人名称',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0正常，1删除',
  PRIMARY KEY (`id`),
  KEY `idx_store_create_time` (`store_id`, `create_time`) COMMENT '按店铺和时间查询审核记录'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺审核历史表';
