package io.odysz.semantics.sql.parts.antlr;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import gen.antlr.sql.exprs.SearchExprs;
import gen.antlr.sql.exprs.SearchExprs.Search_conditionContext;
import gen.antlr.sql.exprs.SearchExprs.Search_condition_andContext;
import gen.antlr.sql.exprs.SearchExprs.Search_condition_notContext;
import gen.antlr.sql.exprs.SearchExprsBaseVisitor;
import gen.antlr.sql.exprs.TSqlLexer;
import io.odysz.semantics.sql.parts.Logic.type;
import io.odysz.semantics.sql.parts.condition.Condit;
import io.odysz.semantics.sql.parts.condition.Predicate;

/**Sample: <a href='https://stackoverflow.com/questions/23092081/antlr4-visitor-pattern-on-simple-arithmetic-example'>at stackoverflow</a>
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
    | expression assignment_operator expression
    ;</pre>
 * @author ody
 *
 */
@SuppressWarnings("deprecation")
public class ExprsVisitor extends SearchExprsBaseVisitor<Condit> {
	
//	@Override
//	public Condit visitSearch_condition(Search_conditionContext ctx) {
//		/* For antlr4 visitor tutorial:
//		 * see http://jakubdziworski.github.io/java/2016/04/01/antlr_visitor_vs_listener.html
//		 * For JDK 8 Stream tutorial:
//		 * see https://www.baeldung.com/java-8-streams-introduction
//		 * and https://www.baeldung.com/java-8-streams
//		 * 
//		String className = ctx.className().getText();
//        MethodVisitor methodVisitor = new MethodVisitor();
//        List<Method> methods = ctx.method()
//                .stream()
//                .map(method -> method.accept(methodVisitor))
//                .collect(toList());
//        return new Class(className, methods);
//        */
//		List<Condit> condts = ctx.search_condition_and()
//				.stream().map(condAnd -> condAnd.accept(this))
//				.collect(toList());
//		return new Condit(type.or, condts);
//	}
//
//	@Override
//	public Condit visitSearch_condition_and(Search_condition_andContext ctx) {
//		// return super.visitSearch_condition_and(ctx);
//		List<Condit> condts = ctx.search_condition_not()
//				.stream().map(condNot -> condNot.accept(this))
//				.collect(toList());
//		return new Condit(type.and, condts);
//	}
//
//	@Override
//	public Condit visitSearch_condition_not(Search_condition_notContext ctx) {
//		// Predicate predicate = ctx.predicate().accept(new PredicatVisitor());
//		PredicatVisitor vist = new PredicatVisitor();
//		Predicate predicate = vist.visit(ctx.predicate());
//		predicate.not(ctx.NOT());
//		return (Condit) predicate;
//	}


}
