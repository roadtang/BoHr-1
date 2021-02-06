package org.bohr.gui.laf;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.CellRendererPane;
import javax.swing.ComboBoxEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalButtonUI;
import javax.swing.plaf.metal.MetalComboBoxEditor;
import javax.swing.plaf.metal.MetalComboBoxUI;

import org.bohr.gui.uiUtils.FontUtils;

public class DefaultComboBoxUI extends MetalComboBoxUI {
	public static ComponentUI createUI(JComponent c) {
		return new DefaultComboBoxUI();
	}

	class SelectedListCellRender extends JLabel implements ListCellRenderer {

		private static final long serialVersionUID = -1296293768041139988L;

		public SelectedListCellRender() {
			super();
			setOpaque(true);

			setBorder(new EmptyBorder(0, 0, 0, 0));
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {

			if(value.toString().length()<=0) {
				setText(" ");
			}else {
				setText(value.toString());
			}
			

			setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
			setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());

			return this;
		}
	}

	@Override
	public void installUI(JComponent c) {
		super.installUI(c);

		comboBox.setOpaque(false);
		comboBox.setForeground(new Color(0xffffff));
		FontUtils.setPLAINFont(comboBox, 16);
		listBox.setBackground(new Color(0x363636));
		listBox.setForeground(new Color(0xffffff));
		listBox.setCellRenderer(new SelectedListCellRender());
		listBox.setSelectionBackground(new Color(0xffff00));
		listBox.setFont(listBox.getFont().deriveFont(10F));

		currentValuePane = new CellRendererPane() {
			@Override
			public void paintComponent(Graphics g, Component c, Container p, int x, int y, int w, int h,
					boolean shouldValidate) {
				if (c == null) {
					if (p != null) {
						Color oldColor = g.getColor();
						g.setColor(p.getBackground());
						g.fillRect(x, y, w, h);
						g.setColor(oldColor);
					}
					return;
				}

				if (c.getParent() != this) {
					this.add(c);
				}

				c.setBounds(x, y, w, h);

				if (shouldValidate) {
					c.validate();
				}

				boolean wasDoubleBuffered = false;
				if ((c instanceof JComponent) && ((JComponent) c).isDoubleBuffered()) {
					wasDoubleBuffered = true;
					((JComponent) c).setDoubleBuffered(false);
				}

				Graphics cg = g.create(x, y, w, h);
				try {
					if (c instanceof JComponent) {
						JComponent cc = (JComponent) c;
						cc.setOpaque(false);
					}

					c.paint(cg);
				} finally {
					cg.dispose();
				}

				if (wasDoubleBuffered && (c instanceof JComponent)) {
					((JComponent) c).setDoubleBuffered(true);
				}

				c.setBounds(-w, -h, 0, 0);
			}
		};
	}

	@Override
	protected ComboBoxEditor createEditor() {
		return new DefaultComboBoxEditor();
	}

	class DefaultComboBoxEditor extends MetalComboBoxEditor {
		DefaultComboBoxEditor() {
			super();
			editor.setBorder(new EmptyBorder(0, 10, 0, 0));
			editor.setOpaque(false);
		}
	}

	@Override
	public void update(Graphics g, JComponent c) {
		paintBackground(g, c.getBounds());
		paint(g, c);
	}

	@Override
	public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
//		super.paintCurrentValueBackground(g, bounds, hasFocus);
	}

	public void paintBackground(Graphics g, Rectangle bounds) {
		if (g != null && g instanceof Graphics2D) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			RoundRectangle2D.Double rect = new RoundRectangle2D.Double();
			rect.setRoundRect(0, 0, bounds.width, bounds.height, 15, 15);

			g2.setColor(new Color(0x363636));
			g2.fill(rect);
		}
	}

	@Override
	public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
		bounds.x = 10;
		super.paintCurrentValue(g, bounds, false);
	}

	@Override
	protected Rectangle rectangleForCurrentValue() {
		int width = comboBox.getWidth();
		int height = comboBox.getHeight();
		Insets insets = getInsets();
		int buttonSize = height - (insets.top + insets.bottom);
		if (arrowButton != null) {
			buttonSize = arrowButton.getWidth();
		}
		if (isLeftToRight(comboBox)) {
			return new Rectangle(insets.left, insets.top, width - (insets.left + insets.right + buttonSize) + 10,
					height - (insets.top + insets.bottom));
		} else {
			return new Rectangle(insets.left + buttonSize, insets.top,
					width - (insets.left + insets.right + buttonSize) + 10, height - (insets.top + insets.bottom));
		}
	}

	boolean isLeftToRight(Component c) {
		return c.getComponentOrientation().isLeftToRight();
	}

	@Override
	protected JButton createArrowButton() {
		JButton b = new JButton();
		b.setUI(new MetalButtonUI() {
			@Override
			public void paint(Graphics g, JComponent c) {
				if (g != null && g instanceof Graphics2D) {
					Graphics2D g2 = (Graphics2D) g;

					//
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

					g2.setColor(new Color(0x444659));

					int h = c.getHeight();
					int h1 = 7;
					int y = (h - h1) / 2;

					g2.drawLine(21, y + h1, 14, y);
					g2.drawLine(21, y + h1, 28, y);

				}

			}

			@Override
			public Dimension getMaximumSize(JComponent c) {
				return new Dimension(45, super.getMaximumSize(c).height);
			}

			@Override
			public Dimension getMinimumSize(JComponent c) {
				return new Dimension(45, super.getMinimumSize(c).height);
			}

			@Override
			public Dimension getPreferredSize(JComponent c) {
				return new Dimension(45, super.getPreferredSize(c).height);
			}
		});

		b.setBorder(null);
		b.setOpaque(false);
		b.setFocusPainted(false);
		b.setFocusable(false);
		b.setContentAreaFilled(false);
		return b;
	}
}
