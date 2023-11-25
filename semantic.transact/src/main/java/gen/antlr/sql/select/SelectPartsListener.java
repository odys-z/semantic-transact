// Generated from ./SelectParts.g4 by ANTLR 4.13.1
package gen.antlr.sql.select;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SelectParts}.
 */
public interface SelectPartsListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SelectParts#select_list}.
	 * @param ctx the parse tree
	 */
	void enterSelect_list(SelectParts.Select_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectParts#select_list}.
	 * @param ctx the parse tree
	 */
	void exitSelect_list(SelectParts.Select_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link SelectParts#select_list_elem}.
	 * @param ctx the parse tree
	 */
	void enterSelect_list_elem(SelectParts.Select_list_elemContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectParts#select_list_elem}.
	 * @param ctx the parse tree
	 */
	void exitSelect_list_elem(SelectParts.Select_list_elemContext ctx);
	/**
	 * Enter a parse tree produced by {@link SelectParts#as_column_alias}.
	 * @param ctx the parse tree
	 */
	void enterAs_column_alias(SelectParts.As_column_aliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectParts#as_column_alias}.
	 * @param ctx the parse tree
	 */
	void exitAs_column_alias(SelectParts.As_column_aliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link SelectParts#column_alias}.
	 * @param ctx the parse tree
	 */
	void enterColumn_alias(SelectParts.Column_aliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectParts#column_alias}.
	 * @param ctx the parse tree
	 */
	void exitColumn_alias(SelectParts.Column_aliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link SelectParts#column_elem}.
	 * @param ctx the parse tree
	 */
	void enterColumn_elem(SelectParts.Column_elemContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectParts#column_elem}.
	 * @param ctx the parse tree
	 */
	void exitColumn_elem(SelectParts.Column_elemContext ctx);
	/**
	 * Enter a parse tree produced by {@link SelectParts#asterisk}.
	 * @param ctx the parse tree
	 */
	void enterAsterisk(SelectParts.AsteriskContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectParts#asterisk}.
	 * @param ctx the parse tree
	 */
	void exitAsterisk(SelectParts.AsteriskContext ctx);
	/**
	 * Enter a parse tree produced by {@link SelectParts#search_condition}.
	 * @param ctx the parse tree
	 */
	void enterSearch_condition(SelectParts.Search_conditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectParts#search_condition}.
	 * @param ctx the parse tree
	 */
	void exitSearch_condition(SelectParts.Search_conditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SelectParts#search_condition_and}.
	 * @param ctx the parse tree
	 */
	void enterSearch_condition_and(SelectParts.Search_condition_andContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectParts#search_condition_and}.
	 * @param ctx the parse tree
	 */
	void exitSearch_condition_and(SelectParts.Search_condition_andContext ctx);
	/**
	 * Enter a parse tree produced by {@link SelectParts#search_condition_not}.
	 * @param ctx the parse tree
	 */
	void enterSearch_condition_not(SelectParts.Search_condition_notContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectParts#search_condition_not}.
	 * @param ctx the parse tree
	 */
	void exitSearch_condition_not(SelectParts.Search_condition_notContext ctx);
	/**
	 * Enter a parse tree produced by {@link SelectParts#predicate}.
	 * @param ctx the parse tree
	 */
	void enterPredicate(SelectParts.PredicateContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectParts#predicate}.
	 * @param ctx the parse tree
	 */
	void exitPredicate(SelectParts.PredicateContext ctx);
	/**
	 * Enter a parse tree produced by {@link SelectParts#expression_list}.
	 * @param ctx the parse tree
	 */
	void enterExpression_list(SelectParts.Expression_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectParts#expression_list}.
	 * @param ctx the parse tree
	 */
	void exitExpression_list(SelectParts.Expression_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link SelectParts#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(SelectParts.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectParts#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(SelectParts.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SelectParts#function_call}.
	 * @param ctx the parse tree
	 */
	void enterFunction_call(SelectParts.Function_callContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectParts#function_call}.
	 * @param ctx the parse tree
	 */
	void exitFunction_call(SelectParts.Function_callContext ctx);
	/**
	 * Enter a parse tree produced by {@link SelectParts#aggregate_windowed_function}.
	 * @param ctx the parse tree
	 */
	void enterAggregate_windowed_function(SelectParts.Aggregate_windowed_functionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectParts#aggregate_windowed_function}.
	 * @param ctx the parse tree
	 */
	void exitAggregate_windowed_function(SelectParts.Aggregate_windowed_functionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SelectParts#func_proc_name}.
	 * @param ctx the parse tree
	 */
	void enterFunc_proc_name(SelectParts.Func_proc_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectParts#func_proc_name}.
	 * @param ctx the parse tree
	 */
	void exitFunc_proc_name(SelectParts.Func_proc_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SelectParts#full_column_name}.
	 * @param ctx the parse tree
	 */
	void enterFull_column_name(SelectParts.Full_column_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectParts#full_column_name}.
	 * @param ctx the parse tree
	 */
	void exitFull_column_name(SelectParts.Full_column_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SelectParts#table_name}.
	 * @param ctx the parse tree
	 */
	void enterTable_name(SelectParts.Table_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectParts#table_name}.
	 * @param ctx the parse tree
	 */
	void exitTable_name(SelectParts.Table_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SelectParts#unary_operator_expression}.
	 * @param ctx the parse tree
	 */
	void enterUnary_operator_expression(SelectParts.Unary_operator_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectParts#unary_operator_expression}.
	 * @param ctx the parse tree
	 */
	void exitUnary_operator_expression(SelectParts.Unary_operator_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SelectParts#bracket_expression}.
	 * @param ctx the parse tree
	 */
	void enterBracket_expression(SelectParts.Bracket_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectParts#bracket_expression}.
	 * @param ctx the parse tree
	 */
	void exitBracket_expression(SelectParts.Bracket_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SelectParts#constant_expression}.
	 * @param ctx the parse tree
	 */
	void enterConstant_expression(SelectParts.Constant_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectParts#constant_expression}.
	 * @param ctx the parse tree
	 */
	void exitConstant_expression(SelectParts.Constant_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SelectParts#comparison_operator}.
	 * @param ctx the parse tree
	 */
	void enterComparison_operator(SelectParts.Comparison_operatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectParts#comparison_operator}.
	 * @param ctx the parse tree
	 */
	void exitComparison_operator(SelectParts.Comparison_operatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link SelectParts#assignment_operator}.
	 * @param ctx the parse tree
	 */
	void enterAssignment_operator(SelectParts.Assignment_operatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectParts#assignment_operator}.
	 * @param ctx the parse tree
	 */
	void exitAssignment_operator(SelectParts.Assignment_operatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link SelectParts#null_notnull}.
	 * @param ctx the parse tree
	 */
	void enterNull_notnull(SelectParts.Null_notnullContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectParts#null_notnull}.
	 * @param ctx the parse tree
	 */
	void exitNull_notnull(SelectParts.Null_notnullContext ctx);
	/**
	 * Enter a parse tree produced by {@link SelectParts#constant}.
	 * @param ctx the parse tree
	 */
	void enterConstant(SelectParts.ConstantContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectParts#constant}.
	 * @param ctx the parse tree
	 */
	void exitConstant(SelectParts.ConstantContext ctx);
	/**
	 * Enter a parse tree produced by {@link SelectParts#sign}.
	 * @param ctx the parse tree
	 */
	void enterSign(SelectParts.SignContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectParts#sign}.
	 * @param ctx the parse tree
	 */
	void exitSign(SelectParts.SignContext ctx);
	/**
	 * Enter a parse tree produced by {@link SelectParts#id}.
	 * @param ctx the parse tree
	 */
	void enterId(SelectParts.IdContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectParts#id}.
	 * @param ctx the parse tree
	 */
	void exitId(SelectParts.IdContext ctx);
}