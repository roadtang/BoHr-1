/**
 * Copyright (c) 2019 The Bohr Developers
 * <p>
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr;

import org.bohr.cli.BohrCli;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        List<String> startArgs = new ArrayList<>();
        for (String arg : args) {
            startArgs.add(arg);
        }

        BohrCli.main(startArgs.toArray(new String[0]));
    }
}
