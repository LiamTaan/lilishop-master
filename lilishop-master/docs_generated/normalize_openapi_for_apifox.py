import argparse
import copy
import json
from pathlib import Path


REF_PREFIX = "#/components/schemas/"


def load_json(path: Path):
    with path.open("r", encoding="utf-8") as f:
        return json.load(f)


def dump_json(path: Path, data):
    with path.open("w", encoding="utf-8", newline="\n") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write("\n")


def resolve_schema(schema, components, stack=None):
    if stack is None:
        stack = []

    if not isinstance(schema, dict):
        return schema

    if "$ref" in schema and schema["$ref"].startswith(REF_PREFIX):
        name = schema["$ref"][len(REF_PREFIX):]
        if name in stack:
            return schema
        target = components.get(name)
        if target is None:
            return schema
        return resolve_schema(copy.deepcopy(target), components, stack + [name])

    resolved = {}
    for key, value in schema.items():
        if key in {"properties", "patternProperties"} and isinstance(value, dict):
            resolved[key] = {
                prop_name: resolve_schema(prop_schema, components, stack)
                for prop_name, prop_schema in value.items()
            }
        elif key in {"items", "additionalProperties", "not"}:
            resolved[key] = resolve_schema(value, components, stack)
        elif key in {"allOf", "anyOf", "oneOf"} and isinstance(value, list):
            resolved[key] = [resolve_schema(item, components, stack) for item in value]
        else:
            resolved[key] = value
    return resolved


def normalize_for_apifox(doc):
    normalized = copy.deepcopy(doc)
    normalized["openapi"] = "3.0.3"
    normalized.pop("jsonSchemaDialect", None)

    components = normalized.get("components", {}).get("schemas", {})
    paths = normalized.get("paths", {})

    for path_item in paths.values():
        if not isinstance(path_item, dict):
            continue
        for operation in path_item.values():
            if not isinstance(operation, dict):
                continue
            request_body = operation.get("requestBody")
            if not isinstance(request_body, dict):
                continue
            content = request_body.get("content", {})
            for media in content.values():
                if not isinstance(media, dict):
                    continue
                schema = media.get("schema")
                if isinstance(schema, dict):
                    media["schema"] = resolve_schema(schema, components)

    return normalized


def main():
    parser = argparse.ArgumentParser(
        description="Normalize OpenAPI JSON for Apifox-style importers."
    )
    parser.add_argument("input", type=Path, help="Input OpenAPI JSON path")
    parser.add_argument(
        "-o",
        "--output",
        type=Path,
        help="Output path. Defaults to <input-stem>-apifox.json",
    )
    args = parser.parse_args()

    input_path = args.input
    output_path = args.output or input_path.with_name(f"{input_path.stem}-apifox.json")

    doc = load_json(input_path)
    normalized = normalize_for_apifox(doc)
    dump_json(output_path, normalized)
    print(output_path)


if __name__ == "__main__":
    main()
