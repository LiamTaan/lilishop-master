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
    for func_name, quote, raw_endpoint in pattern.findall(text):
        if "${" in raw_endpoint:
            result[func_name] = raw_endpoint
        else:
            result[func_name] = raw_endpoint
    return result


def collect_api_usage(entry_file: Path) -> dict[str, list[str]]:
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
            names = [
                item.strip().split(" as ")[0].strip()
                for item in imported_names.split(",")
                if item.strip()
            ]
            for name in names:
                api_functions[name] = (api_module, api_import_path)

    endpoint_map: dict[str, list[str]] = {}
    cached_api_exports: dict[Path, dict[str, str]] = {}
    for func_name, (api_module, _api_import_path) in api_functions.items():
        exports = cached_api_exports.setdefault(
            api_module, parse_api_module_exports(api_module)
        )
        endpoint = exports.get(func_name)
        if endpoint:
            endpoint_map.setdefault(str(api_module.relative_to(FRONTEND_ROOT)), []).append(
                f"{func_name} -> {endpoint}"
            )
    return endpoint_map


def main() -> int:
    routes = parse_route_views()
    print("=== Current Manager Route API Usage ===")
    for route in routes:
        print(f"\n[{route.parent_title} / {route.title}] {route.path}")
        print(f"component: {route.component_path.relative_to(FRONTEND_ROOT)}")
        if not route.component_path.exists():
            print("- component file missing")
            continue
        usage = collect_api_usage(route.component_path)
        if not usage:
            print("- no direct api wrapper usage found")
            continue
        for api_module, bindings in sorted(usage.items()):
            print(f"- {api_module}")
            for binding in sorted(set(bindings)):
                print(f"  {binding}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
