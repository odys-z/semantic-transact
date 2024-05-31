package io.odysz.transact.sql.parts;


import io.odysz.common.LangExt;
import io.odysz.transact.sql.Query;
import io.odysz.transact.sql.parts.Logic.op;
import io.odysz.transact.sql.parts.antlr.ConditVisitor;
import io.odysz.transact.sql.parts.condition.Condit;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.x.TransException;

/**Logic expression etc's helper */
public class Sql {
	
	/**Create {@link Condit} from string.<br>
	 * Note: '%' (like) in format must be '%%'.
	 * @param format e.g. on condition string in join clause.
	 * @param args runtime arguements
	 * @return Condit object
	 */
	public static Condit condt(String format, Object... args) {
		// try format == op - user can be confused like condt("=", "f.c1", "v1");
		op op = Logic.op(format);
		if (op != null && args != null && args.length == 2)
			return condt(op, (String)args[0], (String)args[1]);

		String exp = String.format(format, args);
		
		return parseCondit(exp);
	}

	private static Condit parseCondit(String exp) {
		return ConditVisitor.parse(exp);
	}

	public static Condit condt(op op, String loperand, String roperand) {
		return new Condit(op, loperand, roperand);
	}

	public static Condit condt(op op, String loperand, ExprPart part) {
		return new Condit(op, loperand, part);
	}

	public static Condit condt(op op, String loperand, Query q) throws TransException {
		return new Condit(op, loperand, q);
	}

	/**
	 * @since 1.4.40
	 * @param op
	 * @param loperand
	 * @param part
	 * @return condition
	 */
	public static Condit condt(op op, ExprPart lop, ExprPart rop) {
		return new Condit(op, lop, rop);
	}

	/**
	 * @since 1.4.40
	 * @param op
	 * @param loperand
	 * @param part
	 * @return condition
	 */
	public static Condit condt(op op, ExprPart lop, String rop) {
		return new Condit(op, lop, new ExprPart(rop));
	}
	
	/**Filtering out "'", replaced with "''".
	 * @param v
	 * @return replaced v
	 */
	public static String filterVal(String v) {
		// for java regex lookahead and lookbehined, see
		// https://www.logicbig.com/tutorials/core-java-tutorial/java-regular-expressions/regex-lookahead.html
		// https://www.logicbig.com/tutorials/core-java-tutorial/java-regular-expressions/regex-lookbehind.html
		return v == null ? null : v.replaceAll("(?<!^)'(?!$)", "''");
	}

    /**Convert a boolean represented by string to integer represented by string.
     * e.g. "true" | int != 0 | "T" | "Y" -&gt; "1", else -&gt; "0", 
     * @param bool
     * @return "1" or "0"
     */
	public static String bool2Int(String bool) {
		if (LangExt.isblank(bool)) return "0";
		
		try {// true false
			boolean v = Boolean.valueOf(bool);
			return v ? "1" : "0";
		} catch (Throwable t) {}

		try {// d
			int v = Integer.valueOf(bool);
			return v == 0 ? "0" : "1";
		} catch (Throwable t) {}

		try {// T, F, Y, N
			bool = bool.trim().toLowerCase();
			return "t".equals(bool) || "y".equals(bool) ? "1" : "0";
		} catch (Throwable t) {}

		return "0";
	}
}
