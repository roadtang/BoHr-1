package org.bohr.gui.uiUtils;

import org.bohr.crypto.Hex;
import org.bohr.crypto.Key;
import org.bohr.util.StringUtil;

public class ModelUtils {
	/**
	 * @param key
	 * @return
	 */
	public static String getKEY(Key key) {
		String addressStr = Hex.encode0x(key.toAddress());
		String reStr = StringUtil.hexToBase58(addressStr);
		return reStr;
	}
	
	/**
	 * @param address
	 * @return
	 */
	public static String getKEY(byte[] address) {
		String addressStr = Hex.encode0x(address);
		String restr = StringUtil.hexToBase58(addressStr);
		return restr;
	}
	
	/**
	 * @param addressStr
	 * @return
	 */
	public static String getKEY(String addressStr) {
		String restr = StringUtil.hexToBase58(addressStr);
		return restr;
	}
	
	/**
	 * @param addressK
	 * @return
	 */
	public static String getOX(String addressK) {
		return StringUtil.base58ToHex(addressK);
	}
}
