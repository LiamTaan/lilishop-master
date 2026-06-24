-- Execute this file after 03_procurement_schema_patch.sql
-- Important:
-- 1. Run the DB_INIT chain from repository root so the SOURCE paths below resolve correctly.
-- 2. This file folds the current wholesale manager menu / permission alignment into empty-db initialization.

SOURCE DB/patch_wholesale_manager_menu_route_baseline.sql;
SOURCE DB/patch_wholesale_manager_menu_permission_fill.sql;
SOURCE DB/patch_wholesale_manager_legacy_menu_archive.sql;
SOURCE DB/patch_wholesale_manager_archived_menu_purge.sql;
SOURCE DB/patch_manager_role_baseline.sql;
