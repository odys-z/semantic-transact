package io.odysz.transact.sql.parts.select;

import static io.odysz.common.LangExt.isNull;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.common.Utils;
import io.odysz.common.dbtype;
import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.Delete;
import io.odysz.transact.sql.Query;
import io.odysz.transact.sql.Update;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.sql.parts.Tabl;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.sql.parts.select.SelectElem.ElemType;
import io.odysz.transact.x.TransException;

/**
 * https://github.com/antlr/grammars-v4/blob/master/tsql/TSqlParser.g4
 * <pre>with_expression
    : WITH (XMLNAMESPACES ',')? common_table_expression (',' common_table_expression)*
    | WITH BLOCKING_HIERARCHY ('(' full_column_name_list ')')? AS '(' select_statement ')'
    ;</pre>
 *
 * <p>Since 1.5.79, a recursive table can be built fluently, then attached to a statement,
 * without being held by the {@link io.odysz.transact.sql.Transcxt}:</p>
 * <pre>
 * st.with(true)
 *   .withTable("sub_(nid)")
 *   .withVal(constr("a2"))	// select 'a2'
 *   .union(st.select("doctree", "me")
 *            .col("me.nid")
 *            .je("me", "sub_", "s", "parent", "nid"))
 *   .delete(st.delete("doctree"))
 *   .whereIn("nid", st.select("sub_").col("nid"))
 *
 * with recursive sub_(nid) as (select 'a2' union select me.nid from doctree me join sub_ s on me.parent = s.nid)
 * delete from doctree where nid in (select nid from sub_)</pre>
 *
 * @since 1.4.36
 * @author Ody
 *
 */
public class WithClause extends AbsPart {

	static final ExprPart unionAll = new ExprPart("union all");
	static final ExprPart unionDistinct = new ExprPart("union");

	boolean recursive;
	/**
	 * Recursive: [[with clause' as-tabl-name, anchor (initial value), {@link Query}, union operator]],
	 * where union operator is optional, default "union all";<br>
	 * not recursive: [[{@link Query}]], with the query's alias as the table name.
	 */
	ArrayList<AbsPart[]> withs;

	/** The recursive table being built by {@link #withTable(String)} ... {@link #union(Query)}. */
	private Tabl buildingTabl;
	/** The anchor (initial value) being built by {@link #withVal(String)}. */
	private AbsPart buildingVal;

	public WithClause(boolean recursive, ArrayList<AbsPart[]> withs) {
		this.recursive = recursive;
		this.withs = withs;
	}

	public WithClause(boolean recursive) {
		this.recursive = recursive;
		this.withs = new ArrayList<AbsPart[]>();
	}

	public WithClause recursive(boolean recur) {
		this.recursive = recur;
		return this;
	}

	@Override
	public String sql(ISemantext sctx) throws TransException {
		if (isNull(withs))
			return "";

		ArrayList<String> tabls = new ArrayList<String>(withs.size());
		for (AbsPart[] q : withs) {
			if (q == null)
				continue;
			if (q.length >= 3)
				// recursive table: name as (anchor union [all] query)
				tabls.add(Stream.of(
						((Tabl)q[0]).sql(sctx),
						" as (",
						q[1].sql(sctx),
						" ",
						(q.length > 3 && q[3] != null ? q[3] : unionAll).sql(sctx),
						" ",
						((Query) q[2]).sql(sctx),
						")")
					.collect(Collectors.joining()));
			else
				tabls.add(Stream.of(
						((Query)q[0]).alias().sql(sctx),
						" as (",
						((Query) q[0]).sql(sctx),
						")")
					.collect(Collectors.joining()));
		}

		boolean recur = recursive;
		if (recur && sctx != null && sctx.dbtype() == dbtype.ms2k) {
			// SQL Server doesn't accept the keyword: a with-table referencing itself is recursive.
			Utils.warn("WithClause: SQL Server has no keyword 'recursive', which is ignored. %s", tabls);
			recur = false;
		}

		return tabls.stream().collect(Collectors.joining(", ", recur ? "with recursive " : "with ", ""));
	}

	public WithClause with(String recurTabl, String rootValue, Query subSelect) {
		if (this.withs == null)
			this.withs = new ArrayList<AbsPart[]>();
		this.withs.add(new AbsPart[] {new Tabl(recurTabl), new SelectElem(ElemType.constant, rootValue), subSelect});
		return this;
	}

	public WithClause with(Query q0, Query[] qi) {
		if (this.withs == null)
			this.withs = new ArrayList<AbsPart[]>();

		if (!isNull(q0)) {
			if (isblank(q0.alias()))
					Utils.warn("Adding with-table without alias? %s", q0.sql(null));;
			this.withs.add(new AbsPart[] {q0});
		}

		if (!isNull(qi))
			for (Query q : qi) {
				if (isblank(q.alias()))
						Utils.warn("Adding with-table without alias? %s", q.sql(null));;
				this.withs.add(new AbsPart[] {q});
			}

		return this;
	}

	/**
	 * Start a recursive table, completed by {@link #union(Query)} or {@link #unionAll(Query)}.
	 * @param recurTabl table name and columns, e. g. "sub_(nid)"
	 * @return this
	 * @since 1.5.79
	 */
	public WithClause withTable(String recurTabl) {
		this.buildingTabl = new Tabl(recurTabl);
		this.buildingVal = null;
		return this;
	}

	/**
	 * The anchor (initial rows) of the recursive table, as a sql snippet, used as is.
	 * @param sql e. g. "values('kerson', 'ur-zsu', 0)", or "select ..."
	 * @return this
	 * @since 1.5.79
	 */
	public WithClause withVal(String sql) {
		this.buildingVal = new ExprPart(sql).escape(false);
		return this;
	}

	/**
	 * The anchor (initial row) of the recursive table, one value per column, each generated
	 * by its expression, e.g. {@link ExprPart#constr(String)} for a quoted string,
	 * {@link ExprPart#constVal(String)}, or a {@link io.odysz.transact.sql.parts.condition.Funcall}.
	 * The row is generated according to the db type of the semantext:
	 * <pre>select v0, v1      (sqlite, mysql, ms2k, ...)
	 *select v0, v1 from dual      (oracle)</pre>
	 * @param vals
	 * @return this
	 * @since 1.5.79
	 */
	public WithClause withVal(ExprPart... vals) {
		this.buildingVal = new AbsPart() {
			@Override
			public String sql(ISemantext ctx) throws TransException {
				ArrayList<String> vs = new ArrayList<String>(vals.length);
				for (ExprPart v : vals)
					vs.add(v.sql(ctx));
				return vs.stream().collect(Collectors.joining(", ", "select ",
						ctx != null && ctx.dbtype() == dbtype.oracle ? " from dual" : ""));
			}
		};
		return this;
	}

	/**
	 * Complete the recursive table with "union" (distinct): rows already produced are not
	 * queried again, so the recursion stops on cyclic data.
	 * @param recursion the recursive query, joining the table itself
	 * @return this
	 * @since 1.5.79
	 */
	public WithClause union(Query recursion) {
		return addBuilding(recursion, unionDistinct);
	}

	/**
	 * Complete the recursive table with "union all".
	 * The recursion never stops on cyclic data.
	 * @param recursion the recursive query, joining the table itself
	 * @return this
	 * @since 1.5.79
	 */
	public WithClause unionAll(Query recursion) {
		return addBuilding(recursion, unionAll);
	}

	private WithClause addBuilding(Query recursion, ExprPart union) {
		if (buildingTabl == null || buildingVal == null)
			throw new IllegalStateException("WithClause: withTable() and withVal() must be called before union.");
		if (this.withs == null)
			this.withs = new ArrayList<AbsPart[]>();
		this.withs.add(new AbsPart[] {buildingTabl, buildingVal, recursion, union});
		buildingTabl = null;
		buildingVal = null;
		return this;
	}

	/**
	 * Attach this clause to the query.
	 * @param q
	 * @return q
	 * @since 1.5.79
	 */
	public Query select(Query q) {
		q.with(this);
		return q;
	}

	/**
	 * Attach this clause to the delete statement.
	 * @param d
	 * @return d
	 * @since 1.5.79
	 */
	public Delete delete(Delete d) {
		d.with(this);
		return d;
	}

	/**
	 * Attach this clause to the update statement.
	 * @param u
	 * @return u
	 * @since 1.5.79
	 */
	public Update update(Update u) {
		u.with(this);
		return u;
	}
}
