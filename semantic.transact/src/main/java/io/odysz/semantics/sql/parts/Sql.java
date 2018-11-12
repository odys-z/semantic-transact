package io.odysz.semantics.sql.parts;


import io.odysz.semantics.sql.parts.Logic.op;
import io.odysz.semantics.sql.parts.condition.Condit;

public class Sql {
	
	public static Condit condt(String format, String... args) {
		// return new Condt(new ExprBuilder(format, (Object[])args));
		// TODO parse
		return new Condit(Logic.op(format), args[0], args[1]);
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
