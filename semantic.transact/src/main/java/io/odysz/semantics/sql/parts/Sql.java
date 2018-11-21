package io.odysz.semantics.sql.parts;


import io.odysz.semantics.sql.parts.Logic.op;
import io.odysz.semantics.sql.parts.antlr.ConditVisitor;
import io.odysz.semantics.sql.parts.condition.Condit;

public class Sql {
	
	public static Condit condt(String format, Object... args) {
		// try format == op - user can be confused like condt("=", "f.c1", "v1");
		op op = Logic.op(format);
		if (op != null && args != null && args.length == 2)
			return condt(op, (String)args[0], (String)args[1]);


		// TODO parse
		String exp = String.format(format, args);
		
		return parseCondit(exp);

		// return new Condit(Logic.op(format), args[0], args[1]);
	}

	private static Condit parseCondit(String exp) {
		return ConditVisitor.parse(exp);
		// return new Condit(expr.op(), expr.lop(), expr.rop());
	}

	public static Condit condt(op op, String loperand, String roperand) {
		// return new Condt(new ExprBuilder(op, loperand, roperand));
		return new Condit(op, loperand, roperand);
	}
	

	/**
	 * @param users
	 * @return "'ele1','ele2','ele3',..."
	 */
	public static String str(String[] ele) {
		return "'ele1','ele2','ele3'";
	}

}
