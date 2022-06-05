package io.odysz.common;

/**For Windows file system not distinguish upper lower cases.
 * Radix 32 (String) v.s. int converter
 * @version '=' is replaced by '-' for easyui compatibility (last '=' in id makes trouble).
 * @author ody
 *
 */
public class Radix32 {
	/**The same table as in db table ir_radix64 <br>
	 * Any modification must been synchronized.*/
	static char[] radchar = new char[] {
			'0', '1', '2', '3', '4', '5', '6', '7',		'8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
			'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',		'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V'};

	/**
	 * convert v to Radix64 integer. Chars are same to Base64 except '/', which is replaced by '-'
	 * @param v fix 12 bytes Base32 chars.
	 * @return String representation in radix 64.
	 */
	public static String toString(long v) {
		char[] buf = new char[12];
		for (int i = 0; i < 6; i++) {
			int idx = (int) (v & 0x3f);
			char digit = radchar[idx];
			buf[5 - i] = digit;
			v = v >>> 6;
		}
		return String.valueOf(buf);
	}
	
	public static String toString(long v, int digits) {
		char[] buf = new char[digits];
		for (int i = 0; i < digits; i++) {
			int idx = (int) (v & 0x1f);
			char digit = radchar[idx];
			buf[digits - 1 - i] = digit;
			v = v >>> 5;
		}
		return String.valueOf(buf);
	
	}
}