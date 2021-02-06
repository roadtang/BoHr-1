package org.bohr.gui.laf;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicOptionPaneUI;

public class DefaultOptionPaneUI extends BasicOptionPaneUI {
	public static ComponentUI createUI(JComponent c) {
		return new DefaultOptionPaneUI();
	}

	@Override
	protected void installDefaults() {
		super.installDefaults();
		optionPane.setBackground(Color.WHITE);
	}

}
