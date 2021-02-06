/**
 * Copyright (c) 2019 The Bohr Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr.config;

import org.bohr.Network;
import org.bohr.config.exception.ConfigException;
import org.bohr.core.Amount;
import org.bohr.core.Fork;
import org.bohr.core.TransactionType;
import org.bohr.crypto.Hash;
import org.bohr.net.Capability;
import org.bohr.net.CapabilityTreeSet;
import org.bohr.net.NodeManager.Node;
import org.bohr.net.msg.MessageCode;
import org.bohr.util.BigIntegerUtil;
import org.bohr.util.Bytes;
import org.bohr.util.StringUtil;
import org.bohr.util.SystemUtil;
import org.bohr.util.exception.UnreachableException;
import org.bohr.vm.client.BohrSpec;
import org.ethereum.vm.chainspec.Spec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.bohr.Network.*;
import static org.bohr.core.Fork.*;
import static org.bohr.core.Unit.*;

public abstract class AbstractConfig implements Config, ChainSpec {

    private static final Logger logger = LoggerFactory.getLogger(AbstractConfig.class);

    private static final String CONFIG_FILE = "bohr.properties";

    // =========================
    // Chain spec
    // =========================
    protected long maxBlockGasLimit = 30_000_000L; // 30m gas
    protected Amount minTransactionFee = Amount.of(100, MICRO_BOHR);    //0.0001Bohr
    protected Amount minDelegateBurnAmount = Amount.of(1000, BOHR);
    protected long nonVMTransactionGasCost = 5_000L;

    // =========================
    // General
    // =========================
    protected File dataDir;
    protected Network network;
    protected short networkVersion;

    // =========================
    // P2P
    // =========================
    protected String p2pDeclaredIp = null;
    protected String p2pListenIp = "0.0.0.0";
    protected int p2pListenPort = Constants.DEFAULT_P2P_PORT;
    protected Set<Node> p2pSeedNodes = new HashSet<>();

    // =========================
    // Network
    // =========================
    protected int netMaxOutboundConnections = 128;
    protected int netMaxInboundConnections = 512;
    protected int netMaxInboundConnectionsPerIp = 5;
    protected int netMaxMessageQueueSize = 4096;
    protected int netMaxFrameBodySize = 128 * 1024;
    protected int netMaxPacketSize = 16 * 1024 * 1024;
    protected int netRelayRedundancy = 8;
    protected int netHandshakeExpiry = 5 * 60 * 1000;
    protected int netChannelIdleTimeout = 2 * 60 * 1000;
    protected Amount maxVoteAmount = Amount.of(10000000, BOHR);
    protected int maxVoteCount = 1000;
    protected Amount maxUnVoteAmount = Amount.of(10000000, BOHR);
    protected int maxUnVoteCount = 1000;
    protected Set<MessageCode> netPrioritizedMessages = new HashSet<>(Arrays.asList(
            MessageCode.BFT_NEW_HEIGHT,
            MessageCode.BFT_NEW_VIEW,
            MessageCode.BFT_PROPOSAL,
            MessageCode.BFT_VOTE));
    protected List<String> netDnsSeedsMainNet = Collections
            .unmodifiableList(Arrays.asList("mainnet.bohrweb.org"));
    protected List<String> netDnsSeedsTestNet = Collections
            .unmodifiableList(Arrays.asList("testnet.bohrweb.org"));

    // =========================
    // Sync
    // =========================
    protected long syncDownloadTimeout = 10_000L;
    protected int syncMaxQueuedJobs = 8192;
    protected int syncMaxPendingJobs = 256;
    protected int syncMaxPendingBlocks = 512;
    protected boolean syncDisconnectOnInvalidBlock = false;
    protected boolean syncFastSync = false;

    // =========================
    // API
    // =========================
    protected boolean apiEnabled = false;
    protected String apiListenIp = "127.0.0.1";
    protected int apiListenPort = Constants.DEFAULT_API_PORT;
    protected String apiUsername = "BOHR_API_USERNAME";
    protected String apiPassword = "BOHR_API_PASSWORD";
    protected String[] apiPublicServices = {
            "blockchain", "account", "delegate", "tool"
    };
    protected String[] apiPrivateServices = {
            "node", "wallet"
    };

    // =========================
    // BFT consensus
    // =========================
    protected long bftNewHeightTimeout = 500L;
    protected long bftProposeTimeout = 2000L;
    protected long bftValidateTimeout = 1000L;
    protected long bftPreCommitTimeout = 1000L;
    protected long bftCommitTimeout = 500L;
    protected long bftFinalizeTimeout = 500L;
    protected long bftMaxBlockTimeDrift = TimeUnit.SECONDS.toMillis(3);

    // =========================
    // Transaction pool
    // =========================
    protected int poolBlockGasLimit = 10_000_000;
    protected Amount poolMinGasPrice = Amount.of(1); // 1 NanoBohr = 100 Gwei
    protected long poolMaxTransactionTimeDrift = TimeUnit.HOURS.toMillis(2);

    // =========================
    // UI
    // =========================
    protected Locale uiLocale = Locale.getDefault();
    protected String uiUnit = "BR";
    protected int uiFractionDigits = 9;

    // =========================
    // Forks
    // =========================
    protected boolean forkUniformDistributionEnabled = false;
    protected boolean forkVirtualMachineEnabled = false;
    protected boolean forkVotingPrecompiledUpgradeEnabled = false;

    @Override
    public ChainSpec spec() {
        return this;
    }

    protected AbstractConfig(String dataDir, Network network, short networkVersion) {
        this.dataDir = new File(dataDir);
        this.network = network;
        this.networkVersion = networkVersion;

        init();
        validate();
    }

    @Override
    public long maxBlockGasLimit() {
        if (this.network() == MAINNET) {
            return maxBlockGasLimit;
        } else {
            return maxBlockGasLimit * 5;
        }
    }

    @Override
    public int maxTransactionDataSize(TransactionType type) {
        switch (type) {
            case COINBASE:
            case UNVOTE:
            case VOTE:
                return 0; // not required

            case TRANSFER:
                return 128; // for memo

            case DELEGATE:
                return 50; // for name

            case CREATE:
            case CALL:
                return 512 * 1024; // for dapps

            default:
                throw new UnreachableException();
        }
    }

    @Override
    public Amount minTransactionFee() {
        return minTransactionFee;
    }

    @Override
    public Amount minDelegateBurnAmount() {
        return minDelegateBurnAmount;
    }

    @Override
    public long nonVMTransactionGasCost() {
        return nonVMTransactionGasCost;
    }

    @Override
    public Amount getBlockReward(long number) {

        long i = number % 17000;
        if (i == 0) {
            if (number <= 5635000) {
                return Amount.of(1280000, BOHR);
            } else if (number <= 11270000) {
                return Amount.of(640000, BOHR);
            } else if (number <= 16905000) {
                return Amount.of(320000, BOHR);
            } else if (number <= 22540000) {
                return Amount.of(160000, BOHR);
            } else if (number <= 28175000) {
                return Amount.of(80000, BOHR);
            } else if (number <= 33810000) {
                return Amount.of(40000, BOHR);
            } else if (number <= 39445000) {
                return Amount.of(20000, BOHR);
            } else if (number <= 45080000) {
                return Amount.of(10000, BOHR);
            } else if (number <= 55016500) {
                return Amount.of(5000, BOHR);
            } else if (number <= 58963500) {
                return Amount.of(2500, BOHR);
            } else {
                return Amount.of(2288, BOHR);
            }
        } else {
            return Amount.ZERO;
        }
    }

    @Override
    public long getValidatorUpdateInterval() {
        return 200L;
    }

    @Override
    public int getNumberOfValidators(long number) {
        return 6;
    }

    @Override
    public String getPrimaryValidator(List<String> validators, long height, int view, boolean uniformDist) {
        if (uniformDist) {
            return validators.get(getUniformDistPrimaryValidatorNumber(validators.size(), height, view));
        } else {
            byte[] key = Bytes.merge(Bytes.of(height), Bytes.of(view));
            return validators.get((Hash.h256(key)[0] & 0xff) % validators.size());
        }
    }

    private int getUniformDistPrimaryValidatorNumber(int size, long height, long view) {
        // use round-robin for view 0
        if (view == 0) {
            return (int) (height % (long) size);
        }

        // here we ensure there will never be consecutive block forgers after view
        // change
        int deterministicRand;
        final int prevDeterministicRand = getUniformDistPrimaryValidatorNumber(size, height, view - 1);
        BigInteger subView = BigInteger.ZERO;
        do {
            BigInteger seed = BigIntegerUtil
                    .random(BigInteger.valueOf(height))
                    .xor(BigIntegerUtil.random(BigInteger.valueOf(view)))
                    .add(subView);
            deterministicRand = BigIntegerUtil
                    .random(seed)
                    .mod(BigInteger.valueOf(size))
                    .intValue();
            subView = subView.add(BigInteger.ONE);
        } while (deterministicRand == prevDeterministicRand && size > 1);

        return deterministicRand;
    }

    @Override
    public Spec vmSpec() {
        return new BohrSpec();
    }

    private static long[][][] periods = new long[3][64][];

    static {
        // 200,000 block time is about 69 days
        periods[MAINNET.id()][UNIFORM_DISTRIBUTION.id()] = new long[] { 200_001L, 400_000L };
        periods[MAINNET.id()][VIRTUAL_MACHINE.id()] = new long[] { 1_500_001L, 1_700_000L };
        periods[MAINNET.id()][VOTING_PRECOMPILED_UPGRADE.id()] = new long[] { 1_600_001L, 1_800_000L };

        periods[TESTNET.id()][UNIFORM_DISTRIBUTION.id()] = new long[] { 1L, 200_000L };
        periods[TESTNET.id()][VIRTUAL_MACHINE.id()] = new long[] { 1L, 200_000L };
        periods[TESTNET.id()][VOTING_PRECOMPILED_UPGRADE.id()] = new long[] { 150_001L, 350_000L };

        // as soon as possible
        periods[DEVNET.id()][UNIFORM_DISTRIBUTION.id()] = new long[] { 1L, 200_000L };
        periods[DEVNET.id()][VIRTUAL_MACHINE.id()] = new long[] { 1L, 200_000L };
        periods[DEVNET.id()][VOTING_PRECOMPILED_UPGRADE.id()] = new long[] { 1, 200_000L };
    }

    @Override
    public long[] getForkSignalingPeriod(Fork fork) {
        return periods[network().id()][fork.id()];
    }

    @Override
    public File getFile() {
        return new File(configDir(), CONFIG_FILE);
    }

    @Override
    public File dataDir() {
        return dataDir;
    }

    @Override
    public File databaseDir() {
        return databaseDir(network);
    }

    @Override
    public File databaseDir(Network network) {
        return new File(dataDir, Constants.DATABASE_DIR + File.separator + network.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public File configDir() {
        return new File(dataDir, Constants.CONFIG_DIR);
    }

    @Override
    public Network network() {
        return network;
    }

    @Override
    public short networkVersion() {
        return networkVersion;
    }

    @Override
    public String getClientId() {
        return String.format("%s/v%s-%s/%s/%s",
                Constants.CLIENT_NAME,
                Constants.CLIENT_VERSION,
                SystemUtil.getImplementationVersion(),
                SystemUtil.getOsName().toString(),
                SystemUtil.getOsArch());
    }

    @Override
    public CapabilityTreeSet getClientCapabilities() {
        return CapabilityTreeSet.of(Capability.Bohr, Capability.FAST_SYNC);
    }

    @Override
    public Amount maxVoteAmount() {
        return maxVoteAmount;
    }

    @Override
    public int maxVoteCount() {
        return maxVoteCount;
    }

    @Override
    public Amount maxUnVoteAmount() {
        return maxUnVoteAmount;
    }

    @Override
    public int maxUnVoteCount() {
        return maxUnVoteCount;
    }

    @Override
    public Optional<String> p2pDeclaredIp() {
        return StringUtil.isNullOrEmpty(p2pDeclaredIp) ? Optional.empty() : Optional.of(p2pDeclaredIp);
    }

    @Override
    public String p2pListenIp() {
        return p2pListenIp;
    }

    @Override
    public int p2pListenPort() {
        return p2pListenPort;
    }

    @Override
    public Set<Node> p2pSeedNodes() {
        return p2pSeedNodes;
    }

    @Override
    public int netMaxOutboundConnections() {
        return netMaxOutboundConnections;
    }

    @Override
    public int netMaxInboundConnections() {
        return netMaxInboundConnections;
    }

    @Override
    public int netMaxInboundConnectionsPerIp() {
        return netMaxInboundConnectionsPerIp;
    }

    @Override
    public int netMaxMessageQueueSize() {
        return netMaxMessageQueueSize;
    }

    @Override
    public int netMaxFrameBodySize() {
        return netMaxFrameBodySize;
    }

    @Override
    public int netMaxPacketSize() {
        return netMaxPacketSize;
    }

    @Override
    public int netRelayRedundancy() {
        return netRelayRedundancy;
    }

    @Override
    public int netHandshakeExpiry() {
        return netHandshakeExpiry;
    }

    @Override
    public int netChannelIdleTimeout() {
        return netChannelIdleTimeout;
    }

    @Override
    public Set<MessageCode> netPrioritizedMessages() {
        return netPrioritizedMessages;
    }

    @Override
    public List<String> netDnsSeedsMainNet() {
        return netDnsSeedsMainNet;
    }

    @Override
    public List<String> netDnsSeedsTestNet() {
        return netDnsSeedsTestNet;
    }

    @Override
    public long syncDownloadTimeout() {
        return syncDownloadTimeout;
    }

    @Override
    public int syncMaxQueuedJobs() {
        return syncMaxQueuedJobs;
    }

    @Override
    public int syncMaxPendingJobs() {
        return syncMaxPendingJobs;
    }

    @Override
    public int syncMaxPendingBlocks() {
        return syncMaxPendingBlocks;
    }

    @Override
    public boolean syncDisconnectOnInvalidBlock() {
        return syncDisconnectOnInvalidBlock;
    }

    @Override
    public boolean syncFastSync() {
        return syncFastSync;
    }

    @Override
    public boolean apiEnabled() {
        return apiEnabled;
    }

    @Override
    public String apiListenIp() {
        return apiListenIp;
    }

    @Override
    public int apiListenPort() {
        return apiListenPort;
    }

    @Override
    public String apiUsername() {
        return apiUsername;
    }

    @Override
    public String apiPassword() {
        return apiPassword;
    }

    @Override
    public String[] apiPublicServices() {
        return apiPublicServices;
    }

    @Override
    public String[] apiPrivateServices() {
        return apiPrivateServices;
    }

    @Override
    public long bftNewHeightTimeout() {
        return bftNewHeightTimeout;
    }

    @Override
    public long bftProposeTimeout() {
        return bftProposeTimeout;
    }

    @Override
    public long bftValidateTimeout() {
        return bftValidateTimeout;
    }

    @Override
    public long bftPreCommitTimeout() {
        return bftPreCommitTimeout;
    }

    @Override
    public long bftCommitTimeout() {
        return bftCommitTimeout;
    }

    @Override
    public long bftFinalizeTimeout() {
        return bftFinalizeTimeout;
    }

    @Override
    public long bftMaxBlockTimeDrift() {
        return bftMaxBlockTimeDrift;
    }

    @Override
    public int poolBlockGasLimit() {
        if (this.network() == MAINNET) {
            return poolBlockGasLimit;
        } else {
            return poolBlockGasLimit * 5;
        }
    }

    @Override
    public Amount poolMinGasPrice() {
        return poolMinGasPrice;
    }

    @Override
    public long poolMaxTransactionTimeDrift() {
        return poolMaxTransactionTimeDrift;
    }

    @Override
    public Locale uiLocale() {
        return uiLocale;
    }

    @Override
    public String uiUnit() {
        return uiUnit;
    }

    @Override
    public int uiFractionDigits() {
        return uiFractionDigits;
    }

    @Override
    public boolean forkUniformDistributionEnabled() {
        return forkUniformDistributionEnabled;
    }

    @Override
    public boolean forkVirtualMachineEnabled() {
        return forkVirtualMachineEnabled;
    }

    @Override
    public boolean forkVotingPrecompiledUpgradeEnabled() {
        return forkVotingPrecompiledUpgradeEnabled;
    }

    protected void init() {
        File f = getFile();
        if (!f.exists()) {
            // exit if the config file does not exist
            return;
        }

        try (FileInputStream in = new FileInputStream(f)) {

            Properties props = new Properties();
            props.load(in);

            for (Object k : props.keySet()) {
                String name = (String) k;

                switch (name) {
                    case "p2p.declaredIp":
                        p2pDeclaredIp = props.getProperty(name).trim();
                        break;
                    case "p2p.listenIp":
                        p2pListenIp = props.getProperty(name).trim();
                        break;
                    case "p2p.listenPort":
                        p2pListenPort = Integer.parseInt(props.getProperty(name).trim());
                        break;
                    case "p2p.seedNodes":
                        String[] nodes = props.getProperty(name).trim().split(",");
                        for (String node : nodes) {
                            if (!node.trim().isEmpty()) {
                                String[] tokens = node.trim().split(":");
                                if (tokens.length == 2) {
                                    p2pSeedNodes.add(new Node(tokens[0], Integer.parseInt(tokens[1])));
                                } else {
                                    p2pSeedNodes.add(new Node(tokens[0], Constants.DEFAULT_P2P_PORT));
                                }
                            }
                        }
                        break;

                    case "net.maxInboundConnections":
                        netMaxInboundConnections = Integer.parseInt(props.getProperty(name).trim());
                        break;
                    case "net.maxInboundConnectionsPerIp":
                        netMaxInboundConnectionsPerIp = Integer.parseInt(props.getProperty(name).trim());
                        break;
                    case "net.maxOutboundConnections":
                        netMaxOutboundConnections = Integer.parseInt(props.getProperty(name).trim());
                        break;
                    case "net.maxMessageQueueSize":
                        netMaxMessageQueueSize = Integer.parseInt(props.getProperty(name).trim());
                        break;
                    case "net.relayRedundancy":
                        netRelayRedundancy = Integer.parseInt(props.getProperty(name).trim());
                        break;
                    case "net.channelIdleTimeout":
                        netChannelIdleTimeout = Integer.parseInt(props.getProperty(name).trim());
                        break;
                    case "net.dnsSeeds.mainNet":
                        netDnsSeedsMainNet = Arrays.asList(props.getProperty(name).trim().split(","));
                        break;
                    case "net.dnsSeeds.testNet":
                        netDnsSeedsTestNet = Arrays.asList(props.getProperty(name).trim().split(","));
                        break;

                    case "sync.downloadTimeout":
                        syncDownloadTimeout = Long.parseLong(props.getProperty(name).trim());
                        break;
                    case "sync.maxQueuedJobs":
                        syncMaxQueuedJobs = Integer.parseInt(props.getProperty(name).trim());
                        break;
                    case "sync.maxPendingJobs":
                        syncMaxPendingJobs = Integer.parseInt(props.getProperty(name).trim());
                        break;
                    case "sync.maxPendingBlocks":
                        syncMaxPendingBlocks = Integer.parseInt(props.getProperty(name).trim());
                        break;
                    case "sync.disconnectOnInvalidBlock":
                        syncDisconnectOnInvalidBlock = Boolean.parseBoolean(props.getProperty(name).trim());
                        break;
                    case "sync.fastSync":
                        syncFastSync = Boolean.parseBoolean(props.getProperty(name).trim());
                        break;

                    case "api.enabled":
                        apiEnabled = Boolean.parseBoolean(props.getProperty(name).trim());
                        break;
                    case "api.listenIp":
                        apiListenIp = props.getProperty(name).trim();
                        break;
                    case "api.listenPort":
                        apiListenPort = Integer.parseInt(props.getProperty(name).trim());
                        break;
                    case "api.username":
                        apiUsername = props.getProperty(name).trim();
                        break;
                    case "api.password":
                        apiPassword = props.getProperty(name).trim();
                        break;
                    case "api.public":
                        apiPublicServices = Stream.of(props.getProperty(name).trim().split(","))
                                .map(String::trim)
                                .toArray(String[]::new);
                        break;
                    case "api.private":
                        apiPrivateServices = Stream.of(props.getProperty(name).trim().split(","))
                                .map(String::trim)
                                .toArray(String[]::new);
                        break;
                    case "ui.locale": {
                        // ui.locale must be in format of en_US ([language]_[country])
                        String[] localeComponents = props.getProperty(name).trim().split("_");
                        if (localeComponents.length == 2) {
                            uiLocale = new Locale(localeComponents[0], localeComponents[1]);
                        }
                        break;
                    }
                    case "ui.unit": {
                        uiUnit = props.getProperty(name).trim();
                        break;
                    }
                    case "ui.fractionDigits": {
                        uiFractionDigits = Integer.parseInt(props.getProperty(name).trim());
                        break;
                    }
                    case "txpool.blockGasLimit": {
                        poolBlockGasLimit = Integer.parseInt(props.getProperty(name).trim());
                        break;
                    }
                    case "txpool.minGasPrice": {
                        poolMinGasPrice = Amount.of(props.getProperty(name).trim());
                        break;
                    }
                    case "txpool.maxTransactionTimeDrift": {
                        poolMaxTransactionTimeDrift = Integer.parseInt(props.getProperty(name).trim());
                        break;
                    }
                    default:
                        logger.error("Unsupported option: {} = {}", name, props.getProperty(name));
                        break;
                }
            }
        } catch (Exception e) {
            logger.error("Failed to load config file: {}", f, e);
        }
    }

    private void validate() {
        if (apiEnabled) {
            if ("BOHR_API_USERNAME".equals(apiUsername) || "BOHR_API_PASSWORD".equals(apiPassword)) {
                throw new ConfigException("Please change your API username/password from the default values.");
            }

            if (Arrays.stream(apiPublicServices)
                    .anyMatch(x -> Arrays.stream(apiPrivateServices).anyMatch(y -> y.equals(x)))) {
                throw new ConfigException("There are services which belong to both api.public and api.private");
            }
        }
    }
}
