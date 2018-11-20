package io.odysz.semantics.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.odysz.semantics.sql.parts.antlr.SelectElemVisitor;
import io.odysz.semantics.sql.parts.condition.Condit;
import io.odysz.semantics.sql.parts.select.SelectElem;

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
public class Query extends Statement {
	private List<SelectElem> selectList;

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

	/**
	 * @param col example: f.funcId, count(*), ifnull(f.roleId, '0')
	 * @param alias
	 * @return
	 * /
	public Query col(String col, String... alias) {
		allColumn = false;
		if (alias == null || alias.length <= 0 || alias[0] == null)
			// q.addCustomColumns(col);
			addCol(q, col);
		else {
			// TODO test expression as col
			DbColumn dcol = transc.getColumn(mt, col);
			if (dcol != null)
				// TODO
				// "stamp", "st" -> "t2.stamp AS st" (malias == null)
				// "stamp", "st" -> "lg.stamp AS st" (malias != null)
				q.addAliasedColumn(dcol, alias[0]);
			else
				// "l.stamp", "st" -> "l.stamp AS st"
				q.addAliasedColumn(new CustomSql(col), alias[0]);
		}
		return this;
	}

	/**Try figure out is the col is an expression (a.col1, f(a.col), ...), then add the col to qry.
	 * @param qry
	 * @param col
	 * /
	private void addCol(SelectQry qry, String col) {
		// TODO
		// parse col
		String[] colss = col.split(".");
		String colAlias = colss[0];
		String colExpr = colss[1];

		if (colAlias == null) {
			DbColumn dbcol = transc.getColumn(mt, colExpr);
			if (dbcol != null) {
				qry.addColumns(dbcol);
				return;
			}
		}

		qry.addCustomColumns(col);
	}*/

	Query(Transcxt transc, String tabl, String... alias) {
		super(transc, tabl, alias == null || alias.length == 0 ? null : alias[0]);

//		Table tbl = transc.getTable(tabl);
//		if (alias != null && alias[0] != null)
//			tbl = new RejoinTable(tbl, alias[0]);

	}

	/**
	 * @param col example: f.funcId, count(*), ifnull(f.roleId, '0')
	 * @param alias
	 * @return
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

	/**Try figure out is the col is an expression (a.col1, f(a.col), ...), then add the col to qry.
	 * @param qry
	 * @param col
	private void addCol(String col) {
		// parser...
		// SelectElemVisitor.visit(col) 
		SelectElem colElem = null;
		selectList.add(colElem);
	}
	 */

	/**Inner Join
	 * @param withTabl
	 * @param onCondtion e.g "t.f1='a' t.f2='b'", 2 AND conditions
	 * @return
	 */
	public Query j(String withTabl, Condit onCondtion) {
		return this;
	}

	@Override
	public Statement commit(ArrayList<String> sqls) {
		sqls.add(sql());
		return this;
	}

	public String sql() {
		// FIXME append to buffer?
		return String.format("select %s from %s",
				selectList
					.stream()
					.map(ele -> ele.sql())
					.collect(Collectors.joining(", ")),
				"joinings..."
				);
	}
}
