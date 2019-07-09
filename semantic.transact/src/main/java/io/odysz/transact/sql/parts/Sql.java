package io.odysz.transact.sql.parts;


import io.odysz.transact.sql.parts.Logic.op;
import io.odysz.transact.sql.parts.antlr.ConditVisitor;
import io.odysz.transact.sql.parts.condition.Condit;
import io.odysz.transact.sql.parts.condition.ExprPart;

/**Logic expression etc's helper */
public class Sql {
	
	/**Create {@link Condit} from string.<br>
	 * Note: '%' (like) in format must be '%%'.
	 * @param format e.g. on condition string in join clause.
	 * @param args runtime arguements
	 * @return
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
	
	/**Filter "'", replace with "''".
	 * @param v
	 * @return
	 */
	public static String filterVal(String v) {
		// for java regex lookahead and lookbehined, see
		// https://www.logicbig.com/tutorials/core-java-tutorial/java-regular-expressions/regex-lookahead.html
		// https://www.logicbig.com/tutorials/core-java-tutorial/java-regular-expressions/regex-lookbehind.html
		return v == null ? null : v.replaceAll("(?<!^)'(?!$)", "''");
	}
}
