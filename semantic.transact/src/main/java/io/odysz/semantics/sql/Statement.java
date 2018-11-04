package io.odysz.semantics.sql;


import java.util.ArrayList;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.CustomSql;
import com.healthmarketscience.sqlbuilder.InCondition;

import io.odysz.semantics.sql.parts.Logic;
import io.odysz.semantics.sql.parts.Sql;
import io.odysz.semantics.sql.parts.Sql.Condt;
import io.odysz.semantics.x.StException;

public abstract class Statement {
	public enum Type { select, insert, update, delete }

	protected static boolean verbose = true;

	// private DataSource ds;

	protected String mt;
	protected String malias;
	
	/**Conditions of where condtions
	 * 
	 */
	protected ArrayList<Condt> wheres;

	protected Transc transc;

	public Statement(Transc transc, String tabl, String alias) {
		this.transc = transc;
		this.mt = tabl;
		this.malias = alias; // == null || alias.length == 0 ? null : alias[0];
	}

	public Statement where(String logic, String loperand, String roperand) throws StException {
		// try find operand in columns
		switch (Logic.op(logic)) {
		case eq:
			// orWheres.add(BinaryCondition.equalTo(loperand, roperand));
			wheres.add(Sql.Condt(logic, loperand, roperand));
			break;
		case ge:
			wheres.add(BinaryCondition.greaterThanOrEq(loperand, roperand));
			break;
		default:
			throw new StException("Logic not recogonized: %s", logic);
		}
		return this;
	}

	public Statement where(Condt condt) {
		wheres.add(condt);
		return this;
	}
	
	protected static Condition formatCond(String maintbl, String oper,
			String ltabl, String lcol, String lconst,
			String rtabl, String rcol, String rconst) {
		Object lop = formatOperand(ltabl, lconst, lcol);
		Object rop = formatOperand(rtabl, rconst, rcol);
		
		if (lop == null || rop == null) return null;

   		String op = oper == null ? null : oper.trim().toLowerCase();

	   	Condition jc = "%".equals(op) || "like".equals(op) ? formatLikeCondition(lop, rconst)
	   				 : "=%".equals(op) || "rlike".equals(op) ? formatRLikeCondition(lop, rconst)
	   				 : "%=".equals(op) || "llike".equals(op) ? formatLLikeCondition(lop, rconst)
   					 : "=".equals(op) || "eq".equals(op) ? BinaryCondition.equalTo(lop, rop)
   					 : "<=".equals(op) || "le".equals(op) ? BinaryCondition.lessThanOrEq(lop, rop)
   					 : "<".equals(op) || "lt".equals(op) ? BinaryCondition.lessThan(lop, rop)
   					 : ">=".equals(op) || "ge".equals(op) ? BinaryCondition.greaterThanOrEq(lop, rop)
   					 : ">".equals(op) || "gt".equals(op) ? BinaryCondition.greaterThan(lop, rop)
   					 : "[]".equals(op) || "in".equals(op) ? new InCondition(lop, rop == null ? new Object[] {""} : (Object[])rconst.split(",")) // join condition shouldn't reach here.
   					 : "][".equals(op) || "notin".equals(op) || "not in".equals(op) || "><".equals(op) ?
   							 new InCondition(lop, rop == null ? new Object[] {""} : (Object[])rconst.split(",")).setNegate(true)
   					 : BinaryCondition.notEqualTo(lop, rop); // <> !=
		return jc;
	}
	
	private static Object formatOperand(String tabl, String conststr, String col) {
		Object lop;
		if (conststr != null)
			lop = conststr.replaceFirst("^'", "").replaceAll("'$", "");
		else {
//			if (aliases == null || !aliases.containsKey(tabl)) {
//				lop = ds.getColumn(connId, tabl, col);
//				// t11.lever -> t11."LEVEL"
//				if (lop != null && ds.isKeywords(connId, col))
//					lop = new CustomSql(String.format("%s.%s", tabl, ds.formatFieldName(connId, col)));
//				else if (lop == null)
//					if (verbose)
//						Utils.logi("Can't find column: ltabl = %s, lcol = %s, lconst = %s, condition ignored.", tabl, col, conststr);
//			}
//			else
//				lop = new CustomSql(String.format("%s.%s", tabl, col));
			lop = new CustomSql(String.format("%s.%s", tabl, col));
		}
		return lop;
	}
		
	private static Condition formatLikeCondition(Object lop, String rconst) {
		return BinaryCondition.like(lop, String.format("%%%s%%", rconst));
	}

	/** lop like 'val%'
	 * @param connId
	 * @param aliases
	 * @param lop
	 * @param rconst
	 * @return
	 */
	private static Condition formatLLikeCondition(Object lop, String rconst) {
		return BinaryCondition.like(lop, String.format("%%%s", rconst));
	}

	/** lop like '%val'
	 * @param connId
	 * @param aliases
	 * @param lop
	 * @param rconst
	 * @return
	 */
	private static Condition formatRLikeCondition(Object lop, String rconst) {
		return BinaryCondition.like(lop, String.format("%s%%", rconst));
	}

	public Statement commit(ArrayList<String> sqls) throws StException {
		throw new StException("Shouldn't reach here");
	}

}
