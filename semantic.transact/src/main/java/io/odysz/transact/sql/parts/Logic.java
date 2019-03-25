package io.odysz.transact.sql.parts;

public class Logic {
	/** empty type is used to subclass {@link io.odysz.transact.sql.parts.condition.Predicate}
	 * as {@link io.odysz.transact.sql.parts.condition.Condit}*/
	public enum type { and, or, not, empty };

	public enum op {eq, ne, lt, le, gt, ge, like, rlike, llike, notlike, in, notin, isnull, isNotnull;

		public String sql(op oper, String rop, boolean... not) {
			if (oper == op.eq)
				return "= " + rop;
			else if (oper == op.ne)
				return "<> " + rop;
			else if (oper == op.lt)
				return "< " + rop;
			else if (oper == op. le)
				return "<= " + rop;
			else if (oper == op.gt)
				return "> " + rop;
			else if (oper == op.ge)
				return ">= " + rop;
			else if (oper == op.like)
				if (not != null && not.length > 0 && not[0])
					return "not like " + likeOp(rop);
				else
					return "like " + likeOp(rop);
			else if (oper == op.rlike)
				if (not != null && not.length > 0 && not[0])
					return "not like " + rlikeOp(rop);
				else
					return "like " + rlikeOp(rop);
			else if (oper == op.llike)
				if (not != null && not.length > 0 && not[0])
					return "not like " + llikeOp(rop);
				else
					return "like " + llikeOp(rop);
			else if (oper == op.in)
				if (not != null && not.length > 0 && not[0])
					return "not in (" + rop + ")";
				else
					return "in (" + rop + ")";
			else if (oper == op.isnull)
				if (not != null && not.length > 0 && not[0])
					return "is null";
				else
					return "is null";
			else if (oper == op.isNotnull)
				return "is not null";
			else
				return " TODO ";
		}
	
		private String llikeOp(String op) {
			if (op != null && op.length() > 2 && op.startsWith("'") && !op.startsWith("'%"))
				return op.replaceFirst("^'", "'%");
			else return op;
		}
	
		private String rlikeOp(String op) {
			if (op != null && op.length() > 2 && op.endsWith("'") && !op.endsWith("%'"))
				return op.replaceFirst("'$", "%'");
			else return op;
		}
	
	
		private String likeOp(String op) {
			return rlikeOp(llikeOp(op));
		}
	}
	
	public static op op(String oper, boolean... withNot) {
		oper = oper.toLowerCase();
		op jc = "=".equals(oper) || "eq".equals(oper) ? op.eq
	   				 : "%".equals(oper) || "like".equals(oper) ? op.like
	   				 : "~%".equals(oper) || "=%".equals(oper) || "rlike".equals(oper) ? op.rlike
	   				 : "%~".equals(oper) || "%=".equals(oper) || "llike".equals(oper) ? op.llike
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
}
