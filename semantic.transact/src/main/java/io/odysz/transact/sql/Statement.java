package io.odysz.transact.sql;

import java.sql.SQLException;
import java.util.ArrayList;

import io.odysz.semantics.ISemantext;
import io.odysz.semantics.IUser;
import io.odysz.semantics.SemanticObject;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.sql.parts.Logic;
import io.odysz.transact.sql.parts.Sql;
import io.odysz.transact.sql.parts.condition.Condit;
import io.odysz.transact.x.TransException;

@SuppressWarnings("unchecked")
public abstract class Statement<T extends Statement<T>> extends AbsPart {
	public interface IPostOperat {
		// TODO return SemanticObject?
		Object op (ArrayList<String> sqls) throws TransException, SQLException;
	}

	public enum Type { select, insert, update, delete }

	protected static boolean verbose = true;

	protected String mainTabl;
	protected String mainAlias;
	
	/**Conditions of where conditions */
	protected Condit where;

	protected Transcxt transc;

	private ArrayList<Statement<?>> postate;

	protected IPostOperat postOp;

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
	
	/**Add post semantics after the parent statement, like add children after insert new parent.
	 * @param postatement
	 * @return the calling statement
	 */
	public <U extends Statement<U>> T post(U postatement) {
		if (postate == null)
			postate = new ArrayList<Statement<?>>();
		this.postate.add(postatement); 
		return (T) this;
	}
	
	/**Generat all sqls, put int list.
	 * @param sqls
	 * @param usrInfo
	 * @return sqls
	 * @throws TransException
	 */
	public SemanticObject commit(ArrayList<String> sqls, IUser... usrInfo) throws TransException {
		ISemantext context = transc.ctx((T) this, mainTabl, usrInfo);
		commit(context, sqls);
		return context == null ? null : context.results();
	}
	
	protected T commit(ISemantext cxt, ArrayList<String> sqls) throws TransException {
		sqls.add(sql(cxt));
		if (postate != null)
			for (Statement<?> pst : postate)
				pst.commit(cxt, sqls);
		return (T) this;
	}
	
	/**Set done operation - typically a database statement committing.<br>
	 * See {@link Query#rs()}
	 * @param operat
	 */
	public void doneOp(IPostOperat operat) {
		postOp = operat;
	}


}
