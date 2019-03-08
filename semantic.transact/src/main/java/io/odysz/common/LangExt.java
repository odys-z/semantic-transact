package io.odysz.common;

import java.util.Arrays;
import java.util.stream.Collectors;

public class LangExt {
	/**Split and trim elements.
	 * <p>Empty element won't be ignored if there are 2 consequent separator. <br>
	 * That means two junctural, succeeding, cascading separators without an element in between, expect white space.
	 * Sorry for that poor English.</p>
	 * <p> See https://stackoverflow.com/questions/41953388/java-split-and-trim-in-one-shot
	 * @param s
	 * @param regex
	 * @param noTrim
	 * @return
	 */
	public static String[] split(String s, String regex, boolean... noTrim) {
		if (s == null)
			return null;
		else {
			if (noTrim == null || noTrim.length > 0 && noTrim[0])
				regex = "\\s*" + regex + "\\s*";
			return s.split(regex);
		}
	}
	
	public static String toString(String[] ss) {
		return ss == null ? null : Arrays.stream(ss).collect(Collectors.joining(",", "[", "]"));
	}
	
	/**Convert 2D array to string: "[{ss[0][1]: ss[0][1]}, {ss[1][0]: ss[1][1]}, ...]"
	 * @param ss
	 * @return
	 */
	public static String toString(String[][] ss) {
		return Arrays.stream(ss)
				.filter(s -> s != null)
				.map(e -> toString(e))
				.collect(Collectors.joining(",", "[", "]"));
	}
}
