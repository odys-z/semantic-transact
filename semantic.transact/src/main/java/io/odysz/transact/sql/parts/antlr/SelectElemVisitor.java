package io.odysz.transact.sql.parts.antlr;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;

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
import io.odysz.transact.sql.parts.Colname;
import io.odysz.transact.sql.parts.Logic;
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
    | expression op=('+' | '-' | '&amp;' | '^' | '|' | '||') expression
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
		// v4.7.1/2 -> 4.9.2
//		if (exp != null && exp.constant() != null) {
//			text = exp.constant().getText();
//			ele = new SelectElem(ElemType.constant, text);
//		}
		if (exp != null && exp.constant_expression() != null) {
			text = exp.constant_expression().getText();
			ele = new SelectElem(ElemType.constant, text);
		}
		else if (exp != null)  {
			text = exp.getText();
			// function
			Function_callContext f = exp.function_call();
			if (f != null)  {
				// aggregation funcion
				if (f.aggregate_windowed_function() != null) {
					text = f.aggregate_windowed_function().getChild(0).getText();
					// '(' ('*' | full_column_name) ')'
					if (text != null) {
						String coln = "*";
						if (f.aggregate_windowed_function().full_column_name() != null)
							coln = f.aggregate_windowed_function().full_column_name().getText();
						// 2019 v9.2
						// Funcall func = new Funcall(text, coln);
						Funcall func = new Funcall(text, new Colname[] {Colname.parseFullname(coln)});
						ele = new SelectElem(func);
					}
				}
				// funcion
				else {
					text = f.func_proc_name().getText();
					if (text != null) {
						Expression_listContext args = f.expression_list();
						Funcall func = new Funcall(text, funcArgs(args.expression()));
						ele = new SelectElem(func);
					}
				}
			}
			// expression IS null_notnull
			else if (ctx.IS() != null) {
				// simplified handling - all raw text as expression, bug here
				Logic.op op = ctx.null_notnull().NOT() != null ?
						Logic.op.isNotnull : Logic.op.isnull;
				ExprPart expr = new ExprPart(op, ctx.expression().getText(), "");
				ele = new SelectElem(expr);
			}
			// expression
			else if (text != null) {
				Logic.op logic = Logic.op(exp.op.getText());
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

	public static ExprPart[] funcArgs(List<?> list) {
		if (list != null) {
			ArrayList<ExprPart> lst = new ArrayList<ExprPart>();
			for (Object exp : list) {
				// 2019 v 0.9.2 parse func args, at least find out full column name, for adding "" to oracle
				ExprPart arg = ExprsVisitor.parse(((ParserRuleContext)exp).getText());
				lst.add(arg);
			}
			return lst.toArray(new ExprPart[lst.size()]);
		}
		return null;
	}

	public static ExprPart[] funcArgs(ParserRuleContext exp) {
		ExprPart arg = ExprsVisitor.parse(exp.getText());
		return new ExprPart[] {arg};
	}
	
}
