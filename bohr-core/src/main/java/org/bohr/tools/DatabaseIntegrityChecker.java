/**
 * Copyright (c) 2019 The Bohr Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr.tools;

import org.bohr.core.Amount;
import org.bohr.core.state.Account;
import org.bohr.crypto.Hex;
import org.bohr.db.Database;
import org.bohr.db.DatabaseFactory;
import org.bohr.db.DatabaseName;
import org.bohr.db.LeveldbDatabase;
import org.bohr.util.Bytes;
import org.bohr.util.ClosableIterator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.LongStream;

import static org.bohr.core.Amount.ZERO;
import static org.bohr.core.Unit.BOHR;

public class DatabaseIntegrityChecker {

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: java DatabaseIntegrityCheker.java [DATABASE_DIR]");
            return;
        }

        DatabaseFactory dbFactory = new LeveldbDatabase.LeveldbFactory(new File(args[0]));
        PrintStream out = System.out;
        if (args.length >= 2) {
            out = new PrintStream(new FileOutputStream(args[1]), true, StandardCharsets.UTF_8.name());
        }

        Database indexDB = dbFactory.getDB(DatabaseName.INDEX);
        long blockNumber = Bytes.toLong(indexDB.get(new byte[] { 0x00 }));

        List<Account> accounts = new ArrayList<>();
        Database accountDB = dbFactory.getDB(DatabaseName.ACCOUNT);
        ClosableIterator<Map.Entry<byte[], byte[]>> iterator = accountDB.iterator();
        while (iterator.hasNext()) {
            Map.Entry<byte[], byte[]> entry = iterator.next();
            byte[] key = entry.getKey();
            byte[] value = entry.getValue();

            if (key[0] == 0x00) {
                byte[] address = Arrays.copyOfRange(key, 1, key.length);
                Account account = Account.fromBytes(address, value);
                out.println(account);
                accounts.add(account);
            }
        }
        iterator.close();

        long totalVotes = 0;
        Database voteDB = dbFactory.getDB(DatabaseName.VOTE);
        iterator = voteDB.iterator();
        while (iterator.hasNext()) {
            Map.Entry<byte[], byte[]> entry = iterator.next();
            byte[] key = entry.getKey();
            byte[] value = entry.getValue();

            if (key.length == 40) {
                byte[] delegate = Arrays.copyOfRange(key, 0, 20);
                byte[] voter = Arrays.copyOfRange(key, 20, 40);
                long amount = Bytes.toLong(value);
                if (amount != 0) {
                    out.println(Hex.encode(delegate) + " <= " + Hex.encode(voter) + ": " + amount);
                    totalVotes += amount;
                }
            }
        }
        iterator.close();

        long totalBlockRewards = LongStream.range(1, blockNumber + 1).map(number -> {
            if (number <= 500_000L) {
                return Amount.of(1000, BOHR).toLong();
            } else if (number <= 1_000_000L) {
                return Amount.of(800, BOHR).toLong();
            } else if (number <= 1_500_000L) {
                return Amount.of(500, BOHR).toLong();
            }else if (number <= 3_000_000L) {
                return Amount.of(300, BOHR).toLong();
            }else if (number <= 5_000_000L) {
                return Amount.of(200, BOHR).toLong();
            }else if (number <= 10_000_000L) {
                return Amount.of(100, BOHR).toLong();
            }else if (number <= 50_000_000L) {
                return Amount.of(50, BOHR).toLong();
            } else {
                return ZERO.toLong();
            }

        }).sum();
        long totalAvailable = accounts.stream().mapToLong(acc -> acc.getAvailable().toLong()).sum();
        long totalLocked = accounts.stream().mapToLong(acc -> acc.getLocked().toLong()).sum();

        out.println("Latest block number: " + blockNumber);
        out.println("Total block rewards: " + totalBlockRewards);
        out.println("Total available    : " + totalAvailable);
        out.println("Total locked       : " + totalLocked);
        out.println("Total votes        : " + totalVotes);
        out.println("Diff 1: " + (totalLocked + totalAvailable - totalBlockRewards - 25000000000000000L));
        out.println("Diff 2: " + (totalLocked - totalVotes));

        dbFactory.close();
    }
}
