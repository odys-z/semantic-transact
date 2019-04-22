package io.odysz.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	 * @return String[]
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
	
	/** Get a string that can be parsed by {@link #toArray(String)}.
	 * @param ss
	 * @return [e0, e1, ...]
	 */
	public static String toString(Object[] ss) {
		return ss == null ? null : Arrays.stream(ss)
				.filter(e -> e != null)
				.map(e -> e.toString()).collect(Collectors.joining(",", "[", "]"));
	}
	
	/**Get a string array that composed into string by {@link #toString(Object[])}.
	 * @param str
	 * @return string[]
	 */
	public static String[] toArray(String str) {
		return str.replaceAll("^\\[", "").replaceAll("\\]$", "").split(",");
	}	

	/**Convert 2D array to string: "[{ss[0][1]: ss[0][1]}, {ss[1][0]: ss[1][1]}, ...]"
	 * @param ss
	 * @return converted String
	 */
	public static String toString(String[][] ss) {
		return Arrays.stream(ss)
				.filter(s -> s != null)
				.map(e -> toString(e))
				.collect(Collectors.joining(",", "[", "]"));
	}

	public static String toString(Map<String, ?> map) {
		if (map == null) return null;
		else return map.entrySet().stream()
				.map(e -> "{" + e.getKey() + ": " + e.getValue() + "}")
				.collect(Collectors.joining(",", "[", "]"));
	}

	public static String toString(List<Object[]> lst) {
		if (lst == null) return null;
		else return lst.stream()
				.map(e -> toString(e))
				.collect(Collectors.joining(",", "[", "]"));
	}
	
	/**Parse formatted string into hash map.
	 * @param str "k1:v1,k2:v2,..."
	 * @return hash map
	 */
	public static HashMap<String, String> parseMap(String str) {
		if (str != null && str.trim().length() > 0) {
			String[] entryss = str.trim().split(",");
			HashMap<String, String> refMap = new HashMap<String, String>(entryss.length);
			for (String entry : entryss) {
				try {
					String[] e = entry.split(":");
					refMap.put(e[0].trim(), e[1].trim());
				}
				catch (Exception ex) {
					Utils.warn("WARN: - can't parse: " + entry);
					continue;
				}
			}
			return refMap;
		}
		return null;
	}
}
