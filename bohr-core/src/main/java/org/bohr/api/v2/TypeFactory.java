/**
 * Copyright (c) 2019 The Bohr Developers
 * <p>
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr.api.v2;

import org.ethereum.vm.LogInfo;
import org.bohr.Kernel;
import org.bohr.api.v2.model.TransactionType;
import org.bohr.api.v2.model.*;
import org.bohr.core.*;
import org.bohr.core.state.Account;
import org.bohr.core.state.Delegate;
import org.bohr.crypto.Hex;
import org.bohr.net.Peer;
import org.bohr.vm.client.BohrInternalTransaction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.bohr.core.TransactionType.*;

public class TypeFactory {

    public static AccountType accountType(Account account, int transactionCount, int internalTransactionCount,
                                          int pendingTransactionCount) {
        return new AccountType()
                .address(Hex.encode0x(account.getAddress()))
                .available(encodeAmount(account.getAvailable()))
                .locked(encodeAmount(account.getLocked()))
                .nonce(String.valueOf(account.getNonce()))
                .transactionCount(transactionCount)
                .internalTransactionCount(internalTransactionCount)
                .pendingTransactionCount(pendingTransactionCount);
    }

    public static BlockType blockType(Block block, Transaction coinbaseTransaction,Transaction rewardTransaction,Transaction burnTransaction) {
        List<Transaction> txs = block.getTransactions();
        if (coinbaseTransaction != null) {
            txs.add(0, coinbaseTransaction);
        }
        if (rewardTransaction != null) {
            txs.add(1, rewardTransaction);
        }
        if (burnTransaction != null) {
            txs.add(2, burnTransaction);
        }
        return new BlockType()
                .hash(Hex.encode0x(block.getHash()))
                .number(String.valueOf(block.getNumber()))
                .view(block.getView())
                .coinbase(Hex.encode0x(block.getCoinbase()))
                .parentHash(Hex.encode0x(block.getParentHash()))
                .timestamp(String.valueOf(block.getTimestamp()))
                .transactionsRoot(Hex.encode0x(block.getTransactionsRoot()))
                .resultsRoot(Hex.encode0x(block.getResultsRoot()))
                .stateRoot(Hex.encode0x(block.getStateRoot()))
                .data(Hex.encode0x(block.getData()))
                .transactions(txs.stream().map(TypeFactory::transactionType).collect(Collectors.toList()));
    }

    public static DelegateType delegateType(BlockchainImpl.ValidatorStats validatorStats, Delegate delegate,
                                            boolean isValidator) {
        return new DelegateType()
                .address(Hex.encode0x(delegate.getAddress()))
                .name(delegate.getNameString())
                .registeredAt(String.valueOf(delegate.getRegisteredAt()))
                .votes(encodeAmount(delegate.getVotes()))
                .blocksForged(String.valueOf(validatorStats.getBlocksForged()))
                .turnsHit(String.valueOf(validatorStats.getTurnsHit()))
                .turnsMissed(String.valueOf(validatorStats.getTurnsMissed()))
                .validator(isValidator);
    }

    public static List<AccountVoteType> accountVotes(Blockchain blockchain, byte[] address) {
        Set<String> validators = new HashSet<>(blockchain.getValidators());
        return blockchain.getDelegateState()
                .getDelegates()
                .parallelStream()
                .map(delegate -> accountVoteType(blockchain, address, delegate,
                        validators.contains(delegate.getAddressString())))
                .filter(accountVote -> !accountVote.getVotes().equals("0"))
                .collect(Collectors.toList());
    }

    public static AccountVoteType accountVoteType(Blockchain blockchain, byte[] address, Delegate delegate,
                                                  Boolean isValidator) {
        return new AccountVoteType()
                .delegate(
                        TypeFactory
                                .delegateType(blockchain.getValidatorStats(delegate.getAddress()), delegate,
                                        isValidator))
                .votes(blockchain.getDelegateState().getVote(address, delegate.getAddress()).toString());
    }

    public static InfoType infoType(Kernel kernel) {
        return new InfoType()
                .network(InfoType.NetworkEnum.fromValue(kernel.getConfig().network().name()))
                .capabilities(kernel.getConfig().getClientCapabilities().toList())
                .clientId(kernel.getConfig().getClientId())
                .coinbase(Hex.encode0x(kernel.getCoinbase().toAddress()))
                .latestBlockNumber(String.valueOf(kernel.getBlockchain().getLatestBlockNumber()))
                .latestBlockHash(Hex.encode0x(kernel.getBlockchain().getLatestBlockHash()))
                .activePeers(kernel.getChannelManager().getActivePeers().size())
                .pendingTransactions(kernel.getPendingManager().getPendingTransactions().size());
    }

    public static PeerType peerType(Peer peer) {
        return new PeerType()
                .ip(peer.getIp())
                .port(peer.getPort())
                .networkVersion((int) peer.getNetworkVersion())
                .clientId(peer.getClientId())
                .peerId(Hex.PREF + peer.getPeerId())
                .latestBlockNumber(String.valueOf(peer.getLatestBlockNumber()))
                .latency(String.valueOf(peer.getLatency()))
                .capabilities(Arrays.asList(peer.getCapabilities()));
    }

    public static TransactionLimitsType transactionLimitsType(Kernel kernel,
                                                              org.bohr.core.TransactionType transactionType) {
        return new TransactionLimitsType()
                .maxTransactionDataSize(kernel.getConfig().spec().maxTransactionDataSize(transactionType))
                .minTransactionFee(encodeAmount(
                        transactionType.equals(CREATE) || transactionType.equals(CALL) ? Amount.ZERO
                                : kernel.getConfig().spec().minTransactionFee()))
                .minDelegateBurnAmount(encodeAmount(
                        transactionType.equals(DELEGATE) ? Amount.ZERO : null));
    }

    public static TransactionType transactionType(Transaction tx) {
        TransactionType txType = new TransactionType();

        txType.hash(Hex.encode0x(tx.getHash()))
                .type(TransactionType.TypeEnum.fromValue(tx.getType().name()))
                .from(Hex.encode0x(tx.getFrom()))
                .to(Hex.encode0x(tx.getTo()))
                .value(encodeAmount(tx.getValue()))
                .fee(encodeAmount(tx.getFee()))
                .nonce(String.valueOf(tx.getNonce()))
                .timestamp(String.valueOf(tx.getTimestamp()))
                .data(Hex.encode0x(tx.getData()))
                .gas(String.valueOf(tx.getGas()))
                .gasPrice(encodeAmount(tx.getGasPrice()));

        return txType;
    }

    public static TransactionResultType transactionResultType(TransactionResult result, Amount fee, byte[] contractAddress, long blockNumber) {
        return new TransactionResultType()
                .blockNumber(Long.toString(blockNumber))
                .code(result.getCode().name())
                .logs(result.getLogs().stream().map(TypeFactory::logInfoType).collect(Collectors.toList()))
                .gas(Long.toString(result.getGas()))
                .gasUsed(String.valueOf(result.getGasUsed()))
                .gasPrice(encodeAmount(result.getGasPrice()))
                .fee(encodeAmount(fee))
                .code(result.getCode().name())
                .internalTransactions(result.getInternalTransactions().stream()
                        .map(TypeFactory::internalTransactionType).collect(Collectors.toList()))
                .returnData(Hex.encode0x(result.getReturnData()))
                .contractAddress(contractAddress == null ? null : Hex.encode0x(contractAddress));
    }

    private static LogInfoType logInfoType(LogInfo log) {
        return new LogInfoType()
                .address(Hex.encode0x(log.getAddress()))
                .data(Hex.encode0x(log.getData()))
                .topics(log.getTopics().stream().map(topic -> Hex.encode0x(topic.getData()))
                        .collect(Collectors.toList()));
    }

    public static InternalTransactionType internalTransactionType(BohrInternalTransaction it) {
        return new InternalTransactionType()
                .rootTransactionHash(Hex.encode0x(it.getRootTxHash()))
                .rejected(it.isRejected())
                .depth(Integer.toString(it.getDepth()))
                .index(Integer.toString(it.getIndex()))
                .type(it.getType())
                .from(Hex.encode0x(it.getFrom()))
                .to(Hex.encode0x(it.getTo()))
                .nonce(Long.toString(it.getNonce()))
                .gas(Long.toString(it.getGas()))
                .gasPrice(encodeAmount(it.getGasPrice()))
                .value(encodeAmount(it.getValue()))
                .data(Hex.encode0x(it.getData()));
    }

    public static String encodeAmount(Amount a) {
        return a == null ? null : a.toString();
    }
}
