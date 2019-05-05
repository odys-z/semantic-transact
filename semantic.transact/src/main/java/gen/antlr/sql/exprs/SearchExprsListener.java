// Generated from SearchExprs.g4 by ANTLR 4.7.1
package gen.antlr.sql.exprs;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SearchExprs}.
 */
public interface SearchExprsListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SearchExprs#search_condition}.
	 * @param ctx the parse tree
	 */
	void enterSearch_condition(SearchExprs.Search_conditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchExprs#search_condition}.
	 * @param ctx the parse tree
	 */
	void exitSearch_condition(SearchExprs.Search_conditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchExprs#search_condition_and}.
	 * @param ctx the parse tree
	 */
	void enterSearch_condition_and(SearchExprs.Search_condition_andContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchExprs#search_condition_and}.
	 * @param ctx the parse tree
	 */
	void exitSearch_condition_and(SearchExprs.Search_condition_andContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchExprs#search_condition_not}.
	 * @param ctx the parse tree
	 */
	void enterSearch_condition_not(SearchExprs.Search_condition_notContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchExprs#search_condition_not}.
	 * @param ctx the parse tree
	 */
	void exitSearch_condition_not(SearchExprs.Search_condition_notContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchExprs#predicate}.
	 * @param ctx the parse tree
	 */
	void enterPredicate(SearchExprs.PredicateContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchExprs#predicate}.
	 * @param ctx the parse tree
	 */
	void exitPredicate(SearchExprs.PredicateContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchExprs#expression_list}.
	 * @param ctx the parse tree
	 */
	void enterExpression_list(SearchExprs.Expression_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchExprs#expression_list}.
	 * @param ctx the parse tree
	 */
	void exitExpression_list(SearchExprs.Expression_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchExprs#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(SearchExprs.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchExprs#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(SearchExprs.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchExprs#function_call}.
	 * @param ctx the parse tree
	 */
	void enterFunction_call(SearchExprs.Function_callContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchExprs#function_call}.
	 * @param ctx the parse tree
	 */
	void exitFunction_call(SearchExprs.Function_callContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchExprs#aggregate_windowed_function}.
	 * @param ctx the parse tree
	 */
	void enterAggregate_windowed_function(SearchExprs.Aggregate_windowed_functionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchExprs#aggregate_windowed_function}.
	 * @param ctx the parse tree
	 */
	void exitAggregate_windowed_function(SearchExprs.Aggregate_windowed_functionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchExprs#func_proc_name}.
	 * @param ctx the parse tree
	 */
	void enterFunc_proc_name(SearchExprs.Func_proc_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchExprs#func_proc_name}.
	 * @param ctx the parse tree
	 */
	void exitFunc_proc_name(SearchExprs.Func_proc_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchExprs#full_column_name}.
	 * @param ctx the parse tree
	 */
	void enterFull_column_name(SearchExprs.Full_column_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchExprs#full_column_name}.
	 * @param ctx the parse tree
	 */
	void exitFull_column_name(SearchExprs.Full_column_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchExprs#table_name}.
	 * @param ctx the parse tree
	 */
	void enterTable_name(SearchExprs.Table_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchExprs#table_name}.
	 * @param ctx the parse tree
	 */
	void exitTable_name(SearchExprs.Table_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchExprs#unary_operator_expression}.
	 * @param ctx the parse tree
	 */
	void enterUnary_operator_expression(SearchExprs.Unary_operator_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchExprs#unary_operator_expression}.
	 * @param ctx the parse tree
	 */
	void exitUnary_operator_expression(SearchExprs.Unary_operator_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchExprs#bracket_expression}.
	 * @param ctx the parse tree
	 */
	void enterBracket_expression(SearchExprs.Bracket_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchExprs#bracket_expression}.
	 * @param ctx the parse tree
	 */
	void exitBracket_expression(SearchExprs.Bracket_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchExprs#constant_expression}.
	 * @param ctx the parse tree
	 */
	void enterConstant_expression(SearchExprs.Constant_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchExprs#constant_expression}.
	 * @param ctx the parse tree
	 */
	void exitConstant_expression(SearchExprs.Constant_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchExprs#comparison_operator}.
	 * @param ctx the parse tree
	 */
	void enterComparison_operator(SearchExprs.Comparison_operatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchExprs#comparison_operator}.
	 * @param ctx the parse tree
	 */
	void exitComparison_operator(SearchExprs.Comparison_operatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchExprs#assignment_operator}.
	 * @param ctx the parse tree
	 */
	void enterAssignment_operator(SearchExprs.Assignment_operatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchExprs#assignment_operator}.
	 * @param ctx the parse tree
	 */
	void exitAssignment_operator(SearchExprs.Assignment_operatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchExprs#null_notnull}.
	 * @param ctx the parse tree
	 */
	void enterNull_notnull(SearchExprs.Null_notnullContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchExprs#null_notnull}.
	 * @param ctx the parse tree
	 */
	void exitNull_notnull(SearchExprs.Null_notnullContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchExprs#constant}.
	 * @param ctx the parse tree
	 */
	void enterConstant(SearchExprs.ConstantContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchExprs#constant}.
	 * @param ctx the parse tree
	 */
	void exitConstant(SearchExprs.ConstantContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchExprs#sign}.
	 * @param ctx the parse tree
	 */
	void enterSign(SearchExprs.SignContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchExprs#sign}.
	 * @param ctx the parse tree
	 */
	void exitSign(SearchExprs.SignContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchExprs#id}.
	 * @param ctx the parse tree
	 */
	void enterId(SearchExprs.IdContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchExprs#id}.
	 * @param ctx the parse tree
	 */
	void exitId(SearchExprs.IdContext ctx);
}