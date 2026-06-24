SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 批发商城管理端菜单路由基线
-- 作用：
-- 1. 按当前 lilishop-admin/src/router/business-routes.ts 的真实路由骨架，补齐 li_menu 菜单树
-- 2. 仅按固定 ID 补种子，不自动删除旧版平台菜单，避免误伤历史数据
-- 3. permission 列此处不做伪造映射，保持为空，后续由真实接口维度继续补齐
--
-- 使用建议：
-- 1. 先执行 tools/audit_admin_consistency.py 确认当前库状态
-- 2. 新库可直接执行；旧库若仍保留旧版平台菜单，需要先备份再决定是否清理旧菜单
-- 3. 本脚本只负责 li_menu 与当前前端导航对齐，不负责修复 li_role / li_role_menu 断链

DROP TEMPORARY TABLE IF EXISTS `tmp_wholesale_manager_menu_seed`;

CREATE TEMPORARY TABLE `tmp_wholesale_manager_menu_seed` (
  `id` bigint NOT NULL,
  `create_by` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `delete_flag` bit(1) DEFAULT NULL,
  `update_by` varchar(255) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `front_route` varchar(255) DEFAULT NULL,
  `icon` varchar(255) DEFAULT NULL,
  `level` int DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `parent_id` varchar(255) DEFAULT NULL,
  `path` varchar(255) DEFAULT NULL,
  `sort_order` decimal(10,2) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `front_component` varchar(255) DEFAULT NULL,
  `permission` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
);

INSERT INTO `tmp_wholesale_manager_menu_seed` (
  `id`,`create_by`,`create_time`,`delete_flag`,`update_by`,`update_time`,
  `description`,`front_route`,`icon`,`level`,`name`,`parent_id`,`path`,
  `sort_order`,`title`,`front_component`,`permission`
) VALUES
  ('3062200000000000001', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/', 'ep:data-board', '0', 'Home', '0', '/', 10.00, '工作台', NULL, NULL),
  ('3062200000000010001', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/welcome', '', '1', 'WholesaleDashboardHome', '3062200000000000001', '/welcome', 1.00, '工作台', NULL, NULL),
  ('3062200000000000002', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/dashboard', 'ep:data-board', '0', 'WholesaleDashboard', '0', '/dashboard', 20.00, '数据概览', NULL, NULL),
  ('3062200000000010002', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/dashboard/wholesale', '', '1', 'WholesaleDashboard', '3062200000000000002', '/dashboard/wholesale', 1.00, '工作台', NULL, NULL),
  ('3062200000000010003', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/dashboard/overview', '', '1', 'PlatformOverview', '3062200000000000002', '/dashboard/overview', 2.00, '数据概览', NULL, NULL),
  ('3062200000000010004', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/dashboard/store-rank', '', '1', 'StoreRank', '3062200000000000002', '/dashboard/store-rank', 3.00, '门店排行', NULL, NULL),
  ('3062200000000010005', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/dashboard/goods-rank', '', '1', 'GoodsRank', '3062200000000000002', '/dashboard/goods-rank', 4.00, '商品/品类排行', NULL, NULL),
  ('3062200000000010006', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/dashboard/order-overview', '', '1', 'OrderOverview', '3062200000000000002', '/dashboard/order-overview', 5.00, '订单概览', NULL, NULL),
  ('3062200000000000003', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/store-governance', 'ep:shop', '0', 'StoreApplyAudit', '0', '/store-governance', 30.00, '店铺治理', NULL, NULL),
  ('3062200000000010007', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/store-governance/store-apply', '', '1', 'StoreApplyAudit', '3062200000000000003', '/store-governance/store-apply', 1.00, '入驻审核', NULL, NULL),
  ('3062200000000010008', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/store-governance/store-manage', '', '1', 'StoreManage', '3062200000000000003', '/store-governance/store-manage', 2.00, '店铺管理', NULL, NULL),
  ('3062200000000010009', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/store-governance/agent-manage', '', '1', 'AgentManage', '3062200000000000003', '/store-governance/agent-manage', 3.00, '代理商治理', NULL, NULL),
  ('3062200000000010010', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/store-governance/store-audit-log', '', '1', 'StoreAuditLog', '3062200000000000003', '/store-governance/store-audit-log', 4.00, '店铺审核历史', NULL, NULL),
  ('3062200000000000004', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/goods-governance', 'ep:goods', '0', 'GoodsManage', '0', '/goods-governance', 40.00, '商品管理', NULL, NULL),
  ('3062200000000010011', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/goods-governance/goods-manage', '', '1', 'GoodsManage', '3062200000000000004', '/goods-governance/goods-manage', 1.00, '商品列表', NULL, NULL),
  ('3062200000000010093', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/goods-governance/brand-manage', '', '1', 'BrandManage', '3062200000000000004', '/goods-governance/brand-manage', 2.00, '品牌管理', NULL, NULL),
  ('3062200000000010094', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/goods-governance/parameter-manage', '', '1', 'ParameterManage', '3062200000000000004', '/goods-governance/parameter-manage', 3.00, '参数管理', NULL, NULL),
  ('3062200000000010012', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/goods-governance/category-manage', '', '1', 'CategoryManage', '3062200000000000004', '/goods-governance/category-manage', 4.00, '分类管理', NULL, NULL),
  ('3062200000000010095', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/goods-governance/goods-group-manage', '', '1', 'GoodsGroupManage', '3062200000000000004', '/goods-governance/goods-group-manage', 5.00, '商品分组', NULL, NULL),
  ('3062200000000010013', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/goods-governance/home-category', '', '1', 'HomeCategoryManage', '3062200000000000004', '/goods-governance/home-category', 6.00, '首页分类配置', NULL, NULL),
  ('3062200000000010014', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/goods-governance/points-goods', '', '1', 'PointsGoodsManage', '3062200000000000004', '/goods-governance/points-goods', 7.00, '积分商品治理', NULL, NULL),
  ('3062200000000010015', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/goods-governance/card-coupon-goods', '', '1', 'CardCouponGoodsManage', '3062200000000000004', '/goods-governance/card-coupon-goods', 8.00, '卡券商品治理', NULL, NULL),
  ('3062200000000000005', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/order-governance', 'ep:document', '0', 'OrderManage', '0', '/order-governance', 50.00, '订单管理', NULL, NULL),
  ('3062200000000010016', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/order-governance/order-manage', '', '1', 'OrderManage', '3062200000000000005', '/order-governance/order-manage', 1.00, '订单列表', NULL, NULL),
  ('3062200000000010096', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/order-governance/order-complaint', '', '1', 'OrderComplaintManage', '3062200000000000005', '/order-governance/order-complaint', 2.00, '订单投诉', NULL, NULL),
  ('3062200000000010017', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/order-governance/verification-rule', '', '1', 'VerificationRuleManage', '3062200000000000005', '/order-governance/verification-rule', 3.00, '核销规则', NULL, NULL),
  ('3062200000000010107', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/order-governance/refund-log', '', '1', 'RefundLogManage', '3062200000000000005', '/order-governance/refund-log', 4.00, '退款日志', NULL, NULL),
  ('3062200000000010108', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/order-governance/payment-log', '', '1', 'PaymentLogManage', '3062200000000000005', '/order-governance/payment-log', 5.00, '支付日志', NULL, NULL),
  ('3062200000000010109', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/order-governance/receipt-manage', '', '1', 'ReceiptManage', '3062200000000000005', '/order-governance/receipt-manage', 6.00, '发票管理', NULL, NULL),
  ('3062200000000010110', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/order-governance/order-log', '', '1', 'OrderLogManage', '3062200000000000005', '/order-governance/order-log', 7.00, '订单日志', NULL, NULL),
  ('3062200000000010018', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/order-governance/after-sale', '', '1', 'AfterSaleManage', '3062200000000000005', '/order-governance/after-sale', 8.00, '售后治理', NULL, NULL),
  ('3062200000000010019', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/order-governance/verification-record', '', '1', 'VerificationRecordManage', '3062200000000000005', '/order-governance/verification-record', 9.00, '核销记录', NULL, NULL),
  ('3062200000000000006', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/marketing-governance', 'ep:sell', '0', 'MarketingGovernance', '0', '/marketing-governance', 60.00, '营销管理', NULL, NULL),
  ('3062200000000010101', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/marketing-governance/coupon-manage', '', '1', 'CouponManage', '3062200000000000006', '/marketing-governance/coupon-manage', 1.00, '优惠券管理', NULL, NULL),
  ('3062200000000010102', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/marketing-governance/coupon-activity', '', '1', 'CouponActivityManage', '3062200000000000006', '/marketing-governance/coupon-activity', 2.00, '券活动管理', NULL, NULL),
  ('3062200000000010103', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/marketing-governance/full-discount', '', '1', 'FullDiscountManage', '3062200000000000006', '/marketing-governance/full-discount', 3.00, '满减活动管理', NULL, NULL),
  ('3062200000000010104', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/marketing-governance/seckill-manage', '', '1', 'SeckillManage', '3062200000000000006', '/marketing-governance/seckill-manage', 4.00, '秒杀活动管理', NULL, NULL),
  ('3062200000000010105', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/marketing-governance/kanjia-manage', '', '1', 'KanjiaManage', '3062200000000000006', '/marketing-governance/kanjia-manage', 5.00, '砍价活动管理', NULL, NULL),
  ('3062200000000010020', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/marketing-governance/pintuan-rule', '', '1', 'PintuanRule', '3062200000000000006', '/marketing-governance/pintuan-rule', 6.00, '拼团规则', NULL, NULL),
  ('3062200000000010021', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/marketing-governance/pintuan-record', '', '1', 'PintuanRecord', '3062200000000000006', '/marketing-governance/pintuan-record', 7.00, '拼团记录', NULL, NULL),
  ('3062200000000000007', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/fund-governance', 'ep:money', '0', 'ProfitSharingRule', '0', '/fund-governance', 70.00, '分账管理', NULL, NULL),
  ('3062200000000010022', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/fund-governance/profitsharing-rule', '', '1', 'ProfitSharingRule', '3062200000000000007', '/fund-governance/profitsharing-rule', 1.00, '分账规则', NULL, NULL),
  ('3062200000000010106', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/fund-governance/bill-manage', '', '1', 'BillManage', '3062200000000000007', '/fund-governance/bill-manage', 2.00, '结算单', NULL, NULL),
  ('3062200000000010023', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/fund-governance/profitsharing-record', '', '1', 'ProfitSharingRecord', '3062200000000000007', '/fund-governance/profitsharing-record', 3.00, '分账明细', NULL, NULL),
  ('3062200000000010024', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/fund-governance/wallet-log', '', '1', 'WalletLog', '3062200000000000007', '/fund-governance/wallet-log', 4.00, '余额记录', NULL, NULL),
  ('3062200000000010025', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/fund-governance/profitsharing-balance', '', '1', 'ProfitSharingBalance', '3062200000000000007', '/fund-governance/profitsharing-balance', 5.00, '分账治理概览', NULL, NULL),
  ('3062200000000010026', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/fund-governance/wallet-account', '', '1', 'WalletAccount', '3062200000000000007', '/fund-governance/wallet-account', 6.00, '钱包账户台账', NULL, NULL),
  ('3062200000000010027', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/fund-governance/withdraw-apply', '', '1', 'WithdrawApply', '3062200000000000007', '/fund-governance/withdraw-apply', 7.00, '提现审核', NULL, NULL),
  ('3062200000000010028', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/fund-governance/procurement-reconciliation', '', '1', 'ProcurementReconciliation', '3062200000000000007', '/fund-governance/procurement-reconciliation', 8.00, '采购对账', NULL, NULL),
  ('3062200000000010029', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/fund-governance/fund-reconciliation', '', '1', 'FundReconciliation', '3062200000000000007', '/fund-governance/fund-reconciliation', 9.00, '资金对账', NULL, NULL),
  ('3062200000000000008', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config', 'ep:tools', '0', 'PlatformBaseSetting', '0', '/platform-config', 80.00, '平台配置', NULL, NULL),
  ('3062200000000010030', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/base-setting', '', '1', 'PlatformBaseSetting', '3062200000000000008', '/platform-config/base-setting', 1.00, '基础设置', NULL, NULL),
  ('3062200000000010031', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/payment-setting', '', '1', 'PlatformPaymentSetting', '3062200000000000008', '/platform-config/payment-setting', 2.00, '支付配置', NULL, NULL),
  ('3062200000000010032', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/withdraw-setting', '', '1', 'PlatformWithdrawSetting', '3062200000000000008', '/platform-config/withdraw-setting', 3.00, '提现设置', NULL, NULL),
  ('3062200000000010033', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/oss-setting', '', '1', 'PlatformOssSetting', '3062200000000000008', '/platform-config/oss-setting', 4.00, '对象存储配置', NULL, NULL),
  ('3062200000000010034', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/sms-setting', '', '1', 'PlatformSmsSetting', '3062200000000000008', '/platform-config/sms-setting', 5.00, '短信配置', NULL, NULL),
  ('3062200000000010035', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/email-setting', '', '1', 'PlatformEmailSetting', '3062200000000000008', '/platform-config/email-setting', 6.00, '邮件配置', NULL, NULL),
  ('3062200000000010036', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/goods-setting', '', '1', 'PlatformGoodsSetting', '3062200000000000008', '/platform-config/goods-setting', 7.00, '商品设置', NULL, NULL),
  ('3062200000000010037', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/order-setting', '', '1', 'PlatformOrderSetting', '3062200000000000008', '/platform-config/order-setting', 8.00, '订单设置', NULL, NULL),
  ('3062200000000010038', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/logistics-setting', '', '1', 'PlatformLogisticsSetting', '3062200000000000008', '/platform-config/logistics-setting', 9.00, '物流设置', NULL, NULL),
  ('3062200000000010039', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/point-setting', '', '1', 'PlatformPointSetting', '3062200000000000008', '/platform-config/point-setting', 10.00, '积分设置', NULL, NULL),
  ('3062200000000010040', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/experience-setting', '', '1', 'PlatformExperienceSetting', '3062200000000000008', '/platform-config/experience-setting', 11.00, '经验值设置', NULL, NULL),
  ('3062200000000010041', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/seckill-setting', '', '1', 'PlatformSeckillSetting', '3062200000000000008', '/platform-config/seckill-setting', 12.00, '秒杀设置', NULL, NULL),
  ('3062200000000010042', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/im-setting', '', '1', 'PlatformImSetting', '3062200000000000008', '/platform-config/im-setting', 13.00, 'IM配置', NULL, NULL),
  ('3062200000000010043', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/connect-setting', '', '1', 'PlatformConnectSetting', '3062200000000000008', '/platform-config/connect-setting', 14.00, '登录设置', NULL, NULL),
  ('3062200000000010044', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/distribution-setting', '', '1', 'PlatformDistributionSetting', '3062200000000000008', '/platform-config/distribution-setting', 15.00, '分销设置', NULL, NULL),
  ('3062200000000010045', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/alipay-setting', '', '1', 'PlatformAlipaySetting', '3062200000000000008', '/platform-config/alipay-setting', 16.00, '支付宝配置', NULL, NULL),
  ('3062200000000010046', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/wechat-payment-setting', '', '1', 'PlatformWechatPaymentSetting', '3062200000000000008', '/platform-config/wechat-payment-setting', 17.00, '微信支付配置', NULL, NULL),
  ('3062200000000010047', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/unionpay-setting', '', '1', 'PlatformUnionpaySetting', '3062200000000000008', '/platform-config/unionpay-setting', 18.00, '银联支付配置', NULL, NULL),
  ('3062200000000010048', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/wechat-connect-setting', '', '1', 'PlatformWechatConnectSetting', '3062200000000000008', '/platform-config/wechat-connect-setting', 19.00, '微信登录配置', NULL, NULL),
  ('3062200000000010049', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/qq-connect-setting', '', '1', 'PlatformQqConnectSetting', '3062200000000000008', '/platform-config/qq-connect-setting', 20.00, 'QQ登录配置', NULL, NULL),
  ('3062200000000010050', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/hot-words-setting', '', '1', 'PlatformHotWordsSetting', '3062200000000000008', '/platform-config/hot-words-setting', 21.00, '热词设置', NULL, NULL),
  ('3062200000000010051', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/platform-config/wx-channels-setting', '', '1', 'PlatformWxChannelsSetting', '3062200000000000008', '/platform-config/wx-channels-setting', 22.00, '视频号配置', NULL, NULL),
  ('3062200000000000009', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/member-center', 'ep:user', '0', 'MemberList', '0', '/member-center', 90.00, '用户中心', NULL, NULL),
  ('3062200000000010052', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/member-center/member-list', '', '1', 'MemberList', '3062200000000000009', '/member-center/member-list', 1.00, '前台用户管理', NULL, NULL),
  ('3062200000000010053', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/member-center/member-grade', '', '1', 'MemberGrade', '3062200000000000009', '/member-center/member-grade', 2.00, '会员等级', NULL, NULL),
  ('3062200000000010054', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/member-center/member-group', '', '1', 'MemberGroup', '3062200000000000009', '/member-center/member-group', 3.00, '会员分组', NULL, NULL),
  ('3062200000000010055', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/member-center/member-benefit', '', '1', 'MemberBenefit', '3062200000000000009', '/member-center/member-benefit', 4.00, '会员权益', NULL, NULL),
  ('3062200000000010056', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/member-center/member-points-history', '', '1', 'MemberPointsHistory', '3062200000000000009', '/member-center/member-points-history', 5.00, '积分记录', NULL, NULL),
  ('3062200000000010057', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/member-center/member-evaluation', '', '1', 'MemberEvaluation', '3062200000000000009', '/member-center/member-evaluation', 6.00, '用户评价', NULL, NULL),
  ('3062200000000010058', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/member-center/member-address', '', '1', 'MemberAddress', '3062200000000000009', '/member-center/member-address', 7.00, '用户地址', NULL, NULL),
  ('3062200000000000010', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/message-center', 'ep:chat-dot-round', '0', 'MemberNotice', '0', '/message-center', 100.00, '通知与消息', NULL, NULL),
  ('3062200000000010059', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/message-center/member-notice', '', '1', 'MemberNotice', '3062200000000000010', '/message-center/member-notice', 1.00, '站内通知', NULL, NULL),
  ('3062200000000010060', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/message-center/member-notice-log', '', '1', 'MemberNoticeLog', '3062200000000000010', '/message-center/member-notice-log', 2.00, '通知日志', NULL, NULL),
  ('3062200000000010061', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/message-center/sms-sign', '', '1', 'SmsSign', '3062200000000000010', '/message-center/sms-sign', 3.00, '短信签名', NULL, NULL),
  ('3062200000000010062', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/message-center/sms-template', '', '1', 'SmsTemplate', '3062200000000000010', '/message-center/sms-template', 4.00, '短信模板', NULL, NULL),
  ('3062200000000010063', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/message-center/member-notice-sender', '', '1', 'MemberNoticeSender', '3062200000000000010', '/message-center/member-notice-sender', 5.00, '发送任务', NULL, NULL),
  ('3062200000000010064', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/message-center/service-notice', '', '1', 'ServiceNotice', '3062200000000000010', '/message-center/service-notice', 6.00, '服务通知', NULL, NULL),
  ('3062200000000010065', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/message-center/message-channel', '', '1', 'MessageChannel', '3062200000000000010', '/message-center/message-channel', 7.00, '消息发送管理', NULL, NULL),
  ('3062200000000010066', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/message-center/member-message', '', '1', 'MemberMessage', '3062200000000000010', '/message-center/member-message', 8.00, '客户消息', NULL, NULL),
  ('3062200000000010067', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/message-center/store-message', '', '1', 'StoreMessage', '3062200000000000010', '/message-center/store-message', 9.00, '店铺消息', NULL, NULL),
  ('3062200000000000011', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/content-center', 'ep:document', '0', 'ContentArticle', '0', '/content-center', 110.00, '内容管理', NULL, NULL),
  ('3062200000000010068', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/content-center/article', '', '1', 'ContentArticle', '3062200000000000011', '/content-center/article', 1.00, '公告管理', NULL, NULL),
  ('3062200000000010070', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/content-center/page-data', '', '1', 'ContentPageData', '3062200000000000011', '/content-center/page-data', 2.00, '页面装修', NULL, NULL),
  ('3062200000000010071', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/content-center/shortcut-nav', '', '1', 'ContentShortcutNav', '3062200000000000011', '/content-center/shortcut-nav', 3.00, '快捷导航', NULL, NULL),
  ('3062200000000010072', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/content-center/article-category', '', '1', 'ContentArticleCategory', '3062200000000000011', '/content-center/article-category', 4.00, '文章分类', NULL, NULL),
  ('3062200000000010073', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/content-center/sensitive-words', '', '1', 'ContentSensitiveWords', '3062200000000000011', '/content-center/sensitive-words', 5.00, '敏感词管理', NULL, NULL),
  ('3062200000000010074', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/content-center/custom-words', '', '1', 'ContentCustomWords', '3062200000000000011', '/content-center/custom-words', 6.00, '自定义词库', NULL, NULL),
  ('3062200000000010075', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/content-center/hot-words-manage', '', '1', 'ContentHotWordsManage', '3062200000000000011', '/content-center/hot-words-manage', 7.00, '热词治理', NULL, NULL),
  ('3062200000000010076', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/content-center/special', '', '1', 'ContentSpecial', '3062200000000000011', '/content-center/special', 8.00, '专题管理', NULL, NULL),
  ('3062200000000010077', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/content-center/app-version', '', '1', 'ContentAppVersion', '3062200000000000011', '/content-center/app-version', 9.00, 'APP版本', NULL, NULL),
  ('3062200000000000012', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/support-center', 'ep:setting', '0', 'SupportVerificationSource', '0', '/support-center', 120.00, '系统支撑', NULL, NULL),
  ('3062200000000010078', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/support-center/verification-source', '', '1', 'SupportVerificationSource', '3062200000000000012', '/support-center/verification-source', 1.00, '验证码资源', NULL, NULL),
  ('3062200000000010079', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/support-center/region-manage', '', '1', 'SupportRegionManage', '3062200000000000012', '/support-center/region-manage', 2.00, '区域管理', NULL, NULL),
  ('3062200000000010080', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/support-center/logistics-manage', '', '1', 'SupportLogisticsManage', '3062200000000000012', '/support-center/logistics-manage', 3.00, '物流公司', NULL, NULL),
  ('3062200000000010081', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/support-center/after-sale-reason', '', '1', 'SupportAfterSaleReason', '3062200000000000012', '/support-center/after-sale-reason', 4.00, '售后原因', NULL, NULL),
  ('3062200000000010082', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/support-center/feedback-manage', '', '1', 'SupportFeedbackManage', '3062200000000000012', '/support-center/feedback-manage', 5.00, '意见反馈', NULL, NULL),
  ('3062200000000010083', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/support-center/setting-log', '', '1', 'SupportSettingLog', '3062200000000000012', '/support-center/setting-log', 6.00, '配置日志', NULL, NULL),
  ('3062200000000000013', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/merchant-grant', 'ep:key', '0', 'MerchantGrantMenuPermission', '0', '/merchant-grant', 130.00, '系统与权限', NULL, NULL),
  ('3062200000000010086', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/merchant-grant/menu-permission', '', '1', 'MerchantGrantMenuPermission', '3062200000000000013', '/merchant-grant/menu-permission', 1.00, '平台菜单权限', NULL, NULL),
  ('3062200000000010087', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/merchant-grant/role-permission', '', '1', 'MerchantGrantRolePermission', '3062200000000000013', '/merchant-grant/role-permission', 2.00, '平台角色权限', NULL, NULL),
  ('3062200000000010088', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/merchant-grant/department-manage', '', '1', 'MerchantGrantDepartmentManage', '3062200000000000013', '/merchant-grant/department-manage', 3.00, '组织架构配置', NULL, NULL),
  ('3062200000000010089', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/merchant-grant/backend-account', '', '1', 'MerchantGrantBackendAccount', '3062200000000000013', '/merchant-grant/backend-account', 4.00, '平台后台账号', NULL, NULL),
  ('3062200000000010097', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/merchant-grant/store-menu', '', '1', 'MerchantGrantStoreMenu', '3062200000000000013', '/merchant-grant/store-menu', 5.00, '店铺菜单资源', NULL, NULL),
  ('3062200000000000014', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/system', 'ep:setting', '0', 'SystemWarningSetting', '0', '/system', 140.00, '系统设置', NULL, NULL),
  ('3062200000000010090', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/system/warning-setting', '', '1', 'SystemWarningSetting', '3062200000000000014', '/system/warning-setting', 1.00, '预警设置', NULL, NULL),
  ('3062200000000010091', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/system/maintenance-log', '', '1', 'SystemMaintenanceLog', '3062200000000000014', '/system/maintenance-log', 2.00, '系统维护记录', NULL, NULL),
  ('3062200000000010092', 'admin', CURRENT_TIMESTAMP, b'0', 'admin', CURRENT_TIMESTAMP, 'wholesale-manager-route-baseline', '/system/operation-log', '', '1', 'SystemOperationLog', '3062200000000000014', '/system/operation-log', 3.00, '操作日志', NULL, NULL);

INSERT INTO `li_menu` (
  `id`,`create_by`,`create_time`,`delete_flag`,`update_by`,`update_time`,
  `description`,`front_route`,`icon`,`level`,`name`,`parent_id`,`path`,
  `sort_order`,`title`,`front_component`,`permission`
)
SELECT
  `id`,`create_by`,`create_time`,`delete_flag`,`update_by`,`update_time`,
  `description`,`front_route`,`icon`,`level`,`name`,`parent_id`,`path`,
  `sort_order`,`title`,`front_component`,`permission`
FROM `tmp_wholesale_manager_menu_seed` seed
WHERE NOT EXISTS (
  SELECT 1 FROM `li_menu` m WHERE m.`id` = seed.`id`
);

UPDATE `li_menu` m
JOIN `tmp_wholesale_manager_menu_seed` seed ON m.`id` = seed.`id`
SET
  m.`update_by` = seed.`update_by`,
  m.`update_time` = CURRENT_TIMESTAMP,
  m.`delete_flag` = seed.`delete_flag`,
  m.`description` = seed.`description`,
  m.`front_route` = seed.`front_route`,
  m.`icon` = seed.`icon`,
  m.`level` = seed.`level`,
  m.`name` = seed.`name`,
  m.`parent_id` = seed.`parent_id`,
  m.`path` = seed.`path`,
  m.`sort_order` = seed.`sort_order`,
  m.`title` = seed.`title`,
  m.`front_component` = seed.`front_component`;

DELETE FROM `li_role_menu`
WHERE `menu_id` = '3062200000000010069';

DELETE FROM `li_menu`
WHERE `id` = 3062200000000010069
   OR (`front_route` = '/content-center/message-monitor' AND `id` NOT IN (
     SELECT `id` FROM `tmp_wholesale_manager_menu_seed`
   ));

SELECT `id`, `parent_id`, `title`, `front_route`, `path`
FROM `li_menu`
WHERE `id` BETWEEN 3062200000000000001 AND 3062200000000010110
ORDER BY `level`, `sort_order`, `id`;

SET FOREIGN_KEY_CHECKS = 1;
