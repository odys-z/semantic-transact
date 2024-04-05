package io.odysz.transact.sql.parts.antlr;

import java.util.List;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import gen.antlr.sql.exprs.SearchExprs;
import gen.antlr.sql.exprs.SearchExprs.Search_conditionContext;
import gen.antlr.sql.exprs.SearchExprs.Search_condition_andContext;
import gen.antlr.sql.exprs.SearchExprs.Search_condition_notContext;
import io.odysz.transact.sql.parts.Logic.type;
import io.odysz.transact.sql.parts.condition.Condit;
import io.odysz.transact.sql.parts.condition.Predicate;
import gen.antlr.sql.exprs.SearchExprsBaseVisitor;
import gen.antlr.sql.exprs.TSqlLexer;

/**For simple exprssion visitor sample, see <a href='https://stackoverflow.com/questions/23092081/antlr4-visitor-pattern-on-simple-arithmetic-example'>Question at StackoverFlow</a>
 * <pre>
parser grammar SearchExprs;

search_condition
    : search_condition_and (OR search_condition_and)*
    ;

search_condition_and
    : search_condition_not (AND search_condition_not)*
    ;

search_condition_not
    : NOT? predicate
    ;</pre>
 * For predicate definition, see {@link PredicatVisitor}.<br>
 * For expression definition, see {@link ExprsVisitor}.
 * @author ody
 *
 */
@SuppressWarnings("deprecation")
public class ConditVisitor extends SearchExprsBaseVisitor<Condit> {
	static ConditVisitor visitor = new ConditVisitor();                
	static PredicatVisitor predVist = new PredicatVisitor();

	public static Condit parse(String strExpr) {
		ANTLRInputStream inputStream = new ANTLRInputStream(strExpr);
	        TSqlLexer markupLexer = new TSqlLexer(inputStream);
	        CommonTokenStream commonTokenStream = new CommonTokenStream(markupLexer);
	        SearchExprs exprParser = new SearchExprs(commonTokenStream);
	 
	        Search_conditionContext ctx = exprParser.search_condition();                
	        return visitor.visit(ctx);  
	}
	
	@Override
	public Condit visitSearch_condition(Search_conditionContext ctx) {
		/* For antlr4 visitor tutorial:
		 * see http://jakubdziworski.github.io/java/2016/04/01/antlr_visitor_vs_listener.html
		 * For JDK 8 Stream tutorial:
		 * see https://www.baeldung.com/java-8-streams-introduction
		 * and https://www.baeldung.com/java-8-streams
		 * 
		String className = ctx.className().getText();
        MethodVisitor methodVisitor = new MethodVisitor();
        List<Method> methods = ctx.method()
                .stream()
                .map(method -> method.accept(methodVisitor))
                .collect(toList());
        return new Class(className, methods);
        */
		List<Condit> condts = ctx.search_condition_and()
				.stream().map(condAnd -> condAnd.accept(this))
				.collect(Collectors.toList());
		return new Condit(type.or, condts);
	}

	@Override
	public Condit visitSearch_condition_and(Search_condition_andContext ctx) {
		// return super.visitSearch_condition_and(ctx);
		List<Condit> condts = ctx.search_condition_not()
				.stream().map(condNot -> condNot.accept(this))
				.collect(Collectors.toList());
		return new Condit(type.and, condts);
	}

	@Override
	public Condit visitSearch_condition_not(Search_condition_notContext ctx) {
		// Predicate predicate = ctx.predicate().accept(new PredicatVisitor());
		Predicate predicate = predVist.visit(ctx.predicate());
		// predicate.not(ctx.NOT());
		return new Condit(predicate);
	}


}
