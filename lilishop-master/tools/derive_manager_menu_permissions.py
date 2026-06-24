#!/usr/bin/env python3
from __future__ import annotations

import re
from dataclasses import dataclass
from pathlib import Path

import pymysql


BACKEND_ROOT = Path(__file__).resolve().parents[1]
FRONTEND_ROOT = BACKEND_ROOT.parent / "lilishop-admin"
MODULE_ROOT = FRONTEND_ROOT / "src/router/modules"
BUSINESS_ROUTE_FILE = FRONTEND_ROOT / "src/router/business-routes.ts"
MENU_SYNC_FILE = FRONTEND_ROOT / "src/router/menu-sync.ts"


@dataclass
class CurrentRouteRecord:
    module_name: str
    path: str
    name: str
    title: str
    parent_title: str
    visible_in_sidebar: bool


def normalize_token(value: object) -> str:
    return (
        str(value or "")
        .strip()
        .lower()
        .replace("\\", "/")
        .strip("/")
    )


def parse_alias_map() -> dict[str, str]:
    text = MENU_SYNC_FILE.read_text("utf-8")
    pattern = re.compile(r'"([^"]+)":\s*"([^"]+)"')
    return {
        normalize_token(key): value.strip()
        for key, value in pattern.findall(text)
        if key.strip() and value.strip().startswith("/")
    }


def load_module_names() -> list[str]:
    text = BUSINESS_ROUTE_FILE.read_text("utf-8")
    pattern = re.compile(r'import\s+\w+\s+from\s+"\.\/modules\/([^"]+)";')
    return [match + ".ts" for match in pattern.findall(text)]


def load_current_routes() -> list[CurrentRouteRecord]:
    path_pattern = re.compile(r'path:\s*"([^"]+)"')
    name_pattern = re.compile(r'name:\s*"([^"]+)"')
    title_pattern = re.compile(r'title:\s*"([^"]+)"')
    show_link_pattern = re.compile(r"showLink:\s*false")

    routes: list[CurrentRouteRecord] = []
    for module_name in load_module_names():
        text = (MODULE_ROOT / module_name).read_text("utf-8")
        paths = path_pattern.findall(text)
        names = name_pattern.findall(text)
        titles = title_pattern.findall(text)
        show_link_matches = show_link_pattern.findall(text)

        if not paths or not titles:
            continue
        parent_title = titles[0]
        child_titles = titles[1:]
        child_paths = paths[1 : 1 + len(child_titles)]
        child_names = names[-len(child_titles) :] if child_titles else []

        # Count hidden pages by scanning each child block.
        child_blocks = re.findall(r"\{[^{}]*?path:\s*\"/[^\"]+\"[^{}]*?\}", text, re.S)
        hidden_flags: list[bool] = [bool(show_link_pattern.search(block)) for block in child_blocks]
        hidden_flags = hidden_flags[: len(child_titles)]

        for index, (path, name, title) in enumerate(
            zip(child_paths, child_names, child_titles), start=0
        ):
            routes.append(
                CurrentRouteRecord(
                    module_name=module_name,
                    path=path,
                    name=name,
                    title=title,
                    parent_title=parent_title,
                    visible_in_sidebar=not (hidden_flags[index] if index < len(hidden_flags) else False),
                )
            )
    return routes


def build_route_indexes(routes: list[CurrentRouteRecord]):
    by_path: dict[str, CurrentRouteRecord] = {}
    by_name: dict[str, CurrentRouteRecord] = {}
    by_title: dict[str, CurrentRouteRecord] = {}
    by_token: dict[str, CurrentRouteRecord] = {}
    for route in routes:
        path_token = normalize_token(route.path)
        name_token = normalize_token(route.name)
        title_token = normalize_token(route.title)
        last_token = path_token.split("/")[-1] if path_token else ""
        by_path.setdefault(path_token, route)
        by_name.setdefault(name_token, route)
        by_title.setdefault(title_token, route)
        if last_token:
            by_token.setdefault(last_token, route)
    return by_path, by_name, by_title, by_token


def find_current_route(menu_row: dict[str, object], alias_map: dict[str, str], indexes):
    by_path, by_name, by_title, by_token = indexes
    candidates = [
        normalize_token(menu_row.get("front_route")),
        normalize_token(menu_row.get("path")),
        normalize_token(menu_row.get("name")),
        normalize_token(menu_row.get("title")),
    ]

    for token in candidates:
        alias_path = alias_map.get(token)
        if alias_path:
            record = by_path.get(normalize_token(alias_path))
            if record:
                return record, "alias"

    for token in candidates:
        record = by_path.get(token) or by_name.get(token) or by_title.get(token)
        if record:
            return record, "exact"

    for token in candidates:
        last_token = token.split("/")[-1] if token else ""
        if not last_token:
            continue
        record = by_token.get(last_token)
        if record:
            return record, "token"

    return None, "none"


def main() -> int:
    alias_map = parse_alias_map()
    routes = load_current_routes()
    indexes = build_route_indexes(routes)

    conn = pymysql.connect(
        host="127.0.0.1",
        port=3306,
        user="root",
        password="TanLiMing123!",
        database="lilishop",
        charset="utf8mb4",
        cursorclass=pymysql.cursors.DictCursor,
    )

    mapped: dict[str, list[dict[str, str]]] = {}
    unmatched: list[dict[str, object]] = []

    try:
        with conn.cursor() as cur:
            cur.execute(
                """
                SELECT CAST(id AS CHAR) AS id, title, name, path, front_route, permission
                FROM li_menu
                ORDER BY level, sort_order, id
                """
            )
            menus = cur.fetchall()
    finally:
        conn.close()

    for menu in menus:
        record, match_type = find_current_route(menu, alias_map, indexes)
        if record and menu.get("permission"):
            mapped.setdefault(record.path, []).append(
                {
                    "route_title": record.title,
                    "route_parent": record.parent_title,
                    "menu_id": str(menu["id"]),
                    "menu_title": str(menu.get("title") or ""),
                    "permission": str(menu.get("permission") or ""),
                    "match_type": match_type,
                }
            )
        elif record is None and menu.get("permission"):
            unmatched.append(menu)

    print("=== Derived Manager Permission Mapping ===")
    matched_paths = 0
    for route in routes:
        entries = mapped.get(route.path, [])
        if entries:
            matched_paths += 1
            print(f"\n[{route.parent_title} / {route.title}] {route.path}")
            for item in entries:
                print(
                    f"- {item['match_type']}: old_menu={item['menu_title']} ({item['menu_id']})"
                )
                print(f"  permission={item['permission']}")

    print(f"\nMatched current routes: {matched_paths}/{len(routes)}")

    unresolved_routes = [route for route in routes if route.path not in mapped]
    print("\n=== Current Routes Without Derived Permission ===")
    for route in unresolved_routes:
        print(f"- [{route.parent_title}] {route.title} -> {route.path}")

    print("\n=== Old Menu Rows With Permission But No Current Route Match ===")
    for menu in unmatched[:80]:
        print(
            f"- {menu['title']} | name={menu['name']} | path={menu['path']} | front_route={menu['front_route']}"
        )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
