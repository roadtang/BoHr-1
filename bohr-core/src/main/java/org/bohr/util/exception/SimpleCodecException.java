/**
 * Copyright (c) 2019 The Bohr Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr.util.exception;

public class SimpleCodecException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public SimpleCodecException() {
    }

    public SimpleCodecException(String s) {
        super(s);
    }

    public SimpleCodecException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public SimpleCodecException(Throwable throwable) {
        super(throwable);
    }

    public SimpleCodecException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
