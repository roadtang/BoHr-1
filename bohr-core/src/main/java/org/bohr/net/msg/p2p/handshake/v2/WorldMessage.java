/**
 * Copyright (c) 2019 The Bohr Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr.net.msg.p2p.handshake.v2;

import java.util.Arrays;

import org.bohr.Network;
import org.bohr.crypto.Hex;
import org.bohr.crypto.Key;
import org.bohr.net.msg.MessageCode;

public class WorldMessage extends HandshakeMessage {

    public WorldMessage(Network network, short networkVersion, String peerId, int port,
            String clientId, String[] capabilities, long latestBlockNumber,
            byte[] secret, Key coinbase) {
        super(MessageCode.HANDSHAKE_WORLD, null, network, networkVersion, peerId, port, clientId,
                capabilities, latestBlockNumber, secret, coinbase);
    }

    public WorldMessage(byte[] encoded) {
        super(MessageCode.HANDSHAKE_WORLD, null, encoded);
    }

    @Override
    public String toString() {
        return "WorldMessage{" +
                "network=" + network +
                ", networkVersion=" + networkVersion +
                ", peerId='" + peerId + '\'' +
                ", port=" + port +
                ", clientId='" + clientId + '\'' +
                ", capabilities=" + Arrays.toString(capabilities) +
                ", latestBlockNumber=" + latestBlockNumber +
                ", secret=" + Hex.encode(secret) +
                ", timestamp=" + timestamp +
                '}';
    }
}