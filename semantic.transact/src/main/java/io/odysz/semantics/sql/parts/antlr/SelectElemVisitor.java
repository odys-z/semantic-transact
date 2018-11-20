package io.odysz.semantics.sql.parts.antlr;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import gen.antlr.sql.exprs.SearchExprs;
import gen.antlr.sql.exprs.SearchExprsBaseVisitor;
import gen.antlr.sql.exprs.TSqlLexer;
import gen.antlr.sql.exprs.SearchExprs.ExpressionContext;
import io.odysz.semantics.sql.parts.select.SelectElem;

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
</pre> 
 * @author ody
 *
 */
@SuppressWarnings("deprecation")
public class SelectElemVisitor extends SearchExprsBaseVisitor<SelectElem> {
	static SelectElemVisitor selemVisitor = new SelectElemVisitor();

	/**Parse column expression (select_list_elem without alias).
	 * @param colExpr
	 * @return
	 */
	public static SelectElem parse(String colExpr) {
		ANTLRInputStream inputStream = new ANTLRInputStream(colExpr);
        TSqlLexer markupLexer = new TSqlLexer(inputStream);
        CommonTokenStream commonTokenStream = new CommonTokenStream(markupLexer);
        SearchExprs exprParser = new SearchExprs(commonTokenStream);
 
        ExpressionContext ctx = exprParser.expression();
        return selemVisitor.visit(ctx);  
	}

}
