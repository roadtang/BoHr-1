/**
 * Copyright (c) 2019 The Bohr Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr.net.msg.consensus;

import org.bohr.consensus.Proposal;
import org.bohr.net.msg.Message;
import org.bohr.net.msg.MessageCode;

public class ProposalMessage extends Message {

    private final Proposal proposal;

    public ProposalMessage(Proposal proposal) {
        super(MessageCode.BFT_PROPOSAL, null);

        this.proposal = proposal;

        // TODO: consider wrapping by simple codec
        this.body = proposal.toBytes();
    }

    public ProposalMessage(byte[] body) {
        super(MessageCode.BFT_PROPOSAL, null);

        this.proposal = Proposal.fromBytes(body);

        this.body = body;
    }

    public Proposal getProposal() {
        return proposal;
    }

    @Override
    public String toString() {
        return "BFTProposalMessage: " + proposal;
    }
}
