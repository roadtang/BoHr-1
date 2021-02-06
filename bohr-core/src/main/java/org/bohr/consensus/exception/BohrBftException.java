/**
 * Copyright (c) 2019 The Bohr Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr.consensus.exception;

public class BohrBftException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public BohrBftException() {
    }

    public BohrBftException(String s) {
        super(s);
    }

    public BohrBftException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public BohrBftException(Throwable throwable) {
        super(throwable);
    }

    public BohrBftException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
