// Generated from ./SearchExprs.g4 by ANTLR 4.13.1
package gen.antlr.sql.exprs;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link SearchExprs}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface SearchExprsVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link SearchExprs#search_condition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSearch_condition(SearchExprs.Search_conditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchExprs#search_condition_and}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSearch_condition_and(SearchExprs.Search_condition_andContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchExprs#search_condition_not}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSearch_condition_not(SearchExprs.Search_condition_notContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchExprs#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicate(SearchExprs.PredicateContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchExprs#expression_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression_list(SearchExprs.Expression_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchExprs#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(SearchExprs.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchExprs#function_call}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction_call(SearchExprs.Function_callContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchExprs#aggregate_windowed_function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregate_windowed_function(SearchExprs.Aggregate_windowed_functionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchExprs#func_proc_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_proc_name(SearchExprs.Func_proc_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchExprs#full_column_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFull_column_name(SearchExprs.Full_column_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchExprs#table_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_name(SearchExprs.Table_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchExprs#unary_operator_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnary_operator_expression(SearchExprs.Unary_operator_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchExprs#bracket_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBracket_expression(SearchExprs.Bracket_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchExprs#constant_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstant_expression(SearchExprs.Constant_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchExprs#comparison_operator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparison_operator(SearchExprs.Comparison_operatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchExprs#assignment_operator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignment_operator(SearchExprs.Assignment_operatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchExprs#null_notnull}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNull_notnull(SearchExprs.Null_notnullContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchExprs#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstant(SearchExprs.ConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchExprs#sign}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSign(SearchExprs.SignContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchExprs#id}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitId(SearchExprs.IdContext ctx);
}