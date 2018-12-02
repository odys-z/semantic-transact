package io.odysz.transact.sql.parts;

import io.odysz.semantics.ISemantext;

/**<pre>
https://github.com/antlr/grammars-v4/blob/master/tsql/TSqlParser.g4
search_condition     : search_condition_and (OR search_condition_and)* ;
search_condition_and : search_condition_not (AND search_condition_not)* ;
search_condition_not : NOT? predicate ;
predicate            : expression comparison_operator expression
                     | (search_condtion);
expression           : expression op expression
                     | (search_condition);
</pre>
 * @author ody
 *
 */
abstract public class AbsPart {

	/**Generating SQL after all elements in AST are ready.
	 * A context is a semantics context for resolving value references, etc.
	 * This method shouldn't been called. Use #{@link Query#commit(ArrayList<String>)} to generate SQLs.
	 * @param context
	 * @return
	 */
	public abstract String sql(ISemantext context);
}
