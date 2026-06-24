#!/usr/bin/env python3
from __future__ import annotations

import argparse
import os
import re
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable

try:
    import pymysql
except ImportError as exc:  # pragma: no cover
    raise SystemExit(
        "Missing dependency: pymysql. Install it before running this audit."
    ) from exc


BACKEND_ROOT = Path(__file__).resolve().parents[1]
FRONTEND_ROOT = BACKEND_ROOT.parent / "lilishop-admin"
BUSINESS_ROUTE_FILE = FRONTEND_ROOT / "src/router/business-routes.ts"
SRC_ROOT = FRONTEND_ROOT / "src"
API_ROOT = SRC_ROOT / "api"
ROUTER_MODULE_ROOT = FRONTEND_ROOT / "src/router/modules"
MENU_SYNC_FILE = FRONTEND_ROOT / "src/router/menu-sync.ts"

ALLOWED_API_PREFIXES = (
    "/manager/",
    "/seller/",
    "/buyer/",
    "/common/",
    "http://",
    "https://",
)


@dataclass
class RouteModule:
    file_name: str
    top_title: str
    top_path: str
    child_titles: list[str]
    hidden_child_titles: list[str]


def load_current_manager_route_modules() -> list[RouteModule]:
    import_pattern = re.compile(
        r'import\s+\w+\s+from\s+"\.\/modules\/(?P<name>[^"]+)";'
    )
    title_pattern = re.compile(r'title:\s*"([^"]+)"')
    path_pattern = re.compile(r'path:\s*"([^"]+)"')
    child_block_pattern = re.compile(
        r'\{\s*path:\s*"([^"]+)"[\s\S]*?name:\s*"([^"]+)"[\s\S]*?meta:\s*\{([\s\S]*?)\}\s*\}',
        re.S,
    )
    child_title_pattern = re.compile(r'title:\s*"([^"]+)"')

    module_names = [
        match.group("name") + ".ts"
        for match in import_pattern.finditer(BUSINESS_ROUTE_FILE.read_text("utf-8"))
    ]

    modules: list[RouteModule] = []
    for module_name in module_names:
        module_path = ROUTER_MODULE_ROOT / module_name
        text = module_path.read_text("utf-8")
        titles = title_pattern.findall(text)
        paths = path_pattern.findall(text)
        if not titles or not paths:
            continue
        visible_child_titles: list[str] = []
        hidden_child_titles: list[str] = []
        for child_path, _child_name, child_meta_block in child_block_pattern.findall(text):
            if child_path == paths[0]:
                continue
            title_match = child_title_pattern.search(child_meta_block)
            if not title_match:
                continue
            if "showLink: false" in child_meta_block:
                hidden_child_titles.append(title_match.group(1))
            else:
                visible_child_titles.append(title_match.group(1))
        modules.append(
            RouteModule(
                file_name=module_name,
                top_title=titles[0],
                top_path=paths[0],
                child_titles=visible_child_titles,
                hidden_child_titles=hidden_child_titles,
            )
        )
    return modules


def scan_mock_imports() -> list[str]:
    results: list[str] = []
    for file_path in SRC_ROOT.rglob("*.*"):
        if not file_path.is_file():
            continue
        try:
            text = file_path.read_text("utf-8")
        except UnicodeDecodeError:
            continue
        if '@/api/mock' in text:
            results.append(str(file_path.relative_to(FRONTEND_ROOT)))
    return sorted(results)


def scan_hardcoded_super_routes() -> list[str]:
    results: list[str] = []
    for file_path in ROUTER_MODULE_ROOT.glob("*.ts"):
        text = file_path.read_text("utf-8")
        if 'roles: ["super"]' in text:
            results.append(str(file_path.relative_to(FRONTEND_ROOT)))
    return sorted(results)


def collect_route_paths() -> set[str]:
    route_paths: set[str] = set()
    path_pattern = re.compile(r'path:\s*"([^"]+)"')
    for file_path in ROUTER_MODULE_ROOT.glob("*.ts"):
        text = file_path.read_text("utf-8")
        route_paths.update(path_pattern.findall(text))
    return route_paths


def scan_invalid_menu_sync_aliases() -> list[tuple[str, str]]:
    route_paths = collect_route_paths()
    text = MENU_SYNC_FILE.read_text("utf-8")
    alias_pattern = re.compile(r'"([^"]+)":\s*"([^"]+)"')
    findings: list[tuple[str, str]] = []
    for alias_token, target_path in alias_pattern.findall(text):
        if not target_path.startswith("/"):
            continue
        if target_path not in route_paths:
            findings.append((alias_token, target_path))
    return sorted(findings)


def scan_suspicious_api_endpoints() -> list[tuple[str, str]]:
    request_pattern = re.compile(
        r'http\.request(?:<[^>]+>)?\(\s*"[^"]+"\s*,\s*"([^"]+)"',
        re.MULTILINE,
    )
    findings: list[tuple[str, str]] = []
    for file_path in API_ROOT.rglob("*.ts"):
        text = file_path.read_text("utf-8")
        for endpoint in request_pattern.findall(text):
            if endpoint.startswith(ALLOWED_API_PREFIXES):
                continue
            if endpoint.startswith("/"):
                findings.append((str(file_path.relative_to(FRONTEND_ROOT)), endpoint))
    return sorted(set(findings))


def print_section(title: str) -> None:
    print(f"\n=== {title} ===")


def print_items(items: Iterable[str], empty_text: str = "none") -> None:
    items = list(items)
    if not items:
        print(empty_text)
        return
    for item in items:
        print(f"- {item}")


def get_connection(args: argparse.Namespace):
    return pymysql.connect(
        host=args.host,
        port=args.port,
        user=args.user,
        password=args.password,
        database=args.database,
        charset="utf8mb4",
    )


def fetch_table_count(cursor, table_name: str) -> int:
    cursor.execute(f"SELECT COUNT(*) FROM {table_name}")
    return int(cursor.fetchone()[0])


def fetch_top_level_titles(cursor, table_name: str) -> list[str]:
    cursor.execute(
        f"""
        SELECT title
        FROM {table_name}
        WHERE (level = 0 OR parent_id = '0')
          AND delete_flag = b'0'
        ORDER BY CAST(COALESCE(sort_order, 0) AS DECIMAL(10,2)), id
        """
    )
    return [row[0] or "" for row in cursor.fetchall()]


def fetch_orphan_user_roles(cursor) -> list[tuple[str, int]]:
    cursor.execute(
        """
        SELECT ur.role_id, COUNT(*)
        FROM li_user_role ur
        LEFT JOIN li_role r ON CAST(r.id AS CHAR) = ur.role_id
        WHERE r.id IS NULL
        GROUP BY ur.role_id
        ORDER BY COUNT(*) DESC, ur.role_id
        """
    )
    return [(str(role_id), int(total)) for role_id, total in cursor.fetchall()]


def fetch_orphan_role_menus(cursor) -> list[tuple[str, int]]:
    cursor.execute(
        """
        SELECT rm.menu_id, COUNT(*)
        FROM li_role_menu rm
        LEFT JOIN li_menu m ON CAST(m.id AS CHAR) = rm.menu_id
        WHERE m.id IS NULL
        GROUP BY rm.menu_id
        ORDER BY COUNT(*) DESC, rm.menu_id
        """
    )
    return [(str(menu_id), int(total)) for menu_id, total in cursor.fetchall()]


def fetch_admin_role_fields(cursor) -> list[tuple[str, str, str]]:
    cursor.execute(
        """
        SELECT CAST(id AS CHAR), username, COALESCE(role_ids, '')
        FROM li_admin_user
        ORDER BY id
        """
    )
    return [(str(row_id), username or "", role_ids or "") for row_id, username, role_ids in cursor.fetchall()]


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Audit manager menu / role consistency across DB and frontend."
    )
    parser.add_argument("--host", default=os.getenv("LILISHOP_DB_HOST", "127.0.0.1"))
    parser.add_argument(
        "--port", type=int, default=int(os.getenv("LILISHOP_DB_PORT", "3306"))
    )
    parser.add_argument("--user", default=os.getenv("LILISHOP_DB_USER", "root"))
    parser.add_argument(
        "--password", default=os.getenv("LILISHOP_DB_PASSWORD", "TanLiMing123!")
    )
    parser.add_argument(
        "--database", default=os.getenv("LILISHOP_DB_NAME", "lilishop")
    )
    args = parser.parse_args()

    current_modules = load_current_manager_route_modules()
    current_top_titles = [item.top_title for item in current_modules]

    print("Frontend root:", FRONTEND_ROOT)
    print("Backend root :", BACKEND_ROOT)

    print_section("Current Manager Navigation")
    for module in current_modules:
        print(
            f"- {module.top_title} ({module.file_name})"
            f" -> children: {', '.join(module.child_titles) if module.child_titles else '(none)'}"
        )
        if module.hidden_child_titles:
            print(f"  hidden aliases: {', '.join(module.hidden_child_titles)}")

    conn = get_connection(args)
    try:
        with conn.cursor() as cursor:
            platform_top_titles = fetch_top_level_titles(cursor, "li_menu")
            store_top_titles = fetch_top_level_titles(cursor, "li_store_menu")

            print_section("DB Table Counts")
            for table_name in [
                "li_menu",
                "li_store_menu",
                "li_role",
                "li_role_menu",
                "li_user_role",
                "li_admin_user",
                "li_store_role",
                "li_store_menu_role",
            ]:
                print(f"- {table_name}: {fetch_table_count(cursor, table_name)}")

            print_section("Platform Top Menu Comparison")
            print("Current frontend top menus:")
            print_items(current_top_titles)
            print("DB li_menu top menus:")
            print_items(platform_top_titles)
            missing_in_db = [title for title in current_top_titles if title not in platform_top_titles]
            extra_in_db = [title for title in platform_top_titles if title not in current_top_titles]
            print("Missing in li_menu:")
            print_items(missing_in_db)
            print("Extra in li_menu:")
            print_items(extra_in_db)

            print_section("Store Menu Snapshot")
            print("DB li_store_menu top menus:")
            print_items(store_top_titles)
            print(
                "- seller-side frontend route source is not in this workspace, "
                "so this script only reports DB-side store menu status."
            )

            orphan_user_roles = fetch_orphan_user_roles(cursor)
            orphan_role_menus = fetch_orphan_role_menus(cursor)
            print_section("Role Chain Issues")
            if orphan_user_roles:
                print("Orphan li_user_role.role_id values:")
                for role_id, total in orphan_user_roles:
                    print(f"- role_id={role_id}, bindings={total}")
            else:
                print("No orphan li_user_role.role_id records.")
            if orphan_role_menus:
                print("Orphan li_role_menu.menu_id values:")
                for menu_id, total in orphan_role_menus:
                    print(f"- menu_id={menu_id}, bindings={total}")
            else:
                print("No orphan li_role_menu.menu_id records.")

            print("Admin user role_ids snapshot:")
            for user_id, username, role_ids in fetch_admin_role_fields(cursor):
                print(f"- {username} ({user_id}) -> role_ids={role_ids or '(empty)'}")
    finally:
        conn.close()

    mock_imports = scan_mock_imports()
    hardcoded_super_routes = scan_hardcoded_super_routes()
    invalid_aliases = scan_invalid_menu_sync_aliases()
    suspicious_endpoints = scan_suspicious_api_endpoints()

    print_section("Frontend Mock / Hardcoded Findings")
    print("Files still importing @/api/mock:")
    print_items(mock_imports)
    print("Route modules still hardcoding roles=[\"super\"]:")
    print_items(hardcoded_super_routes)
    print("Menu alias targets pointing to missing routes:")
    if not invalid_aliases:
        print("none")
    else:
        for alias_token, target_path in invalid_aliases:
            print(f"- {alias_token}: {target_path}")

    print_section("Suspicious API Wrappers")
    if not suspicious_endpoints:
        print("none")
    else:
        for file_name, endpoint in suspicious_endpoints:
            print(f"- {file_name}: {endpoint}")

    print_section("Suggested Next Actions")
    next_actions: list[str] = []
    if missing_in_db or extra_in_db:
        next_actions.append("继续对齐 li_menu 与当前批发管理端顶级菜单骨架")
    if mock_imports:
        next_actions.append("移除剩余 @/api/mock 依赖，避免演示接口混入业务页面")
    if hardcoded_super_routes:
        next_actions.append("清理剩余 roles=[\"super\"] 写死权限，改为真实菜单/角色链路")
    if invalid_aliases:
        next_actions.append("修正 menu-sync 别名映射，避免后台菜单 token 映射到不存在页面")
    if suspicious_endpoints:
        next_actions.append("把剩余可疑占位接口改为真实业务接口或明确本地示例数据")
    if not next_actions:
        next_actions.append("当前前端菜单骨架、DB 菜单基线、别名映射和 mock 扫描已对齐")
    for action in next_actions:
        print(f"- {action}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
