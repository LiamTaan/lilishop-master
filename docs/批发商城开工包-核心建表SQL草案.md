# 批发商城开工包-核心建表SQL草案

## 1. 说明

本文档用于补齐批发商城新增业务域的核心建表草案。

- 目标：提供可直接进入数据库设计评审的 SQL 草案
- 范围：仅覆盖现有商城骨架不能直接表达的新增业务
- 要求：
  - 表注释、字段注释、索引含义全部使用中文
  - 金额统一使用 `decimal(18,2)`
  - 比例统一使用 `decimal(10,4)`
  - 时间统一使用 `datetime`
  - 默认字符集使用 `utf8mb4`

## 2. 代理商角色关系表

```sql
CREATE TABLE `agent_role_relation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
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
```

## 3. 代理商店铺绑定表

```sql
CREATE TABLE `agent_store_bind` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `agent_member_id` varchar(64) NOT NULL COMMENT '代理商会员ID',
  `store_id` varchar(64) NOT NULL COMMENT '店铺ID',
  `store_name` varchar(128) NOT NULL COMMENT '店铺名称',
  `region_id` varchar(64) NOT NULL COMMENT '所属区域ID',
  `bind_status` varchar(32) NOT NULL DEFAULT 'BOUND' COMMENT '绑定状态：UNBOUND未绑定，BOUND已绑定，DISABLED已停用',
  `bind_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  `unbind_time` datetime DEFAULT NULL COMMENT '解绑时间',
  `audit_status` varchar(32) NOT NULL DEFAULT 'APPROVED' COMMENT '审核状态：SUBMITTED待审核，APPROVED通过，REJECTED驳回',
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
```

## 4. 代理商采购对账单表

```sql
CREATE TABLE `agent_procurement_reconciliation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `reconciliation_no` varchar(64) NOT NULL COMMENT '采购对账单号',
  `agent_member_id` varchar(64) NOT NULL COMMENT '代理商会员ID',
  `region_id` varchar(64) NOT NULL COMMENT '区域ID',
  `start_time` datetime NOT NULL COMMENT '对账开始时间',
  `end_time` datetime NOT NULL COMMENT '对账结束时间',
  `order_count` int NOT NULL DEFAULT 0 COMMENT '订单数量',
  `goods_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '货款金额',
  `freight_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '运费金额',
  `discount_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
  `payable_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '应付金额',
  `reconciliation_status` varchar(32) NOT NULL DEFAULT 'INIT' COMMENT '对账状态：INIT待生成，PENDING_CONFIRM待确认，CONFIRMED已确认，DISPUTED有异议，FINISHED已完成',
  `confirm_time` datetime DEFAULT NULL COMMENT '确认时间',
  `confirm_by` varchar(64) DEFAULT NULL COMMENT '确认人',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_reconciliation_no` (`reconciliation_no`) COMMENT '采购对账单号唯一',
  KEY `idx_agent_time` (`agent_member_id`, `start_time`, `end_time`) COMMENT '按代理商和周期查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代理商采购对账单表';
```

## 5. 代理商采购对账明细表

```sql
CREATE TABLE `agent_procurement_reconciliation_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `reconciliation_id` bigint NOT NULL COMMENT '采购对账单ID',
  `order_sn` varchar(64) NOT NULL COMMENT '订单编号',
  `order_item_id` varchar(64) NOT NULL COMMENT '订单项ID',
  `store_id` varchar(64) NOT NULL COMMENT '供货店铺ID',
  `goods_id` varchar(64) NOT NULL COMMENT '商品ID',
  `sku_id` varchar(64) NOT NULL COMMENT 'SKU ID',
  `goods_name` varchar(255) NOT NULL COMMENT '商品名称',
  `num` int NOT NULL DEFAULT 0 COMMENT '购买数量',
  `price` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '成交单价',
  `subtotal_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '小计金额',
  `order_type` varchar(32) NOT NULL COMMENT '订单类型：NORMAL普通，PINTUAN拼团，WHOLESALE批发，POINTS积分',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_reconciliation_id` (`reconciliation_id`) COMMENT '按对账单查询明细',
  KEY `idx_order_sn` (`order_sn`) COMMENT '按订单号查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代理商采购对账明细表';
```

## 6. 代理商资金对账单表

```sql
CREATE TABLE `agent_fund_reconciliation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `reconciliation_no` varchar(64) NOT NULL COMMENT '资金对账单号',
  `agent_member_id` varchar(64) NOT NULL COMMENT '代理商会员ID',
  `region_id` varchar(64) NOT NULL COMMENT '区域ID',
  `start_time` datetime NOT NULL COMMENT '对账开始时间',
  `end_time` datetime NOT NULL COMMENT '对账结束时间',
  `income_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '入账金额',
  `expense_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '出账金额',
  `commission_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '佣金金额',
  `withdraw_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '提现金额',
  `balance_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '期末余额',
  `reconciliation_status` varchar(32) NOT NULL DEFAULT 'INIT' COMMENT '对账状态：INIT待生成，PENDING_CONFIRM待确认，CONFIRMED已确认，DISPUTED有异议，FINISHED已完成',
  `confirm_time` datetime DEFAULT NULL COMMENT '确认时间',
  `confirm_by` varchar(64) DEFAULT NULL COMMENT '确认人',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_reconciliation_no` (`reconciliation_no`) COMMENT '资金对账单号唯一',
  KEY `idx_agent_cycle` (`agent_member_id`, `start_time`, `end_time`) COMMENT '按代理商和周期查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代理商资金对账单表';
```

## 7. 分账规则表

```sql
CREATE TABLE `profit_sharing_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rule_name` varchar(128) NOT NULL COMMENT '规则名称',
  `role_code` varchar(32) NOT NULL COMMENT '适用角色编码',
  `region_id` varchar(64) DEFAULT NULL COMMENT '适用区域ID，空表示全区域',
  `category_id` varchar(64) DEFAULT NULL COMMENT '适用品类ID，空表示全品类',
  `min_amount` decimal(18,2) DEFAULT NULL COMMENT '最低订单金额',
  `max_amount` decimal(18,2) DEFAULT NULL COMMENT '最高订单金额',
  `ratio` decimal(10,4) NOT NULL COMMENT '分账比例，例如0.1500表示15%',
  `status` varchar(32) NOT NULL DEFAULT 'ENABLE' COMMENT '状态：ENABLE启用，DISABLE停用',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_role_region_category` (`role_code`, `region_id`, `category_id`) COMMENT '按角色区域品类查询规则'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分账规则表';
```

## 8. 分账明细表

```sql
CREATE TABLE `profit_sharing_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `record_no` varchar(64) NOT NULL COMMENT '分账记录号',
  `order_sn` varchar(64) NOT NULL COMMENT '订单号',
  `order_type` varchar(32) NOT NULL COMMENT '订单类型：NORMAL普通，PINTUAN拼团，WHOLESALE批发，POINTS积分',
  `rule_id` bigint NOT NULL COMMENT '分账规则ID',
  `beneficiary_role_code` varchar(32) NOT NULL COMMENT '受益角色编码',
  `beneficiary_id` varchar(64) NOT NULL COMMENT '受益人ID',
  `region_id` varchar(64) DEFAULT NULL COMMENT '区域ID',
  `base_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '分账基数金额',
  `ratio` decimal(10,4) NOT NULL COMMENT '分账比例',
  `share_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '分账金额',
  `settlement_status` varchar(32) NOT NULL DEFAULT 'WAIT_CALCULATE' COMMENT '结算状态：WAIT_CALCULATE待计算，WAIT_AUDIT待审核，AUDIT_REJECTED驳回，WAIT_SETTLEMENT待结算，SETTLED已结算，FAILED失败',
  `audit_by` varchar(64) DEFAULT NULL COMMENT '审核人',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_remark` varchar(255) DEFAULT NULL COMMENT '审核备注',
  `settle_time` datetime DEFAULT NULL COMMENT '结算时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_no` (`record_no`) COMMENT '分账记录号唯一',
  KEY `idx_order_sn` (`order_sn`) COMMENT '按订单号查询',
  KEY `idx_beneficiary_status` (`beneficiary_id`, `settlement_status`) COMMENT '按受益人和结算状态查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分账明细表';
```

## 9. 核销记录表

```sql
CREATE TABLE `verification_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_sn` varchar(64) NOT NULL COMMENT '订单号',
  `order_item_id` varchar(64) NOT NULL COMMENT '订单项ID',
  `verification_code` varchar(64) NOT NULL COMMENT '核销码',
  `store_id` varchar(64) NOT NULL COMMENT '核销门店ID',
  `operator_id` varchar(64) NOT NULL COMMENT '核销操作人ID',
  `verify_status` varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT '核销状态：PENDING待核销，VERIFIED已核销，EXPIRED已过期，CANCELLED已作废',
  `verify_time` datetime DEFAULT NULL COMMENT '核销时间',
  `verify_remark` varchar(255) DEFAULT NULL COMMENT '核销备注',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_verification_code` (`verification_code`) COMMENT '核销码唯一',
  KEY `idx_order_sn` (`order_sn`) COMMENT '按订单号查询',
  KEY `idx_store_status` (`store_id`, `verify_status`) COMMENT '按门店和核销状态查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='核销记录表';
```

## 10. 首页金刚区配置表

```sql
CREATE TABLE `operation_shortcut_nav` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `nav_name` varchar(64) NOT NULL COMMENT '导航名称',
  `nav_icon` varchar(255) NOT NULL COMMENT '导航图标',
  `nav_route` varchar(255) NOT NULL COMMENT '跳转路由',
  `nav_sort` int NOT NULL DEFAULT 0 COMMENT '排序值',
  `nav_visible` tinyint NOT NULL DEFAULT 1 COMMENT '是否展示：0否，1是',
  `role_scope` varchar(64) NOT NULL COMMENT '角色范围：AGENT代理商，CONSUMER消费者，ALL全部',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_role_visible_sort` (`role_scope`, `nav_visible`, `nav_sort`) COMMENT '按角色和展示排序查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页金刚区配置表';
```

## 11. 卡券商品表

```sql
CREATE TABLE `card_coupon_goods` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `store_id` varchar(64) NOT NULL COMMENT '店铺ID',
  `card_coupon_name` varchar(128) NOT NULL COMMENT '卡券名称',
  `card_coupon_type` varchar(32) NOT NULL COMMENT '卡券类型：PHYSICAL实物卡，ELECTRONIC电子卡，EXCHANGE兑换券',
  `face_value` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '面值',
  `stock` int NOT NULL DEFAULT 0 COMMENT '库存',
  `sold_num` int NOT NULL DEFAULT 0 COMMENT '已售数量',
  `valid_start_time` datetime DEFAULT NULL COMMENT '生效时间',
  `valid_end_time` datetime DEFAULT NULL COMMENT '失效时间',
  `status` varchar(32) NOT NULL DEFAULT 'ENABLE' COMMENT '状态：ENABLE上架，DISABLE下架，SOLD_OUT售罄',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_store_status` (`store_id`, `status`) COMMENT '按店铺和状态查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='卡券商品表';
```

## 12. 落地说明

- 上述 SQL 为建模草案，正式落库前需与现有实体、主键策略、审计基类再对齐一次
- 若现有项目已有同义表，应优先复用并补字段，不要平行新建重复表
- 生成正式 SQL 时，必须保留中文注释，不得省略
