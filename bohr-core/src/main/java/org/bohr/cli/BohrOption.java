/**
 * Copyright (c) 2019 The Bohr Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr.cli;

/**
 * Bohr launcher options.
 */
public enum BohrOption {

    HELP("help"),

    VERSION("version"),

    ACCOUNT("account"),

    CHANGE_PASSWORD("changepassword"),

    DATA_DIR("datadir"),

    COINBASE("coinbase"),

    PASSWORD("password"),

    DUMP_PRIVATE_KEY("dumpprivatekey"),

    IMPORT_PRIVATE_KEY("importprivatekey"),

    NETWORK("network"),

    HD_WALLET("hdwallet"),

    REINDEX("reindex");

    private final String name;

    BohrOption(String s) {
        name = s;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
