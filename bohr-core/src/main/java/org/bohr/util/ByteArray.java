/**
 * Copyright (c) 2019 The Bohr Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr.util;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;

import org.bouncycastle.util.Arrays;
import org.bohr.crypto.Hex;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

public class ByteArray implements Comparable<ByteArray> {
    private final byte[] data;
    private final int hash;

    public ByteArray(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("Input data can not be null");
        }
        this.data = data;
        this.hash = Arrays.hashCode(data);
    }

    public static ByteArray of(byte[] data) {
        return new ByteArray(data);
    }

    public int length() {
        return data.length;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof ByteArray) && Arrays.areEqual(data, ((ByteArray) other).data);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public int compareTo(ByteArray o) {
        return Arrays.compareUnsigned(data, o.data);
    }

    @Override
    public String toString() {
        return Hex.encode(data);
    }

    public static class ByteArrayKeyDeserializer extends KeyDeserializer {

        @Override
        public Object deserializeKey(String key, DeserializationContext context) throws IOException {
            return new ByteArray(Hex.decode0x(key));
        }
    }

    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    public static String toHexString(byte[] data) {
        return data == null ? "" : org.spongycastle.util.encoders.Hex.toHexString(data);
    }

    public static byte[] fromHexString(String data) {
        if (data == null) {
            return EMPTY_BYTE_ARRAY;
        }
        if (data.startsWith("0x")) {
            data = data.substring(2);
        }
        if (data.length() % 2 == 1) {
            data = "0" + data;
        }
        return Hex.decode(data);
    }

    public static long toLong(byte[] b) {
        if (b == null || b.length == 0) {
            return 0;
        }
        return new BigInteger(1, b).longValue();
    }

    public static byte[] fromString(String str) {
        if (str == null) {
            return null;
        }

        return str.getBytes();
    }

    public static String toStr(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }

        return new String(byteArray);
    }

    public static byte[] fromLong(long val) {
        return ByteBuffer.allocate(8).putLong(val).array();
    }

    /**
     * Generate a subarray of a given byte array.
     *
     * @param input the input byte array
     * @param start the start index
     * @param end the end index
     * @return a subarray of <tt>input</tt>, ranging from <tt>start</tt> (inclusively) to <tt>end</tt>
     * (exclusively)
     */
    public static byte[] subArray(byte[] input, int start, int end) {
        byte[] result = new byte[end - start];
        System.arraycopy(input, start, result, 0, end - start);
        return result;
    }
}