#!/usr/bin/env python3
"""Generate iOS App Store icon from Android adaptive icon (vector + background)."""
from __future__ import annotations

import subprocess
import sys
import xml.etree.ElementTree as ET
from pathlib import Path
from typing import Optional

NS = {"android": "http://schemas.android.com/apk/res/android"}
ROOT = Path(__file__).resolve().parents[1]
FOREGROUND = ROOT / "composeApp/src/androidMain/res/drawable/ic_launcher_foreground.xml"
OUT_DIR = ROOT / "iosApp/iosApp/Assets.xcassets/AppIcon.appiconset"
BACKGROUND = "#0983C3"  # lochmara (ic_launcher_background)
SIZE = 1024


def android_attr(elem, name: str) -> Optional[str]:
    return elem.get(f"{{{NS['android']}}}{name}")


def path_to_svg(elem: ET.Element) -> str:
    d = android_attr(elem, "pathData")
    if not d:
        return ""
    fill = android_attr(elem, "fillColor") or "#000000"
    stroke = android_attr(elem, "strokeColor")
    sw = android_attr(elem, "strokeWidth")
    cap = android_attr(elem, "strokeLineCap")
    parts = [f'd="{d}"']
    if fill and fill.lower() not in ("#00000000", "@android:color/transparent"):
        parts.append(f'fill="{fill}"')
    else:
        parts.append('fill="none"')
    if stroke:
        parts.append(f'stroke="{stroke}"')
        parts.append(f'stroke-width="{sw or 1}"')
        if cap == "round":
            parts.append('stroke-linecap="round"')
    return f"<path {' '.join(parts)}/>"


def group_to_svg(elem: ET.Element, depth: int = 0) -> str:
    sx = android_attr(elem, "scaleX") or "1"
    sy = android_attr(elem, "scaleY") or "1"
    tx = android_attr(elem, "translateX") or "0"
    ty = android_attr(elem, "translateY") or "0"
    inner = []
    for child in elem:
        tag = child.tag.split("}")[-1]
        if tag == "path":
            inner.append(path_to_svg(child))
        elif tag == "group":
            inner.append(group_to_svg(child, depth + 1))
    transform = f"translate({tx},{ty}) scale({sx},{sy})"
    return f'<g transform="{transform}">{"".join(inner)}</g>'


def build_svg() -> str:
    tree = ET.parse(FOREGROUND)
    root = tree.getroot()
    body = []
    for child in root:
        tag = child.tag.split("}")[-1]
        if tag == "group":
            body.append(group_to_svg(child))
        elif tag == "path":
            body.append(path_to_svg(child))
    inner = "".join(body)
    return f'''<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" width="{SIZE}" height="{SIZE}" viewBox="0 0 108 108">
  <rect width="108" height="108" fill="{BACKGROUND}"/>
  {inner}
</svg>'''


def main() -> None:
    svg = build_svg()
    out_png = OUT_DIR / "app-icon-1024.png"
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    svg_path = OUT_DIR / "_launcher_source.svg"
    svg_path.write_text(svg, encoding="utf-8")

    if sys.platform == "darwin":
        try:
            subprocess.run(
                [
                    "sips",
                    "-s",
                    "format",
                    "png",
                    "-z",
                    str(SIZE),
                    str(SIZE),
                    str(svg_path),
                    "--out",
                    str(out_png),
                ],
                check=True,
                capture_output=True,
            )
            svg_path.unlink(missing_ok=True)
            print(f"Wrote {out_png} (sips)")
            return
        except (FileNotFoundError, subprocess.CalledProcessError):
            pass

    try:
        import cairosvg

        cairosvg.svg2png(
            bytestring=svg.encode("utf-8"),
            write_to=str(out_png),
            output_width=SIZE,
            output_height=SIZE,
        )
        svg_path.unlink(missing_ok=True)
        print(f"Wrote {out_png} (cairosvg)")
        return
    except (ImportError, OSError):
        pass

    for cmd in (
        ["rsvg-convert", "-w", str(SIZE), "-h", str(SIZE), "-o", str(out_png), str(svg_path)],
        [
            "inkscape",
            str(svg_path),
            "--export-type=png",
            f"--export-filename={out_png}",
            "-w",
            str(SIZE),
            "-h",
            str(SIZE),
        ],
    ):
        try:
            subprocess.run(cmd, check=True, capture_output=True)
            svg_path.unlink(missing_ok=True)
            print(f"Wrote {out_png} via {cmd[0]}")
            return
        except (FileNotFoundError, subprocess.CalledProcessError):
            continue

    print(
        "On macOS, run this script (uses sips). Else: brew install librsvg && re-run.\n"
        f"SVG saved to {svg_path}",
        file=sys.stderr,
    )
    sys.exit(1)


if __name__ == "__main__":
    main()
