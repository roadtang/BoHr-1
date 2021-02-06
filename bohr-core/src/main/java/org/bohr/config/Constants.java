/**
 * Copyright (c) 2019 The Bohr Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr.config;

import org.bohr.crypto.CryptoException;
import org.bohr.crypto.Hex;
import org.bohr.crypto.Key;

import java.security.spec.InvalidKeySpecException;

public class Constants {

    /**
     * Default data directory.
     */
    public static final String DEFAULT_DATA_DIR = ".";

    /**
     * Network versions.
     */
    public static final short MAINNET_VERSION = 0;
    public static final short TESTNET_VERSION = 0;
    public static final short DEVNET_VERSION = 0;

    /**
     * Name of this client.
     */
    public static final String CLIENT_NAME = "Bohr";

    /**
     * Version of this client.
     */
    public static final String CLIENT_VERSION = "1.0.0";

    /**
     * Algorithm name for the 256-bit hash.
     */
    public static final String HASH_ALGORITHM = "BLAKE2B-256";

    /**
     * Name of the config directory.
     */
    public static final String CONFIG_DIR = "config";

    /**
     * Name of the database directory.
     */
    public static final String DATABASE_DIR = "database";

    /**
     * The default IP port for p2p protocol
     */
    public static final int DEFAULT_P2P_PORT = 5381;

    /**
     * The default IP port for RESTful API.
     */
    public static final int DEFAULT_API_PORT = 5391;

    /**
     * The default user agent for HTTP requests.
     */
    public static final String DEFAULT_USER_AGENT = "Mozilla/4.0";

    /**
     * The default connect timeout.
     */
    public static final int DEFAULT_CONNECT_TIMEOUT = 4000;

    /**
     * The default read timeout.
     */
    public static final int DEFAULT_READ_TIMEOUT = 4000;

    /**
     * The number of blocks per day.
     */
    public static final long BLOCKS_PER_DAY = 20L * 60L * 24L;

    /**
     * The number of blocks per year.
     */
    public static final long BLOCKS_PER_YEAR = 20L * 60L * 24L * 365L;

    /**
     * The public-private key pair for signing coinbase transactions.
     */
    public static final Key COINBASE_KEY;

    /**
     * Address bytes of {@link this#COINBASE_KEY}. This is stored as a cache to
     * avoid redundant h160 calls.
     */
    public static final byte[] COINBASE_ADDRESS;

    /**
     * The public-private key pair of the genesis validator.
     */
    public static final Key DEVNET_KEY;

    public static final byte[] DELEGATE_BURN_ADDRESS = Hex.decode0x("0x000000000000000000000000000000000000dead");

    public static final byte[] BOHR_GAME_REWARD_GENERATE_ADDRESS = Hex.decode0x("0xeaf41d36eb93293595280efca1b006225325a444");

    static {
        try {
            COINBASE_KEY = new Key(Hex.decode0x(
                    "0x302e020100300506032b6570042204205555be7bbc469df67a50438dfdadf29689b956646b1d0535f4101f0329cb8019"));
            COINBASE_ADDRESS = COINBASE_KEY.toAddress();
            DEVNET_KEY = new Key(Hex.decode0x(
                    "0x302e020100300506032b657004220420b3cbba762721ef8c0175bc987f8dd350f7db327cbec8828f93aac996183c720b"));

        } catch (InvalidKeySpecException e) {
            throw new CryptoException(e);
        }
    }

    private Constants() {
    }
}
