SET NAMES utf8mb4;

-- 批发商城正式交付后台菜单清理
-- 目标：
-- 1. 下线页面装修 / 专题 / 快捷导航等黑箱配置入口
-- 2. 下线旧分销、微信模板、视频号、直播、ES 运维等非批发主链路菜单
-- 3. 仅影响 li_menu / li_role_menu，不改动业务数据表

UPDATE li_menu
SET
  delete_flag = b'1',
  update_by = 'codex',
  update_time = CURRENT_TIMESTAMP(6)
WHERE delete_flag = b'0'
  AND (
    front_route IN (
      '/goods-governance/home-category',
      '/content-center/page-data',
      '/content-center/shortcut-nav',
      '/content-center/special'
    )
    OR path IN (
      '/goods-governance/home-category',
      '/content-center/page-data',
      '/content-center/shortcut-nav',
      '/content-center/special'
    )
    OR permission LIKE '%/manager/other/shortcutNav%'
    OR permission LIKE '%/manager/distribution/%'
    OR permission LIKE '%/manager/wechat/%'
    OR permission LIKE '%/manager/wxchannels/%'
    OR permission LIKE '%/manager/broadcast/%'
    OR permission LIKE '%/manager/other/elasticsearch%'
    OR permission LIKE '%/manager/setting/messageTemplate%'
    OR permission LIKE '%/manager/setting/settingx%'
  );

DELETE FROM li_role_menu
WHERE menu_id IN (
  SELECT id
  FROM li_menu
  WHERE delete_flag = b'1'
    AND (
      front_route IN (
        '/goods-governance/home-category',
        '/content-center/page-data',
        '/content-center/shortcut-nav',
        '/content-center/special'
      )
      OR path IN (
        '/goods-governance/home-category',
        '/content-center/page-data',
        '/content-center/shortcut-nav',
        '/content-center/special'
      )
      OR permission LIKE '%/manager/other/shortcutNav%'
      OR permission LIKE '%/manager/distribution/%'
      OR permission LIKE '%/manager/wechat/%'
      OR permission LIKE '%/manager/wxchannels/%'
      OR permission LIKE '%/manager/broadcast/%'
      OR permission LIKE '%/manager/other/elasticsearch%'
      OR permission LIKE '%/manager/setting/messageTemplate%'
      OR permission LIKE '%/manager/setting/settingx%'
    )
);
