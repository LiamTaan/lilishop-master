-- 消息模板：不合并 li_notice_message 与 li_wechat_* 表结构；以 scene_code 关联。
-- 扩展站内信邮箱渠道；微信模板表增加 scene_code（与 NoticeMessageNodeEnum.name 一致）。
-- 可重复执行：通过 information_schema 判断列是否存在。

SET NAMES utf8mb4;

-- li_notice_message：场景码、邮箱
SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_notice_message' AND COLUMN_NAME = 'scene_code');
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_notice_message` ADD COLUMN `scene_code` varchar(64) NULL COMMENT ''业务场景编码，与 NoticeMessageNodeEnum.name 一致'' AFTER `variable`',
  'SELECT ''li_notice_message.scene_code exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_notice_message' AND COLUMN_NAME = 'email_status');
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_notice_message` ADD COLUMN `email_status` varchar(16) NOT NULL DEFAULT ''CLOSE'' COMMENT ''邮箱渠道 OPEN/CLOSE'' AFTER `scene_code`',
  'SELECT ''li_notice_message.email_status exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_notice_message' AND COLUMN_NAME = 'email_content');
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_notice_message` ADD COLUMN `email_content` varchar(2000) NULL COMMENT ''邮箱正文，空则与站内信同文'' AFTER `email_status`',
  'SELECT ''li_notice_message.email_content exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- li_wechat_message.scene_code
SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_wechat_message' AND COLUMN_NAME = 'scene_code');
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_wechat_message` ADD COLUMN `scene_code` varchar(64) NULL COMMENT ''关联站内信场景'' AFTER `remark`',
  'SELECT ''li_wechat_message.scene_code exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- li_wechat_mp_message.scene_code
SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'li_wechat_mp_message' AND COLUMN_NAME = 'scene_code');
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `li_wechat_mp_message` ADD COLUMN `scene_code` varchar(64) NULL COMMENT ''关联站内信场景'' AFTER `order_status`',
  'SELECT ''li_wechat_mp_message.scene_code exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 回填 notice.scene_code（与 NoticeMessageNodeEnum 文案一致，去掉换行）
UPDATE `li_notice_message` SET `scene_code` = CASE TRIM(REPLACE(REPLACE(`notice_node`, CHAR(10), ''), CHAR(13), ''))
  WHEN '订单提交成功通知' THEN 'ORDER_CREATE_SUCCESS'
  WHEN '订单取消成功通知' THEN 'ORDER_CANCEL_SUCCESS'
  WHEN '订单支付成功通知' THEN 'ORDER_PAY_SUCCESS'
  WHEN '支付失败自动退款通知' THEN 'ORDER_PAY_ERROR'
  WHEN '订单发货通知' THEN 'ORDER_DELIVER'
  WHEN '订单完成通知' THEN 'ORDER_COMPLETE'
  WHEN '订单评价提醒' THEN 'ORDER_EVALUATION'
  WHEN '售后提交成功通知' THEN 'AFTER_SALE_CREATE_SUCCESS'
  WHEN '退货审核通过通知' THEN 'RETURN_GOODS_PASS'
  WHEN '退款审核通过通知' THEN 'RETURN_MONEY_PASS'
  WHEN '退货审核未通过通知' THEN 'RETURN_GOODS_REFUSE'
  WHEN '退款审核未通过通知' THEN 'RETURN_MONEY_REFUSE'
  WHEN '退货物品签收通知' THEN 'AFTER_SALE_ROG_PASS'
  WHEN '退货物品拒收通知' THEN 'AFTER_SALE_ROG_REFUSE'
  WHEN '售后完成通知' THEN 'AFTER_SALE_COMPLETE'
  WHEN '开团成功通知' THEN 'PINTUAN_CREATE'
  WHEN '拼团失败通知' THEN 'PINTUAN_ERROR'
  WHEN '拼团成功通知' THEN 'PINTUAN_SUCCESS'
  WHEN '积分变更通知' THEN 'POINT_CHANGE'
  WHEN '余额账户变更通知' THEN 'WALLET_CHANGE'
  WHEN '余额提现申请提交成功通知' THEN 'WALLET_WITHDRAWAL_CREATE'
  WHEN '余额提现成功通知' THEN 'WALLET_WITHDRAWAL_SUCCESS'
  WHEN '余额提现申请失败通知' THEN 'WALLET_WITHDRAWAL_ERROR'
  WHEN '余额提现申请驳回通知' THEN 'WALLET_WITHDRAWAL_AUDIT_ERROR'
  WHEN '余额提现申请通过通知' THEN 'WALLET_WITHDRAWAL_AUDIT_SUCCESS'
  WHEN '微信提现成功通知' THEN 'WALLET_WITHDRAWAL_SUCCESS'
  WHEN '提现申请提交成功通知' THEN 'WALLET_WITHDRAWAL_CREATE'
  WHEN '提现申请驳回通知' THEN 'WALLET_WITHDRAWAL_AUDIT_ERROR'
  ELSE `scene_code`
END
WHERE (`scene_code` IS NULL OR `scene_code` = '');

-- 已存在微信数据时按名称或历史模糊关系补 scene_code（重新执行「同步模板」后会以代码为准）
UPDATE `li_wechat_message` w
INNER JOIN `li_notice_message` n ON n.`scene_code` IS NOT NULL AND n.`scene_code` != ''
  AND TRIM(w.`name`) = TRIM(REPLACE(REPLACE(n.`notice_node`, CHAR(10), ''), CHAR(13), ''))
SET w.`scene_code` = n.`scene_code`
WHERE w.`scene_code` IS NULL OR w.`scene_code` = '';

UPDATE `li_wechat_message` SET `scene_code` = 'ORDER_PAY_SUCCESS' WHERE (`scene_code` IS NULL OR `scene_code` = '') AND `name` = '订单支付成功通知';
UPDATE `li_wechat_message` SET `scene_code` = 'ORDER_DELIVER' WHERE (`scene_code` IS NULL OR `scene_code` = '') AND `name` = '订单发货';
UPDATE `li_wechat_message` SET `scene_code` = 'ORDER_COMPLETE' WHERE (`scene_code` IS NULL OR `scene_code` = '') AND `name` = '订单完成';

UPDATE `li_wechat_mp_message` w
INNER JOIN `li_notice_message` n ON n.`scene_code` IS NOT NULL AND n.`scene_code` != ''
  AND TRIM(w.`name`) = TRIM(REPLACE(REPLACE(n.`notice_node`, CHAR(10), ''), CHAR(13), ''))
SET w.`scene_code` = n.`scene_code`
WHERE w.`scene_code` IS NULL OR w.`scene_code` = '';

UPDATE `li_wechat_mp_message` SET `scene_code` = 'ORDER_PAY_SUCCESS' WHERE (`scene_code` IS NULL OR `scene_code` = '') AND `name` LIKE '%订单支付%';
UPDATE `li_wechat_mp_message` SET `scene_code` = 'ORDER_DELIVER' WHERE (`scene_code` IS NULL OR `scene_code` = '') AND `name` LIKE '%发货%';
UPDATE `li_wechat_mp_message` SET `scene_code` = 'ORDER_COMPLETE' WHERE (`scene_code` IS NULL OR `scene_code` = '') AND `name` LIKE '%订单完成%';

-- 已为「消息模版」菜单赋权的实例：补齐聚合接口权限（仅追加，不重复）
UPDATE `li_menu`
SET `permission` = TRIM(BOTH ',' FROM CONCAT(`permission`, ',/manager/setting/messageTemplate*'))
WHERE `path` = 'message-template'
  AND (`permission` IS NOT NULL AND `permission` != '' AND `permission` NOT LIKE '%/manager/setting/messageTemplate*%');
