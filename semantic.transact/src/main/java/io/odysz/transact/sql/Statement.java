package io.odysz.transact.sql;


import java.util.ArrayList;

import io.odysz.semantics.x.StException;
import io.odysz.transact.sql.parts.Logic;
import io.odysz.transact.sql.parts.Sql;
import io.odysz.transact.sql.parts.condition.AbsPart;
import io.odysz.transact.sql.parts.condition.Condit;

public abstract class Statement extends AbsPart {
	public enum Type { select, insert, update, delete }

	protected static boolean verbose = true;

	// private DataSource ds;

	protected String mainTabl;
	protected String mainAlias;
	
	/**Conditions of where clause * */
	// protected ArrayList<Condt> wheres;

	/**Conditions of where condtions
	 * 
	 */
	protected Condit where;

	protected Transcxt transc;

	public Statement(Transcxt transc, String tabl, String alias) {
		this.transc = transc;
		this.mainTabl = tabl;
		this.mainAlias = alias; // == null || alias.length == 0 ? null : alias[0];
	}

//	protected ArrayList<Object[]> nvs;


	public Statement where(String logic, String loperand, String roperand) {
		// try find operand in columns
		/*
		switch (Logic.op(logic)) {
		case eq:
			// orWheres.add(BinaryCondition.equalTo(loperand, roperand));
			where.and(Sql.Condt(logic, loperand, roperand));
			break;
		case ge:
			where.and(BinaryCondition.greaterThanOrEq(loperand, roperand));
			break;
		default:
			throw new StException("Logic not recogonized: %s", logic);
		}*/
		
		return where(Sql.condt(Logic.op(logic), loperand, roperand));
	}

	public Statement where(Condit condt, Condit... ands) {
		if (where == null)
			where = condt;

//		Condt top = null;
//		if (whereAnds.size() > 0) {
//			top = whereAnds.remove(whereAnds.size() - 1);
//		}
//
//		if (top != null) 
//			top.and(condt);
//		else top = condt;

		if (ands != null)
			for (Condit and : ands)
				where.and(and);
		return this;
	}
	
//	protected static Condition formatCond(String maintbl, String oper,
//			String ltabl, String lcol, String lconst,
//			String rtabl, String rcol, String rconst) {
//		Object lop = formatOperand(ltabl, lconst, lcol);
//		Object rop = formatOperand(rtabl, rconst, rcol);
//		
//		if (lop == null || rop == null) return null;
//
//   		String op = oper == null ? null : oper.trim().toLowerCase();
//
//   		// TODO merge with Statement.where()
//	   	Condition jc = "%".equals(op) || "like".equals(op) ? formatLikeCondition(lop, rconst)
//	   				 : "=%".equals(op) || "rlike".equals(op) ? formatRLikeCondition(lop, rconst)
//	   				 : "%=".equals(op) || "llike".equals(op) ? formatLLikeCondition(lop, rconst)
//   					 : "=".equals(op) || "eq".equals(op) ? BinaryCondition.equalTo(lop, rop)
//   					 : "<=".equals(op) || "le".equals(op) ? BinaryCondition.lessThanOrEq(lop, rop)
//   					 : "<".equals(op) || "lt".equals(op) ? BinaryCondition.lessThan(lop, rop)
//   					 : ">=".equals(op) || "ge".equals(op) ? BinaryCondition.greaterThanOrEq(lop, rop)
//   					 : ">".equals(op) || "gt".equals(op) ? BinaryCondition.greaterThan(lop, rop)
//   					 : "[]".equals(op) || "in".equals(op) ? new InCondition(lop, rop == null ? new Object[] {""} : (Object[])rconst.split(",")) // join condition shouldn't reach here.
//   					 : "][".equals(op) || "notin".equals(op) || "not in".equals(op) || "><".equals(op) ?
//   							 new InCondition(lop, rop == null ? new Object[] {""} : (Object[])rconst.split(",")).setNegate(true)
//   					 : BinaryCondition.notEqualTo(lop, rop); // <> !=
//		return jc;
//	}
	
//	private static Object formatOperand(String tabl, String conststr, String col) {
//		Object lop;
//		if (conststr != null)
//			lop = conststr.replaceFirst("^'", "").replaceAll("'$", "");
//		else {
////			if (aliases == null || !aliases.containsKey(tabl)) {
////				lop = ds.getColumn(connId, tabl, col);
////				// t11.lever -> t11."LEVEL"
////				if (lop != null && ds.isKeywords(connId, col))
////					lop = new CustomSql(String.format("%s.%s", tabl, ds.formatFieldName(connId, col)));
////				else if (lop == null)
////					if (verbose)
////						Utils.logi("Can't find column: ltabl = %s, lcol = %s, lconst = %s, condition ignored.", tabl, col, conststr);
////			}
////			else
////				lop = new CustomSql(String.format("%s.%s", tabl, col));
//			lop = new CustomSql(String.format("%s.%s", tabl, col));
//		}
//		return lop;
//	}
		
//	private static Condition formatLikeCondition(Object lop, String rconst) {
//		return BinaryCondition.like(lop, String.format("%%%s%%", rconst));
//	}

	/** lop like 'val%'
	 * @param connId
	 * @param aliases
	 * @param lop
	 * @param rconst
	 * @return
	 */
//	private static Condition formatLLikeCondition(Object lop, String rconst) {
//		return BinaryCondition.like(lop, String.format("%%%s", rconst));
//	}

	/** lop like '%val'
	 * @param connId
	 * @param aliases
	 * @param lop
	 * @param rconst
	 * @return
	 */
//	private static Condition formatRLikeCondition(Object lop, String rconst) {
//		return BinaryCondition.like(lop, String.format("%s%%", rconst));
//	}

	public Statement commit(ArrayList<String> sqls) throws StException {
		sqls.add(sql());
		return this;
	}

}
