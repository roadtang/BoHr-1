package org.bohr.gui.uiUtils;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JLabel;

public class GraphicsUtils {

	public static void drawString(Graphics g, Font font, String text, int x, int y, int anchor) {
		if (g == null) {
			return;
		}
		if (font == null) {
			return;
		}
		if (text == null) {
			return;
		}
		if (text.equals("")) {
			return;
		}

		JLabel label = new JLabel(text);
		label.setFont(font);

		FontMetrics fontMetrics = label.getFontMetrics(font);
		g.setFont(font);
		int textWidth = fontMetrics.stringWidth(text);
		int textHeight = fontMetrics.getHeight();

		y = y + fontMetrics.getAscent();
		switch (anchor) {
		case 0:
			break;
		case 1:
			x = x - textWidth / 2;
			break;
		case 2:
			x = x - textWidth;
			break;
		case 3:
			y = y - textHeight / 2;
			break;
		case 4:
			x = x - textWidth / 2;
			y = y - textHeight / 2;
			break;
		case 5:
			x = x - textWidth;
			y = y - textHeight / 2;
			break;
		case 6:
			y = y - textHeight;
			break;
		case 7:
			x = x - textWidth / 2;
			y = y - textHeight;
			break;
		case 8:
			x = x - textWidth;
			y = y - textHeight;
			break;
		}
		Object o = GraphicsUtils.getRenderingHint((Graphics2D) g);
		GraphicsUtils.setRenderingHint_ON((Graphics2D) g);
		g.drawString(text, x, y);
		GraphicsUtils.setRenderingHint((Graphics2D) g, o);
	}

	public static void setRenderingHint(Graphics2D g2d, Object o) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, o);
	}

	public static Object getRenderingHint(Graphics2D g2d) {
		return g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
	}

	public static void setRenderingHint_OFF(Graphics2D g2d) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	public static void setRenderingHint_ON(Graphics2D g2d) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}
}
