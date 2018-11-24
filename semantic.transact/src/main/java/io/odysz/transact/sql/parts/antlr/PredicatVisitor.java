package io.odysz.transact.sql.parts.antlr;

import java.util.List;
import java.util.stream.Collectors;

import gen.antlr.sql.exprs.SearchExprs.PredicateContext;
import io.odysz.transact.sql.parts.Logic;
import io.odysz.transact.sql.parts.condition.Condit;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.sql.parts.condition.Predicate;
import gen.antlr.sql.exprs.SearchExprsBaseVisitor;

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
	public Predicate visitPredicate(PredicateContext ctx) {
		super.visitPredicate(ctx);
		
		ExprsVisitor expvisit = new ExprsVisitor();

		List<ExprPart> exprs = ctx.expression()
				.stream()
				.map(expr -> expr.accept(expvisit))
				.collect(Collectors.toList());

		boolean not = ctx.NOT() != null;

		if (ctx.comparison_operator() != null) {
			Logic.op op = Logic.op(ctx.comparison_operator().getText());
			return new Predicate(op, exprs.get(0), exprs.get(1));
		}
		// IN
		else if (ctx.IN() != null) {
			List<ExprPart> exprlst = ctx.expression_list().expression()
				.stream()
				.map(expr -> expr.accept(expvisit))
				.collect(Collectors.toList());
			return new Predicate(not ? Logic.op.notin : Logic.op.in,
					exprs.get(0), exprlst, not);
		}
		// LIKE
		else if (ctx.LIKE() != null) {
			Logic.op op = Logic.op(ctx.LIKE().getText(), not);
			List<ExprPart> expr2 = ctx.expression()
				.stream().skip(1)
				.map(expr -> expr.accept(expvisit))
				.collect(Collectors.toList());
			return new Predicate(op, exprs.get(0), expr2);
		}
		// is (not) null
		else if (ctx.IS() != null) {
			Logic.op op = ctx.null_notnull().NOT() == null ? Logic.op.isnull : Logic.op.isNotnull;
			String rnull = ctx.null_notnull().getText();
			return new Predicate(op, exprs.get(0), rnull);
		}
		// (search_conditons)
		else {
			ConditVisitor vist = new ConditVisitor();
			Condit condit = vist.visit(ctx.search_condition());
			return new Predicate(condit);
		}
	}



}
