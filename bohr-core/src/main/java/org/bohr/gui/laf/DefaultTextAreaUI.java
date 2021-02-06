package org.bohr.gui.laf;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicTextAreaUI;

public class DefaultTextAreaUI extends BasicTextAreaUI {
	private JComponent com;

	@Override
	public void installUI(JComponent c) {
		super.installUI(c);
		c.setBorder(new EmptyBorder(10, 10, 10, 10));
		c.setOpaque(true);
		com = c;
	}

	@Override
	protected void paintBackground(Graphics g) {
//		super.paintBackground(g);
		paintBackground(g, com.getBounds());
	}

	public void paintBackground(Graphics g, Rectangle bounds) {
		if (g != null && g instanceof Graphics2D) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			RoundRectangle2D.Double rect = new RoundRectangle2D.Double();
			rect.setRoundRect(0, 0, bounds.width, bounds.height, 15, 15);

			g2.setColor(new Color(0xf2f3f6));
			g2.fill(rect);
		}
	}
}
