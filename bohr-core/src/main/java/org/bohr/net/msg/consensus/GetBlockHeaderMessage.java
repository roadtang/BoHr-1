/**
 * Copyright (c) 2019 The Bohr Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr.net.msg.consensus;

import org.bohr.net.msg.Message;
import org.bohr.net.msg.MessageCode;
import org.bohr.util.SimpleDecoder;
import org.bohr.util.SimpleEncoder;

public class GetBlockHeaderMessage extends Message {

    private final long number;

    public GetBlockHeaderMessage(long number) {
        super(MessageCode.GET_BLOCK_HEADER, BlockHeaderMessage.class);

        this.number = number;

        SimpleEncoder enc = new SimpleEncoder();
        enc.writeLong(number);
        this.body = enc.toBytes();
    }

    public GetBlockHeaderMessage(byte[] body) {
        super(MessageCode.GET_BLOCK_HEADER, BlockHeaderMessage.class);

        SimpleDecoder dec = new SimpleDecoder(body);
        this.number = dec.readLong();

        this.body = body;
    }

    public long getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return "GetBlockHeaderMessage [number=" + number + "]";
    }
}
