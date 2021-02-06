package org.bohr.gui.laf;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicPanelUI;

public class RoundRectPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	public RoundRectPanel() {
		this(null);
	}

	public RoundRectPanel(final Color color) {
		setUI(new BasicPanelUI() {
			@Override
			public void update(Graphics g, JComponent c) {
//				super.update(g, c);
				if (c.isOpaque()) {
					g.setColor(c.getBackground());
//					g.fillRect(0, 0, c.getWidth(), c.getHeight());
					if (g != null && g instanceof Graphics2D) {
						Graphics2D g2 = (Graphics2D) g;
						g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

						RoundRectangle2D.Double rect = new RoundRectangle2D.Double();
						rect.setRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);

						g2.fill(rect);
					}
				}
				paint(g, c);
			}
		});

		if (color != null) {
			setBackground(color);
		}
	}
}
