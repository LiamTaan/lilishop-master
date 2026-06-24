ALTER TABLE li_store
    ADD COLUMN IF NOT EXISTS agent_level VARCHAR(32) NULL COMMENT '代理等级',
    ADD COLUMN IF NOT EXISTS agent_region_id VARCHAR(64) NULL COMMENT '代理区域ID',
    ADD COLUMN IF NOT EXISTS agent_region_name VARCHAR(255) NULL COMMENT '代理区域名称';

ALTER TABLE li_store_detail
    ADD COLUMN IF NOT EXISTS agent_level VARCHAR(32) NULL COMMENT '代理等级',
    ADD COLUMN IF NOT EXISTS agent_region_id VARCHAR(64) NULL COMMENT '代理区域ID',
    ADD COLUMN IF NOT EXISTS agent_region_name VARCHAR(255) NULL COMMENT '代理区域名称';

ALTER TABLE agent_role_relation
    ADD COLUMN IF NOT EXISTS agent_level VARCHAR(32) NULL COMMENT '代理等级';

UPDATE li_store
SET store_type = 'SUPPLIER'
WHERE store_type IS NULL OR store_type = '';

UPDATE li_store_detail d
INNER JOIN li_store s ON s.id = d.store_id
SET d.store_type = s.store_type
WHERE d.store_type IS NULL OR d.store_type = '';

UPDATE li_store_detail d
INNER JOIN li_store s ON s.id = d.store_id
SET d.agent_level = s.agent_level,
    d.agent_region_id = s.agent_region_id,
    d.agent_region_name = s.agent_region_name
WHERE (d.agent_level IS NULL OR d.agent_level = '')
   OR (d.agent_region_id IS NULL OR d.agent_region_id = '')
   OR (d.agent_region_name IS NULL OR d.agent_region_name = '');

UPDATE agent_role_relation
SET agent_level = 'WHOLESALER'
WHERE agent_level IS NULL OR agent_level = '';
