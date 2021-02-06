package org.bohr.gui.laf;

import javax.swing.UIDefaults;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class DefaultLookAndFeel extends MetalLookAndFeel {
	private static final long serialVersionUID = 1L;

	@Override
	protected void initClassDefaults(UIDefaults table) {
		super.initClassDefaults(table);

		String packageName = "org.bohr.gui.laf.Default";

		// ButtonUI
		Object[] buttonUI = { "ButtonUI", packageName + "ButtonUI" };
		table.putDefaults(buttonUI);

		// TextFieldUI
		Object[] textFieldUI = { "TextFieldUI", packageName + "TextFieldUI" };
		table.putDefaults(textFieldUI);

		// PasswordFieldUI
		Object[] passwordFieldUI = { "PasswordFieldUI", packageName + "PasswordFieldUI" };
		table.putDefaults(passwordFieldUI);

		// ComboBoxUI
		Object[] comboBoxUI = { "ComboBoxUI", packageName + "ComboBoxUI" };
		table.putDefaults(comboBoxUI);

		// OptionPaneUI
		Object[] optionPaneUI = { "OptionPaneUI", packageName + "OptionPaneUI" };
		table.putDefaults(optionPaneUI);
	}

	@Override
	protected void initComponentDefaults(UIDefaults table) {
		super.initComponentDefaults(table);

		// panel
		Object[] panel = { "Panel.background", new ColorUIResource(0xffffff) };
		table.putDefaults(panel);

		// OptionPane
//		Object[] optionPane = { "OptionPane.background", new ColorUIResource(0xffffff), "OptionPane.border",
//				new EmptyBorder(10, 10, 10, 10) };
//		table.putDefaults(panel);
	}
}
