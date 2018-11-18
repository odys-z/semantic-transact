package io.odysz.semantics.sql.parts.antlr;

import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import gen.antlr.sql.exprs.SearchExprsBaseVisitor;
import io.odysz.semantics.sql.parts.condition.Predicate;

/**
 * <pre>
predicate
    // maybe replace subquery with sql-id?
    : expression comparison_operator expression
    | expression NOT? IN '(' expression_list ')'
    | expression NOT? LIKE expression 
    | expression IS null_notnull
    | '(' search_condition ')'
    ;

// Expression.

expression_list
    : expression (',' expression)*
    ;

expression
    : primitive_expression
    | function_call
    | full_column_name
    | bracket_expression
    | unary_operator_expression
    | expression op=('*' | '/' | '%') expression
    | expression op=('+' | '-' | '&' | '^' | '|' | '||') expression
    | expression comparison_operator expression
    | expression assignment_operator expression
    ;</pre>
 * @author ody
 *
 */
public class PredicatVisitor extends SearchExprsBaseVisitor<Predicate> {

	@Override
	public Predicate visit(ParseTree tree) {
		return null;
	}


}
