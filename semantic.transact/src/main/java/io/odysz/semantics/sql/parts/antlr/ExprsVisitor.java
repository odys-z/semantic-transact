package io.odysz.semantics.sql.parts.antlr;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import gen.antlr.sql.exprs.SearchExprs;
import gen.antlr.sql.exprs.SearchExprs.Search_conditionContext;
import gen.antlr.sql.exprs.SearchExprsBaseVisitor;
import gen.antlr.sql.exprs.TSqlLexer;
import io.odysz.semantics.sql.parts.Logic;

public class ExprsVisitor extends SearchExprsBaseVisitor<String> {
	public static ExprsVisitor parse(String strExpr) {
		ANTLRInputStream inputStream = new ANTLRInputStream(strExpr);
//		ANTLRInputStream inputStream = new ANTLRInputStream(
//	            "I would like to [b][i]emphasize[/i][/b] this and [u]underline [b]that[/b][/u] ." +
//	            "Let's not forget to quote: [quote author=\"John\"]You're wrong![/quote]");
	        TSqlLexer markupLexer = new TSqlLexer(inputStream);
	        CommonTokenStream commonTokenStream = new CommonTokenStream(markupLexer);
	        SearchExprs markupParser = new SearchExprs(commonTokenStream);
	 
	        Search_conditionContext ctx = markupParser.search_condition();                
	        ExprsVisitor visitor = new ExprsVisitor();                
	        visitor.visit(ctx);  
	        return visitor;
	}
	
	public Logic.op op() {
		return null;
		
	}

	public String rop() {
		// TODO Auto-generated method stub
		return null;
	}

	public String lop() {
		// TODO Auto-generated method stub
		return null;
	}

}
