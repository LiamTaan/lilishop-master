SET NAMES utf8mb4;

-- 逻辑归档旧版平台菜单树，避免与当前批发商城管理端菜单并存
-- 说明：
-- 1. 依赖 MenuMapper / StoreMenuMapper 已补 delete_flag = 0 过滤
-- 2. 建议先执行 patch_wholesale_manager_menu_route_baseline.sql
-- 3. 本脚本只逻辑归档旧树，不直接物理删除历史菜单与 role_menu 绑定

WITH RECURSIVE legacy_roots AS (
  SELECT CAST(id AS CHAR) AS id
  FROM li_menu
  WHERE parent_id = '0'
    AND id NOT BETWEEN 3062200000000000001 AND 3062200000000010097
    AND title IN ('会员', '订单', '商品', '促销', '店铺', '运营', '统计', '设置', '日志')
),
legacy_tree AS (
  SELECT id FROM legacy_roots
  UNION ALL
  SELECT CAST(child.id AS CHAR) AS id
  FROM li_menu child
  INNER JOIN legacy_tree parent
    ON child.parent_id = parent.id
)
UPDATE li_menu target
INNER JOIN (
  SELECT DISTINCT id
  FROM legacy_tree
) archived
  ON CAST(target.id AS CHAR) = archived.id
SET
  target.delete_flag = b'1',
  target.update_by = 'codex',
  target.update_time = CURRENT_TIMESTAMP(6)
WHERE target.id NOT BETWEEN 3062200000000000001 AND 3062200000000010097;

SELECT
  CAST(id AS CHAR) AS id,
  title,
  parent_id,
  level,
  delete_flag
FROM li_menu
WHERE parent_id = '0'
ORDER BY sort_order, id;
