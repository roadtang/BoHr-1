/**
 * Copyright (c) 2019 The Bohr Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bohr.Network;
import org.bohr.core.Fork;
import org.apache.commons.collections4.MapUtils;

public class MainnetConfig extends AbstractConfig {

    private static final Map<Long, byte[]> checkpoints;
    static {
        HashMap<Long, byte[]> initCheckpoints = new HashMap<>();

        checkpoints = MapUtils.unmodifiableMap(initCheckpoints);
    }

    public MainnetConfig(String dataDir) {
        super(dataDir, Network.MAINNET, Constants.MAINNET_VERSION);

        this.forkUniformDistributionEnabled = true;
        this.forkVirtualMachineEnabled = true;
        this.forkVotingPrecompiledUpgradeEnabled = true;
    }

    @Override
    public Map<Long, byte[]> checkpoints() {
        return checkpoints;
    }

    @Override
    public Map<Fork, Long> manuallyActivatedForks() {
        return Collections.emptyMap();
    }
}