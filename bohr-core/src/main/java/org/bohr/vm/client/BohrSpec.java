/**
 * Copyright (c) 2019 The Bohr Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr.vm.client;

import org.ethereum.vm.chainspec.ConstantinopleSpec;
import org.ethereum.vm.chainspec.PrecompiledContracts;

public class BohrSpec extends ConstantinopleSpec {

    private static final PrecompiledContracts precompiledContracts = new BohrPrecompiledContracts();

    @Override
    public PrecompiledContracts getPrecompiledContracts() {
        return precompiledContracts;
    }
}
