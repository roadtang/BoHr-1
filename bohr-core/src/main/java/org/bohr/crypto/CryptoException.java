/**
 * Copyright (c) 2019 The Bohr Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr.crypto;

public class CryptoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CryptoException() {
    }

    public CryptoException(String msg) {
        super(msg);
    }

    public CryptoException(Throwable cause) {
        super(cause);
    }

    public CryptoException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
