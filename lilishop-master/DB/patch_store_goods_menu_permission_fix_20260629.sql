UPDATE li_store_menu
SET permission = '/store/goods/goods/list*,/store/goods/goods/goodsNumber*,/store/goods/goods/get*'
WHERE id = 1349237207378714624
  AND (permission IS NULL OR permission = '' OR permission = 'null');

UPDATE li_store_menu
SET permission = '/store/goods/goods/list/stock*,/store/goods/goods/sku/list*,/store/goods/goods/update/stocks*,/store/goods/goods/update/alert/stocks*,/store/goods/goods/batch/update/alert/stocks*,/store/goods/goods/queryExportStock*,/store/goods/goods/importStockExcel*'
WHERE id = 1349237928434098177
  AND (permission IS NULL OR permission = '' OR permission = 'null');
