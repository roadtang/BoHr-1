/**
 * Copyright (c) 2019 The Bohr Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr.net.msg.consensus;

import org.bohr.consensus.Proof;
import org.bohr.net.msg.Message;
import org.bohr.net.msg.MessageCode;

public class NewViewMessage extends Message {

    private final Proof proof;

    public NewViewMessage(Proof proof) {
        super(MessageCode.BFT_NEW_VIEW, null);

        this.proof = proof;

        // TODO: consider wrapping by simple codec
        this.body = proof.toBytes();
    }

    public NewViewMessage(byte[] body) {
        super(MessageCode.BFT_NEW_VIEW, null);

        this.proof = Proof.fromBytes(body);

        this.body = body;
    }

    public Proof getProof() {
        return proof;
    }

    public long getHeight() {
        return proof.getHeight();
    }

    public int getView() {
        return proof.getView();
    }

    @Override
    public String toString() {
        return "BFTNewViewMessage [proof=" + proof + "]";
    }
}