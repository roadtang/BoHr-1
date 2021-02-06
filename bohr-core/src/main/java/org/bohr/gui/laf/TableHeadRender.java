package org.bohr.gui.laf;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;

import sun.swing.table.DefaultTableCellHeaderRenderer;

public class TableHeadRender extends DefaultTableCellHeaderRenderer {
	private static final long serialVersionUID = 1L;
	private int[] h;

	public TableHeadRender(int... h) {
		this.h = h;
		setVerticalAlignment(TOP);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (c instanceof JLabel) {
			JLabel label = (JLabel) c;
			label.setHorizontalAlignment(h[column]);
			label.setBorder(BorderFactory.createEmptyBorder());
		}
		return c;
	}
}
