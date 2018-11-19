package io.odysz.semantics.sql.parts.antlr;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.antlr.v4.runtime.CommonTokenStream;

import gen.antlr.sql.exprs.SearchExprs;
import gen.antlr.sql.exprs.SearchExprs.ExpressionContext;
import gen.antlr.sql.exprs.SearchExprs.Full_column_nameContext;
import gen.antlr.sql.exprs.SearchExprs.Primitive_expressionContext;
import gen.antlr.sql.exprs.SearchExprs.Search_conditionContext;
import gen.antlr.sql.exprs.SearchExprs.Search_condition_andContext;
import gen.antlr.sql.exprs.SearchExprs.Search_condition_notContext;
import gen.antlr.sql.exprs.SearchExprsBaseVisitor;
import gen.antlr.sql.exprs.TSqlLexer;
import io.odysz.semantics.sql.parts.Logic;
import io.odysz.semantics.sql.parts.Logic.type;
import io.odysz.semantics.sql.parts.condition.Condit;
import io.odysz.semantics.sql.parts.condition.ExprPart;
import io.odysz.semantics.sql.parts.condition.Predicate;

/**
 * <pre>
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
    // | expression assignment_operator expression
    ;

function_call
    : aggregate_windowed_function
    | func_proc_name '(' expression_list? ')'
    ;

// https://msdn.microsoft.com/en-us/library/ms173454.aspx
aggregate_windowed_function
    : (AVG | MAX | MIN | SUM | STDEV | STDEVP | VAR | VARP)
      '(' full_column_name ')'
    | (COUNT | COUNT_BIG)
      '(' ('*' | full_column_name) ')'
    ;

func_proc_name
    : procedure=id
    ;

/*  There are some RESERVED WORDS that can be column names * /
full_column_name
    : (table_name '.')? column_name=id
    ;

table_name
    : (database=id '.' (schema=id)? '.' | schema=id '.')? table=id
    | (database=id '.' (schema=id)? '.' | schema=id '.')? BLOCKING_HIERARCHY
    ;


primitive_expression
    : DEFAULT | NULL | LOCAL_ID | constant
    ;

unary_operator_expression
    : '~' expression
    | op=('+' | '-') expression
    ;

bracket_expression
    : '(' expression ')' 
    ;

constant_expression
    : NULL
    | constant
    // system functions: https://msdn.microsoft.com/en-us/library/ms187786.aspx
    | function_call
    // TODO: variables
    // | LOCAL_ID         // TODO: remove.
    | '(' constant_expression ')'
    ;

// https://msdn.microsoft.com/en-us/library/ms188074.aspx
// Spaces are allowed for comparison operators.
// TODO rlike, llike
comparison_operator
    : '=' | '>' | '<' | '<' '=' | '>' '=' | '<' '>' | '!' '=' | '!' '>' | '!' '<'
    ;

// assignment_operator
//     : '+=' | '-=' | '*=' | '/=' | '%=' | '&=' | '^=' | '|='
//     ;

</pre>
 * @author ody
 *
 */
public class ExprsVisitor extends SearchExprsBaseVisitor<ExprPart> {

	/**<pre>
expression
    : primitive_expression
    | function_call
    | full_column_name
    | bracket_expression
    | unary_operator_expression
    | expression op=('*' | '/' | '%') expression
    | expression op=('+' | '-' | '&' | '^' | '|' | '||') expression
    | expression comparison_operator expression
    // | expression assignment_operator expression
    ;</pre>
	 * @see gen.antlr.sql.exprs.SearchExprsBaseVisitor#visitExpression(gen.antlr.sql.exprs.SearchExprs.ExpressionContext)
	 */
	@Override
	public ExprPart visitExpression(ExpressionContext ctx) {
		// return super.visitExpression(ctx);
		if (ctx.op != null)
			return new ExprPart(Logic.op.eq, "A", "B");
		else {
			Primitive_expressionContext pe = ctx.primitive_expression();
			if (pe != null)
				return new ExprPart(pe.getText());

			Full_column_nameContext fn = ctx.full_column_name();
			if (fn != null)
				return new ExprPart(fn.getText());


			return null;
		}
	}
	
}
