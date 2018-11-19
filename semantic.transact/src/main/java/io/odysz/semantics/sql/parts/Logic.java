package io.odysz.semantics.sql.parts;

public class Logic {
	public enum op {eq, ne, lt, le, gt, ge, like, rlike, llike, notlike, in, notin, isnull, isNotnull;

	public String sql(op op, String rop) {
		switch(op) {
		case eq:
			return "= " + rop;
		case ne:
			return "<> " + rop;
		case lt:
			return "< " + rop;
		case le:
			return "<= " + rop;
		case gt:
			return "> " + rop;
		case ge:
			return ">= " + rop;
		case like:
			return "like " + rop;
		case in:
			return "in (" + rop + ")";
		default:
			return " TODO ";
		}
	}};
	
	//public static final String eq = "=";

	public static op op(String oper, boolean... withNot) {
		op jc = "=".equals(oper) || "eq".equals(oper) ? op.eq
	   				 : "%".equals(oper) || "like".equals(oper) ? op.like
	   				 : "=%".equals(oper) || "rlike".equals(oper) ? op.rlike
	   				 : "%=".equals(oper) || "llike".equals(oper) ? op.llike
   					 : "<=".equals(oper) || "le".equals(oper) ? op.le
   					 : "<".equals(oper) || "lt".equals(oper) ? op.lt
   					 : ">=".equals(oper) || "ge".equals(oper) ? op.ge
   					 : ">".equals(oper) || "gt".equals(oper) ? op.gt
   		   			 : "><".equals(oper) || "[]".equals(oper) || "in".equals(oper) ? op.in
   		   	   		 : "][".equals(oper) || "notin".equals(oper) || "not in".equals(oper) ? op.notin
   		   	   		 : "?0".equals(oper) || "is null".equals(oper) ? op.isnull
   		   	   		 : "!?0".equals(oper) || "?!0".equals(oper) || "!0".equals(oper) || "?!".equals(oper) ? op.isNotnull
   		   	   		 : op.ne; // <> !=
		if (withNot != null && withNot.length > 0 && withNot[0] == true) {
			jc  = jc == op.like ? op.notlike
				: jc == op.eq ? op.ne
				: jc == op.in ? op.notin
				: jc;
		}
		return jc;
	}

	public enum type { and, or, not };
}
