package io.odysz.common;

import static io.odysz.common.LangExt.indexOf;
import static io.odysz.common.LangExt.len;

import java.util.Arrays;

import io.odysz.transact.x.TransException;

/**
 * Radix 64 (String) v.s. int converter
 * 
 * @author ody
 */
public class Radix64 {
	/**The same table as in db table ir_radix64 <br>
	 * Any modification must been synchronized.*/
	static char[] radchar = new char[] {
			'0', '1', '2', '3', '4', '5', '6', '7',		'8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
			'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',		'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
			'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',		'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
			'm', 'n', 'o', 'p', 'q', 'r', 's', 't',		'u', 'v', 'w', 'x', 'y', 'z', '+', '-'};
	
	/**
	 * convert v to Radix64 integer. Chars are same to Base64 except '/', which is replaced by '-'
	 * @param v fix 6 bytes Base64 chars.
	 * @return String representation in radix 64.
	 */
	public static String toString(long v) {
		try {
			return toString(v, 6, 64, radchar);
		} catch (TransException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * @since 1.4.25
	 * @param v
	 * @param digits max digits (Radix string length, fill higher with '0')
	 * @return Radix 64/32 string
	 */
	public static String toString(long v, int digits) {
		try { return toString(v, digits, 64, radchar);
		} catch (TransException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @since 1.4.25
	 * @param v
	 * @param digits max digits (Radix string length, fill more significant with '0')
	 * @param radix 32 or 64 only
	 * @param radchar the lookup table
	 * @return Radix 64/32 string
	 * @throws TransException 
	 */
	public static String toString(long v, int digits, int radix, char[] radchar) throws TransException {
		char[] buf = new char[digits];
		Arrays.fill(buf, '0');

		if (radix != 64 && radix != 32)
			throw new TransException("radix != 64 or 32.");

		for (int i = 0; i < digits; i++) {
			int idx = (int) (v & (radix == 64 ? 0x3f : 0x1f));
			char digit = radchar[idx];
			buf[digits - 1 - i] = digit;
			v = v >>> (radix == 64 ? 6 : 5);
			if (v == 0) break;
		}
		return String.valueOf(buf);
	}

	/**
	 * @since 1.4.25
	 * @return long
	 * @throws TransException 
	 */
	public static long toLong(String r64) throws TransException {
		return toLong(r64, radchar, 64);
	}
	
	/**
	 * @since 1.4.25
	 * @param rad
	 * @param radchar
	 * @param radix
	 * @return long
	 * @throws TransException
	 */
	public static long toLong(String rad, char[] radchar, int radix) throws TransException {
		if (radix != 64 && radix != 32)
			throw new TransException("radix != 64 or 32.");

		long v = 0;

		for (int i = 0; i < len(rad); i++) {
			char digit = rad.charAt(i);
			int r = indexOf(radchar, digit);

			v = v << (radix == 64 ? 6 : 5);
			v += r;
		}
		return v;
	}

	public static boolean validate(String radixv) {
		return validate(radixv, radchar);
	}

	/**
	 * Is the value a validate radix64 number?
	 * @since 1.4.25
	 * @param radixVal
	 * @return true if is valid
	 */
	public static boolean validate(String radixVal, char[] radchar) {
		boolean isRad64 =len(radchar) == 32 ? false : true;
		for (int i = 0; i < len(radixVal); i++)
			if ((isRad64 ? number(radixVal.charAt(i)) : Radix32.number(radixVal.charAt(i))) < 0)
				return false;
		return true;
	}

	static int number(char digit) {
		for (int i = 0; i < len(radchar); i++)
			if (indexOf(radchar, digit) >= 0)
				return i;
		return -1;
	}
}
