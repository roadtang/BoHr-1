/**
 * Copyright (c) 2019 The Bohr Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr.gui;

import javax.swing.text.JTextComponent;

/**
 * Placeholder of a Swing text component based on ${@link TextPrompt}.
 */
public class PlaceHolder extends TextPrompt {

    private static final long serialVersionUID = -1350764114359129512L;

    public PlaceHolder(String text, JTextComponent component) {
        super(text, component);
        changeAlpha(0.5f);
    }
}