// Generated from ./SelectParts.g4 by ANTLR 4.13.1
package gen.antlr.sql.select;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link SelectParts}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface SelectPartsVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link SelectParts#select_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_list(SelectParts.Select_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link SelectParts#select_list_elem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_list_elem(SelectParts.Select_list_elemContext ctx);
	/**
	 * Visit a parse tree produced by {@link SelectParts#as_column_alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAs_column_alias(SelectParts.As_column_aliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link SelectParts#column_alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_alias(SelectParts.Column_aliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link SelectParts#column_elem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_elem(SelectParts.Column_elemContext ctx);
	/**
	 * Visit a parse tree produced by {@link SelectParts#asterisk}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAsterisk(SelectParts.AsteriskContext ctx);
	/**
	 * Visit a parse tree produced by {@link SelectParts#search_condition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSearch_condition(SelectParts.Search_conditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SelectParts#search_condition_and}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSearch_condition_and(SelectParts.Search_condition_andContext ctx);
	/**
	 * Visit a parse tree produced by {@link SelectParts#search_condition_not}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSearch_condition_not(SelectParts.Search_condition_notContext ctx);
	/**
	 * Visit a parse tree produced by {@link SelectParts#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicate(SelectParts.PredicateContext ctx);
	/**
	 * Visit a parse tree produced by {@link SelectParts#expression_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression_list(SelectParts.Expression_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link SelectParts#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(SelectParts.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SelectParts#function_call}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction_call(SelectParts.Function_callContext ctx);
	/**
	 * Visit a parse tree produced by {@link SelectParts#aggregate_windowed_function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregate_windowed_function(SelectParts.Aggregate_windowed_functionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SelectParts#func_proc_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_proc_name(SelectParts.Func_proc_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SelectParts#full_column_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFull_column_name(SelectParts.Full_column_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SelectParts#table_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_name(SelectParts.Table_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SelectParts#unary_operator_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnary_operator_expression(SelectParts.Unary_operator_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SelectParts#bracket_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBracket_expression(SelectParts.Bracket_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SelectParts#constant_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstant_expression(SelectParts.Constant_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SelectParts#comparison_operator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparison_operator(SelectParts.Comparison_operatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link SelectParts#assignment_operator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignment_operator(SelectParts.Assignment_operatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link SelectParts#null_notnull}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNull_notnull(SelectParts.Null_notnullContext ctx);
	/**
	 * Visit a parse tree produced by {@link SelectParts#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstant(SelectParts.ConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link SelectParts#sign}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSign(SelectParts.SignContext ctx);
	/**
	 * Visit a parse tree produced by {@link SelectParts#id}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitId(SelectParts.IdContext ctx);
}