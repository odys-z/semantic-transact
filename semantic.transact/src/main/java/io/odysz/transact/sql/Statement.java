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

	/**
	 * Callback of post commitment operation.
	 * @since 1.4.12
	 */
	public interface IPostOptn {
		SemanticObject onCommitOk (ISemantext ctx, ArrayList<String> sqls)
				throws TransException, SQLException;
	}
	
	/**
	 * Callback of post query operation.
	 * @since 1.4.12
	 */
	public interface IPostSelectOptn {
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
	protected IPostOptn postOp;

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
	public T nv(String n, String v) {
		String conn = transc.basictx == null ? null : transc.basictx.connId();

		TableMeta mt = transc.tableMeta(mainTabl.name())
				.conn(conn);
		return nv(n, composeVal(v, mt, n));
	}

	public T nv(String n, long v) {
		return nv(n, String.valueOf(v));
	}

	public T nv(String n, int v) {
		return nv(n, String.valueOf(v));
	}

	public T nv(String n, String[] v) {
		return (T) nv(n, Stream
					.of(v)
					.filter(m -> !isblank(m))
					.collect(Collectors.joining(",")));
	}

	public T nv(String n, AbsPart v) {
		Utils.warn("Statement.nv(): Only Update and Insert can use nv() function.");
		return (T) this;
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
	
	public T whereAnds(Condit ... andCondts ) {
		if (where == null)
			where = andCondts[0];

		for (int i = 1; i < andCondts.length; i++)
			where = where.and(andCondts[i]);

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
	
	public T where(String logic, String loperand, ExprPart roperand) {
		return where(Logic.op(logic), loperand, roperand);
	}

	public T where(Logic.op op, String loperand, ExprPart roperand) {
		return where(Sql.condt(op, loperand, roperand));
	}

	/**
	 * E.g. where t.id in select id from tab. 
	 * @param logic
	 * @param loperand
	 * @param q
	 * @return this
	 * @throws TransException
	 */
	public T where(Logic.op logic, String loperand, Query q) throws TransException {
		return where(Sql.condt(logic, loperand, q));
	}

	public T where(Logic.op op, String lop, Statement<?> statement) throws TransException {
		if (statement instanceof Query)
			return (T) where(op, lop, (Query)statement);
		else
			throw new TransException("Don't use this way. (statement must be a Query object)");
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

	public T where(Logic.op op, String loperand, Long roperand) {
		return where(Sql.condt(op, loperand, roperand.toString()));
	}

	public T where(Logic.op op, String loperand, Integer roperand) {
		return where(Sql.condt(op, loperand, roperand.toString()));
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

	public T whereEq(String tabl, String col, String constv) {
		return where_("=", String.format("%s.%s", tabl, col), constv);
	}

	/**
	 * where tablA.colA = tblB.colB
	 * @param tblA
	 * @param colA
	 * @param tblB
	 * @param colB
	 * @return this
	 * @since 1.4.40
	 */
	public T whereEq(String tblA, String colA, String tblB, String colB) {
		return where("=", String.format("%s.%s", tblA, colA), String.format("%s.%s", tblB, colB));
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
	 * TODO rewrite using Condit.or()
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
	
	public T whereEq(String col, Query q) throws TransException {
		return where(Sql.condt(Logic.op.eq, col, q));
	}

	/**
	 * @param col
	 * @param constv
	 * @return this
	 * @since v1.3.0
	 */
	public T whereIn(String col, String[] constv) {
		ExprPart inOp = new ExprPart(constv);
		return where(Sql.condt(Logic.op.in, col, inOp));
	}

	public T whereIn(String col, List<String> constvs) {
		if (isblank(constvs))
			return (T) this;
		else
			return whereIn(col, constvs.toArray(new String[0]));
	}

	/**
	 * where clause for "In Select".
	 * @param col
	 * @param q the static query - query can't be generated with context.
	 * @return this
	 * @since 1.4.36
	 */
	public T whereIn(String col, Query q) {
		return where(Sql.condt(Logic.op("in"), col, q.sql(null)));
	}

	/**
	 * <p>Add post semantics after the parent statement,
	 * like add children after insert new parent.</p>
	 * <b>Side effect</b>: the added post statement's context is changed
	 * - to referencing the same instance for resolving, etc.
	 * @param postatement
	 * @return this
	 */
	public T post(Statement<?> postatement) {
		if (postatement != null) {
			if (postate == null)
				postate = new ArrayList<Statement<?>>();
			this.postate.add(postatement);
		}
		return (T) this;
	}

	/**
	 * Add setaments before this statement'sql been generated.<br>
	 * @param postatement
	 * @return this
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
	
	/**
	 * Generating sqls with running context ctx.
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
	public void doneOp(IPostOptn operatn) {
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
			return ExprPart.constr((String)v);
		else if (mt != null && !isQuoted && v == null)
			return ExprPart.constVal(null);
		else if (mt != null && !isQuoted && LangExt.isblank(v, "''", "null"))
			return ExprPart.constVal("0");
		else
			return new ExprPart((String)v);
	}

}
