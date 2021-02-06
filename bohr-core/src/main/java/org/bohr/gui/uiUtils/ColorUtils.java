package org.bohr.gui.uiUtils;

import java.awt.Color;

public class ColorUtils {

	public static Color createColor(String color_16) {
		int idx = 0;
		if (color_16.length() == 6) {
			idx = 0;
		} else if (color_16.length() == 7) {
			idx = 1;
		}
		String r = String.valueOf(color_16.charAt(idx++)) + String.valueOf(color_16.charAt(idx++));
		String g = String.valueOf(color_16.charAt(idx++)) + String.valueOf(color_16.charAt(idx++));
		String b = String.valueOf(color_16.charAt(idx++)) + String.valueOf(color_16.charAt(idx++));

		int red = Integer.parseInt(r, 16);
		int green = Integer.parseInt(g, 16);
		int blue = Integer.parseInt(b, 16);

		return new Color(red, green, blue);
	}

	public static Color brightness(Color c, double scale) {
		int r = Math.min(255, (int) (c.getRed() * scale));
		int g = Math.min(255, (int) (c.getGreen() * scale));
		int b = Math.min(255, (int) (c.getBlue() * scale));
		return new Color(r, g, b);
	}
}
