/**
 * Copyright (c) 2019 The Bohr Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr.util;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class NullPrintStream extends PrintStream {

    public NullPrintStream() throws UnsupportedEncodingException {
        super(new NullByteArrayOutputStream(), false, StandardCharsets.UTF_8.name());
    }

    private static class NullByteArrayOutputStream extends ByteArrayOutputStream {

        @Override
        public void write(int b) {
            // do nothing
        }

        @Override
        public void write(byte[] b, int off, int len) {
            // do nothing
        }

        @Override
        public void writeTo(OutputStream out) throws IOException {
            // do nothing
        }

    }

}