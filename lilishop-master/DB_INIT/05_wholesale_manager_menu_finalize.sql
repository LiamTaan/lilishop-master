-- Execute this file after 04_wholesale_manager_menu_alignment.sql
-- Important:
-- 1. Run the DB_INIT chain from repository root so the SOURCE paths below resolve correctly.
-- 2. This file closes the remaining schema + menu gaps for the wholesale app/admin plan.

SOURCE DB/patch_goods_barcode_columns.sql;
SOURCE DB/patch_wholesale_manager_menu_finalize.sql;
