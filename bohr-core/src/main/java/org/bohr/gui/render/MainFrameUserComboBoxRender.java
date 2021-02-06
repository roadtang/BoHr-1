package org.bohr.gui.render;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import org.bohr.gui.SwingUtil;
import org.bohr.gui.model.WalletAccount;

public class MainFrameUserComboBoxRender extends DefaultListCellRenderer {
	private static final long serialVersionUID = 1L;

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		if (value != null && value instanceof WalletAccount) {
			WalletAccount v = (WalletAccount) value;
			value = SwingUtil.getAddressAbbr_new(v.getKey().toAddress(),8);
		}

		return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	}
}
