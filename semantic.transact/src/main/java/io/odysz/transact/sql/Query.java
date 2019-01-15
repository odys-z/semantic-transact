package io.odysz.transact.sql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.Sql;
import io.odysz.transact.sql.parts.antlr.ConditVisitor;
import io.odysz.transact.sql.parts.antlr.SelectElemVisitor;
import io.odysz.transact.sql.parts.condition.Condit;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.sql.parts.select.GroupbyList;
import io.odysz.transact.sql.parts.select.JoinTabl;
import io.odysz.transact.sql.parts.select.JoinTabl.join;
import io.odysz.transact.x.TransException;
import io.odysz.transact.sql.parts.select.OrderyList;
import io.odysz.transact.sql.parts.select.SelectElem;
import io.odysz.transact.sql.parts.select.SelectList;

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
 * @author ody
 *
 */
public class Query extends Statement<Query> {
	/**String Array Index definition. Not using java field for compliance with JS (without GWT).
	 * @author ody
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
		
		public static final int predicateOper = 0;
		public static final int predicateL = 1;
		public static final int predicateR = 2;
		public static final int predicateSize = 3;
	}

	private List<SelectElem> selectList;
	private List<JoinTabl> joins;
	private ArrayList<String[]> orderList;
	private ArrayList<String> groupList;
	private int pg;
	private int pgSize;

	/**
	private SelectQry q;
	private boolean allColumn;
	Query(Transc transc, String tabl, String... alias) {
		super(transc, tabl, alias == null || alias.length == 0 ? null : alias[0]);
		q = new SelectQry();

		Table tbl = transc.getTable(tabl);
		if (alias != null && alias[0] != null)
			tbl = new RejoinTable(tbl, alias[0]);

		q.addFromTable(tbl);
		allColumn = true;
	}
	*/

	Query(Transcxt transc, String tabl, String... alias) {
		super(transc, tabl, alias == null || alias.length == 0 ? null : alias[0]);
	}

	/**
	 * @param col example: f.funcId, count(*), ifnull(f.roleId, '0')
	 * @param alias
	 * @return current query object
	 */
	public Query col(String col, String... alias) {
		// parser...
		SelectElem colElem = SelectElemVisitor.parse(col) ;

		if (alias != null && alias.length > 0 && alias[0] != null)
			colElem.as(alias[0]);
		if (selectList == null)
			selectList = new ArrayList<SelectElem>();

		selectList.add(colElem);
		return this;
	}

	public Query page(int page, int pgSize) {
		// paging
		this.pg = page;
		this.pgSize = pgSize;

		return this;
	}

	public Query cols(String... colAliases) {
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
	
	public Query orderby(String col, String... desc) {
		if (orderList == null)
			orderList = new ArrayList<String[]>();
		orderList.add(new String[] {col, desc == null || desc.length <= 0 ? null : desc[0]});
		return this;
	}

	@Override
	public String sql(ISemantext sctx) {
		Predicate<? super JoinTabl> hasJoin = e -> joins != null && joins.size() > 0;

		Stream<String> s = Stream.concat(
				// select ... from ... join ...
				Stream.concat(
						// select ...
						// Stream.concat(Stream.of(new ExprPart("select")), selectList.stream()),
						Stream.concat(Stream.of(new ExprPart("select")), Stream.of(new SelectList(selectList))),
						// from ... join ...
						Stream.concat(
								Stream.of(new JoinTabl(join.main, mainTabl, mainAlias)),
								// join can be null
								Optional.ofNullable(joins).orElse(Collections.emptyList()).stream().filter(hasJoin))
				), Stream.concat(
						// where ... group by ... order by ...
						Stream.of(new ExprPart("where"), where).filter(w -> where != null),
						Stream.concat(
								// group by
								Stream.of(new GroupbyList(groupList)).filter(o -> groupList != null),
								// order by
								Stream.of(new OrderyList(orderList)).filter(o -> orderList != null)))
			).map(m -> m.sql(sctx));
		
		if (pg >= 0 && pgSize > 0) {
			if (sctx != null)
				try {
					s = sctx.pagingStream(s, pg, pgSize);
				} catch (TransException e1) {
					e1.printStackTrace();
				}
		}

		return s.collect(Collectors.joining(" "));
	}
	
	/**<p>Use this method to do post operation, a. k. a. for {@link Select} get selected results -
	 * commit select statement, for {@link Insert} to get inserted new Ids.</p>
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
	 * <p><b>Node:</b>This method shouldn't been used the same time with {@link #commit(ArrayList)}
	 * because the inserting values will be handled / smirred in both methods.</p>
	 * <p>If you can make sure the ISemantext instance provided to Transcxt is clean of data
	 * invention, you can safely use both of these methods. But it's not guaranteed in the
	 * future version.</p>
	 * Also it's not recommended for the performance reason. The sql string is already generated
	 * by {@link #commit(ArrayList)}, don't generate it and travels AST again in this method, 
	 * use it directly.
	 * @return the result set
	 * @throws TransException
	 * @throws SQLException
	 */
	public Object rs() throws TransException, SQLException {
		if (postOp != null) {
			ArrayList<String> sqls = new ArrayList<String>(); 
			commit(sqls);
			return postOp.op(sqls);
		}
		return null;
	}
}
