package io.odysz.semantics.sql.parts;

import java.util.ArrayList;

import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.CustomCondition;

public class Sql {
	
//	public static Condt condt(String format, String... args) {
//		return new Condt(new ExprBuilder (format, (Object[])args));
//	}

	public static class Condt {
		
		private ArrayList<ExprBuilder> ands;

		private ArrayList<Object> ors;

		public Condt(ExprBuilder expr) {
		}

		public Condt and(String logic, String from, String... to) {
			ExprBuilder expr = new ExprBuilder(logic).left(from).right(to == null || to.length == 0 ? null : to[0]);
			if (ands == null)
				ands = new ArrayList<ExprBuilder>(1);
			ands.add(expr);
			return this;
		}

		public Condt or(String logic, String from, String... to) {
			ExprBuilder expr = new ExprBuilder(logic).left(from).right(to == null || to.length == 0 ? null : to[0]);
			
			if (ands != null) {
				ors.add(ands);
				ors.clear();
			}

			if (ors == null)
				ors.add(expr);

			return this;
		}
	
		public Condt or(Condt c1, Condt... c2) {
			return this;
		}

//		private static Condition formatCond(String connId, HashMap<String, String> aliases, String maintbl, String oper,
//				String ltabl, String lcol, String lconst,
//				String rtabl, String rcol, String rconst) {
//			Object lop = formatOperand(connId, aliases, ltabl, lconst, lcol);
//			Object rop = formatOperand(connId, aliases, rtabl, rconst, rcol);
//			
//			if (lop == null || rop == null) return null;
//	
//	   		String op = oper == null ? null : oper.trim().toLowerCase();
//	
//		   	Condition jc = "%".equals(op) || "like".equals(op) ? formatLikeCondition(connId, aliases, lop, rconst)
//		   				 : "=%".equals(op) || "rlike".equals(op) ? formatRLikeCondition(connId, aliases, lop, rconst)
//		   				 : "%=".equals(op) || "llike".equals(op) ? formatLLikeCondition(connId, aliases, lop, rconst)
//	   					 : "=".equals(op) || "eq".equals(op) ? BinaryCondition.equalTo(lop, rop)
//	   					 : "<=".equals(op) || "le".equals(op) ? BinaryCondition.lessThanOrEq(lop, rop)
//	   					 : "<".equals(op) || "lt".equals(op) ? BinaryCondition.lessThan(lop, rop)
//	   					 : ">=".equals(op) || "ge".equals(op) ? BinaryCondition.greaterThanOrEq(lop, rop)
//	   					 : ">".equals(op) || "gt".equals(op) ? BinaryCondition.greaterThan(lop, rop)
//	   					 : "><".equals(op) || "[]".equals(op) || "in".equals(op) ? new InCondition(lop, rop == null ? new Object[] {""} : (Object[])rconst.split(",")) // join condition shouldn't reach here.
//	   					 : "][".equals(op) || "notin".equals(op) || "not in".equals(op) ? new InCondition(lop, rop == null ? new Object[] {""} : (Object[])rconst.split(",")).setNegate(true)
//	   					 : BinaryCondition.notEqualTo(lop, rop); // <> !=
//			return jc;
//		}
//	
//		private static Condition formatLikeCondition(String connId, HashMap<String, String> aliases, Object lop, String rconst) {
//			return BinaryCondition.like(lop, String.format("%%%s%%", rconst));
//		}
//	
//		/** lop like 'val%'
//		 * @param connId
//		 * @param aliases
//		 * @param lop
//		 * @param rconst
//		 * @return
//		 */
//		private static Condition formatLLikeCondition(String connId, HashMap<String, String> aliases, Object lop, String rconst) {
//			return BinaryCondition.like(lop, String.format("%%%s", rconst));
//		}
//	
//		/** lop like '%val'
//		 * @param connId
//		 * @param aliases
//		 * @param lop
//		 * @param rconst
//		 * @return
//		 */
//		private static Condition formatRLikeCondition(String connId, HashMap<String, String> aliases, Object lop, String rconst) {
//			return BinaryCondition.like(lop, String.format("%s%%", rconst));
//		}
	
	}

	/**
	 * @param users
	 * @return "'ele1','ele2','ele3',..."
	 */
	public static String str(String[] ele) {
		return "'ele1','ele2','ele3'";
	}

}
