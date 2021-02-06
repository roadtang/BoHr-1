/**
 * Copyright (c) 2019 The Bohr Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr.message;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * This enum encapsulates available resource bundles of messages and an utility
 * function getDefaultBundle for deciding the default locale
 *
 * <p>
 * The locale used is the current value of the default locale for this instance
 * of the Java Virtual Machine.
 * </p>
 */
public enum ResourceBundles {

	GUI_MESSAGES("org/bohr/gui/messages"), CLI_MESSAGES("org/bohr/cli/messages");

	private final String bundleName;

	ResourceBundles(String bundleName) {
		this.bundleName = bundleName;
	}

	public String getBundleName() {
		return bundleName;
	}

	public static ResourceBundle getDefaultBundle(ResourceBundles bundleName) {

		return ResourceBundle.getBundle(bundleName.getBundleName(), Locale.ENGLISH);

	}

	@Override
	public String toString() {
		return bundleName;
	}
}
