SET NAMES utf8mb4;

START TRANSACTION;

-- 本补丁用于清理本次已删除功能对应的历史菜单、角色菜单绑定、设置项和会员成长数据。
-- 注意：
-- 1. WECHAT_CONNECT 配置未删除，因为当前代码仍用于微信小程序/微信相关内部链路，不属于“后台微信登录配置页”范畴。
-- 2. 本补丁不删除表结构，只清理废弃功能数据，避免影响现有代码字段映射。

DROP TEMPORARY TABLE IF EXISTS tmp_removed_menu_ids;

CREATE TEMPORARY TABLE tmp_removed_menu_ids AS
SELECT m.id
FROM li_menu m
WHERE m.delete_flag = b'0'
  AND (
    m.path IN (
      '/platform-config/experience-setting',
      '/platform-config/wechat-connect-setting',
      '/platform-config/qq-connect-setting',
      '/platform-config/hot-words-setting',
      '/member-center/member-grade',
      '/content-center/hot-words-manage',
      'member-grade',
      'member-grade-experience',
      'member-grade-experience-log',
      'hotKeyWord'
    )
    OR m.front_route IN (
      'page/article-manage/hotWords'
    )
  );

-- 清理菜单角色绑定
DELETE FROM li_role_menu
WHERE menu_id IN (SELECT id FROM tmp_removed_menu_ids);

-- 清理菜单本体
DELETE FROM li_menu
WHERE id IN (SELECT id FROM tmp_removed_menu_ids);

DROP TEMPORARY TABLE IF EXISTS tmp_removed_menu_ids;

-- 清理已废弃设置项
DELETE FROM li_setting
WHERE id IN (
  'EXPERIENCE_SETTING',
  'HOT_WORDS',
  'QQ_CONNECT'
);

-- 清理会员成长体系数据
UPDATE li_member
SET grade_id = NULL,
    experience = 0
WHERE grade_id IS NOT NULL
   OR IFNULL(experience, 0) <> 0;

DELETE FROM li_member_grade_benefit_grant;
DELETE FROM li_member_experience_log;
DELETE FROM li_member_grade;

COMMIT;

-- 执行后可选自检：
-- SELECT id, path, title FROM li_menu
-- WHERE path IN (
--   '/platform-config/experience-setting',
--   '/platform-config/wechat-connect-setting',
--   '/platform-config/qq-connect-setting',
--   '/platform-config/hot-words-setting',
--   '/member-center/member-grade',
--   '/content-center/hot-words-manage',
--   'member-grade',
--   'member-grade-experience',
--   'member-grade-experience-log',
--   'hotKeyWord'
-- );
--
-- SELECT id FROM li_setting
-- WHERE id IN ('EXPERIENCE_SETTING', 'HOT_WORDS', 'QQ_CONNECT');
--
-- SELECT COUNT(*) AS grade_count FROM li_member_grade;
-- SELECT COUNT(*) AS grade_grant_count FROM li_member_grade_benefit_grant;
-- SELECT COUNT(*) AS experience_log_count FROM li_member_experience_log;
