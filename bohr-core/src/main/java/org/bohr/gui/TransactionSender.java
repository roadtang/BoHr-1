/**
 * Copyright (c) 2019 The Bohr Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr.gui;

import org.bohr.Kernel;
import org.bohr.Network;
import org.bohr.core.Amount;
import org.bohr.core.PendingManager;
import org.bohr.core.Transaction;
import org.bohr.core.TransactionType;
import org.bohr.gui.model.WalletAccount;
import org.bohr.util.TimeUtil;

public class TransactionSender {

    public static PendingManager.ProcessingResult send(Kernel kernel, WalletAccount account, TransactionType type,
                                                       byte[] to, Amount value, Amount fee, byte[] data) {
        return send(kernel, account, type, to, value, fee, data, 0, Amount.ZERO);
    }

    public static PendingManager.ProcessingResult send(Kernel kernel, WalletAccount account, TransactionType type,
                                                       byte[] to, Amount value, Amount fee, byte[] data, long gas, Amount gasPrice) {
        PendingManager pendingMgr = kernel.getPendingManager();

        Network network = kernel.getConfig().network();
        byte[] from = account.getKey().toAddress();
        long nonce = pendingMgr.getNonce(from);
        long timestamp = TimeUtil.currentTimeMillis();
        Transaction tx = new Transaction(network, type, to, value, fee, nonce, timestamp, data, gas, gasPrice);
        tx.sign(account.getKey());

        return pendingMgr.addTransactionSync(tx);
    }
}
