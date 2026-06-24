#!/usr/bin/env python3
from __future__ import annotations

import re
from dataclasses import dataclass
from pathlib import Path


BACKEND_ROOT = Path(__file__).resolve().parents[1]
FRONTEND_ROOT = BACKEND_ROOT.parent / "lilishop-admin"
SRC_ROOT = FRONTEND_ROOT / "src"
MODULE_ROOT = SRC_ROOT / "router/modules"
BUSINESS_ROUTE_FILE = SRC_ROOT / "router/business-routes.ts"
OUTPUT_SQL_FILE = BACKEND_ROOT / "DB/patch_wholesale_manager_menu_permission_fill.sql"

BASELINE_MENU_ID_START = 3062200000000000001
BASELINE_MENU_ID_END = 3062200000000010097
IGNORED_API_MODULES = {
    "src/api/user.ts",
}


@dataclass
class RouteView:
    module_name: str
    title: str
    parent_title: str
    path: str
    component_path: Path


def load_business_module_files() -> list[Path]:
    text = BUSINESS_ROUTE_FILE.read_text("utf-8")
    pattern = re.compile(r'import\s+\w+\s+from\s+"\.\/modules\/([^"]+)";')
    return [MODULE_ROOT / f"{name}.ts" for name in pattern.findall(text)]


def resolve_alias_path(import_path: str, base_file: Path) -> Path | None:
    candidates: list[Path] = []
    if import_path.startswith("@/"):
        base = SRC_ROOT / import_path[2:]
        candidates.extend(
            [
                base,
                base.with_suffix(".ts"),
                base.with_suffix(".tsx"),
                base.with_suffix(".vue"),
                base / "index.ts",
                base / "index.tsx",
                base / "index.vue",
            ]
        )
    elif import_path.startswith("."):
        base = (base_file.parent / import_path).resolve()
        candidates.extend(
            [
                base,
                base.with_suffix(".ts"),
                base.with_suffix(".tsx"),
                base.with_suffix(".vue"),
                base / "index.ts",
                base / "index.tsx",
                base / "index.vue",
            ]
        )
    else:
        return None

    for candidate in candidates:
        if candidate.exists() and candidate.is_file():
            return candidate
    return None


def parse_route_views() -> list[RouteView]:
    route_views: list[RouteView] = []
    parent_title_pattern = re.compile(r'meta:\s*\{[\s\S]*?title:\s*"([^"]+)"', re.S)
    children_block_pattern = re.compile(r"children:\s*\[(.*)\]\s*\}\s*satisfies", re.S)
    child_block_pattern = re.compile(
        r'\{\s*path:\s*"([^"]+)"[\s\S]*?component:\s*\(\)\s*=>\s*import\("@/([^"]+)"\)[\s\S]*?meta:\s*\{[\s\S]*?title:\s*"([^"]+)"[\s\S]*?\}',
        re.S,
    )

    for module_file in load_business_module_files():
        text = module_file.read_text("utf-8")
        parent_match = parent_title_pattern.search(text)
        if not parent_match:
            continue
        parent_title = parent_match.group(1)
        children_match = children_block_pattern.search(text)
        if not children_match:
            continue
        children_text = children_match.group(1)
        for path, component, title in child_block_pattern.findall(children_text):
            route_views.append(
                RouteView(
                    module_name=module_file.name,
                    title=title,
                    parent_title=parent_title,
                    path=path,
                    component_path=SRC_ROOT / component,
                )
            )
    return route_views


def collect_local_dependency_files(entry_file: Path) -> set[Path]:
    visited: set[Path] = set()
    import_pattern = re.compile(r'import\s+[^;]*?\s+from\s+["\']([^"\']+)["\']')

    def visit(file_path: Path):
        if file_path in visited or not file_path.exists():
            return
        visited.add(file_path)
        try:
            text = file_path.read_text("utf-8")
        except UnicodeDecodeError:
            return
        for import_path in import_pattern.findall(text):
            resolved = resolve_alias_path(import_path, file_path)
            if resolved and str(resolved).startswith(str(SRC_ROOT)):
                visit(resolved)

    visit(entry_file)
    return visited


def parse_api_module_exports(api_module: Path) -> dict[str, str]:
    text = api_module.read_text("utf-8")
    pattern = re.compile(
        r"export const (\w+)\s*=\s*\([^)]*\)\s*=>[\s\S]*?http\.request[^(]*\(\s*['\"][^'\"]+['\"]\s*,\s*([`'\"])(.+?)\2",
        re.S,
    )
    result: dict[str, str] = {}
    for func_name, _quote, raw_endpoint in pattern.findall(text):
        result[func_name] = raw_endpoint
    return result


def normalize_permission(endpoint: str) -> str | None:
    endpoint = endpoint.strip()
    if not endpoint.startswith("/"):
        return None
    endpoint = re.sub(r"\$\{[^}]+\}", "*", endpoint)
    if "*" in endpoint:
        endpoint = endpoint.split("*", 1)[0].rstrip("/")
    endpoint = re.sub(r"/\*", "*", endpoint)
    endpoint = re.sub(r"\*+", "*", endpoint)
    if not endpoint.endswith("*"):
        endpoint = f"{endpoint}*"
    return endpoint


def compress_permissions(permissions: list[str]) -> list[str]:
    normalized = sorted(set(permissions), key=lambda item: (len(item), item))
    compressed: list[str] = []
    for permission in normalized:
        permission_prefix = permission[:-1] if permission.endswith("*") else permission
        covered = False
        for existing in compressed:
            existing_prefix = existing[:-1] if existing.endswith("*") else existing
            if permission_prefix.startswith(existing_prefix):
                covered = True
                break
        if not covered:
            compressed.append(permission)
    return compressed


def collect_route_permissions(entry_file: Path) -> list[str]:
    dependency_files = collect_local_dependency_files(entry_file)
    import_pattern = re.compile(
        r'import\s+\{([^}]+)\}\s+from\s+["\'](@/api/[^"\']+)["\']'
    )

    api_functions: dict[str, tuple[Path, str]] = {}
    for file_path in dependency_files:
        try:
            text = file_path.read_text("utf-8")
        except UnicodeDecodeError:
            continue
        for imported_names, api_import_path in import_pattern.findall(text):
            api_module = resolve_alias_path(api_import_path, file_path)
            if not api_module:
                continue
            relative_api_module = str(api_module.relative_to(FRONTEND_ROOT)).replace("\\", "/")
            if relative_api_module in IGNORED_API_MODULES:
                continue
            names = [
                item.strip().split(" as ")[0].strip()
                for item in imported_names.split(",")
                if item.strip()
            ]
            for name in names:
                api_functions[name] = (api_module, relative_api_module)

    cached_api_exports: dict[Path, dict[str, str]] = {}
    permissions: list[str] = []
    for func_name, (api_module, _relative_api_module) in sorted(api_functions.items()):
        exports = cached_api_exports.setdefault(api_module, parse_api_module_exports(api_module))
        endpoint = exports.get(func_name)
        if not endpoint:
            continue
        permission = normalize_permission(endpoint)
        if permission and permission not in permissions:
            permissions.append(permission)
    return compress_permissions(permissions)


def build_sql(routes: list[RouteView]) -> str:
    statements: list[str] = [
        "SET NAMES utf8mb4;",
        "",
        "-- 基于当前 lilishop-admin 路由实际调用的后端接口生成",
        "-- 仅回填批发商城管理端新菜单基线范围内的 permission 字段",
        f"-- baseline range: {BASELINE_MENU_ID_START} ~ {BASELINE_MENU_ID_END}",
        "",
    ]

    matched_count = 0
    unresolved_routes: list[str] = []
    for route in routes:
        if not route.component_path.exists():
            unresolved_routes.append(f"-- missing component: {route.path} -> {route.component_path}")
            continue
        permissions = collect_route_permissions(route.component_path)
        if not permissions:
            unresolved_routes.append(
                f"-- no business api detected: [{route.parent_title}] {route.title} -> {route.path}"
            )
            continue
        matched_count += 1
        permission_sql = ",".join(permissions).replace("'", "''")
        path_sql = route.path.replace("'", "''")
        title_sql = route.title.replace("'", "''")
        statements.append(
            f"-- [{route.parent_title}] {title_sql} -> {path_sql}"
        )
        statements.append(
            "UPDATE li_menu "
            f"SET permission = '{permission_sql}' "
            f"WHERE id BETWEEN {BASELINE_MENU_ID_START} AND {BASELINE_MENU_ID_END} "
            f"AND path = '{path_sql}';"
        )
        statements.append("")

    statements.append(
        "SELECT CAST(id AS CHAR) AS id, title, path, permission "
        "FROM li_menu "
        f"WHERE id BETWEEN {BASELINE_MENU_ID_START} AND {BASELINE_MENU_ID_END} "
        "ORDER BY level, sort_order, id;"
    )
    statements.append("")
    statements.append(
        f"-- matched routes with permission fill: {matched_count}/{len(routes)}"
    )
    statements.extend(unresolved_routes)
    statements.append("")
    return "\n".join(statements)


def main() -> int:
    routes = parse_route_views()
    sql = build_sql(routes)
    OUTPUT_SQL_FILE.write_text(sql, encoding="utf-8")
    print(f"generated: {OUTPUT_SQL_FILE}")
    print(f"routes: {len(routes)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
