SET NAMES utf8mb4;

-- 基于当前 lilishop-admin 路由实际调用的后端接口生成
-- 仅回填批发商城管理端新菜单基线范围内的 permission 字段
-- baseline range: 3062200000000000001 ~ 3062200000000010110

-- [工作台] 工作台 -> /welcome
UPDATE li_menu SET permission = '/manager/dashboard/wholesale*,/manager/statistics/index/notice*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/welcome';

-- [数据概览] 工作台 -> /dashboard/wholesale
UPDATE li_menu SET permission = '/manager/dashboard/wholesale*,/manager/statistics/index/notice*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/dashboard/wholesale';

-- [数据概览] 数据概览 -> /dashboard/overview
UPDATE li_menu SET permission = '/manager/dashboard/wholesale*,/manager/statistics/order/overview*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/dashboard/overview';

-- [数据概览] 门店排行 -> /dashboard/store-rank
UPDATE li_menu SET permission = '/manager/statistics/index/storeStatistics*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/dashboard/store-rank';

-- [数据概览] 商品/品类排行 -> /dashboard/goods-rank
UPDATE li_menu SET permission = '/manager/statistics/index/goodsStatistics*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/dashboard/goods-rank';

-- [数据概览] 订单概览 -> /dashboard/order-overview
UPDATE li_menu SET permission = '/manager/statistics/order/overview*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/dashboard/order-overview';

-- [店铺治理] 入驻审核 -> /store-governance/store-apply
UPDATE li_menu SET permission = '/manager/store/store*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/store-governance/store-apply';

-- [店铺治理] 店铺管理 -> /store-governance/store-manage
UPDATE li_menu SET permission = '/manager/store/store*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/store-governance/store-manage';

-- [店铺治理] 代理商治理 -> /store-governance/agent-manage
UPDATE li_menu SET permission = '/manager/agent/role*,/manager/agent/store*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/store-governance/agent-manage';

-- [店铺治理] 店铺审核历史 -> /store-governance/store-audit-log
UPDATE li_menu SET permission = '/manager/store/store*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/store-governance/store-audit-log';

-- [商品管理] 商品列表 -> /goods-governance/goods-manage
UPDATE li_menu SET permission = '/manager/goods/goods/up*,/manager/goods/goods/get*,/manager/goods/goods/auth*,/manager/goods/goods/under*,/manager/goods/goods/wholesale/list*,/manager/goods/goodsUnit*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/goods-governance/goods-manage';

-- [商品管理] 品牌管理 -> /goods-governance/brand-manage
UPDATE li_menu SET permission = '/manager/goods/brand*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/goods-governance/brand-manage';

-- [商品管理] 参数管理 -> /goods-governance/parameter-manage
UPDATE li_menu SET permission = '/manager/goods/parameters*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/goods-governance/parameter-manage';

-- [商品管理] 分类管理 -> /goods-governance/category-manage
UPDATE li_menu SET permission = '/manager/goods/category*,/manager/goods/brand/all*,/manager/goods/categoryBrand*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/goods-governance/category-manage';

-- [商品管理] 商品分组 -> /goods-governance/goods-group-manage
UPDATE li_menu SET permission = '/manager/goods/goodsGroup*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/goods-governance/goods-group-manage';

-- [商品管理] 积分商品治理 -> /goods-governance/points-goods
UPDATE li_menu SET permission = '/manager/promotion/pointsGoods*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/goods-governance/points-goods';

-- [商品管理] 卡券商品治理 -> /goods-governance/card-coupon-goods
UPDATE li_menu SET permission = '/manager/promotion/cardCouponGoods*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/goods-governance/card-coupon-goods';

-- [订单管理] 订单列表 -> /order-governance/order-manage
UPDATE li_menu SET permission = '/manager/order/order*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/order-governance/order-manage';

-- [订单管理] 订单投诉 -> /order-governance/order-complaint
UPDATE li_menu SET permission = '/manager/order/complain*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/order-governance/order-complaint';

-- [订单管理] 核销规则 -> /order-governance/verification-rule
UPDATE li_menu SET permission = '/manager/setting/setting/get/VERIFICATION_RULE_SETTING*,/manager/setting/setting/put/VERIFICATION_RULE_SETTING*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/order-governance/verification-rule';

-- [订单管理] 退款日志 -> /order-governance/refund-log
UPDATE li_menu SET permission = '/manager/order/refundLog*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/order-governance/refund-log';

-- [订单管理] 支付日志 -> /order-governance/payment-log
UPDATE li_menu SET permission = '/manager/order/paymentLog*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/order-governance/payment-log';

-- [订单管理] 发票管理 -> /order-governance/receipt-manage
UPDATE li_menu SET permission = '/manager/trade/receipt*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/order-governance/receipt-manage';

-- [订单管理] 订单日志 -> /order-governance/order-log
UPDATE li_menu SET permission = '/manager/order/orderLog*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/order-governance/order-log';

-- [订单管理] 售后治理 -> /order-governance/after-sale
UPDATE li_menu SET permission = '/manager/order/afterSale/page*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/order-governance/after-sale';

-- [订单管理] 核销记录 -> /order-governance/verification-record
UPDATE li_menu SET permission = '/manager/other/verificationRecord*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/order-governance/verification-record';

-- [营销管理] 优惠券管理 -> /marketing-governance/coupon-manage
UPDATE li_menu SET permission = '/manager/promotion/coupon*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/marketing-governance/coupon-manage';

-- [营销管理] 券活动管理 -> /marketing-governance/coupon-activity
UPDATE li_menu SET permission = '/manager/promotion/couponActivity*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/marketing-governance/coupon-activity';

-- [营销管理] 满减活动管理 -> /marketing-governance/full-discount
UPDATE li_menu SET permission = '/manager/promotion/fullDiscount*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/marketing-governance/full-discount';

-- [营销管理] 秒杀活动管理 -> /marketing-governance/seckill-manage
UPDATE li_menu SET permission = '/manager/promotion/seckill*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/marketing-governance/seckill-manage';

-- [营销管理] 砍价活动管理 -> /marketing-governance/kanjia-manage
UPDATE li_menu SET permission = '/manager/promotion/kanJiaGoods*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/marketing-governance/kanjia-manage';

-- [营销管理] 拼团规则 -> /marketing-governance/pintuan-rule
UPDATE li_menu SET permission = '/manager/promotion/pintuan*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/marketing-governance/pintuan-rule';

-- [营销管理] 拼团记录 -> /marketing-governance/pintuan-record
UPDATE li_menu SET permission = '/manager/promotion/pintuan*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/marketing-governance/pintuan-record';

-- [分账管理] 分账规则 -> /fund-governance/profitsharing-rule
UPDATE li_menu SET permission = '/manager/profitsharing/rule*,/common/common/region/allCity*,/manager/goods/category/allChildren*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/fund-governance/profitsharing-rule';

-- [分账管理] 结算单 -> /fund-governance/bill-manage
UPDATE li_menu SET permission = '/manager/order/bill*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/fund-governance/bill-manage';

-- [分账管理] 分账明细 -> /fund-governance/profitsharing-record
UPDATE li_menu SET permission = '/manager/profitsharing/record*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/fund-governance/profitsharing-record';

-- [分账管理] 余额记录 -> /fund-governance/wallet-log
UPDATE li_menu SET permission = '/manager/wallet/log*,/manager/wallet/wallet*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/fund-governance/wallet-log';

-- [分账管理] 分账治理概览 -> /fund-governance/profitsharing-balance
UPDATE li_menu SET permission = '/manager/profitsharing/balance*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/fund-governance/profitsharing-balance';

-- [分账管理] 钱包账户台账 -> /fund-governance/wallet-account
UPDATE li_menu SET permission = '/manager/wallet/wallet/page*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/fund-governance/wallet-account';

-- [分账管理] 提现审核 -> /fund-governance/withdraw-apply
UPDATE li_menu SET permission = '/manager/wallet/withdrawApply*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/fund-governance/withdraw-apply';

-- [分账管理] 采购对账 -> /fund-governance/procurement-reconciliation
UPDATE li_menu SET permission = '/manager/reconciliation/purchase*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/fund-governance/procurement-reconciliation';

-- [分账管理] 资金对账 -> /fund-governance/fund-reconciliation
UPDATE li_menu SET permission = '/manager/reconciliation/fund*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/fund-governance/fund-reconciliation';

-- [平台配置] 基础设置 -> /platform-config/base-setting
UPDATE li_menu SET permission = '/manager/setting/setting/get*,/manager/setting/setting/put*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/platform-config/base-setting';

-- [平台配置] 支付配置 -> /platform-config/payment-setting
UPDATE li_menu SET permission = '/manager/setting/setting/get*,/manager/setting/setting/put*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/platform-config/payment-setting';

-- [平台配置] 提现设置 -> /platform-config/withdraw-setting
UPDATE li_menu SET permission = '/manager/setting/setting/get*,/manager/setting/setting/put*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/platform-config/withdraw-setting';

-- [平台配置] 对象存储配置 -> /platform-config/oss-setting
UPDATE li_menu SET permission = '/manager/setting/setting/get*,/manager/setting/setting/put*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/platform-config/oss-setting';

-- [平台配置] 短信配置 -> /platform-config/sms-setting
UPDATE li_menu SET permission = '/manager/setting/setting/get*,/manager/setting/setting/put*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/platform-config/sms-setting';

-- [平台配置] 邮件配置 -> /platform-config/email-setting
UPDATE li_menu SET permission = '/manager/setting/setting/get*,/manager/setting/setting/put*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/platform-config/email-setting';

-- [平台配置] 商品设置 -> /platform-config/goods-setting
UPDATE li_menu SET permission = '/manager/setting/setting/get*,/manager/setting/setting/put*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/platform-config/goods-setting';

-- [平台配置] 订单设置 -> /platform-config/order-setting
UPDATE li_menu SET permission = '/manager/setting/setting/get*,/manager/setting/setting/put*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/platform-config/order-setting';

-- [平台配置] 物流设置 -> /platform-config/logistics-setting
UPDATE li_menu SET permission = '/manager/setting/setting/get*,/manager/setting/setting/put*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/platform-config/logistics-setting';

-- [平台配置] 积分设置 -> /platform-config/point-setting
UPDATE li_menu SET permission = '/manager/setting/setting/get*,/manager/setting/setting/put*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/platform-config/point-setting';

-- [平台配置] 经验值设置 -> /platform-config/experience-setting
UPDATE li_menu SET permission = '/manager/setting/setting/get*,/manager/setting/setting/put*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/platform-config/experience-setting';

-- [平台配置] 秒杀设置 -> /platform-config/seckill-setting
UPDATE li_menu SET permission = '/manager/setting/setting/get*,/manager/setting/setting/put*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/platform-config/seckill-setting';

-- [平台配置] IM配置 -> /platform-config/im-setting
UPDATE li_menu SET permission = '/manager/setting/setting/get*,/manager/setting/setting/put*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/platform-config/im-setting';

-- [平台配置] 登录设置 -> /platform-config/connect-setting
UPDATE li_menu SET permission = '/manager/setting/setting/get*,/manager/setting/setting/put*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/platform-config/connect-setting';

-- [平台配置] 分销设置 -> /platform-config/distribution-setting
UPDATE li_menu SET permission = '/manager/setting/setting/get*,/manager/setting/setting/put*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/platform-config/distribution-setting';

-- [平台配置] 支付宝配置 -> /platform-config/alipay-setting
UPDATE li_menu SET permission = '/manager/setting/setting/get*,/manager/setting/setting/put*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/platform-config/alipay-setting';

-- [平台配置] 微信支付配置 -> /platform-config/wechat-payment-setting
UPDATE li_menu SET permission = '/manager/setting/setting/get*,/manager/setting/setting/put*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/platform-config/wechat-payment-setting';

-- [平台配置] 银联支付配置 -> /platform-config/unionpay-setting
UPDATE li_menu SET permission = '/manager/setting/setting/get*,/manager/setting/setting/put*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/platform-config/unionpay-setting';

-- [平台配置] 微信登录配置 -> /platform-config/wechat-connect-setting
UPDATE li_menu SET permission = '/manager/setting/setting/get*,/manager/setting/setting/put*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/platform-config/wechat-connect-setting';

-- [平台配置] QQ登录配置 -> /platform-config/qq-connect-setting
UPDATE li_menu SET permission = '/manager/setting/setting/get*,/manager/setting/setting/put*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/platform-config/qq-connect-setting';

-- [平台配置] 热词设置 -> /platform-config/hot-words-setting
UPDATE li_menu SET permission = '/manager/setting/setting/get*,/manager/setting/setting/put*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/platform-config/hot-words-setting';

-- [平台配置] 视频号配置 -> /platform-config/wx-channels-setting
UPDATE li_menu SET permission = '/manager/setting/setting/get*,/manager/setting/setting/put*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/platform-config/wx-channels-setting';

-- [用户中心] 前台用户管理 -> /member-center/member-list
UPDATE li_menu SET permission = '/manager/passport/member*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/member-center/member-list';

-- [用户中心] 会员等级 -> /member-center/member-grade
UPDATE li_menu SET permission = '/manager/member/memberGrade*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/member-center/member-grade';

-- [用户中心] 会员分组 -> /member-center/member-group
UPDATE li_menu SET permission = '/manager/passport/member*,/manager/member/memberGroup*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/member-center/member-group';

-- [用户中心] 会员权益 -> /member-center/member-benefit
UPDATE li_menu SET permission = '/manager/member/benefit*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/member-center/member-benefit';

-- [用户中心] 积分记录 -> /member-center/member-points-history
UPDATE li_menu SET permission = '/manager/member/memberPointsHistory/getByPage*,/manager/member/memberPointsHistory/getMemberPointsHistoryVO*,/manager/member/memberPointsHistory/queryMemberPointsStatistics*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/member-center/member-points-history';

-- [用户中心] 用户评价 -> /member-center/member-evaluation
UPDATE li_menu SET permission = '/manager/member/evaluation/get*,/manager/member/evaluation/delete*,/manager/member/evaluation/updateTop*,/manager/member/evaluation/updateStatus*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/member-center/member-evaluation';

-- [用户中心] 用户地址 -> /member-center/member-address
UPDATE li_menu SET permission = '/manager/member/address*,/manager/passport/member*,/manager/setting/region/item*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/member-center/member-address';

-- [通知与消息] 站内通知 -> /message-center/member-notice
UPDATE li_menu SET permission = '/manager/message/memberNotice*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/message-center/member-notice';

-- [通知与消息] 通知日志 -> /message-center/member-notice-log
UPDATE li_menu SET permission = '/manager/message/memberNoticeLog/get*,/manager/message/memberNoticeLog/delByIds*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/message-center/member-notice-log';

-- [通知与消息] 短信签名 -> /message-center/sms-sign
UPDATE li_menu SET permission = '/manager/sms/sign*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/message-center/sms-sign';

-- [通知与消息] 短信模板 -> /message-center/sms-template
UPDATE li_menu SET permission = '/manager/sms/template*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/message-center/sms-template';

-- [通知与消息] 发送任务 -> /message-center/member-notice-sender
UPDATE li_menu SET permission = '/manager/message/memberNoticeSenter/get*,/manager/message/memberNoticeSenter/delByIds*,/manager/message/memberNoticeSenter/insertOrUpdate*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/message-center/member-notice-sender';

-- [通知与消息] 服务通知 -> /message-center/service-notice
UPDATE li_menu SET permission = '/manager/message/serviceNotice*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/message-center/service-notice';

-- [通知与消息] 消息发送管理 -> /message-center/message-channel
UPDATE li_menu SET permission = '/manager/other/message*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/message-center/message-channel';

-- [通知与消息] 客户消息 -> /message-center/member-message
UPDATE li_menu SET permission = '/manager/other/memberMessage*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/message-center/member-message';

-- [通知与消息] 店铺消息 -> /message-center/store-message
UPDATE li_menu SET permission = '/manager/other/storeMessage*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/message-center/store-message';

-- [内容与运营] 首页配置 -> /content-center/home-config
UPDATE li_menu SET permission = '/manager/other/platformHomeConfig*,/manager/goods/category/allChildren*,/manager/goods/goods/wholesale/list*,/manager/goods/goods/get*,/manager/other/special*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/content-center/home-config';

-- [内容与运营] 帮助与公告 -> /content-center/article
UPDATE li_menu SET permission = '/manager/other/article*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/content-center/article';

-- [内容与运营] 文章分类 -> /content-center/article-category
UPDATE li_menu SET permission = '/manager/other/articleCategory*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/content-center/article-category';

-- [内容与运营] 敏感词管理 -> /content-center/sensitive-words
UPDATE li_menu SET permission = '/manager/other/sensitiveWords*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/content-center/sensitive-words';

-- [内容与运营] 自定义词库 -> /content-center/custom-words
UPDATE li_menu SET permission = '/manager/other/customWords*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/content-center/custom-words';

-- [内容与运营] 热词治理 -> /content-center/hot-words-manage
UPDATE li_menu SET permission = '/manager/hotwords/hotwords*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/content-center/hot-words-manage';

-- [内容与运营] APP版本 -> /content-center/app-version
UPDATE li_menu SET permission = '/manager/other/appVersion*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/content-center/app-version';

-- [系统支撑] 验证码资源 -> /support-center/verification-source
UPDATE li_menu SET permission = '/manager/other/verificationSource*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/support-center/verification-source';

-- [系统支撑] 区域管理 -> /support-center/region-manage
UPDATE li_menu SET permission = '/manager/setting/region*,/common/common/region/allCity*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/support-center/region-manage';

-- [系统支撑] 物流公司 -> /support-center/logistics-manage
UPDATE li_menu SET permission = '/manager/other/logistics*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/support-center/logistics-manage';

-- [系统支撑] 售后原因 -> /support-center/after-sale-reason
UPDATE li_menu SET permission = '/manager/order/afterSaleReason*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/support-center/after-sale-reason';

-- [系统支撑] 意见反馈 -> /support-center/feedback-manage
UPDATE li_menu SET permission = '/manager/other/feedback*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/support-center/feedback-manage';

-- [系统支撑] 配置日志 -> /support-center/setting-log
UPDATE li_menu SET permission = '/manager/setting/log*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/support-center/setting-log';

-- [系统与权限] 平台菜单权限 -> /merchant-grant/menu-permission
UPDATE li_menu SET permission = '/manager/permission/menu*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/merchant-grant/menu-permission';

-- [系统与权限] 平台角色权限 -> /merchant-grant/role-permission
UPDATE li_menu SET permission = '/manager/permission/role*,/manager/permission/menu/tree*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/merchant-grant/role-permission';

-- [系统与权限] 组织架构配置 -> /merchant-grant/department-manage
UPDATE li_menu SET permission = '/manager/permission/department*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/merchant-grant/department-manage';

-- [系统与权限] 平台后台账号 -> /merchant-grant/backend-account
UPDATE li_menu SET permission = '/manager/passport/user*,/manager/permission/role*,/common/common/upload/file*,/manager/permission/department*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/merchant-grant/backend-account';

-- [系统与权限] 店铺菜单资源 -> /merchant-grant/store-menu
UPDATE li_menu SET permission = '/manager/permission/storeMenu*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/merchant-grant/store-menu';

-- [系统设置] 预警设置 -> /system/warning-setting
UPDATE li_menu SET permission = '/manager/message/serviceNotice*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/system/warning-setting';

-- [系统设置] 系统维护记录 -> /system/maintenance-log
UPDATE li_menu SET permission = '/manager/setting/log*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/system/maintenance-log';

-- [系统设置] 操作日志 -> /system/operation-log
UPDATE li_menu SET permission = '/manager/setting/log*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/system/operation-log';

-- [系统设置] 平台账号台账 -> /system/user
UPDATE li_menu SET permission = '/manager/passport/user*,/manager/permission/role*,/common/common/upload/file*,/manager/permission/department*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/system/user';

-- [系统设置] 角色权限配置 -> /system/role
UPDATE li_menu SET permission = '/manager/permission/role*,/manager/permission/menu/tree*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/system/role';

-- [系统设置] 组织架构配置 -> /system/dept
UPDATE li_menu SET permission = '/manager/permission/department*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/system/dept';

-- [系统设置] 菜单权限配置 -> /system/menu
UPDATE li_menu SET permission = '/manager/permission/menu*' WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 AND path = '/system/menu';

SELECT CAST(id AS CHAR) AS id, title, path, permission FROM li_menu WHERE id BETWEEN 3062200000000000001 AND 3062200000000010110 ORDER BY level, sort_order, id;

-- matched routes with permission fill: 105/105

