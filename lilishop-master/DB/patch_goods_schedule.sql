-- 商品定时上下架字段补充
ALTER TABLE `li_goods`
    ADD COLUMN `scheduled_upper_time` DATETIME NULL COMMENT '计划上架时间',
    ADD COLUMN `scheduled_upper_reason` VARCHAR(255) NULL COMMENT '计划上架原因',
    ADD COLUMN `scheduled_down_time` DATETIME NULL COMMENT '计划下架时间',
    ADD COLUMN `scheduled_down_reason` VARCHAR(255) NULL COMMENT '计划下架原因';
