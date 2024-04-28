package io.odysz.common;

import static io.odysz.common.LangExt.len;
import static io.odysz.common.LangExt.indexOf;

import io.odysz.transact.x.TransException;

/**
 * For Windows file system not distinguishing upper lower cases.
 * Radix 32 (String) v.s. int converter
 * 
 * @author ody
 */
public class Radix32 {
	/**The same table as in db table ir_radix64 <br>
	 * Any modification must been synchronized.*/
	static char[] radchar = new char[] {
			'0', '1', '2', '3', '4', '5', '6', '7',		'8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
			'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',		'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V'};

	/**
	 * convert v to Radix 32 integer. Chars are same to Base64 except '/', which is replaced by '-'
	 * @param v fix 12 bytes Base32 chars.
	 * @return String representation in radix 64.
	 */
	public static String toString(long v) {
		return toString(v, 6);
	}
	
	public static String toString(long v, int digits) {
		try {
			return Radix64.toString(v, digits, 32, radchar);
		} catch (TransException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * @since 1.4.25
	 * @return long
	 * @throws TransException 
	 */
	public static long toLong(String r32) throws TransException {
		return Radix64.toLong(r32, radchar, 32);
	}

	public static boolean validate(String radixv) {
		return Radix64.validate(radixv, radchar);
	}

	static int number(char digit) {
		for (int i = 0; i < len(radchar); i++)
			if (indexOf(radchar, digit) >= 0)
				return i;
		return -1;
	}

}
