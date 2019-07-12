package io.odysz.transact.sql.parts.antlr;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import gen.antlr.sql.exprs.SearchExprs;
import gen.antlr.sql.exprs.SearchExprs.ConstantContext;
import gen.antlr.sql.exprs.SearchExprs.ExpressionContext;
import gen.antlr.sql.exprs.SearchExprs.Full_column_nameContext;
import gen.antlr.sql.exprs.SearchExprs.Function_callContext;
import gen.antlr.sql.exprs.SearchExprs.Unary_operator_expressionContext;
import gen.antlr.sql.exprs.SearchExprsBaseVisitor;
import gen.antlr.sql.exprs.TSqlLexer;
import io.odysz.transact.sql.parts.Colname;
import io.odysz.transact.sql.parts.Logic;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.sql.parts.condition.Funcall;

/**Full or part of an expression.<br>
 * See <a href='https://github.com/antlr/grammars-v4/blob/master/tsql/TSqlParser.g4'>
 * ANTLR4 grammar for T-SQL</a> 
 * <pre>
expression_list
    : expression (',' expression)*
    ;

expression
    // : primitive_expression
    | constant
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


// primitive_expression : DEFAULT | NULL | LOCAL_ID | constant ;

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
 * For expression grammar, see {@link ExprPart}.
 * @author odys-z@github.com
 *
 */
@SuppressWarnings("deprecation")
public class ExprsVisitor extends SearchExprsBaseVisitor<ExprPart> {
	static ExprsVisitor visitor = new ExprsVisitor();

	public static ExprPart parse(String strExpr) {
		ANTLRInputStream inputStream = new ANTLRInputStream(strExpr);
	        TSqlLexer markupLexer = new TSqlLexer(inputStream);
	        CommonTokenStream commonTokenStream = new CommonTokenStream(markupLexer);
	        SearchExprs exprParser = new SearchExprs(commonTokenStream);
//	 
	        ExpressionContext ctx = exprParser.expression();
	        return visitor.visit(ctx);  
	}
	
	/**<pre>
expression
    : constant
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
		ConstantContext constant = ctx.constant();
		if (constant != null)
			return new ExprPart(constant.getText());
		
		Function_callContext fc = ctx.function_call();
		if (fc != null)
			return new Funcall(fc.func_proc_name().getText(),
					SelectElemVisitor.funcArgs(fc.expression_list().expression()));

		Full_column_nameContext fn = ctx.full_column_name();
		if (fn != null)
			// return new ExprPart(fn.getText());
			return Colname.parseFullname(fn.getText());

		Unary_operator_expressionContext uni_op = ctx.unary_operator_expression();
		if (uni_op != null)
			return new ExprPart(Logic.op(uni_op.getText()), ctx.expression().get(0).getText(), null);
		
		if (ctx.op != null) {
			String op = ctx.op.getText();
			if (op != null)
				return new ExprPart(Logic.op(op), ctx.expression().get(0).getText(),
						ctx.expression().size() > 1 ? ctx.expression().get(1).getText() : null);
		}

		return new ExprPart(ctx.getText());
	}
}
