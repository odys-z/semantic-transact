package io.odysz.transact.sql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.common.dbtype;
import io.odysz.semantics.ISemantext;
import io.odysz.semantics.SemanticObject;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.sql.parts.Sql;
import io.odysz.transact.sql.parts.antlr.ConditVisitor;
import io.odysz.transact.sql.parts.antlr.SelectElemVisitor;
import io.odysz.transact.sql.parts.condition.Condit;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.sql.parts.select.GroupbyList;
import io.odysz.transact.sql.parts.select.Havings;
import io.odysz.transact.sql.parts.select.JoinTabl;
import io.odysz.transact.sql.parts.select.JoinTabl.join;
import io.odysz.transact.sql.parts.select.OrderyList;
import io.odysz.transact.sql.parts.select.SelectElem;
import io.odysz.transact.sql.parts.select.SelectList;
import io.odysz.transact.sql.parts.select.SqlUnion;
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
 * @author odys-z@github.com
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

	/**String Array Index definition. Not using java field for compliance with JS (without user type).
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

	private List<SelectElem> selectList;
	private List<JoinTabl> joins;
	private ArrayList<String[]> orderList;
	private ArrayList<String> groupList;
	private int pg;
	public int page() { return pg; }
	int pgSize;
	public int size() { return pgSize; }

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
	private boolean isQueryExpr = false;

	/**Array of unioned query [[0] union | except | intersect, [1] Query], ...<br>
	 * grammar reference: <pre>sql_union
    : (UNION ALL? | EXCEPT | INTERSECT) (query_specification | ('(' query_expression ')'))
    ;</pre> */
	private ArrayList<SqlUnion> union_except_intersect;
	
	Query(Transcxt transc, String tabl, String... alias) {
		super(transc, tabl, alias == null || alias.length == 0 ? null : alias[0]);
	}

	/**
	 * @param col example: f.funcId, count(*), ifnull(f.roleId, '0')
	 * @param alias
	 * @return current query object
	 * @throws TransException 
	 */
	public Query col(String col, String... alias) throws TransException {
		if (col == null)
			throw new TransException("col is null");
		SelectElem colElem = SelectElemVisitor.parse(col) ;
		if (colElem == null)
			throw new TransException("column %s can't been parsed.", col);

		if (alias != null && alias.length > 0 && alias[0] != null)
			colElem.as(alias[0]);
		if (selectList == null)
			selectList = new ArrayList<SelectElem>();

		selectList.add(colElem);
		return this;
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

	public Query page(int page, int pgSize) {
		// paging
		this.pg = page;
		this.pgSize = pgSize;

		return this;
	}

	public Query cols(String... colAliases) throws TransException {
		if (colAliases != null)
			for (String colAlias : colAliases) {
				String[] cass = colAlias.split(" ([Aa][Ss] )?");
				if (cass != null && cass.length > 1)
					col(cass[0], cass[1]);
				else if (cass != null)
					col(cass[0]);
			}
		return this;
	}
	
	public Query l(String withTabl, String alias, String onCondit) {
		return j(join.l, withTabl, alias, onCondit);
	}

	public Query l(Query select, String alias, String onCondit) {
		return j(join.l, select, alias, onCondit);
	}

	public Query r(String withTabl, String alias, String onCondit) {
		return j(join.r, withTabl, alias, onCondit);
	}

	/**Inner or outer join
	 * @param jt
	 * @param withTabl
	 * @param alias
	 * @param onCondit
	 * @return current select statement
	 */
	public Query j(join jt, String withTabl, String alias, Condit onCondit) {
		JoinTabl joining = new JoinTabl(jt, withTabl, alias, onCondit);
		j(joining);
		return this;
	}

	/**Inner or outer join
	 * @param jt
	 * @param withTabl
	 * @param onCondit
	 * @return current select statement
	 */
	public Query j(join jt, String withTabl, String alias, String onCondit) {
		Condit condit = ConditVisitor.parse(onCondit);
		j(jt, withTabl, alias, condit);
		return this;
	}

	/**Inner Join
	 * @param withTabl
	 * @param onCondit e.g "t.f1='a' t.f2='b'", 2 AND conditions
	 * @return current select statement
	 */
	public Query j(String withTabl, Condit onCondit) {
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
	 */
	public Query j(String withTabl, String onCondit) {
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
	
	public Query groupby(String expr) {
		if (groupList == null)
			groupList = new ArrayList<String>();
		groupList.add(expr);
		return this;
	}

	public Query groupby(String[] groups) {
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
	 * @return
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

	/**<p>Update Limited Rows.</p>
	 * <ul><li>ms sql 2k: select [TOP (expression) [PERCENT]  [ WITH TIES ] ] ...
	 * 		see <a href='https://docs.microsoft.com/en-us/sql/t-sql/queries/top-transact-sql?view=sql-server-2017#syntax'>
	 * 		Transact-SQL Syntax</a><br>
	 * 		<b>Note: percent, with ties not supported by this method, user {@link #limit(String, String)}.</b></li>
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

	/**For sqlite only, set limit expr OFFSET expr2 clause.<br>
	 * see <a href='https://www.sqlite.org/lang_select.html'>SQL As Understood By SQLite - SELECT</a>
	 * @see #limit(String, int)
	 * @param lmtExpr
	 * @param xpr2
	 * @return
	 */
	public Query limit(String lmtExpr, String xpr2) {
		this.limit = new String[] {lmtExpr, xpr2};
		return this;
	}

	/**Union query sepecification or expresion(s)<br>
	 * grammar reference: <pre>sql_union
    : (UNION ALL? | EXCEPT | INTERSECT) (query_specification | ('(' query_expression ')'))
    ;</pre>
	 * @param with
	 * @param isExpression
	 * @return
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
	 * @return
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
	 * @return
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
	 * @return
	 */
	public Query asQueryExpr(boolean ... isExpression) {
		this.isQueryExpr = isExpression != null && isExpression.length > 0 ?
				isExpression[0] : false;
		return this;
	}

	@Override
	public String sql(ISemantext sctx) {
		dbtype dbtp = sctx == null ? null : sctx.dbtype();
		Stream<String> s = Stream.of(
					// select ...
					new ExprPart("select"),
					// top(expr) with ties
					dbtp == dbtype.ms2k && limit != null ?
						new ExprPart("top(" + limit[0] + ") " + (limit.length > 1 ? limit[1] : "")) : null,
					new SelectList(selectList),
					// from ... join ...
					new JoinTabl(join.main, mainTabl, mainAlias),
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
		
		return isQueryExpr
				? s.collect(Collectors.joining(" ", "(", ")"))
				: s.collect(Collectors.joining(" "));
	}
	
	/**<p>Use this method to do post operation, a. k. a. for {@link Query} getting selected results -
	 * committing select statement; or for {@link Insert} getting inserted new ids.</p>
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
	 * <p><b>Node:</b>This method shouldn't been used the same time with {@link #commit(ArrayList, int...)}
	 * because the inserting values will be handled / smirred in both methods.</p>
	 * <p>If you can make sure the ISemantext instance provided to Transcxt is clean of data
	 * invention, you can safely use both of these methods. But it's not guaranteed in the
	 * future version.</p>
	 * Also it's not recommended for the performance reason. The sql string is already generated
	 * by {@link #commit(ArrayList, int...)}, don't generate it and travels AST again in this method, 
	 * use it directly.
	 * @param ctx 
	 * @return the result set
	 * @throws TransException
	 * @throws SQLException
	 */
	public SemanticObject rs(ISemantext ctx) throws TransException, SQLException {
		if (postOp != null) {
			ArrayList<String> sqls = new ArrayList<String>(); 
			commit(ctx, sqls);
			// return postOp.op(ctx.connId(), sqls);
			return postOp.onCommitOk(ctx, sqls);
		}
		return null;
	}
}
