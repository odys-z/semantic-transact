package io.odysz.transact.sql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import io.odysz.common.LangExt;
import io.odysz.common.Utils;
import io.odysz.semantics.ISemantext;
import io.odysz.semantics.IUser;
import io.odysz.semantics.SemanticObject;
import io.odysz.transact.sql.Query.Ix;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.sql.parts.Logic;
import io.odysz.transact.sql.parts.Sql;
import io.odysz.transact.sql.parts.condition.Condit;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.sql.parts.condition.Predicate;
import io.odysz.transact.x.TransException;

/**Statement like inert, update - the structured API.<br>
 * Design Notes:<br>
 * Statement don't know semantic context. Only when committing, it's needed.
 * @author odys-z@github.com
 *
 * @param <T> insert/update/delete/...
 */
@SuppressWarnings("unchecked")
public abstract class Statement<T extends Statement<T>> extends AbsPart {
	public interface IPostOperat {
		SemanticObject op (ISemantext ctx, ArrayList<String> sqls) throws TransException, SQLException;
	}

	public enum Type { select, insert, update, delete }

	protected static boolean verbose = true;

	protected String mainTabl;
	protected String mainAlias;
	public String alias() { return mainAlias; }
	
	/**Conditions of where conditions */
	protected Condit where;

	protected Transcxt transc;
	public Transcxt transc() { return transc; }
	
	/**Use this to make post updates etc have the same context with the main statement.
	 */
	protected ArrayList<Statement<?>> postate;

	protected IPostOperat postOp;

	protected ArrayList<Statement<?>> before;

	public Statement(Transcxt transc, String tabl, String alias) {
		this.transc = transc;
		this.mainTabl = tabl;
		this.mainAlias = alias; // == null || alias.length == 0 ? null : alias[0];
	}

	public T where(String logic, String loperand, String roperand) {
		return where(Sql.condt(Logic.op(logic), loperand, roperand));
	}
	
	public T where(ArrayList<String[]> conds) throws TransException {
		if (conds != null && conds.size() > 0)
			for (String[] cond : conds) 
				if (cond == null)
					continue;
				else if (cond.length != Ix.predicateSize)
					throw new TransException("SQL predicate size is invalid: %s",
								LangExt.toString(cond));
				else
					where(cond[Ix.predicateOper], cond[Ix.predicateL], cond[Ix.predicateR]);
		
		return (T) this;
	}
	
	public T where(Predicate pred) {
		if (pred instanceof Condit)
			return where((Condit)pred, new Condit[0]);
		else
			return where(new Condit(pred), new Condit[0]);
	}

	/**
	 * @param condt
	 * @param ands
	 * @return current Statement object
	 */
	public T where(Condit condt, Condit... ands) {
		if (where == null)
			where = condt;
		else
			where = where.and(condt);

		if (ands != null)
			for (Condit and : ands)
				where = where.and(and);
		return (T) this;
	}

	/**This is a wraper of {@link #where(String, String, String)} for convenient
	 * - the third arg is taken as a string constant and added single quotes at begin and end.
	 * @param op
	 * @param lcol left column
	 * @param rconst right constant will be adding single quotes "''"
	 * @return this
	 */
	public T where_(String op, String lcol, String rconst) {
		return where(op, lcol, "'" + rconst + "'");
	}

	public T where_(String op, String lcol, Object rconst) {
		// TODO something to be done to handle constant value other than type of string.
		return where_(op, lcol, (String)rconst);
	}
	
	public Statement<?> where(String logic, String loperand, ExprPart resulving) {
		return where(Sql.condt(Logic.op(logic), loperand, resulving));
	}

	/**Add post semantics after the parent statement,
	 * like add children after insert new parent.<br>
	 * <b>Side effect</b>: the added post statement's context is changed
	 * - to referencing the same instance for resolving, etc.
	 * @param postatement
	 * @return the calling statement
	 */
	public T post(Statement<?> postatement) {
		if (postate == null)
			postate = new ArrayList<Statement<?>>();
		this.postate.add(postatement);
		return (T) this;
	}

	/**Add setaments before this statement'sql been generated.<br>
	 * @param postatement
	 * @return the calling statement
	 */
	public T before(Statement<?> postatement) {
		if (before == null)
			before = new ArrayList<Statement<?>>();
		this.before.add(postatement);
		return (T) this;
	}

	/**Wrapper of {@link #post(Statement)}.
	/**Wrapper of {@link #post(Statement)}.
	 * @param posts
	 * @return
	 */
	public T post(ArrayList<Statement<?>> posts) {
		if (posts != null && posts.size() > 0)
			for (Statement<?> u : posts)
				post(u);
		return (T) this;
	}
	
	/**Generate all sqls, put into list, with a semantext created from usrInfo.
	 * This method is a wrapper of {@link #commit(ISemantext, ArrayList)}.
	 * @param sqls
	 * @param usrInfo
	 * @return the result set map[tabl, {newIds, resolved values}], ...
	 * @throws TransException
	 */
	public T commit(ArrayList<String> sqls, IUser... usrInfo) throws TransException {
		// ISemantext context = transc.ctx((T) this, mainTabl, usrInfo);
		ISemantext context = transc.instancontxt(usrInfo == null || usrInfo.length == 0 ? null : usrInfo[0]);
		
		if (context == null)
			Utils.warn("WARNING: your are building sql with null context, it should only happen for testing.");
		return commit(context, sqls);
	}
	
	/**Generating sqls with running context ctx.
	 * @param cxt
	 * @param sqls
	 * @return the resulting statement.
	 * @throws TransException
	 */
	public T commit(ISemantext cxt, ArrayList<String> sqls) throws TransException {
		prepare(cxt);

		// sql() calling onDelete (generating before sentences), must called before "before"
		String itself = sql(cxt);

		if (before != null)
			for (Statement<?> bf : before)
				bf.commit(cxt, sqls);

		if (!LangExt.isblank(itself))
			sqls.add(itself);

		if (postate != null)
			for (Statement<?> pst : postate)
				if (pst != null)
					pst.commit(cxt, sqls);

		return (T) this;
	}
	
	/**Set done operation - typically a database statement committing.<br>
	 * See {@link Query#rs(ISemantext)}
	 * @param operatn
	 */
	public void doneOp(IPostOperat operatn) {
		postOp = operatn;
	}

	/**Called when starting preparing sql, works like preparing auto generated key etc. should go here.
	 * @param ctx
	 */
	void prepare(ISemantext ctx) { }

	@Override
	public abstract String sql(ISemantext context) throws TransException;

	/** Get columns to be set / insert values.
	 * <p>{@link Delete} an {@link Insert} should override this method.</p>
	 * @return columns
	 */
	public Map<String, Integer> getColumns() { return null; }

}
