SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 商品主条码 / SKU 条码补丁
-- 作用：
-- 1. 为 li_goods 增加商品主条码字段 barcode
-- 2. 为 li_goods_sku 增加 SKU 条码字段 barcode
-- 3. 为现有单规格商品补齐 SKU 条码默认值，便于扫码发布首版直接回查

SET @goods_barcode_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'li_goods'
    AND COLUMN_NAME = 'barcode'
);

SET @goods_barcode_sql = IF(
  @goods_barcode_exists = 0,
  'ALTER TABLE `li_goods` ADD COLUMN `barcode` varchar(64) DEFAULT NULL COMMENT ''商品主条码'' AFTER `sn`',
  'SELECT ''li_goods.barcode already exists'' AS message'
);
PREPARE goods_barcode_stmt FROM @goods_barcode_sql;
EXECUTE goods_barcode_stmt;
DEALLOCATE PREPARE goods_barcode_stmt;

SET @goods_sku_barcode_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'li_goods_sku'
    AND COLUMN_NAME = 'barcode'
);

SET @goods_sku_barcode_sql = IF(
  @goods_sku_barcode_exists = 0,
  'ALTER TABLE `li_goods_sku` ADD COLUMN `barcode` varchar(64) DEFAULT NULL COMMENT ''商品规格条码'' AFTER `sn`',
  'SELECT ''li_goods_sku.barcode already exists'' AS message'
);
PREPARE goods_sku_barcode_stmt FROM @goods_sku_barcode_sql;
EXECUTE goods_sku_barcode_stmt;
DEALLOCATE PREPARE goods_sku_barcode_stmt;

UPDATE `li_goods_sku` sku
JOIN `li_goods` goods ON goods.`id` = sku.`goods_id`
SET sku.`barcode` = goods.`barcode`
WHERE (sku.`barcode` IS NULL OR sku.`barcode` = '')
  AND goods.`barcode` IS NOT NULL
  AND goods.`barcode` <> '';

SET FOREIGN_KEY_CHECKS = 1;
