"""Normalize a Blockbench Java block export for Minecraft 1.21.1.

Blockbench writes `format_version: 26.3` and texture names without a namespace.
Minecraft 1.21.1 needs `format_version: 1.21.1` and namespaced texture paths.
It also needs a `parent` for the isometric item preview and a `particle` texture
for correct break particles.

Run this after every re-export from Blockbench.
"""
import json
import os
import sys

DEFAULT = os.path.join(
    os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
    "src", "main", "resources", "assets", "stonespeaker", "models", "block", "stone_speaker.json",
)

TEXTURES = {
    "stone_light": "stonespeaker:block/stone_light",
    "stone_mid": "stonespeaker:block/stone_mid",
    "stone_dark": "stonespeaker:block/stone_dark",
    "grille2": "stonespeaker:block/grille",
    "grille": "stonespeaker:block/grille",
}


def main(path: str) -> None:
    with open(path, encoding="utf-8") as fh:
        model = json.load(fh)

    model.pop("credit", None)
    model["format_version"] = "1.21.1"
    model["textures"] = {
        key: TEXTURES.get(value, value) for key, value in model.get("textures", {}).items()
    }
    # 等距 3/4 物品预览 + 正确的破坏粒子
    model.setdefault("parent", "minecraft:block/block")
    model["textures"]["particle"] = "stonespeaker:block/stone_mid"

    for element in model.get("elements", []):
        rotation = element.get("rotation")
        if rotation is not None and rotation.get("angle", 0) == 0:
            element.pop("rotation", None)

    with open(path, "w", encoding="utf-8") as fh:
        json.dump(model, fh, indent=2)

    print(f"normalized {path}")
    print("format_version:", model["format_version"])
    print("parent:", model.get("parent"))
    print("textures:", model["textures"])
    print("elements:", len(model.get("elements", [])))


if __name__ == "__main__":
    main(sys.argv[1] if len(sys.argv) > 1 else DEFAULT)
