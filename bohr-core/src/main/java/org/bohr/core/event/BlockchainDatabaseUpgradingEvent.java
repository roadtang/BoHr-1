/**
 * Copyright (c) 2019 The Bohr Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr.core.event;

import org.bohr.event.PubSubEvent;

public class BlockchainDatabaseUpgradingEvent implements PubSubEvent {

    public final Long loaded;

    public final Long total;

    public BlockchainDatabaseUpgradingEvent(Long loaded, Long total) {
        this.loaded = loaded;
        this.total = total;
    }
}
