package org.bohr.gui.uiUtils;

import java.awt.Font;
import java.io.InputStream;

import javax.swing.JComponent;

import org.bohr.gui.SwingUtil;

public class FontUtils {
	private static Font font;

	public static Font getFont() {
		return  new Font("Default",Font.PLAIN, 14);
	}

	public static void setPLAINFont(JComponent c, int size) {
		c.setFont(getFont().deriveFont(Font.PLAIN, size));
	}

	public static void setBOLDFont(JComponent c, int size) {
		c.setFont(getFont().deriveFont(Font.BOLD, size));
	}
}
