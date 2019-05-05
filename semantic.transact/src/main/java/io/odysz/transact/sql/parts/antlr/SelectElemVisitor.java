package io.odysz.transact.sql.parts.antlr;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import gen.antlr.sql.select.SelectParts.Function_callContext;
import gen.antlr.sql.exprs.TSqlLexer;
import gen.antlr.sql.select.SelectParts;
import gen.antlr.sql.select.SelectPartsBaseVisitor;
import gen.antlr.sql.select.SelectParts.As_column_aliasContext;
import gen.antlr.sql.select.SelectParts.AsteriskContext;
import gen.antlr.sql.select.SelectParts.Column_elemContext;
import gen.antlr.sql.select.SelectParts.ExpressionContext;
import gen.antlr.sql.select.SelectParts.Expression_listContext;
import gen.antlr.sql.select.SelectParts.Select_list_elemContext;
import gen.antlr.sql.select.SelectParts.Table_nameContext;
import io.odysz.transact.sql.parts.Logic;
import io.odysz.transact.sql.parts.Logic.op;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.sql.parts.condition.Funcall;
import io.odysz.transact.sql.parts.select.SelectElem;
import io.odysz.transact.sql.parts.select.SelectElem.ElemType;

/**<pre>
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

asterisk
    : '*'
    | table_name '.' asterisk
    ;

// currently only function_call, constant, expression op expression
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
    ;

function_call
    : aggregate_windowed_function
    | func_proc_name '(' expression_list? ')'
    ;
</pre> 
 * @author odys-z@github.com
 *
 */
@SuppressWarnings("deprecation")
public class SelectElemVisitor extends SelectPartsBaseVisitor<SelectElem> {

	/**Parse column expression (select_list_elem without alias).
	 * @param colExpr
	 * @return AST node
	 */
	public static SelectElem parse(String colExpr) {
		ANTLRInputStream inputStream = new ANTLRInputStream(colExpr);
        TSqlLexer markupLexer = new TSqlLexer(inputStream);
        CommonTokenStream commonTokenStream = new CommonTokenStream(markupLexer);
        SelectParts elemParser = new SelectParts(commonTokenStream);
 
        Select_list_elemContext ctx = elemParser.select_list_elem();

        SelectElemVisitor selemVisitor = new SelectElemVisitor();
        return selemVisitor.visit(ctx);  
	}

	@Override
	public SelectElem visitSelect_list_elem(Select_list_elemContext ctx) {
		AsteriskContext asterisk = ctx.asterisk();
		// asterisk : '*' | table_name '.' asterisk;
		if (asterisk != null) {
			return new SelectElem(ElemType.asterisk, asterisk.getText());
		}

		SelectElem ele = null;
		// column_elem : (table_name '.')? (column_name=id) as_column_alias? ;
		Column_elemContext colElem = ctx.column_elem();
		if (colElem != null) {
			Table_nameContext tabl = colElem.table_name();
			if (tabl == null)
				ele = new SelectElem(ElemType.col, colElem.column_name.getText());
			else
				ele = new SelectElem(ElemType.tableCol, tabl.getText(), colElem.column_name.getText());
			As_column_aliasContext alias = colElem.as_column_alias();
			if (alias != null && alias.column_alias() != null)
				ele.as(alias.column_alias().getText());
			return ele;
		}

		/*
		String text = null;
		Function_callContext f = ctx.function_call();
		if (f != null) 
			text = f.getText();
		else {
			ExpressionContext exp = ctx.expression();
			if (exp != null) 
				text = exp.getText();
		}
		
		if (text != null) {
			ele = new SelectElem(ElemType.func, text);
			As_column_aliasContext alias = ctx.as_column_alias();
			if (alias != null && alias.column_alias() != null)
				ele.as(alias.column_alias().getText());
			return ele;
		}
		*/
		
		// expression : function_call | constant | expr op expr;
		String text = null;

		ExpressionContext exp = ctx.expression();
		// constant expression
		if (exp != null && exp.primitive_expression() != null
				&& exp.primitive_expression().constant() != null) {
			text = exp.primitive_expression().constant().getText();
			ele = new SelectElem(ElemType.constant, text);
		}
		else if (exp != null)  {
			text = exp.getText();
			// function
			Function_callContext f = exp.function_call();
			if (f != null)  {
				text = f.getText();
				Expression_listContext args = f.expression_list();
				if (text != null) {
					Funcall func = new Funcall(f.func_proc_name().getText(), funcArgs(args.expression()));
					ele = new SelectElem(func);
				}
			}
			// expression
			else if (text != null) {
				op logic = Logic.op(exp.op.getText());
				ExprPart expr = new ExprPart(logic, exp.getChild(0).getText(),
					exp.getChildCount() > 2 ? exp.getChild(2).getText() : "");
				ele = new SelectElem(expr);
			}
		}
		
		if (ele != null) {
			As_column_aliasContext alias = ctx.as_column_alias();
			if (alias != null && alias.column_alias() != null)
				ele.as(alias.column_alias().getText());
			return ele;
		}
		
		return null;
	}

	private List<ExprPart> funcArgs(List<ExpressionContext> list) {
		if (list != null) {
			ArrayList<ExprPart> lst = new ArrayList<ExprPart>();
			for (ExpressionContext exp : list) {
				String op = exp.op.getText();
				if (op != null)
					// recursive visit?
					lst.add(new ExprPart(exp.getText()));
			}
			return lst;
		}
		return null;
	}
	
}
