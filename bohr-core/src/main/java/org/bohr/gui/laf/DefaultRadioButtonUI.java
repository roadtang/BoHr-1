package org.bohr.gui.laf;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.plaf.metal.MetalRadioButtonUI;

import org.bohr.gui.SwingUtil;

public class DefaultRadioButtonUI extends MetalRadioButtonUI {
	@Override
	public void installDefaults(AbstractButton button) {
		super.installDefaults(button);
		button.setOpaque(false);

		ImageIcon noSelectIcon = SwingUtil.loadImage("r2", 16, 16);

		ImageIcon select_icon = SwingUtil.loadImage("r1", 16, 16);

		button.setIcon(noSelectIcon);

//		button.setRolloverIcon(rolloverIcon);

		button.setRolloverSelectedIcon(select_icon);
		button.setSelectedIcon(select_icon);

//		button.setPressedIcon(pressedIcon);

		button.setDisabledIcon(select_icon);
		button.setDisabledSelectedIcon(select_icon);
	}

	@Override
	protected void paintFocus(Graphics g, Rectangle t, Dimension d) {
//		super.paintFocus(g, t, d);
	}
}
