package org.bohr.gui.laf;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.plaf.metal.MetalButtonUI;

import org.bohr.gui.uiUtils.ColorUtils;
import org.bohr.gui.uiUtils.FontUtils;

public class EmptyBlueRectButtonUI extends MetalButtonUI {
	private Color forColor;

	public EmptyBlueRectButtonUI(Color color) {
		forColor = color;
	}

	@Override
	public void installUI(JComponent c) {
		super.installUI(c);
		c.setFont(FontUtils.getFont().deriveFont(Font.BOLD, 16F));

		if (forColor != null) {
			c.setForeground(forColor);
		}

		if (c instanceof AbstractButton) {
			AbstractButton button = (AbstractButton) c;
			button.setFocusPainted(false);
			button.setFocusable(false);
			button.setOpaque(false);
			button.setContentAreaFilled(false);
			button.setBorder(null);
		}
	}

	/**
	 * @param g
	 * @param c
	 */
	protected void paintBackground_normal(Graphics g, JComponent c) {
		c.setForeground(forColor);
		paintRoundRectButtonBackGround(g, c, forColor);
	}

	/**
	 * @param g
	 * @param c
	 */
	protected void paintBackground_rollover(Graphics g, JComponent c) {
		Color color = ColorUtils.brightness(forColor, 1.4);
		c.setForeground(color);
		paintRoundRectButtonBackGround(g, c, color);
	}

	/**
	 * @param g
	 * @param c
	 */
	protected void paintBackground_pressed(Graphics g, JComponent c) {
		Color color = ColorUtils.brightness(forColor, 0.6);
		c.setForeground(color);
		paintRoundRectButtonBackGround(g, c, color);
	}

	protected void paintRoundRectButtonBackGround(Graphics g, JComponent c, Color color) {
		g.setColor(color);
		if (g != null && g instanceof Graphics2D) {
			Graphics2D g2 = (Graphics2D) g;

			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			RoundRectangle2D.Double rect = new RoundRectangle2D.Double();
			rect.setRoundRect(2, 2, c.getWidth() - 4, c.getHeight() - 4, 15, 15);
			g2.draw(rect);
		}
	}

	public void paintBackground(Graphics g, JComponent c) {
		AbstractButton b = (AbstractButton) c;
		ButtonModel model = b.getModel();
		if (!model.isEnabled()) {
			paintBackground_normal(g, c);
		} else if (model.isPressed() && model.isArmed()) {
			paintBackground_pressed(g, c);
		} else if (b.isRolloverEnabled() && model.isRollover()) {
			paintBackground_rollover(g, c);
		} else {
			paintBackground_normal(g, c);
		}
	}

	@Override
	public void paint(Graphics g, JComponent c) {
		paintBackground(g, c);
		super.paint(g, c);
	}
}
