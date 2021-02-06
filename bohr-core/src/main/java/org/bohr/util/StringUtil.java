/**
 * Copyright (c) 2019 The Bohr Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr.util;

import com.google.protobuf.ByteString;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.bohr.crypto.Hex;

import java.io.UnsupportedEncodingException;

public class StringUtil {
    interface CommonConstant {
        byte ADD_PRE_FIX_BYTE_MAINNET = (byte) 0x41;   //41 + address
        byte ADD_PRE_FIX_BYTE_TESTNET = (byte) 0xa0;   //a0 + address
        int ADDRESS_SIZE = 21;
    }

    /**
     * Returns if the given string is null or empty.
     *
     * @param str
     * @return
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    private StringUtil() {
    }

    public static byte[] decodeFromBase58Check(String addressBase58) {
        if (StringUtils.isEmpty(addressBase58)) {
            System.out.println("Warning: Address is empty !!");
            return null;
        }
        byte[] address = decode58Check(addressBase58);
        if (!addressValid(address)) {
            return null;
        }
        return address;
    }
    private static byte[] decode58Check(String input) {
        byte[] decodeCheck = Base58.decode(input);
        if (decodeCheck.length <= 4) {
            return null;
        }
        byte[] decodeData = new byte[decodeCheck.length - 4];
        System.arraycopy(decodeCheck, 0, decodeData, 0, decodeData.length);
        byte[] hash0 = Sha256Sm3Hash.hash(decodeData);
        byte[] hash1 = Sha256Sm3Hash.hash(hash0);
        if (hash1[0] == decodeCheck[decodeData.length]
                && hash1[1] == decodeCheck[decodeData.length + 1]
                && hash1[2] == decodeCheck[decodeData.length + 2]
                && hash1[3] == decodeCheck[decodeData.length + 3]) {
            return decodeData;
        }
        return null;
    }

    public static boolean addressValid(byte[] address) {
        if (ArrayUtils.isEmpty(address)) {
            System.out.println("Warning: Address is empty !!");
            return false;
        }
        //if (address.length != CommonConstant.ADDRESS_SIZE) {
        //    System.out.println(
        //            "Warning: Address length need "
        //                    + CommonConstant.ADDRESS_SIZE
        //                    + " but "
        //                    + address.length
        //                    + " !!");
        //    return false;
        //}
        byte preFixbyte = address[0];
        //if (preFixbyte != getAddressPreFixByte()) {
        //    System.out.println(
        //            "Warning: Address need prefix with "
        //                    + getAddressPreFixByte()
        //                    + " but "
        //                    + preFixbyte
        //                    + " !!");
        //    return false;
        //}
        // Other rule;
        return true;
    }

    public static String encode58Check(byte[] input) {
        byte[] hash0 = Sha256Sm3Hash.hash(input);
        byte[] hash1 = Sha256Sm3Hash.hash(hash0);
        byte[] inputCheck = new byte[input.length + 4];
        System.arraycopy(input, 0, inputCheck, 0, input.length);
        System.arraycopy(hash1, 0, inputCheck, input.length, 4);
        return Base58.encode(inputCheck);
    }

    public static String base58ToHex(String base58Address){
        try{
            byte[]  x =  decodeFromBase58Check(base58Address);
            String  libraryAddressHex = (new String(org.spongycastle.util.encoders.Hex.encode(x),
                    "US-ASCII"));
            return "0x"+libraryAddressHex.substring(4);
        }catch (Exception es){

        }
        return "";
    }

    public static String hexToBase58(String hexAddress){
        try{
            if(hexAddress.indexOf("0x") ==0 ||hexAddress.indexOf("0X") ==0 ){
                hexAddress = hexAddress.substring(2);
            }
            byte[] input = ByteArray.fromHexString( 600 + hexAddress);
            return  encode58Check(input);
        }catch (Exception es){

        }
        return "";
    }

    public static String hexToBase58(String hexAddress,int prefix){
        try{
            if(hexAddress.indexOf("0x") ==0 ||hexAddress.indexOf("0X") ==0 ){
                hexAddress = hexAddress.substring(2);
            }
            byte[] input = ByteArray.fromHexString( prefix + hexAddress);
            return  encode58Check(input);
        }catch (Exception es){

        }
        return "";
    }

    public static void main(String args[]){
        String address1 = base58ToHex("BfE6DueEe6FaNqztNHHWm9fUQm9SjrFaLeX");
        System.out.println(address1);

        String address2 = hexToBase58("0x000000000000000000000000000000000000dead");
        System.out.println(address2);

    }
}
