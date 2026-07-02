-- 供货商店铺 123123 商品初始化
-- 执行顺序：
-- 1. DB_INIT/01_full_base_schema.sql
-- 2. DB_INIT/02_startup_and_wholesale_patch.sql
-- 3. DB_INIT/03_goods_domain_reset_and_fresh_category_seed.sql
-- 4. 当前文件
--
-- 目标：
-- - 为截图中的供货商账号/店铺初始化一批批发商品
-- - 每个叶子分类初始化 1 个商品
-- - 同步初始化该批商品使用的分类规格绑定
-- - 同步初始化 SKU、商品相册、批发阶梯价

USE `lilishop`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

SET @seed_time = '2026-07-02 00:00:00.000000';
SET @target_store_name = '123123';
SET @target_mobile = '15811473413';

SET @target_store_id = (
  SELECT `id`
  FROM `li_store`
  WHERE `store_name` = @target_store_name
     OR `member_name` = @target_mobile
  ORDER BY `create_time` DESC
  LIMIT 1
);

SET @target_member_id = (
  SELECT `member_id`
  FROM `li_store`
  WHERE `id` = @target_store_id
  LIMIT 1
);

SET @target_member_name = (
  SELECT COALESCE(`member_name`, @target_mobile)
  FROM `li_store`
  WHERE `id` = @target_store_id
  LIMIT 1
);

SET @target_store_logo = (
  SELECT COALESCE(`store_logo`, '')
  FROM `li_store`
  WHERE `id` = @target_store_id
  LIMIT 1
);

SET @target_freight_template_id = (
  SELECT COALESCE(CAST(`id` AS CHAR), '0')
  FROM `li_freight_template`
  ORDER BY `id`
  LIMIT 1
);

SELECT
  @target_store_id AS target_store_id,
  @target_member_id AS target_member_id,
  @target_member_name AS target_member_name,
  @target_freight_template_id AS target_freight_template_id;

DROP TEMPORARY TABLE IF EXISTS `tmp_target_goods_ids`;
CREATE TEMPORARY TABLE `tmp_target_goods_ids` AS
SELECT `id`
FROM `li_goods`
WHERE `store_id` = @target_store_id;

DELETE FROM `li_goods_gallery`
WHERE `goods_id` IN (SELECT `id` FROM `tmp_target_goods_ids`);

DELETE FROM `li_wholesale`
WHERE `goods_id` IN (SELECT `id` FROM `tmp_target_goods_ids`);

DELETE FROM `li_goods_sku`
WHERE `goods_id` IN (SELECT `id` FROM `tmp_target_goods_ids`);

DELETE FROM `li_goods`
WHERE `id` IN (SELECT `id` FROM `tmp_target_goods_ids`);

DROP TEMPORARY TABLE IF EXISTS `tmp_target_goods_ids`;

DELETE FROM `li_category_specification`
WHERE `id` BETWEEN 3303000000000000001 AND 3303000000000000040;

DELETE FROM `li_specification`
WHERE `id` BETWEEN 3302000000000000001 AND 3302000000000000040;

DROP TEMPORARY TABLE IF EXISTS `tmp_supplier_goods_seed`;
CREATE TEMPORARY TABLE `tmp_supplier_goods_seed` (
  `seed_no` INT NOT NULL,
  `category_id` VARCHAR(32) NOT NULL,
  `goods_name` VARCHAR(64) NOT NULL,
  `goods_unit` VARCHAR(16) NOT NULL,
  `selling_point` VARCHAR(128) NOT NULL,
  `spec_name` VARCHAR(32) NOT NULL,
  `spec_values` VARCHAR(255) NOT NULL,
  `sku_value` VARCHAR(64) NOT NULL,
  `wholesale_num_1` INT NOT NULL,
  `wholesale_price_1` DECIMAL(10,2) NOT NULL,
  `wholesale_num_2` INT NOT NULL,
  `wholesale_price_2` DECIMAL(10,2) NOT NULL,
  `cost_price` DECIMAL(10,2) NOT NULL,
  `stock_quantity` INT NOT NULL,
  `weight_value` DECIMAL(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

INSERT INTO `tmp_supplier_goods_seed` (
  `seed_no`, `category_id`, `goods_name`, `goods_unit`, `selling_point`,
  `spec_name`, `spec_values`, `sku_value`,
  `wholesale_num_1`, `wholesale_price_1`, `wholesale_num_2`, `wholesale_price_2`,
  `cost_price`, `stock_quantity`, `weight_value`
) VALUES
  (1,  '3101000000000010001', '烟台红富士苹果', '箱', '脆甜多汁，适合社区团购和门店日销', '规格', '5kg装,10kg装', '5kg装', 1, 49.80, 10, 46.80, 39.00, 180, 5.00),
  (2,  '3101000000000010002', '赣南脐橙', '箱', '果香浓郁，出汁率高，适合批发走量', '规格', '5kg装,9kg装', '5kg装', 1, 46.80, 10, 43.80, 36.50, 160, 5.00),
  (3,  '3101000000000010003', '泰国金枕榴莲', '箱', '进口爆品，适合精品水果档口', '规格', '2-3斤装,3-4斤装', '2-3斤装', 1, 138.00, 10, 132.00, 118.00, 60, 2.50),
  (4,  '3101000000000010004', '智利车厘子', '盒', '礼盒陈列友好，节庆走量稳定', '规格', '2kg礼盒,5kg箱装', '2kg礼盒', 1, 168.00, 10, 158.00, 142.00, 70, 2.00),
  (5,  '3101000000000010005', '奶油生菜', '袋', '叶片完整，适合门店冷藏陈列', '规格', '500g装,1kg装', '500g装', 1, 8.80, 10, 8.20, 6.50, 220, 0.50),
  (6,  '3101000000000010006', '上海青', '袋', '色泽翠绿，适合商超日配', '规格', '500g装,1kg装', '500g装', 1, 7.80, 10, 7.20, 5.80, 220, 0.50),
  (7,  '3101000000000010007', '黄心土豆', '袋', '耐储存，餐饮和零售兼顾', '规格', '5kg装,10kg装', '5kg装', 1, 18.80, 10, 17.60, 14.20, 180, 5.00),
  (8,  '3101000000000010008', '精品番茄', '箱', '果型均匀，适合切配与零售', '规格', '2.5kg装,5kg装', '2.5kg装', 1, 15.80, 10, 14.80, 11.90, 160, 2.50),
  (9,  '3101000000000010009', '冷鲜猪肉分割', '箱', '日常餐饮高频采购，冷链直发', '部位', '精修里脊,肋排段', '精修里脊', 1, 62.00, 10, 58.00, 49.00, 120, 2.00),
  (10, '3101000000000010010', '谷饲牛肉分割', '箱', '火锅和团膳通用，损耗可控', '部位', '牛腩块,牛腱子', '牛腩块', 1, 88.00, 10, 82.00, 71.00, 90, 2.00),
  (11, '3101000000000010011', '白条鸡', '袋', '适合菜市场和小餐饮档口', '加工方式', '冷鲜整鸡,冷冻整鸡', '冷鲜整鸡', 1, 32.80, 10, 30.80, 25.50, 140, 1.50),
  (12, '3101000000000010012', '农家鲜鸡蛋', '箱', '稳定供货，适合早餐档和商超', '装箱数', '30枚装,45枚装', '30枚装', 1, 19.80, 10, 18.60, 15.30, 200, 1.80),
  (13, '3101000000000010013', '活鲈鱼', '箱', '鲜活优先，适合水产档口补货', '鲜活状态', '鲜活装,冰鲜装', '鲜活装', 1, 42.00, 10, 39.80, 33.00, 110, 1.20),
  (14, '3101000000000010014', '南美白虾', '箱', '规格整齐，适合餐饮和火锅渠道', '鲜活状态', '冰鲜虾,冷冻虾', '冰鲜虾', 1, 58.00, 10, 55.00, 46.00, 120, 1.00),
  (15, '3101000000000010015', '挪威三文鱼中段', '盒', '高复购刺身单品，适合精品冷鲜', '规格', '500g装,1kg装', '500g装', 1, 76.00, 10, 72.00, 61.00, 80, 0.50),
  (16, '3101000000000010016', '淡晒海带结', '袋', '干货耐储，适合社区团购拼单', '规格', '1kg装,5kg装', '1kg装', 1, 16.80, 10, 15.80, 12.50, 140, 1.00),
  (17, '3101000000000010017', '常温纯牛奶', '箱', '早餐高频单品，适合整箱铺货', '包装形式', '12盒装,24盒装', '12盒装', 1, 45.00, 10, 42.00, 35.00, 180, 3.00),
  (18, '3101000000000010018', '早餐黄油片', '盒', '烘焙档口常用，冷链配送稳定', '包装形式', '250g盒装,500g盒装', '250g盒装', 1, 18.80, 10, 17.60, 13.90, 120, 0.25),
  (19, '3101000000000010019', '全麦吐司', '箱', '便利店早餐搭配，陈列转化高', '口味', '原味,全麦', '全麦', 1, 12.80, 10, 11.80, 9.50, 150, 0.45),
  (20, '3101000000000010020', '鲜奶瑞士卷', '盒', '甜品柜台引流款，适合节假日促销', '口味', '原味,巧克力', '原味', 1, 22.80, 10, 21.20, 16.80, 100, 0.50),
  (21, '3101000000000010021', '玉米脆片', '箱', '零食组合陈列友好，动销稳定', '风味', '原味,烧烤味', '原味', 1, 26.80, 10, 24.80, 19.60, 160, 2.00),
  (22, '3101000000000010022', '苏打饼干', '箱', '办公室和家庭常备，适合整箱批发', '风味', '原味,芝麻味', '原味', 1, 24.80, 10, 23.20, 18.40, 160, 2.20),
  (23, '3101000000000010023', '黑巧克力', '箱', '高毛利零食，适合精品超市陈列', '风味', '70%可可,85%可可', '70%可可', 1, 39.80, 10, 37.20, 30.00, 120, 1.20),
  (24, '3101000000000010024', '风干牛肉干', '箱', '追剧和礼赠两用，适合节庆备货', '风味', '五香味,麻辣味', '五香味', 1, 48.00, 10, 45.00, 36.00, 110, 1.50),
  (25, '3101000000000010025', '韭菜猪肉水饺', '箱', '冻品刚需，门店和餐饮都好卖', '销售包装', '家庭装,餐饮装', '家庭装', 1, 28.80, 10, 27.00, 21.50, 160, 1.00),
  (26, '3101000000000010026', '黑芝麻汤圆', '箱', '节令常备款，适合整箱活动', '销售包装', '家庭装,整箱装', '家庭装', 1, 26.80, 10, 25.20, 19.80, 140, 1.00),
  (27, '3101000000000010027', '叉烧包', '箱', '早餐渠道通用，复热方便', '销售包装', '家庭装,餐饮装', '家庭装', 1, 23.80, 10, 22.20, 17.50, 130, 1.20),
  (28, '3101000000000010028', '宫保鸡丁半成品', '袋', '预制快手菜，适合夫妻店备货', '销售包装', '500g装,1kg装', '500g装', 1, 18.80, 10, 17.60, 13.90, 150, 0.50),
  (29, '3101000000000010029', '五常香米', '袋', '口碑稳定，适合社区团购和粮油店', '净含量', '5kg,10kg', '5kg', 1, 36.80, 10, 34.80, 28.50, 180, 5.00),
  (30, '3101000000000010030', '压榨花生油', '桶', '家庭刚需，高频复购', '净含量', '5L,10L', '5L', 1, 78.00, 10, 74.00, 64.00, 100, 5.00),
  (31, '3101000000000010031', '零添加生抽', '箱', '餐饮后厨高频耗材，整箱好走量', '包装类型', '500ml瓶装,5L桶装', '500ml瓶装', 1, 22.80, 10, 21.20, 16.80, 140, 3.00),
  (32, '3101000000000010032', '精制白砂糖', '袋', '烘焙和饮品档口通用，损耗低', '净含量', '2kg装,5kg装', '2kg装', 1, 15.80, 10, 14.80, 11.60, 160, 2.00),
  (33, '3101000000000010033', '天然饮用水', '箱', '工地团购和门店促销常备款', '容量规格', '550ml*24,4.5L*4', '550ml*24', 1, 26.00, 10, 24.50, 19.80, 220, 13.20),
  (34, '3101000000000010034', '冰红茶', '箱', '商超冰柜和餐饮渠道都适配', '容量规格', '500ml*15,1L*12', '500ml*15', 1, 35.80, 10, 33.80, 27.50, 180, 7.50),
  (35, '3101000000000010035', '清爽拉格啤酒', '箱', '夜宵和烧烤档口高频畅销', '容量规格', '500ml*12,500ml*24', '500ml*12', 1, 42.80, 10, 39.80, 32.00, 140, 6.00),
  (36, '3101000000000010036', '绵柔白酒', '箱', '宴席和礼赠渠道兼顾', '酒精度', '42度,52度', '42度', 1, 128.00, 10, 118.00, 96.00, 90, 6.50),
  (37, '3101000000000010037', '原木抽纸', '箱', '家清高频品，适合团购拼箱', '纸张层数', '3层24包,4层24包', '3层24包', 1, 29.80, 10, 27.80, 22.50, 180, 2.20),
  (38, '3101000000000010038', '去屑洗发露', '箱', '日化标品，适合便利店和商超补货', '功效', '清爽去屑,滋润柔顺', '清爽去屑', 1, 34.80, 10, 32.80, 26.20, 130, 4.00),
  (39, '3101000000000010039', '柠檬洗洁精', '箱', '家清爆款，适合门店活动堆头', '香型', '柠檬香,青柠香', '柠檬香', 1, 18.80, 10, 17.60, 13.80, 160, 3.00),
  (40, '3101000000000010040', '加厚垃圾袋', '箱', '高复购日杂品，社区店动销稳定', '型号', '中号50只,大号50只', '中号50只', 1, 16.80, 10, 15.60, 12.40, 180, 1.50);

INSERT INTO `li_specification` (
  `id`, `create_by`, `create_time`, `delete_flag`, `update_by`, `update_time`,
  `spec_name`, `store_id`, `spec_value`
)
SELECT
  3302000000000000000 + `seed_no`,
  @target_member_name,
  @seed_time,
  b'0',
  @target_member_name,
  @seed_time,
  `spec_name`,
  '0',
  `spec_values`
FROM `tmp_supplier_goods_seed`;

INSERT INTO `li_category_specification` (
  `id`, `create_by`, `create_time`, `delete_flag`, `update_by`, `update_time`,
  `category_id`, `specification_id`
)
SELECT
  3303000000000000000 + `seed_no`,
  @target_member_name,
  @seed_time,
  b'0',
  @target_member_name,
  @seed_time,
  `category_id`,
  3302000000000000000 + `seed_no`
FROM `tmp_supplier_goods_seed`;

INSERT INTO `li_goods` (
  `id`, `create_by`, `create_time`, `delete_flag`, `update_by`, `update_time`,
  `auth_message`, `brand_id`, `buy_count`, `category_path`, `comment_num`, `cost`,
  `goods_name`, `goods_unit`, `goods_video`, `grade`, `intro`, `auth_flag`,
  `market_enable`, `mobile_intro`, `original`, `price`, `quantity`, `recommend`,
  `sales_model`, `self_operated`, `store_id`, `store_name`, `source_store_id`,
  `source_store_name`, `source_goods_id`, `selling_point`, `shop_category_path`,
  `small`, `sn`, `barcode`, `template_id`, `thumbnail`, `under_message`, `weight`,
  `store_category_path`, `big`, `params`, `goods_type`,
  `scheduled_upper_time`, `scheduled_upper_reason`, `scheduled_down_time`, `scheduled_down_reason`
)
SELECT
  3305000000000000000 + s.`seed_no`,
  @target_member_name,
  @seed_time,
  b'0',
  @target_member_name,
  @seed_time,
  NULL,
  '0',
  0,
  CONCAT(root.`id`, ',', parent.`id`, ',', leaf.`id`),
  0,
  s.`cost_price`,
  s.`goods_name`,
  s.`goods_unit`,
  NULL,
  100.00,
  CONCAT('<p>', s.`goods_name`, '，', s.`selling_point`, '。</p>'),
  'PASS',
  'UPPER',
  CONCAT('<p>', s.`goods_name`, '，', s.`selling_point`, '。</p>'),
  @target_store_logo,
  s.`wholesale_price_1`,
  s.`stock_quantity`,
  b'1',
  'WHOLESALE',
  b'0',
  @target_store_id,
  @target_store_name,
  NULL,
  NULL,
  NULL,
  s.`selling_point`,
  NULL,
  @target_store_logo,
  CONCAT('SPG', LPAD(s.`seed_no`, 4, '0')),
  CONCAT('BCG', LPAD(s.`seed_no`, 6, '0')),
  @target_freight_template_id,
  @target_store_logo,
  NULL,
  s.`weight_value`,
  NULL,
  @target_store_logo,
  '[]',
  'PHYSICAL_GOODS',
  NULL,
  NULL,
  NULL,
  NULL
FROM `tmp_supplier_goods_seed` s
INNER JOIN `li_category` leaf ON leaf.`id` = s.`category_id`
INNER JOIN `li_category` parent ON parent.`id` = leaf.`parent_id`
INNER JOIN `li_category` root ON root.`id` = parent.`parent_id`
WHERE @target_store_id IS NOT NULL;

INSERT INTO `li_goods_sku` (
  `id`, `create_by`, `create_time`, `delete_flag`, `update_by`, `update_time`,
  `auth_message`, `big`, `brand_id`, `buy_count`, `virtual_sales`, `category_path`,
  `comment_num`, `cost`, `freight_payer`, `freight_template_id`, `goods_id`,
  `goods_name`, `goods_unit`, `goods_video`, `grade`, `intro`, `auth_flag`,
  `promotion_flag`, `market_enable`, `mobile_intro`, `original`, `price`,
  `promotion_price`, `quantity`, `recommend`, `sales_model`, `self_operated`,
  `store_id`, `store_name`, `source_store_id`, `source_store_name`,
  `source_goods_id`, `source_sku_id`, `selling_point`, `small`, `sn`, `barcode`,
  `specs`, `template_id`, `thumbnail`, `under_message`, `view_count`, `weight`,
  `simple_specs`, `store_category_path`, `goods_type`, `alert_quantity`
)
SELECT
  3306000000000000000 + s.`seed_no`,
  @target_member_name,
  @seed_time,
  b'0',
  @target_member_name,
  @seed_time,
  NULL,
  @target_store_logo,
  '0',
  0,
  0,
  CONCAT(root.`id`, ',', parent.`id`, ',', leaf.`id`),
  0,
  s.`cost_price`,
  NULL,
  @target_freight_template_id,
  3305000000000000000 + s.`seed_no`,
  CONCAT(s.`goods_name`, ' ', s.`sku_value`),
  s.`goods_unit`,
  NULL,
  100.00,
  CONCAT('<p>', s.`goods_name`, '，', s.`selling_point`, '。</p>'),
  'PASS',
  b'0',
  'UPPER',
  CONCAT('<p>', s.`goods_name`, '，', s.`selling_point`, '。</p>'),
  @target_store_logo,
  s.`wholesale_price_1`,
  NULL,
  s.`stock_quantity`,
  b'1',
  'WHOLESALE',
  b'0',
  @target_store_id,
  @target_store_name,
  NULL,
  NULL,
  NULL,
  NULL,
  s.`selling_point`,
  @target_store_logo,
  CONCAT('SPGSKU', LPAD(s.`seed_no`, 4, '0')),
  CONCAT('BCS', LPAD(s.`seed_no`, 6, '0')),
  JSON_OBJECT(s.`spec_name`, s.`sku_value`),
  @target_freight_template_id,
  @target_store_logo,
  NULL,
  0,
  s.`weight_value`,
  CONCAT(' ', s.`sku_value`),
  NULL,
  'PHYSICAL_GOODS',
  10
FROM `tmp_supplier_goods_seed` s
INNER JOIN `li_category` leaf ON leaf.`id` = s.`category_id`
INNER JOIN `li_category` parent ON parent.`id` = leaf.`parent_id`
INNER JOIN `li_category` root ON root.`id` = parent.`parent_id`
WHERE @target_store_id IS NOT NULL;

INSERT INTO `li_goods_gallery` (
  `id`, `create_by`, `goods_id`, `is_default`, `original`, `small`, `sort`, `thumbnail`
)
SELECT
  3308000000000000000 + `seed_no`,
  @target_member_name,
  3305000000000000000 + `seed_no`,
  1,
  @target_store_logo,
  @target_store_logo,
  0,
  @target_store_logo
FROM `tmp_supplier_goods_seed`
WHERE @target_store_id IS NOT NULL;

INSERT INTO `li_wholesale` (
  `id`, `create_by`, `create_time`, `delete_flag`, `update_by`, `update_time`,
  `price`, `goods_id`, `sku_id`, `num`, `template_id`
)
SELECT
  3307000000000000000 + `seed_no` * 10 + 1,
  @target_member_name,
  @seed_time,
  b'0',
  @target_member_name,
  @seed_time,
  `wholesale_price_1`,
  3305000000000000000 + `seed_no`,
  3306000000000000000 + `seed_no`,
  `wholesale_num_1`,
  CONCAT('WHOLESALE_', 3305000000000000000 + `seed_no`)
FROM `tmp_supplier_goods_seed`
WHERE @target_store_id IS NOT NULL;

INSERT INTO `li_wholesale` (
  `id`, `create_by`, `create_time`, `delete_flag`, `update_by`, `update_time`,
  `price`, `goods_id`, `sku_id`, `num`, `template_id`
)
SELECT
  3307000000000000000 + `seed_no` * 10 + 2,
  @target_member_name,
  @seed_time,
  b'0',
  @target_member_name,
  @seed_time,
  `wholesale_price_2`,
  3305000000000000000 + `seed_no`,
  3306000000000000000 + `seed_no`,
  `wholesale_num_2`,
  CONCAT('WHOLESALE_', 3305000000000000000 + `seed_no`)
FROM `tmp_supplier_goods_seed`
WHERE @target_store_id IS NOT NULL;

SELECT
  COUNT(*) AS seeded_goods_count
FROM `li_goods`
WHERE `store_id` = @target_store_id
  AND `id` BETWEEN 3305000000000000001 AND 3305000000000000040;

SELECT
  COUNT(*) AS seeded_sku_count
FROM `li_goods_sku`
WHERE `goods_id` BETWEEN 3305000000000000001 AND 3305000000000000040;

SELECT
  COUNT(*) AS seeded_wholesale_count
FROM `li_wholesale`
WHERE `goods_id` BETWEEN 3305000000000000001 AND 3305000000000000040;

DROP TEMPORARY TABLE IF EXISTS `tmp_supplier_goods_seed`;

SET FOREIGN_KEY_CHECKS = 1;
