package io.odysz.transact.sql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.semantics.Semantext;
import io.odysz.transact.sql.parts.Sql;
import io.odysz.transact.sql.parts.antlr.ConditVisitor;
import io.odysz.transact.sql.parts.antlr.SelectElemVisitor;
import io.odysz.transact.sql.parts.condition.Condit;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.sql.parts.select.JoinTabl;
import io.odysz.transact.sql.parts.select.OrderyList;
import io.odysz.transact.sql.parts.select.SelectElem;
import io.odysz.transact.sql.parts.select.SelectList;
import io.odysz.transact.sql.parts.select.GroupbyList;
import io.odysz.transact.sql.parts.select.JoinTabl.join;

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
	private List<SelectElem> selectList;
	private List<JoinTabl> joins;
	private ArrayList<String[]> orderList;
	private ArrayList<String> groupList;

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

	/**Inner Join
	 * @param withTabl
	 * @param onCondit e.g "t.f1='a' t.f2='b'", 2 AND conditions
	 * @return current select statement
	 */
	public Query j(String withTabl, Condit onCondit) {
		JoinTabl joining = new JoinTabl(join.j, withTabl, onCondit);
		if (joins == null)
			joins = new ArrayList<JoinTabl>();
		joins.add(joining);
		return this;
	}

	/**Inner join
	 * @param withTabl
	 * @param alias
	 * @param onCondit
	 * @return current select statement
	 */
	public Query j(String withTabl, String alias, Condit onCondit) {
		JoinTabl joining = new JoinTabl(join.j, withTabl, alias, onCondit);
		if (joins == null)
			joins = new ArrayList<JoinTabl>();
		joins.add(joining);
		return this;
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
	public String sql(Semantext sctx) {
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

		return s.collect(Collectors.joining(" "));
	}
}
