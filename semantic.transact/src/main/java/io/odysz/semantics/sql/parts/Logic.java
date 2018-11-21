package io.odysz.semantics.sql.parts;

public class Logic {
	public enum type { and, or, not };

	public enum op {eq, ne, lt, le, gt, ge, like, rlike, llike, notlike, in, notin, isnull, isNotnull;

		public String sql(op op, String rop, boolean... not) {
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
				if (not != null && not.length > 0 && not[0])
					return "not like " + likeOp(rop);
				else
					return "like " + likeOp(rop);
			case rlike:
				if (not != null && not.length > 0 && not[0])
					return "not like " + rlikeOp(rop);
				else
					return "like " + rlikeOp(rop);
			case llike:
				if (not != null && not.length > 0 && not[0])
					return "not like " + llikeOp(rop);
				else
					return "like " + llikeOp(rop);
			case in:
				if (not != null && not.length > 0 && not[0])
					return "not in (" + rop + ")";
				else
					return "in (" + rop + ")";
			case isnull:
				if (not != null && not.length > 0 && not[0])
					return "is not null";
				else
					return "is null";
			case isNotnull:
				return "is not null";
			default:
				return " TODO ";
			}
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
