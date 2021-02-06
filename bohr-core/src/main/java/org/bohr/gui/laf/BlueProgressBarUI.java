package org.bohr.gui.laf;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Arc2D;

import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.metal.MetalProgressBarUI;

public class BlueProgressBarUI extends MetalProgressBarUI {
	@Override
	public void installUI(JComponent c) {
		super.installUI(c);
		progressBar.setOpaque(false);
		progressBar.setBorder(new EmptyBorder(1, 1, 1, 1));
		progressBar.setBackground(Color.white);
		progressBar.setForeground(new Color(0xffad00));
	}

	private void drawBorder(Graphics g, JComponent c) {
		Graphics2D g2 = (Graphics2D) g;

		int width = progressBar.getWidth();
		int height = progressBar.getHeight();

//		g2.setColor(Color.LIGHT_GRAY);
//		g2.fillRect(0, 0, width, height);

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Shape left = new Arc2D.Double(0D, 0D, new Double(height), new Double(height), 90D, 180D, Arc2D.OPEN);
		Shape right = new Arc2D.Double(width - new Double(height), 0D, new Double(height), new Double(height), 270D,
				180D, Arc2D.OPEN);
		g2.setColor(progressBar.getBackground());
		g2.fill(left);
		g2.fill(right);

		g2.fillRect(height / 2, 0, width - height, height);
	}

	private void drawProgress(Graphics g, JComponent c) {
		Insets b = progressBar.getInsets(); // area for border
		int barRectWidth = progressBar.getWidth() - (b.right + b.left);
		int barRectHeight = progressBar.getHeight() - (b.top + b.bottom);

		if (barRectWidth <= 0 || barRectHeight <= 0) {
			return;
		}

		Graphics2D g2 = (Graphics2D) g;

		int width = progressBar.getWidth();
		int height = progressBar.getHeight();

//		g2.setColor(Color.LIGHT_GRAY);
//		g2.fillRect(0, 0, width, height);

		g2.setColor(progressBar.getForeground());

		int minWidth = height + 1;
		int amountWidth = getAmountFull(b, width, height);
		if (amountWidth > 0) {
			if (amountWidth <= minWidth) {
				amountWidth = minWidth;
			}

			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			Shape left = new Arc2D.Double(0D, 0D, new Double(height), new Double(height), 90D, 180D, Arc2D.OPEN);
			Shape right = new Arc2D.Double(amountWidth - new Double(height), 0D, new Double(height), new Double(height),
					270D, 180D, Arc2D.OPEN);
			g2.fill(left);
			g2.fill(right);

			g2.fillRect(height / 2, 0, amountWidth - height, height);
		}

		if (progressBar.isStringPainted()) {
			int amountFull = getAmountFull(b, barRectWidth, barRectHeight);
			paintString(g, b.left, b.top, barRectWidth, barRectHeight, amountFull, b);
		}
	}

	@Override
	public void paintDeterminate(Graphics g, JComponent c) {
		if (!(g instanceof Graphics2D)) {
			return;
		}

		drawBorder(g, c);
		drawProgress(g, c);

	}
}
