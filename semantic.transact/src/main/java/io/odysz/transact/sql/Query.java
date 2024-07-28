package io.odysz.transact.sql;

import static io.odysz.common.LangExt.eq;
import static io.odysz.transact.sql.parts.condition.ExprPart.constr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.common.LangExt;
import io.odysz.common.Utils;
import io.odysz.common.dbtype;
import io.odysz.semantics.ISemantext;
import io.odysz.semantics.SemanticObject;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.sql.parts.Alias;
import io.odysz.transact.sql.parts.JoinTabl;
import io.odysz.transact.sql.parts.JoinTabl.join;
import io.odysz.transact.sql.parts.Logic.op;
import io.odysz.transact.sql.parts.Sql;
import io.odysz.transact.sql.parts.antlr.ConditVisitor;
import io.odysz.transact.sql.parts.antlr.SelectElemVisitor;
import io.odysz.transact.sql.parts.condition.Condit;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.sql.parts.select.GroupbyList;
import io.odysz.transact.sql.parts.select.Havings;
import io.odysz.transact.sql.parts.select.OrderyList;
import io.odysz.transact.sql.parts.select.SelectElem;
import io.odysz.transact.sql.parts.select.SelectElem.ElemType;
import io.odysz.transact.sql.parts.select.SelectList;
import io.odysz.transact.sql.parts.select.SqlUnion;
import io.odysz.transact.sql.parts.select.WithClause;
import io.odysz.transact.x.TransException;

/**
 * <pre>
https://github.com/antlr/grammars-v4/blob/master/tsql/TSqlParser.g4

select_statement
    : with_expression? query_expression order_by_clause? for_clause? option_clause? ';'?
	;

with_expression
    : WITH (XMLNAMESPACES ',')? common_table_expression (',' common_table_expression)*
    | WITH BLOCKING_HIERARCHY ('(' full_column_name_list ')')? AS '(' select_statement ')'
    ;

query_expression
    : (query_specification | '(' query_expression ')') sql_union*
    ;

sql_union
    : (UNION ALL? | EXCEPT | INTERSECT) (query_specification | ('(' query_expression ')'))
    ;

// https://msdn.microsoft.com/en-us/library/ms176104.aspx
query_specification
    : SELECT (ALL | DISTINCT)? top_clause?
      select_list

      // TODO
      // https://msdn.microsoft.com/en-us/library/ms188029.aspx
      // (INTO table_name)?
       
      (FROM table_sources)?
      (WHERE where=search_condition)?

      // https://msdn.microsoft.com/en-us/library/ms177673.aspx
      (GROUP BY (ALL)? group_by_item (',' group_by_item)*)?

      // TODO 'having' is useful, see
      // see http://www.mysqltutorial.org/mysql-having.aspx
      // and https://docs.microsoft.com/en-us/sql/t-sql/queries/select-having-transact-sql?view=sql-server-2017
      // (HAVING having=search_condition)?
    ;

// https://msdn.microsoft.com/en-us/library/ms176104.aspx
select_list
    : select_list_elem (',' select_list_elem)*
	;
select_list_elem
    : asterisk
    | column_elem
    // TODO to be understood
    // | udt_elem
    
    // Modified
    // | expression_elem
    | expression as_column_alias?
	;

as_column_alias
    : AS? column_alias
	;
	
column_elem
    // : (table_name '.')? (column_name=id | '$' IDENTITY | '$' ROWGUID) as_column_alias?
    // changed:
    : (table_name '.')? (column_name=id) as_column_alias?
    ;
	</pre>
 * @author odys-z@github.io
 *
 */
public class Query extends Statement<Query> {
	/**Handling from t0 join t1 on ...
	 * @author odys-z@github.com
	 */
	public class JoinList extends AbsPart {
		private List<JoinTabl> lstJoins;

		public JoinList(List<JoinTabl> joins) {
			this.lstJoins = joins;
		}

		@Override
		public String sql(ISemantext sctx) throws TransException {
			return lstJoins == null ? "" :
				lstJoins.stream().map(m -> {
					try { return m.sql(sctx);
					} catch (TransException e) {
						e.printStackTrace();
						return "";
					}
				}).collect(Collectors.joining(" "));
		}
	}

	/**
	 * String Array Index definition. Data using array instead of java field for compliance with JS (without user type).
	 * 
	 * @author odys-z@github.com
	 */
	public static class Ix {
		/**String[0] = join.j | join.l | join.r */
		public static final int joinType = 0;
		/**String[1] = join-with-tabl */
		public static final int joinTabl = 1;
		/**String[2] = alias */
		public static final int joinAlias = 2;
		/**String[3] = on-condition-string */
		public static final int joinOnCond = 3;
		public static final int joinSize = 4;
		
		public static final int exprExpr = 0;
		public static final int exprAlais = 1;
		public static final int exprTabl = 2;
		public static final int exprSize = 3;

		public static final int nvn = 0;
		public static final int nvv = 1;
		
		public static final int predicateOper = 0;
		public static final int predicateL = 1;
		public static final int predicateR = 2;
		public static final int predicateSize = 3;
		
		public static final int orderExpr = 0;
		public static final int orderAsc = 1;
	}

	/**
	 * @since 1.4.40
	 * 
	 * For refence, see <a href='https://www.sqlite.org/lang_select.html'>SELECT</a>
	 * &amp; <a href='https://www.sqlite.org/syntax/table-or-subquery.html'>table-or-subquery</a>
	 */
	protected Query subquery;

	private List<SelectElem> selectList;
	private List<JoinTabl> joins;
	private ArrayList<String[]> orderList;
	private ArrayList<String> groupList;
	protected long pg;
	public long page() { return pg; }
	protected long pgSize;
	public long size() { return pgSize; }

	protected Condit havings;

	/**limit definition for ms2k, mysql */
	private Object[] limit;
	/**limit definition for sqlite
	private String[] limitSqlit; */

	/**Is this object a query_expression or (true) a query_sepcification?
	 * grammar reference: <pre>sql_union
    : (UNION ALL? | EXCEPT | INTERSECT) (query_specification | ('(' query_expression ')'))
    ;</pre>
	 * */
	protected boolean isQueryExpr = false;

	/**Array of unioned query [[0] union | except | intersect, [1] Query], ...<br>
	 * grammar reference: <pre>sql_union
    : (UNION ALL? | EXCEPT | INTERSECT) (query_specification | ('(' query_expression ')'))
    ;</pre> */
	private ArrayList<SqlUnion> union_except_intersect;

	Query(Transcxt transc, String tabl, String... alias) {
		super(transc, tabl, alias == null || alias.length == 0 ? null : alias[0]);
	}

	public Query(Transcxt transc, Query sub, String[] alias) {
		super(transc, null, alias == null || alias.length == 0 ? null : alias[0]);
		this.subquery = sub;
	}

	/**
	 * with-expressions
	 * 
	 * for temporary storage when composing query. 
	 */
	WithClause withs;

	boolean distinct;

	/**
	 * @param col example: f.funcId, count(*), ifnull(f.roleId, '0')
	 * @param alias
	 * @return current query object
	 * @throws TransException 
	 * @since 1.4.40, {@code col} can be null or empty, and will be used as "null" or "''".
	 */
	public Query col(Object col, String... alias) throws TransException {
		if (selectList == null)
			selectList = new ArrayList<SelectElem>();

		if (col instanceof String) {
			if (col == null || eq("null", (String)col))
				selectList.add(new SelectElem(ElemType.expr, "null"));
			else if (isblank(col))
				selectList.add(new SelectElem(constr("")));
			else {
				SelectElem colElem = SelectElemVisitor.parse((String) col) ;
				if (colElem == null)
					throw new TransException("column '%s' can't be parsed.", col);

				if (alias != null && alias.length > 0 && alias[0] != null)
					colElem.as(alias[0]);

				selectList.add(colElem);
			}
		}
		else {
			try {selectList.add(new SelectElem((ExprPart) col));}
			catch (Exception e) {
				Utils.warn("Col %s is not understandable", col == null ? "null" : col.toString());
			}
		}
		return this;
	}
	
	/**
	 * E.g. select [tbl].[col] as [alias]
	 * @param tbl usually a table alias in like c in "select my_table c"
	 * @param col the DB field name
	 * @param alias expression's alias
	 * @return this
	 * @throws TransException
	 */
	public Query col(String tbl, String col, String alias) throws TransException {
		return col(tbl + "." + col, alias);
	}
	
	public Query col(ExprPart expr, String... alias) {
		if (expr != null) {
			if (selectList == null)
				selectList = new ArrayList<SelectElem>();
			SelectElem ele = new SelectElem(expr);
			if (alias != null && alias.length > 0 && alias[0] != null)
				ele.as(alias[0]);
			selectList.add(ele);
		}
		return this;
	}

	public Query page(long page, long pgSize) {
		// paging
		this.pg = page;
		this.pgSize = pgSize;

		return this;
	}

	public Query page(PageInf page) {
		if (page != null)
			return page(page.page, page.size);
		else return page(0, -1);
	}

	/**
	 * @param col_ases 'col as alias' or 'col_name'
	 * @return this
	 * @throws TransException
	 * 
	 * @since 1.4.40, this method will have {@link SelectElem} generate null and '' for null or empty columns.
	 * <pre> Insert i = st.insert("a_users")
	 *  .cols("userName", "orgId", "pswd", "userId")
	 *  .select(st.select(null).cols("'Ody'", null, "", "'odyz'"))
	 *  .where(op.notexists, null,
	 *  	st.select("a_users")
	 *  	.whereEq("userId", "odyz"));
	 *  
	 * i.commit(mysqlCxt, sqls);
	 * assertEquals("insert into a_users (userName, orgId, pswd, userId) select 'Ody', null, '', 'odyz'  where not exists ( select * from a_users  where userId = 'odyz' )",
	 *		sqls.get(0));</pre>
	 */
	public Query cols(Object ... col_ases) throws TransException {
		if (col_ases != null)
			for (Object col_as : col_ases) {
				if (col_as == null)
					col("null");

				else if (col_as instanceof String) {
					String[] cass = ((String)col_as).split(" ([Aa][Ss] )?");
					if (cass != null && cass.length > 1)
						col(cass[0], cass[1]);
					else if (cass != null)
						col(cass[0]);
				}
				else col(col_as);
			}
		return this;
	}

	/**
	 * @since 1.4.41
	 * @param tblAlias
	 * @param col_ases array for sql, e.g. to sql SELECT tblAlias.col_ases[0], tblAlias.col_ases[1], ...
	 * @return this
	 * @throws TransException
	 */
	public Query cols_byAlias(String tblAlias, Object[] col_ases) throws TransException {
		if (col_ases != null)
			for (Object col_as : col_ases) {
				if (col_as == null) continue;
				if (col_as instanceof String) {
					String[] cass = ((String)col_as).split(" ([Aa][Ss] )?");
					if (cass != null && cass.length > 1)
						col(String.format("%s.%s", tblAlias, cass[0]), cass[1]);
					else if (cass != null)
						col(String.format("%s.%s", tblAlias, cass[0]));
				}
				else // must be ExprPart
					col(col_as);
			}
		return this;
	}

	/**
	 * Convert cols like:<br>
	 * {@code SELECT tblAlias.col_ases[0] as col_ases[1], tblAlias.col_ases[2] as col_ases[3], ...}
	 * 
	 * @param tblAlias
	 * @param col_ases array for sql, e.g. to sql
	 * @return this
	 * @throws TransException
	 * @since 1.4.40
	public Query col_ases(String tblAlias, Object... col_ases) throws TransException {
		if (col_ases != null)
			for (int ax = 0; ax < col_ases.length; ax++) {
				Object expr  = col_ases[ax];
				if (expr == null) continue;
				// String alias = ax < col_ases.length ? (String)expr : null;

				if (expr instanceof String)
					col(String.format("%s.%s", tblAlias, (String)expr), (String)expr);
				else if (expr instanceof ExprPart)
					col(new SelectElem((ExprPart) expr).tableAlias(tblAlias));
				else  // something wrong
					col(String.format("%s.%s", tblAlias, expr.toString()), expr.toString());
			}
		return this;
	}
	 */
	
	/**
	 * <pre>select
	 * .col_ases(a, entCols())
	 * .replacol(uri, extfile(a + "." + uri));</pre>
	 * @param uri
	 * @param extfile
	 * @return this
	 * @throws TransException 
	 * @since 2.0.0
	public Query col_replace(String uri, ExprPart with) throws TransException {
		if (selectList == null)
			throw new TransException("cols are null, nothing to replace.");
		
		for (int ix = 0; ix < selectList.size(); ix++)
			if (selectList.get(ix).col != null)
		return this;
	}
	 */

	public Query l(String withTabl, String alias, String onCondit) throws TransException {
		return j(join.l, withTabl, alias, onCondit);
	}

	public Query l(Query select, String alias, String onCondit) {
		return j(join.l, select, alias, onCondit);
	}

	public Query r(String withTabl, String alias, String onCondit) throws TransException {
		return j(join.r, withTabl, alias, onCondit);
	}

	/**Inner or outer join
	 * @param jt
	 * @param withTabl
	 * @param alias
	 * @param onCondit
	 * @return current select statement
	 * @throws TransException 
	 */
	public Query j(join jt, String withTabl, String alias, Condit onCondit) throws TransException {
		if (jt == null || withTabl == null || onCondit == null)
			throw new TransException("Join condition is not correct.");
		JoinTabl joining = new JoinTabl(jt, withTabl, alias, onCondit);
		j(joining);
		return this;
	}

	/**Inner or outer join
	 * @param jt
	 * @param withTabl
	 * @param onCondit
	 * @return current select statement
	 * @throws TransException 
	 */
	public Query j(join jt, String withTabl, String alias, String onCondit) throws TransException {
		Condit condit = ConditVisitor.parse(onCondit);
		j(jt, withTabl, alias, condit);
		return this;
	}

	/**Inner Join
	 * @param withTabl
	 * @param onCondit e.g "t.f1='a' t.f2='b'", 2 AND conditions
	 * @return current select statement
	 * @throws TransException 
	 */
	public Query j(String withTabl, Condit onCondit) throws TransException {
		return j(join.j, withTabl, null, onCondit);
	}

	/**Inner join
	 * @param withTabl
	 * @param alias
	 * @param onCondit
	 * @return current select statement
	 */
	public Query j(String withTabl, String alias, Condit onCondit) {
		JoinTabl joining = new JoinTabl(join.j, withTabl, alias, onCondit);
		j(joining);
		return this;
	}
	
	public Query j(Query select, String alias, String onCondit) {
		return j(join.j, select, alias, onCondit);
	}

	public Query j(join jt, Query select, String alias, String onCondit) {
		JoinTabl joining = new JoinTabl(jt, select, alias, onCondit);
		j(joining);
		return this;
	}

	public void j(JoinTabl joining) {
		if (joins == null)
			joins = new ArrayList<JoinTabl>();
		joins.add(joining);
	}

	/**Inner join
	 * @param withTabl
	 * @param onCondit
	 * @return current select statement
	 * @throws TransException 
	 */
	public Query j(String withTabl, String onCondit) throws TransException {
		Condit condit = ConditVisitor.parse(onCondit);
		j(withTabl, condit);
		return this;
	}

	/**Inner join
	 * @param withTabl
	 * @param alias
	 * @param on
	 * @param args
	 * @return current select statement
	 */
	public Query j(String withTabl, String alias, String on, Object...args) {
		return j(withTabl, alias, Sql.condt(on, args));
	}

	/**
	 * AST for "join withTbl withAlias on mainTbl.colMaintbl = withalias.colWith[colMaintbl]".
	 * 
	 * <p>Example</p>
	 * <pre>sctx.select(usrMeta.tbl, "u")
	 *    .je("u", usrMeta.roleTbl, "r", usrMeta.role)
	 *    .je("u", usrMeta.orgTbl, "o", usrMeta.org);
	 * //   
	 * sctx.select(userMeta.tbl, "u")
	 *    .je("u", orgMeta.tbl, "o", m.org, orgMeta.pk);</pre>
	 *    
	 * @since 1.4.25, additional columns can be append as AND predict in join clause. 
	 * @param mainAlias e.g. u
	 * @param withTbl e.g. r
	 * @param withAlias
	 * @param onCols, in pairs, e.g. if a, b, c, d, where have condition u.a = r.b and u.c = r.d
	 * @return this
	public Query je(String mainAlias, String withTbl, String withAlias, String... onCols) {
		Condit ands = Sql.condt(op.eq,
				String.format("%s.%s", mainAlias, onCols[0]),
				String.format("%s.%s", withAlias, onCols.length > 1 ? onCols[1] : onCols[0]));

		for (int i = 2; i < onCols.length; i+=2) {
			ands.and(Sql.condt(op.eq,
				String.format("%s.%s", mainAlias, onCols[i]),
				String.format("%s.%s", withAlias, onCols.length > i+1 ? onCols[i+1] : onCols[i])));
		}
		return j(withTbl, withAlias, ands);
	}
	 */

	/**
	 * AST for "join withTbl withAlias on mainTbl.colMaintbl = withalias.colWith[colMaintbl]".
	 * 
	 * <p>Example</p>
	 * <pre>sctx.select(usrMeta.tbl, "u")
	 *    .je("u", usrMeta.roleTbl, "r", usrMeta.role)
	 *    .je("u", usrMeta.orgTbl, "o", usrMeta.org);
	 * //   
	 * sctx.select(userMeta.tbl, "u")
	 *    .je("u", orgMeta.tbl, "o", m.org, orgMeta.pk);</pre>
	 *    
	 * @since 1.4.25 Additional columns can be append as AND predict in join clause. 

	 * @since 1.4.40 The right operand can be a function expression, e.g.
	 * 
	 * @since 1.4.40 This method is depreacated, and be replaced by {@link #je_(String, String, Object...)}.
	 * (No need for parameter {@code mainAlias} 
	 * 
	 * <pre>
	 * je("ent", chgm.tbl, "ch", chgm.uids, Funcall.concat(trsb.synode + chgm.UIDsep + chgm.pk), chgm.uids, chgm.synoder, trsb.synode)
	 * </pre>
	 * 
	 * Example of column name handling:
	 * <pre>
	 * 1. example: override table alias
	 * 	st.select("a_users", "u")
	 *      .je("u", "a_org", "o", "o.orgName", constr("ChaoYang People"), "userName", constr("James Bond"))
	 *      .commit(sqls);
	 *  // it's o.orgName
	 *  assertEquals("select * from a_users u join a_org o on o.orgName = 'ChaoYang People' AND u.userName = 'James Bond'",
	 *  sqls.get(1))
	 *  
	 * 2. example: regular align table alias
	 * 	st.select("a_users", "u")
	 *      .je("u", "a_org", "o", "orgName", constr("ChaoYang People"), "userName", constr("James Bond"))
	 *      .commit(sqls);
	 *  // it's u.orgName
	 *  assertEquals("select * from a_users u join a_org o on u.orgName = 'ChaoYang People' AND u.userName = 'James Bond'",
	 *  sqls.get(2))
	 * </pre>
	 * @param mainAlias
	 * @param withTbl
	 * @param withAlias
	 * @param onCols equals condition pairs, if column name come with table alias, will override it, see the first example.
	 * @return this
	 */
	public Query je(String mainAlias, String withTbl, String withAlias, Object... onCols) {
		Object rop = onCols.length > 1 ? onCols[1] : onCols[0];

		Condit ands = null; 
		if (rop instanceof ExprPart)
			ands = Sql.condt(op.eq,
				String.format("%s.%s", mainAlias, onCols[0]), (ExprPart)rop);
		else
			ands = Sql.condt(op.eq,
				String.format("%s.%s", mainAlias, onCols[0]),
				String.format("%s.%s", withAlias, rop));

		for (int i = 2; i < onCols.length; i+=2) {
			rop = onCols.length > i+1 ? onCols[i+1] : onCols[i];
			if (rop instanceof ExprPart)
				ands = ands.and(Sql.condt(op.eq,
					String.format("%s.%s", mainAlias, onCols[i]), (ExprPart)rop));
			else
				ands = ands.and(Sql.condt(op.eq,
					String.format("%s.%s", mainAlias, onCols[i]),
					String.format("%s.%s", withAlias, rop)));
		}
		return j(withTbl, withAlias, ands);
	}
	
	/**
	 * Add join clause on equals condition.
	 * @param withTbl
	 * @param withAlias
	 * @param onCols on conditions
	 * @return this
	 * @throws TransException
	 */
	public Query je_(String withTbl, String withAlias, Object ... onCols ) throws TransException {
		Condit ands = toAndsCondit(mainAlias, withAlias, onCols);
		return j(withTbl, withAlias, ands);
	}
	
	static Condit toAndsCondit(Alias mainAlias, String withAlias, Object[] onCols) throws TransException {
		Condit and = null;
		Condit ands = null;

		for (int i = 0; i < onCols.length; i+=2) {
			Object rop = onCols.length > i+1 ? onCols[i+1] : onCols[i];
			String lop = onCols[i] instanceof ExprPart
					? ((ExprPart) onCols[i]).sql(null)
					: isblank(mainAlias) ? onCols[i].toString() : String.format("%s.%s", mainAlias.sql(null), onCols[i]);

			if (rop instanceof ExprPart)
				and = Sql.condt(op.eq, lop, (ExprPart)rop);
			else
				and = Sql.condt(op.eq, lop,
					String.format("%s.%s", withAlias, rop));

			if (ands != null)
				ands = ands.and(and);
			else ands = and;
		}
		return ands;
	}

	public Query l_(String withTbl, String alias, Object ... onCols) throws TransException {
		return j(join.l, withTbl, alias, toAndsCondit(mainAlias, alias, onCols));
	}
	
	public Query groupby(String expr) {
		if (groupList == null)
			groupList = new ArrayList<String>();
		groupList.add(expr);
		return this;
	}

	public Query groupby(String... groups) {
		if (groups != null && groups.length > 0)
			for (String g : groups)
				groupby(g);
		return this;
	}
	
	public Query orderby(String col, String... desc) {
		if (orderList == null)
			orderList = new ArrayList<String[]>();
		orderList.add(new String[] {col, desc == null || desc.length <= 0 ? null : desc[0]});
		return this;
	}

	/**set query orders
	 * @param orders [ [col, desc], ...]
	 * @return this
	 */
	public Query orderby(ArrayList<String[]> orders) {
		if (orders != null && orders.size() > 0)
			for (String[] order : orders)
				orderby(order[0], order.length > 1 ? order[1] : "asc");
		return this;
	}
	
	public Query having(String scond, Object... args) {
		return having(Sql.condt(scond, args));
	}

	public Query having(Condit condt, Condit... ands) {
		if (havings == null)
			havings = condt;
		else
			havings = havings.and(condt);

		if (ands != null)
			for (Condit and : ands)
				havings = havings.and(and);
		return this;
	}

	/**
	 * <p>Update Limited Rows.</p>
	 * <ul><li>ms sql 2k: select [TOP (expression) [PERCENT]  [ WITH TIES ] ] ...
	 * 		see <a href='https://docs.microsoft.com/en-us/sql/t-sql/queries/top-transact-sql?view=sql-server-2017#syntax'>
	 * 		Transact-SQL Syntax</a><br>
	 * 		<b>Note: where percent, with ties are not supported by this method. Use {@link #limit(String, String)}.</b></li>
	 * 		<li>mysql: update ... limit N, see <a href='https://dev.mysql.com/doc/refman/8.0/en/select.html'>
	 * 			Mysql Manual: 13.2.12 SELECT Syntax</a><br>
	 * 			<b>Note: only 1 pair of offset rowcount is supported</b></li>
	 *		<li>sqlite: limit expr OFFSET expr2. see <a href='https://www.sqlite.org/lang_select.html'>
	 *		SQL As Understood By SQLite - SELECT</a><br>
	 * 		<b>Note: Don't use this if expr2 is not null, use {@link #limit(String, String)}.</b></li>
	 * 		<li>Oracle: There should be no such syntax:
	 * 		<a href='https://docs.oracle.com/cd/B19306_01/server.102/b14200/statements_10002.htm#i2065706'>
	 * 		Oracle Database SQL Reference - SELECT</a></li>
	 * </ul>
	 * @param lmtExpr
	 * @param cnt only support mysql count
	 * @return this
	 */
	public Query limit(String lmtExpr, int cnt) {
		this.limit = new Object[] {lmtExpr, cnt};
		return this;
	}

	/**
	 * For sqlite only, set limit expr OFFSET expr2 clause.<br>
	 * see <a href='https://www.sqlite.org/lang_select.html'>SQL As Understood By SQLite - SELECT</a>
	 * @see #limit(String, int)
	 * @param cnt
	 * @param offset
	 * @return this
	 */
	public Query limit(String cnt, String offset) {
		this.limit = new String[] {cnt, offset};
		return this;
	}

	public Query limit(long i) {
		if (i >= 0)
			return limit(null, String.valueOf(i));
		else return this;
	}

	/**
	 * Union query sepecification or expresion(s)<br>
	 * grammar reference: <pre>sql_union
    : (UNION ALL? | EXCEPT | INTERSECT) (query_specification | ('(' query_expression ')'))
    ;</pre>
	 * @param with
	 * @param isExpression the {@code with} query as is an expression (wraped with "()")
	 * @return this
	 */
	public Query union(Query with, boolean ... isExpression) {
		return sqlUnion(SqlUnion.UNION, with, isExpression);
	}

	/**Union query sepecification or expresion(s)<br>
	 * grammar reference: <pre>sql_union
    : (UNION ALL? | EXCEPT | INTERSECT) (query_specification | ('(' query_expression ')'))
    ;</pre>
	 * @param with
	 * @param isExpression
	 * @return this
	 */
	public Query except(Query with, boolean ... isExpression) {
		return sqlUnion(SqlUnion.EXCEPT, with, isExpression);
	}

	private Query sqlUnion(int type, Query with, boolean[] asExpression) {
		SqlUnion u = new SqlUnion(type, with,
			asExpression != null && asExpression.length > 0 ?
			asExpression[0] : false);
		if (union_except_intersect == null)
			union_except_intersect = new ArrayList<SqlUnion>();
		union_except_intersect.add(u); 
		return this;
	}

	/**Union query sepecification or expresion(s)<br>
	 * grammar reference: <pre>sql_union
    : (UNION ALL? | EXCEPT | INTERSECT) (query_specification | ('(' query_expression ')'))
    ;</pre>
	 * @param with
	 * @param isExpression
	 * @return this
	 */
	public Query intersect(Query with, boolean ... isExpression) {
		return sqlUnion(SqlUnion.INTERSECT, with, isExpression);
	}
	
	/**Take this instance as query_expression. 
	 * Default is false.
	 * grammar reference: <pre>sql_union
    : (UNION ALL? | EXCEPT | INTERSECT) (query_specification | ('(' query_expression ')'))
    ;</pre>
	 * @param isExpression
	 * @return this
	 */
	public Query asQueryExpr(boolean ... isExpression) {
		this.isQueryExpr = isExpression != null && isExpression.length > 0 ?
				isExpression[0] : false;
		return this;
	}

	@Override
	public String sql(ISemantext sctx) {
		return isQueryExpr
			? sqlstream(sctx).collect(Collectors.joining(" ", "(", ")"))
			: sqlstream(sctx).collect(Collectors.joining(" "));
	}
	
	protected Stream<String> sqlstream(ISemantext sctx) {
		dbtype dbtp = sctx == null ? null : sctx.dbtype();
		Stream<String> s = Stream.of(
				withs,
				// select ...
				new ExprPart("select"),
				this.distinct ? new ExprPart("distinct") : null,
				// top(expr) with ties
				dbtp == dbtype.ms2k && limit != null 
					? limit.length <= 1 || isblank(limit[0]) 
						? new ExprPart("top(" + limit[1] + ") ")
						: new ExprPart("top(" + limit[0] + ") " + (isblank(limit[1]) ? "" : limit[1]))
					: null,
				new SelectList(selectList),
				// from ... join ...
				isblank(mainTabl) || mainTabl.isblank() ? null : new JoinTabl(join.main, mainTabl, mainAlias),
				
				// needing a sub-query AST node
				subquery != null ? new ExprPart("from") : null,
//				subquery != null ? new ExprPart("(") : null,
				subquery,
//				subquery != null ? new ExprPart(")") : null,
				subquery != null ? mainAlias : null,

				// join can be null
				joins != null && joins.size() > 0 ? new JoinList(joins) : null,
				// where ... group by ... order by ...
				where == null ? null : new ExprPart("where"),
				where,
				// group by
				groupList == null ? null : new GroupbyList(groupList),
				// having
				havings == null ? null : new Havings(havings),
				// order by
				orderList == null ? null : new OrderyList(orderList),
				// limit
				(dbtp == dbtype.mysql || dbtp == dbtype.sqlite) && limit != null ?
					// Query#limit() requires 2 arguments, e.g. limit(null, 5)
					limit.length > 1 && isblank(limit[0]) ?
					new ExprPart("limit " + limit[1]) :
					new ExprPart("limit " + limit[0] + (limit.length > 1 ? ", " + limit[1] : "")) : null
			).filter(e -> e != null).map(m -> {
				try {
					return m == null ? "" : m.sql(sctx);
				} catch (TransException e2) {
					e2.printStackTrace();
					return "";
				}
			});
		
		if (union_except_intersect != null)
			 s = Stream.concat(s, union_except_intersect
				.stream()
				.filter(e -> e != null)
				.map(m -> {
					try {
						return m.sql(sctx);
					} catch (TransException e1) {
						e1.printStackTrace();
						return null;
					}
				}));
		
		return s;
	}
	
	/**<p>Use this method to do post operation, a. k. a. for {@link Query} getting selected results -
	 * committing select statement; (deprecated?) or for {@link Insert} getting inserted new ids.</p>
	 * <p>This method must called after the post operation (lambda expression) been initialized.</p>
	 * <h3>Why rs() must been used after setting lambda expression?</h3>
	 * <p>As Query generated sql, it should be used to get result set - execute the SQL
	 * select statement.</p>
	 * <p>But semantic-transact is designed not to depend on any database accessing layer,
	 * so Query shouldn't access database, although in many case it's very convenient to
	 * get result set directly.</p>
	 * <p>This requirements is handled in JDK1.8 lambda expression style:<br>
	 * User override method {@link Transcxt#select(String, String...)}, which add a lambda
	 * expression as the post operation for handling execution of the generated SQL statement,
	 * rs() will call the lambda and return the result set returned by this operation.</p>
	 * <h3>Where is the sample code?</h3>
	 * <p>To see how to extend {@link Transcxt}, see DATranscxt in project semantic-DA.<br>
	 * To see how to use this method, see io.odysz.semantic.DASemantextTest in project sematic-DA.</p>
	 * <p><b>Node:</b>This method shouldn't been used the same time with {@link #commit(ArrayList, io.odysz.semantics.IUser...)}
	 * because the inserting values will be handled / smirred in both methods.</p>
	 * <p>If you can make sure the ISemantext instance provided to Transcxt is clean of data
	 * invention, you can safely use both of these methods. But it's not guaranteed in the
	 * future version.</p>
	 * Also it's not recommended for the performance reason. The sql string is already generated
	 * by {@link #commit(ArrayList, io.odysz.semantics.IUser...)}, don't generate it and travels AST again
	 * in this method, use it directly.
	 * @param ctx 
	 * @return the result set
	 * (For a select result, {@link Query}'s doneOp will set the AnResultset.)
	 * @throws TransException
	 * @throws SQLException
	 */
	public SemanticObject rs(ISemantext ctx) throws TransException, SQLException {
		if (postOp != null) {
			ArrayList<String> sqls = new ArrayList<String>(); 
			commit(ctx, sqls);
			return postOp.onCommitOk(ctx, sqls);
		}
		return null;
	}

	public Query with(WithClause withClause) {
		this.withs = withClause;
		return this;
	}

	/** 
	 * Whether use distinct or not, to generate "select distinct ... "
	 * @param dist
	 * @return this
	 * @since 1.4.36
	 */
	public Query distinct(boolean... dist) {
		this.distinct = LangExt.is(dist, true);
		return this;
	}
}
