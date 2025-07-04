package io.odysz.transact.sql.parts;

import io.odysz.common.Utils;
import io.odysz.common.dbtype;
import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.Query;
import io.odysz.transact.x.TransException;

public class Logic {
	/** empty type is used to subclass {@link io.odysz.transact.sql.parts.condition.Predicate}
	 * as {@link io.odysz.transact.sql.parts.condition.Condit}*/
	public enum type { and, or, not, empty };

	public enum op {eq,
		/** not equal: lop &lt;&gt; rop */
		ne, lt, le, gt, ge, mul, div, add, minus, like,
		/** right like: lop like '[rop]%' */
		rlike,
		/** left like: lop like '%[rop]' */
		llike, notlike, in, notin, isnull, isNotnull,
		/**
		 * <ul>
		 * <li><a href="https://dev.mysql.com/doc/refman/8.0/en/exists-and-not-exists-subqueries.html">
		 * Subqueries with EXISTS or NOT EXISTS</a></li>
		 * <li><a href="https://www.sqlite.org/lang_expr.html">sqlite: SQL Language Expressions</a></li>
		 * <li><a href="https://docs.oracle.com/en/database/other-databases/nosql-database/23.3/sqlreferencefornosql/exists-operator.html#GUID-E390CDFF-EC05-4735-95BE-967EC07BBAD6">
		 * Oracle Exists Operator</a></li>
		 * </ul>
		 * @since 1.4.40
		 */
		exists,
		/**
		 * @see #exists
		 * @since 1.4.40
		 */
		notexists;

		public String sql(ISemantext sctx, op oper, Object r) {
			String rop;
			try {
				rop = r instanceof Query
						? "(" + ((Query)r).sql(sctx) + ")"
						: r instanceof AbsPart 
						? ((AbsPart)r).sql(sctx)
						: r == null ? "" : r.toString();
			} catch (TransException e) {
				e.printStackTrace();
				rop = r.toString();
			}

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
				return "* " + (rop == null ? "" : rop);
			else if (oper == op.div)
				return "/ " + (rop == null ? "" : rop);
			else if (oper == op.add)
				return "+ " + (rop == null ? "" : rop);
			else if (oper == op.minus)
				return "- " + (rop == null ? "" : rop);
			else if (oper == op.like)
				return "like " + likeOp(sctx, rop);
			else if (oper == op.notlike)
				return "not like " + likeOp(sctx, rop);
			else if (oper == op.rlike)
//				if (negative != null && negative.length > 0 && negative[0])
//					return "not like " + rlikeOp(sctx, rop);
//				else
				return "like " + rlikeOp(sctx, rop);
			else if (oper == op.llike)
//				if (negative != null && negative.length > 0 && negative[0])
//					return "not like " + llikeOp(sctx, rop);
//				else
				return "like " + llikeOp(sctx, rop);
			else if (oper == op.in)
				return r instanceof String ? "in (" + rop.toString() + ")" : "in " + rop;
			else if (oper == op.notin)
				return r instanceof String ? "not in (" + rop.toString() + ")" : "not in " + rop;
			else if (oper == op.isnull)
//				if (negative != null && negative.length > 0 && negative[0])
//					return "is null";
//				else
					return "is null";
			else if (oper == op.isNotnull)
				return "is not null";
			else if (oper == op.exists)
				return "exists";
			else if (oper == op.notexists)
				return "not exists";
			else
				return " TODO ";
		}
	
		/**
		 * @param sctx
		 * @param op
		 * @return '%' + op
		 */
		private String llikeOp(ISemantext sctx, String op) {
			if (op != null && op.length() >= 2)
				if (op.startsWith("'") && !op.startsWith("'%"))
					return op.replaceFirst("^'", "'%");
				else return concat(sctx, "'%'", op);
			return op;
		}
	
		/**
		 * @param sctx
		 * @param op
		 * @return op + '%'
		 */
		private String rlikeOp(ISemantext sctx, String op) {
			if (op != null && op.length() >= 2)
				if (op.endsWith("'") && !op.endsWith("%'"))
					return op.replaceFirst("'$", "%'");
				else return concat(sctx, op, "'%'");
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

			if (db == dbtype.sqlite)
				return String.format("%s || %s)", op, with);
			else
				return String.format("concat(%s, %s)", op, with);
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
				// FIXME modify SearchExprs.g4
				// : "!%".equals(oper) || "not like".equals(oper) ? op.notlike
				: "not %".equals(oper) || "not like".equals(oper) ? op.notlike
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
				: "<>".equals(oper) || "!=".equals(oper) ? op.ne //; // <> !=
				: "exists".equals(oper) ? op.exists
				: "notexists".equals(oper) || "nonexists".equals(oper) ? op.notexists
				: null; // unknown

		if (withNot != null && withNot.length > 0 && withNot[0] == true) {
			jc  = jc == op.like ? op.notlike
				: jc == op.eq ? op.ne
				: jc == op.in ? op.notin
				: jc;
		}
		return jc;
	}
}
