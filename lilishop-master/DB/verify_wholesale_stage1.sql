SET NAMES utf8mb4;

-- 1. 校验代理商阶段一表结构
SHOW TABLES LIKE 'agent_role_relation';
SHOW TABLES LIKE 'agent_store_bind';

SHOW COLUMNS FROM `li_store` LIKE 'apply_type';
SHOW COLUMNS FROM `li_store` LIKE 'store_type';
SHOW COLUMNS FROM `li_store` LIKE 'audit_status';
SHOW COLUMNS FROM `li_store` LIKE 'audit_remark';

SHOW COLUMNS FROM `li_store_detail` LIKE 'apply_type';
SHOW COLUMNS FROM `li_store_detail` LIKE 'store_type';
SHOW COLUMNS FROM `li_store_detail` LIKE 'company_name';
SHOW COLUMNS FROM `li_store_detail` LIKE 'company_address';
SHOW COLUMNS FROM `li_store_detail` LIKE 'authorization_url';

-- 2. 校验菜单与权限初始化
SELECT id, title, path, parent_id, permission
FROM `li_menu`
WHERE id IN ('2061700000000000001','2061700000000000002');

SELECT rm.role_id, rm.menu_id, m.title
FROM `li_role_menu` rm
LEFT JOIN `li_menu` m ON rm.menu_id = m.id
WHERE rm.menu_id IN ('2061700000000000001','2061700000000000002')
ORDER BY rm.role_id, rm.menu_id;

-- 3. 校验代理商身份与绑定数据
SELECT id, member_id, region_id, status, delete_flag, create_time
FROM `agent_role_relation`
ORDER BY create_time DESC
LIMIT 20;

SELECT id, agent_member_id, store_id, bind_status, audit_status, audit_remark, unbind_time, create_time
FROM `agent_store_bind`
ORDER BY create_time DESC
LIMIT 20;

-- 4. 校验店铺审核扩展数据
SELECT id, member_id, store_name, apply_type, store_type, audit_status, audit_remark, store_disable
FROM `li_store`
ORDER BY create_time DESC
LIMIT 20;

SELECT store_id, company_name, company_address, apply_type, store_type, legal_name, legal_id
FROM `li_store_detail`
ORDER BY update_time DESC
LIMIT 20;
