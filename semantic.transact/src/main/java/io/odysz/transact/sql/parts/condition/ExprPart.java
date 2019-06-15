package io.odysz.transact.sql.parts.condition;

import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.sql.parts.Logic.op;

/**Expression Nodet.
 * For the Antlr4 grammar, see <a href='https://github.com/antlr/grammars-v4/blob/master/tsql/TSqlParser.g4'>
 * Antlr4 grammars-v4/tsql/TSqlParser.g4</a>.<br>
 * For referencing grammar and how this is parsed, see {@link io.odysz.transact.sql.parts.antlr.ExprsVisitor}
 * and {@link io.odysz.transact.sql.parts.antlr.ExprsVisitor#visitExpression(ExpressionContextt) visit()};<br>
 * Expression Grammar: <pre>
// Expression.

// https://docs.microsoft.com/en-us/sql/t-sql/language-elements/expressions-transact-sql
// Operator precendence: https://docs.microsoft.com/en-us/sql/t-sql/language-elements/operator-precedence-transact-sql
expression
    : primitive_expression
    | function_call
    | expression COLLATE id
    | case_expression
    | full_column_name
    | bracket_expression
    | unary_operator_expression
    | expression op=('*' | '/' | '%') expression
    | expression op=('+' | '-' | '&' | '^' | '|' | '||') expression
    | expression comparison_operator expression
    | expression assignment_operator expression
    | over_clause
    ;

primitive_expression
    : DEFAULT | NULL | LOCAL_ID | constant
    ;

// https://docs.microsoft.com/en-us/sql/t-sql/language-elements/case-transact-sql
case_expression
    : CASE caseExpr=expression switch_section+ (ELSE elseExpr=expression)? END
    | CASE switch_search_condition_section+ (ELSE elseExpr=expression)? END
    ;

unary_operator_expression
    : '~' expression
    | op=('+' | '-') expression
    ;

bracket_expression
    : '(' expression ')' | '(' subquery ')'
    ;

constant_expression
    : NULL
    | constant
    // system functions: https://msdn.microsoft.com/en-us/library/ms187786.aspx
    | function_call
    | LOCAL_ID         // TODO: remove.
    | '(' constant_expression ')'
    ;

subquery
    : select_statement
    ;

// https://msdn.microsoft.com/en-us/library/ms175972.aspx
with_expression
    : WITH (XMLNAMESPACES ',')? common_table_expression (',' common_table_expression)*
    | WITH BLOCKING_HIERARCHY ('(' full_column_name_list ')')? AS '(' select_statement ')'
    ;

common_table_expression
    : expression_name=id ('(' column_name_list ')')? AS '(' select_statement ')'
    ;

update_elem
    : (full_column_name | LOCAL_ID) ('=' | assignment_operator) expression
    | udt_column_name=id '.' method_name=id '(' expression_list ')'
    //| full_column_name '.' WRITE (expression, )
    ;

// https://msdn.microsoft.com/en-us/library/ms173545.aspx
search_condition_list
    : search_condition (',' search_condition)*
    ;

search_condition
    : search_condition_and (OR search_condition_and)*
    ;

search_condition_and
    : search_condition_not (AND search_condition_not)*
    ;

search_condition_not
    : NOT? predicate
    ;

predicate
    : EXISTS '(' subquery ')'
    | expression comparison_operator expression
    | expression comparison_operator (ALL | SOME | ANY) '(' subquery ')'
    | expression NOT? BETWEEN expression AND expression
    | expression NOT? IN '(' (subquery | expression_list) ')'
    | expression NOT? LIKE expression (ESCAPE expression)?
    | expression IS null_notnull
    | '(' search_condition ')'
    ;

// Changed union rule to sql_union to avoid union construct with C++ target.  Issue reported by person who generates into C++.  This individual reports change causes generated code to work

query_expression
    : (query_specification | '(' query_expression ')') sql_union*
    ;
    
// https://msdn.microsoft.com/en-us/library/ms179899.aspx
constant
    : STRING // string, datetime or uniqueidentifier
    | BINARY
    | sign? DECIMAL
    | sign? (REAL | FLOAT)  // float or decimal
    | sign? dollar='$' (DECIMAL | FLOAT)       // money
    ;

sign
    : '+'
    | '-'
    ;

// https://msdn.microsoft.com/en-us/library/ms175874.aspx
id
    : simple_id
    | DOUBLE_QUOTE_ID
    | SQUARE_BRACKET_ID
    ;

simple_id
    : ID
    | ABSOLUTE
    | ACCENT_SENSITIVITY
    | ... </pre>
 * @author odys-z@github.com
 */
public class ExprPart extends AbsPart {
//	private static final op AbsPart = null;
	private boolean isNull;
	private op logic;
	private String lexp;
	private String rexp;

	/**Create an expression.
	 * @param op operator, not necessarily a logical one, can also a mathematical one.
	 * @param lexp
	 * @param rexp
	 */
	public ExprPart(op op, String lexp, String rexp) {
		this.logic =op;
		this.lexp = lexp;
		this.rexp = rexp;
		this.isNull = false;
	}

	public ExprPart(String id) {
		this.logic = null;
		lexp = id;
		this.isNull = false;
	}

	public ExprPart() {
		this.isNull = true;
	}

	/**<p>Mainly used for get string raw value.</p>
	 * FIXME performance problem base64 string, add a class for binary value?
	 */
	@Override
	public String toString() {
		// This function shouldn't been called frequently and only for test,
		// or inner referencing, like fullpath, ectc.
		// So this is not a performance problem?
		String v = sql(null);
		return v.replaceAll("^'|'$", "");
	}

	@Override
	public String sql(ISemantext ctx) {
		if (isNull)
			return "null";
		if (logic == null)
			return lexp == null ? "" : lexp;
		else {
//			Object lresulved = ctx == null ? lexp : ctx.resulvedVal(lexp);
//			Object rresulved = ctx == null ? rexp : ctx.resulvedVal(rexp);
//			return String.format("%s %s",
//				lexp == null ? "" : lresulved,
//				logic.sql(logic, rexp == null ? "" : (String) rresulved));
			return String.format("%s %s",
				lexp == null ? "" : lexp,
				logic.sql(ctx, logic, rexp == null ? "" : rexp));
		}
	}

	public static ExprPart constStr(String v) {
		if (v != null)
			return new ExprPart("'" + v + "'");
		else return new ExprPart();
	}

	public static ExprPart constVal(String v) {
		if (v != null)
			return new ExprPart(v);
		else return new ExprPart();
	}
}
