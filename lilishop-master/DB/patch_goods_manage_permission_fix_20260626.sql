UPDATE li_menu
SET permission = '/manager/goods/goods*,/manager/goods/goodsUnit*,/manager/goods/categoryParameters*,/manager/goods/freightTemplate/store*,/manager/goods/category/allChildren*,/manager/goods/brand/all*,/manager/store/store/all*,/common/common/upload/file*'
WHERE path = '/goods-governance/goods-manage';

SELECT id, title, path, permission
FROM li_menu
WHERE path = '/goods-governance/goods-manage';
