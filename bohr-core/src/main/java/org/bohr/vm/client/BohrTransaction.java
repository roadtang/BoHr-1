/**
 * Copyright (c) 2019 The Bohr Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr.vm.client;

import static org.bohr.vm.client.Conversion.amountToWei;

import java.math.BigInteger;

import org.ethereum.vm.client.Transaction;
import org.bohr.core.TransactionType;

/**
 * Facade for Transaction -> Transaction
 */
public class BohrTransaction implements Transaction {

    private final org.bohr.core.Transaction transaction;

    public BohrTransaction(org.bohr.core.Transaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public boolean isCreate() {
        return transaction.getType().equals(TransactionType.CREATE);
    }

    @Override
    public byte[] getFrom() {
        return transaction.getFrom();
    }

    @Override
    public byte[] getTo() {
        return transaction.getTo();
    }

    @Override
    public long getNonce() {
        return transaction.getNonce();
    }

    @Override
    public BigInteger getValue() {
        return amountToWei(transaction.getValue());
    }

    @Override
    public byte[] getData() {
        return transaction.getData();
    }

    @Override
    public long getGas() {
        return transaction.getGas();
    }

    @Override
    public BigInteger getGasPrice() {
        return amountToWei(transaction.getGasPrice());
    }
}
