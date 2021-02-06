/**
 * Copyright (c) 2019 The Bohr Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr.vm.client;

import java.math.BigInteger;

import org.ethereum.vm.client.Block;
import org.bohr.core.BlockHeader;

/**
 * Facade for BlockHeader -> Block
 */
public class BohrBlock implements Block {

    private final long blockGasLimit;
    private final BlockHeader blockHeader;

    public BohrBlock(BlockHeader block, long blockGasLimit) {
        this.blockHeader = block;
        this.blockGasLimit = blockGasLimit;
    }

    @Override
    public long getGasLimit() {
        return blockGasLimit;
    }

    @Override
    public byte[] getParentHash() {
        return blockHeader.getParentHash();
    }

    @Override
    public byte[] getCoinbase() {
        return blockHeader.getCoinbase();
    }

    @Override
    public long getTimestamp() {
        return blockHeader.getTimestamp();
    }

    @Override
    public long getNumber() {
        return blockHeader.getNumber();
    }

    @Override
    public BigInteger getDifficulty() {
        return BigInteger.ONE;
    }
}
