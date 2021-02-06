package org.bohr.gui.laf;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.plaf.metal.MetalLabelUI;
import javax.swing.table.DefaultTableCellRenderer;

public class TableRender extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1L;

	private int[] h;

	private boolean right = false;

	public TableRender(boolean r, int... h) {
		this(h);
		right = r;
	}

	JLabel rectLabel = new JLabel();
	JLabel rigthRectLabel = new JLabel();

	public TableRender(int... h) {
		this.h = h;

		rectLabel.setOpaque(false);
		rectLabel.setUI(new MetalLabelUI() {
			@Override
			public void paint(Graphics g, JComponent c) {
				Color color = rectLabel.getBackground();
				g.setColor(color);
				if (g != null && g instanceof Graphics2D) {
					Graphics2D g2 = (Graphics2D) g;
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

					RoundRectangle2D.Double rect = new RoundRectangle2D.Double();
					rect.setRoundRect(0, 0, c.getWidth(), c.getHeight(), 15, 15);

					g2.fill(rect);

					g2.fillRect(10, 0, c.getWidth() - 10, c.getHeight());
				}

				super.paint(g, c);
			}
		});

		rigthRectLabel.setOpaque(false);
		rigthRectLabel.setUI(new MetalLabelUI() {
			@Override
			public void paint(Graphics g, JComponent c) {
				Color color = rigthRectLabel.getBackground();
				g.setColor(color);
				if (g != null && g instanceof Graphics2D) {
					Graphics2D g2 = (Graphics2D) g;
					//
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

					//
					RoundRectangle2D.Double rect = new RoundRectangle2D.Double();
					rect.setRoundRect(0, 0, c.getWidth(), c.getHeight(), 15, 15);

					g2.fill(rect);

					g2.fillRect(0, 0, 10, c.getHeight());
				}

				super.paint(g, c);
			}
		});
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (c instanceof JLabel) {
			JLabel label = (JLabel) c;
			label.setHorizontalAlignment(h[column]);
			label.setBorder(BorderFactory.createEmptyBorder());
			label.setOpaque(true);
			if (column == 0) {
				if (isSelected) {
					rectLabel.setBackground(table.getSelectionBackground());
					rectLabel.setText(label.getText());
					rectLabel.setForeground(table.getSelectionForeground());
					rectLabel.setFont(label.getFont());
					rectLabel.setHorizontalAlignment(h[column]);
					rectLabel.setBorder(BorderFactory.createEmptyBorder());
					return rectLabel;
				}
			}

			if (column == table.getColumnCount() - 1) {
				if (isSelected && right) {
					rigthRectLabel.setBackground(table.getSelectionBackground());
					rigthRectLabel.setText(label.getText());
					rigthRectLabel.setForeground(table.getSelectionForeground());
					rigthRectLabel.setFont(label.getFont());
					rigthRectLabel.setHorizontalAlignment(h[column]);
					rigthRectLabel.setBorder(BorderFactory.createEmptyBorder());
					return rigthRectLabel;
				}
			}

		}
		return c;
	}
}
