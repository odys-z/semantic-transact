package io.odysz.semantics.sql.parts.antlr;

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
import gen.antlr.sql.select.SelectParts.Select_list_elemContext;
import gen.antlr.sql.select.SelectParts.Table_nameContext;
import io.odysz.semantics.sql.parts.select.SelectElem;
import io.odysz.semantics.sql.parts.select.SelectElem.ElemType;

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
</pre> 
 * @author ody
 *
 */
@SuppressWarnings("deprecation")
public class SelectElemVisitor extends SelectPartsBaseVisitor<SelectElem> {

	/**Parse column expression (select_list_elem without alias).
	 * @param colExpr
	 * @return
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
		// return super.visitSelect_list_elem(ctx);
		AsteriskContext asterisk = ctx.asterisk();
		if (asterisk != null) {
			// Table_nameContext t = asterisk.table_name();
			return new SelectElem(ElemType.asterisk, asterisk.getText());
		}

		SelectElem ele;
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
		
		return null;
	}

}
