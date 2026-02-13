#!/usr/bin/env python3
"""
Generate launcher icon PNGs for all Android densities.

Design: Purple (#6200EE) background with white circle outline + checkmark.
Matches the vector drawable in ic_launcher_foreground.xml / ic_launcher_background.xml.

Densities and sizes:
  mdpi:    48x48
  hdpi:    72x72
  xhdpi:   96x96
  xxhdpi:  144x144
  xxxhdpi: 192x192
"""

import math
import os
from PIL import Image, ImageDraw

# Project root (one level up from scripts/)
PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES_DIR = os.path.join(PROJECT_ROOT, "app", "src", "main", "res")

DENSITIES = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}

BG_COLOR = (98, 0, 238)  # #6200EE
FG_COLOR = (255, 255, 255)  # White


def draw_icon(size, rounded=False):
    """Draw the launcher icon at the given size.

    Args:
        size: Icon dimension in pixels (square).
        rounded: If True, clip to circle for ic_launcher_round.
    """
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Background
    if rounded:
        # Circular background
        draw.ellipse([0, 0, size - 1, size - 1], fill=BG_COLOR)
    else:
        # Rounded rectangle background
        radius = int(size * 0.18)
        draw.rounded_rectangle([0, 0, size - 1, size - 1], radius=radius, fill=BG_COLOR)

    # The foreground vector uses a 24x24 viewport scaled by 2.61x and translated.
    # In the adaptive icon (108dp viewport), the circle+check occupies roughly
    # the center 62% of the icon.
    # For the PNG fallback, we draw at ~55% of icon size, centered.

    cx, cy = size / 2, size / 2
    icon_radius = size * 0.27  # Radius of the circle outline

    # Circle outline (ring)
    ring_width = max(2, int(size * 0.04))
    outer_r = icon_radius
    inner_r = icon_radius - ring_width
    draw.ellipse(
        [cx - outer_r, cy - outer_r, cx + outer_r, cy + outer_r],
        fill=FG_COLOR,
    )
    draw.ellipse(
        [cx - inner_r, cy - inner_r, cx + inner_r, cy + inner_r],
        fill=BG_COLOR,
    )

    # Checkmark inside the circle
    # The vector path: M16.707,7.293 l-5.707,5.707 l-2.707,-2.707 l-1.414,1.414
    #                   l4.121,4.121 l7.121,-7.121z
    # In 24x24 space, the check goes from roughly (7,11) to (11,15.8) to (18.4,8.7)
    # Scale to our icon space
    scale = icon_radius * 2 / 24  # Map 24-unit viewport to our icon diameter
    offset_x = cx - 12 * scale
    offset_y = cy - 12 * scale

    def to_px(x, y):
        return (offset_x + x * scale, offset_y + y * scale)

    # Checkmark as a thick polyline
    check_width = max(2, int(size * 0.06))
    # Three key points of the checkmark
    p1 = to_px(7.879, 11.707)   # left end
    p2 = to_px(11, 14.828)      # bottom vertex
    p3 = to_px(16.707, 9.121)   # right end

    draw.line([p1, p2, p3], fill=FG_COLOR, width=check_width, joint="curve")

    # Convert to RGB (no alpha needed for Android PNGs)
    result = Image.new("RGB", (size, size), BG_COLOR)
    result.paste(img, mask=img.split()[3])
    return result


def main():
    for density, size in DENSITIES.items():
        mipmap_dir = os.path.join(RES_DIR, f"mipmap-{density}")
        os.makedirs(mipmap_dir, exist_ok=True)

        # Regular icon (rounded rectangle)
        regular = draw_icon(size, rounded=False)
        regular_path = os.path.join(mipmap_dir, "ic_launcher.png")
        regular.save(regular_path, "PNG")
        print(f"Generated {regular_path} ({size}x{size})")

        # Round icon (circle)
        round_icon = draw_icon(size, rounded=True)
        round_path = os.path.join(mipmap_dir, "ic_launcher_round.png")
        round_icon.save(round_path, "PNG")
        print(f"Generated {round_path} ({size}x{size})")

    print("\nDone! All icons generated.")


if __name__ == "__main__":
    main()
