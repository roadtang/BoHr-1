/**
 * Copyright (c) 2019 The Bohr Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr.message;

import java.util.Locale;
import java.util.ResourceBundle;

public final class GuiMessages {

	private static final ResourceBundle RESOURCES = ResourceBundles.getDefaultBundle(ResourceBundles.GUI_MESSAGES);

	private GuiMessages() {
	}

	public static String get(String key, Object... args) {
		return MessageFormatter.get(RESOURCES, key, args);
	}
}