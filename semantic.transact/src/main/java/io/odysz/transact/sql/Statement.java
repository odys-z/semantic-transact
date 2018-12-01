package io.odysz.transact.sql;


import java.util.ArrayList;

import io.odysz.transact.x.TransException;
import io.odysz.transact.sql.parts.Logic;
import io.odysz.transact.sql.parts.Sql;
import io.odysz.semantics.Semantext;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.sql.parts.condition.Condit;

@SuppressWarnings("unchecked")
public abstract class Statement<T extends Statement<T>> extends AbsPart {
	public enum Type { select, insert, update, delete }

	protected static boolean verbose = true;

	protected String mainTabl;
	protected String mainAlias;
	
	/**Conditions of where clause * */
	// protected ArrayList<Condt> wheres;

	/**Conditions of where condtions
	 * 
	 */
	protected Condit where;

	protected Transcxt transc;

	private ArrayList<Statement<?>> postate;

	public Statement(Transcxt transc, String tabl, String alias) {
		this.transc = transc;
		this.mainTabl = tabl;
		this.mainAlias = alias; // == null || alias.length == 0 ? null : alias[0];
	}

	public T where(String logic, String loperand, String roperand) {
		return where(Sql.condt(Logic.op(logic), loperand, roperand));
	}

	/**
	 * @param condt
	 * @param ands
	 * @return current Statement object
	 */
	public T where(Condit condt, Condit... ands) {
		if (where == null)
			where = condt;

		if (ands != null)
			for (Condit and : ands)
				where.and(and);
		return (T) this;
	}
	
	public <U extends Statement<U>> T post(U postatement) {
		if (postate == null)
			postate = new ArrayList<Statement<?>>();
		this.postate.add(postatement); 
		return (T) this;
	}
	
	public T commit(ArrayList<String> sqls) throws TransException {
		Semantext context = transc.inert(mainTabl);
		return commit(context, sqls);
	}
	
	protected T commit(Semantext cxt, ArrayList<String> sqls) throws TransException {
		sqls.add(sql(cxt));
		if (postate != null)
			for (Statement<?> pst : postate)
				pst.commit(cxt, sqls);
		return (T) this;
	}
	
}
