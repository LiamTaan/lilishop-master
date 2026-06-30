INSERT INTO `li_menu` (
  `id`,`create_by`,`create_time`,`delete_flag`,`update_by`,`update_time`,
  `description`,`front_route`,`icon`,`level`,`name`,`parent_id`,`path`,
  `sort_order`,`title`,`front_component`,`permission`
)
SELECT
  '3062200000000010113',
  'admin',
  CURRENT_TIMESTAMP,
  b'0',
  'admin',
  CURRENT_TIMESTAMP,
  'wholesale-manager-route-baseline',
  '/support-center/freight-template-manage',
  '',
  '1',
  'SupportFreightTemplateManage',
  '3062200000000000012',
  '/support-center/freight-template-manage',
  3.50,
  '运费模板',
  NULL,
  '/manager/goods/freightTemplate*,/manager/store/store/all*,/common/common/region/allCity*'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1
  FROM `li_menu`
  WHERE `path` = '/support-center/freight-template-manage'
);

UPDATE `li_menu`
SET `permission` = '/manager/goods/freightTemplate*,/manager/store/store/all*,/common/common/region/allCity*'
WHERE `path` = '/support-center/freight-template-manage';
