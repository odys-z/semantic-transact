package io.odysz.transact.sql.parts;

import io.odysz.common.Utils;
import io.odysz.common.dbtype;
import io.odysz.semantics.ISemantext;

public class Logic {
	/** empty type is used to subclass {@link io.odysz.transact.sql.parts.condition.Predicate}
	 * as {@link io.odysz.transact.sql.parts.condition.Condit}*/
	public enum type { and, or, not, empty };

	public enum op {eq, ne, lt, le, gt, ge, mul, div, add, minus, like, rlike, llike, notlike, in, notin, isnull, isNotnull;

		public String sql(ISemantext sctx, op oper, String rop, boolean... negative) {
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
			else if (oper == op.mul)
				return "* ";
			else if (oper == op.div)
				return "/ ";
			else if (oper == op.add)
				return "+ ";
			else if (oper == op.minus)
				return "- ";
			else if (oper == op.like)
				if (negative != null && negative.length > 0 && negative[0])
					return "not like " + likeOp(sctx, rop);
				else
					return "like " + likeOp(sctx, rop);
			else if (oper == op.rlike)
				if (negative != null && negative.length > 0 && negative[0])
					return "not like " + rlikeOp(sctx, rop);
				else
					return "like " + rlikeOp(sctx, rop);
			else if (oper == op.llike)
				if (negative != null && negative.length > 0 && negative[0])
					return "not like " + llikeOp(sctx, rop);
				else
					return "like " + llikeOp(sctx, rop);
			else if (oper == op.in)
				if (negative != null && negative.length > 0 && negative[0])
					return "not in (" + rop + ")";
				else
					return "in (" + rop + ")";
			else if (oper == op.isnull)
				if (negative != null && negative.length > 0 && negative[0])
					return "is null";
				else
					return "is null";
			else if (oper == op.isNotnull)
				return "is not null";
			else
				return " TODO ";
		}
	
		private String llikeOp(ISemantext sctx, String op) {
			if (op != null && op.length() > 2)
				if (op.startsWith("'") && !op.startsWith("'%"))
					return op.replaceFirst("^'", "'%");
				else return concat(sctx, "'%'", op);
			return op;
		}
	
		private String concat(ISemantext sctx, String op, String with) {
			if (sctx == null) {
				Utils.warn("generating db function concat(%s, %s), should only happen for testing, but semantext is null. This should only happen for testing.", op, with);
				return String.format("concat(%s, %s)", op, with);
			}
			dbtype db = sctx.dbtype();

			if (db != dbtype.mysql || db != dbtype.oracle || db != dbtype.sqlite)
				Utils.warn("db function concat(%s, %s)", op, with);
			return String.format("concat(%s, %s)", op, with);
		}

		private String rlikeOp(ISemantext sctx, String op) {
			if (op != null && op.length() > 2)
				if (op.endsWith("'") && !op.endsWith("%'"))
					return op.replaceFirst("'$", "%'");
				else return concat(sctx, op, "'%'");
			return op;
		}
	
	
		private String likeOp(ISemantext sctx, String op) {
			return rlikeOp(sctx, llikeOp(sctx, op));
		}
	}
	
	public static op op(String oper, boolean... withNot) {
		oper = oper.toLowerCase();
		// check SearchExprs.g4, ?0, !?0, etc. are not working
		op jc = "=".equals(oper) || "eq".equals(oper) ? op.eq
					 : "*".equals(oper) ? op.mul
					 : "/".equals(oper) ? op.div
					 : "+".equals(oper) ? op.add
					 : "-".equals(oper) ? op.minus
	   				 : "%".equals(oper) || "like".equals(oper) ? op.like
	   				 : "~%".equals(oper) || "rlike".equals(oper) ? op.rlike // =% is not correct
	   				 : "%~".equals(oper) || "llike".equals(oper) ? op.llike // %= is assignment operator
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
