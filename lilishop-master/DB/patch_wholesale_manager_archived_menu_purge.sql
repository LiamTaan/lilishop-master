SET NAMES utf8mb4;

START TRANSACTION;

-- 清理本次菜单对齐过程中已归档的旧平台菜单角色绑定
DELETE FROM li_role_menu
WHERE menu_id IN (
  SELECT archived_menu.id_str
  FROM (
    SELECT CAST(id AS CHAR) AS id_str
    FROM li_menu
    WHERE delete_flag = b'1'
      AND update_by = 'codex'
  ) AS archived_menu
);

-- 清理本次菜单对齐过程中已归档的旧平台菜单实体
DELETE FROM li_menu
WHERE delete_flag = b'1'
  AND update_by = 'codex';

COMMIT;
