/**
 * Copyright (c) 2019 The Bohr Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr.event;

public interface PubSubSubscriber {

    void onPubSubEvent(PubSubEvent event);

}
