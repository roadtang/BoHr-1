/**
 * Copyright (c) 2019 The Bohr Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr.core;

import java.math.BigInteger;

import static java.util.Arrays.stream;

public enum Unit {
    NANO_BOHR(0, "nBR"),

    MICRO_BOHR(3, "μBR"),

    MILLI_BOHR(6, "mBR"),

    BOHR(9, "BR"),

    KILO_BOHR(12, "kBR"),

    MEGA_BOHR(15, "MBR");

    public final int exp;
    public final long factor;
    public final String symbol;

    Unit(int exp, String symbol) {
        this.exp = exp;
        this.factor = BigInteger.TEN.pow(exp).longValueExact();
        this.symbol = symbol;
    }

    /**
     * Decode the unit from symbol.
     *
     * @param symbol
     *            the symbol text
     * @return a Unit object if valid; otherwise false
     */
    public static Unit of(String symbol) {
        return stream(values()).filter(v -> v.symbol.equals(symbol)).findAny().orElse(null);
    }
}
