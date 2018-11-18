package io.odysz.semantics.sql.parts;

public class Logic {
	public enum op {eq, ne, lt, le, gt, ge, like, rlike, llike, in, notin, isnull, isNotnull};
	
	//public static final String eq = "=";

	public static op op(String oper) {
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
		return jc;
	}

	public enum type { and, or, not };
}
