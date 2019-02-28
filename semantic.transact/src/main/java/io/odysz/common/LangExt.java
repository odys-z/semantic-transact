package io.odysz.common;

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
}
