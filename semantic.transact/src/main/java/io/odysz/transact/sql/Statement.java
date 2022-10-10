package io.odysz.transact.sql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.common.LangExt;
import io.odysz.common.Utils;
import io.odysz.semantics.ISemantext;
import io.odysz.semantics.IUser;
import io.odysz.semantics.SemanticObject;
import io.odysz.semantics.meta.TableMeta;
import io.odysz.transact.sql.Query.Ix;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.sql.parts.Alias;
import io.odysz.transact.sql.parts.Logic;
import io.odysz.transact.sql.parts.Sql;
import io.odysz.transact.sql.parts.Tabl;
import io.odysz.transact.sql.parts.condition.Condit;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.sql.parts.condition.Predicate;
import io.odysz.transact.x.TransException;

/**Statement like insert, update - the structured API.<br>
 * Design Notes:<br>
 * Statement don't know semantic context. Only when committing, it's needed.
 * @author odys-z@github.com
 *
 * @param <T> insert/update/delete/...
 */
@SuppressWarnings("unchecked")
public abstract class Statement<T extends Statement<T>> extends AbsPart {
	protected static boolean verbose = true;

	public enum Type { select, insert, update, delete }

	public interface IPostOperat {
		SemanticObject onCommitOk (ISemantext ctx, ArrayList<String> sqls)
				throws TransException, SQLException;
	}
	
	public interface IPostSelectOperat {
		void onSelected (ISemantext ctx, ArrayList<Object> row,
				HashMap<String, Object[]> cols) throws TransException, SQLException;
	}

	protected Tabl mainTabl;
	public Tabl mainTabl() { return mainTabl; }

	protected Alias mainAlias;
	public Alias alias() { return mainAlias; }
	
	/**Conditions of where conditions */
	protected Condit where;
	public Condit where() { return where; }

	protected Transcxt transc;
	public Transcxt transc() { return transc; }
	
	/**Use this to make post updates etc have the same context with the main statement.
	 */
	protected ArrayList<Statement<?>> postate;

	/**The committing to db operation callback */
	protected IPostOperat postOp;

	protected ArrayList<Statement<?>> before;

	public Statement(Transcxt transc, String tabl, String alias) {
		this.transc = transc;
		this.mainTabl = new Tabl(tabl);
		this.mainAlias = alias == null ? null : new Alias(alias);
	}

	private ArrayList<Object[]> attaches;
	public ArrayList<Object[]> attaches() { return attaches; }

	/**Add attachments can be handled by <i>attaches</i> semantics handler.
	 * @param attaches
	 * @return this
	 */
	public T attachs(ArrayList<Object[]> attaches) {
		this.attaches = attaches;
		return (T) this;
	}

	/**A wrapper that setting values to {@link Update} and {@link Insert}.<br>
	 * The null/empty values are handled differently according data meta.<br>
	 * See the <a href='https://odys-z.github.io/notes/semantics/ref-transact.html#ref-transact-empty-vals'>discussions</a>.
	 * @param n
	 * @param v
	 * @return this statement
	 * @throws TransException
	 */
	public T nv(String n, String v) throws TransException {
		String conn = transc.basictx == null ? null : transc.basictx.connId();
		TableMeta mt = transc.tableMeta(conn, mainTabl.name())
				.conn(conn);
		return nv(n, composeVal(v, mt, n));
	}

	public T nv(String n, AbsPart v) throws TransException {
		throw new TransException("Only Update and Insert can use nv() function.");
	}

	public T where(String logic, String loperand, String roperand) {
		return where(Sql.condt(Logic.op(logic), loperand, roperand));
	}
	
	/**Append where clause, with arguments of array.<br>
	 * Each element is arguments array is index by {@link io.odysz.transact.sql.Query.Ix Query.Ix}.
	 * @param conds
	 * @return this
	 * @throws TransException
	 */
	public T where(ArrayList<Object[]> conds) throws TransException {
		if (conds != null && conds.size() > 0)
			for (Object[] cond : conds) 
				if (cond == null)
					continue;
				else if (cond.length != Ix.predicateSize)
					throw new TransException("SQL predicate size is invalid: %s",
								LangExt.toString(cond));
				else if (cond[Ix.predicateR] instanceof ExprPart)
					where((String)cond[Ix.predicateOper], (String)cond[Ix.predicateL], (ExprPart)cond[Ix.predicateR]);
				else
					where((String)cond[Ix.predicateOper], (String)cond[Ix.predicateL], (String)cond[Ix.predicateR]);
		
		return (T) this;
	}
	
	public T where(Predicate pred) {
		if (pred instanceof Condit)
			return where((Condit)pred, new Condit[0]);
		else
			return where(new Condit(pred), new Condit[0]);
	}

	/**Set 'where' clause conditions with 'AND' logic.
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
		return where(op, lcol, rconst == null ? "null" : "'" + rconst + "'");
	}

	public T where_(String op, String lcol, Object rconst) {
		// TODO something to be done to handle constant value other than type of string.
		return where_(op, lcol, (String)rconst);
	}
	
	public Statement<?> where(String logic, String loperand, ExprPart resulving) {
		return where(Sql.condt(Logic.op(logic), loperand, resulving));
	}

	/**This is a wraper of {@link #where(String, String, String)} for convenient
	 * - the third arg is taken as a string constant and added single quotes at begin and end.
	 * @param op
	 * @param loperand left column
	 * @param roperand right constant will NOT be adding single quotes "''"
	 * @return this
	 */
	public T where(Logic.op op, String loperand, String roperand) {
		return where(Sql.condt(op, loperand, roperand));
	}

	/**This is a wraper of {@link #where(String, String, String)} for convenient
	 * - the third arg is taken as a string constant and added single quotes at begin and end.
	 * @param op
	 * @param loperand
	 * @param roperand right constant WILL be adding single quotes "''"
	 * @return this
	 */
	public T where_(Logic.op op, String loperand, String roperand) {
		return where(Sql.condt(op, loperand, "'" + roperand + "'"));
	}

	/**Add a where condition. e.g. "t.col = 'constv'".
	 * @param col
	 * @param constv will add "'"
	 * @return this
	 */
	public T whereEq(String col, String constv) {
		return where_("=", col, constv);
	}

	public T whereEqOr(String col, String[] ors) {
		// Temporary solution for API lack of precedence handling
		String exp = null;
		
		exp = Stream.of(ors)
				.map(m -> {
					return String.format("%s = '%s'", col, m);
				})
				.collect(Collectors.joining("(", " or ", ")"));
		return where_(exp, "", "");
	}
	
	/**
	 * Add where condition clause which is embedded in a pair of parentheses.
	 * 
	 * FIXME
	 * FIXME
	 * FIXME rewrite using Condit.or()
	 * 
	 * @param col
	 * @param constv can not be col name
	 * @param orConstvs can not be col names
	 * @return (col = 'constv' or col = 'orConstvs[0]' ...) 
	 */
	public T whereEqOr(String col, String constv, String ... orConstvs) {
		String exp = Stream.of(constv, orConstvs)
				.map(m -> {
					return m == null
						? String.format("%s = null", col)
						: String.format("%s = '%s'", col, m);
				})
				.collect(Collectors.joining("(", " or ", ")"));
		return where_(exp, "", "");
	}

	/** where col like '%likev%'
	 * @param col
	 * @param likev
	 * @return this
	 */
	public T whereLike(String col, String likev) {
		return where("%", col, "'" + likev + "'");
	}

	public T whereEq(String col, Object v) {
		if (v instanceof String)
			return where_("=", col, (String)v);
		else
			return where(Sql.condt(Logic.op.eq, col, (ExprPart)v));
	}
	
	/**Tag: v1.3.0
	 * @param col
	 * @param constv
	 * @return this
	 */
	public T whereIn(String col, String[] constv) {
		ExprPart inOp = new ExprPart(constv);
		return where(Sql.condt(Logic.op.in, col, inOp));
	}

	public T whereIn(String col, List<String> constvs) {
		return whereIn(col, constvs.toArray(new String[0]));
	}

	/**
	 * <p>Add post semantics after the parent statement,
	 * like add children after insert new parent.</p>
	 * <b>Side effect</b>: the added post statement's context is changed
	 * - to referencing the same instance for resolving, etc.
	 * @param postatement
	 * @return the calling statement
	 */
	public T post(Statement<?> postatement) {
		if (postatement != null) {
			if (postate == null)
				postate = new ArrayList<Statement<?>>();
			this.postate.add(postatement);
		}
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
	 * @param posts
	 * @return this
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
		ISemantext context = transc.instancontxt(
				transc != null && transc.basictx != null ? transc.basictx.connId() : null,
				usrInfo == null || usrInfo.length == 0 ? null : usrInfo[0]);
		
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

	public static ExprPart composeVal(Object v, TableMeta mt, String col) {
		boolean isQuoted = mt == null || mt.isQuoted(col);
		if (mt == null || isQuoted)
			return ExprPart.constStr((String)v);
		else if (mt != null && !isQuoted && v == null)
			return ExprPart.constVal(null);
		else if (mt != null && !isQuoted && LangExt.isblank(v, "''", "null"))
			return ExprPart.constVal("0");
		else
			return new ExprPart((String)v);
	}

}
