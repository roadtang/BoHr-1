/**
 * Copyright (c) 2019 The Bohr Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr.vm.client;

import java.math.BigInteger;

import org.bohr.core.Amount;
import org.bohr.core.Unit;

/**
 * Conversion between ETH and Bohr. The idea is to make 1 Bohr = 1 ETH from a
 * smart contract viewpoint.
 */
public class Conversion {

    private static final BigInteger TEN_POW_NINE = BigInteger.TEN.pow(9);

    public static Amount weiToAmount(BigInteger value) {
        BigInteger nanoBohr = value.divide(TEN_POW_NINE);
        return Amount.of(nanoBohr.longValue(), Unit.NANO_BOHR);
    }

    public static BigInteger amountToWei(Amount value) {
        return value.toBigInteger().multiply(TEN_POW_NINE);
    }

    public static BigInteger amountToWei(long nanoBohr) {
        return BigInteger.valueOf(nanoBohr).multiply(TEN_POW_NINE);
    }
}
