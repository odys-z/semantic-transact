package io.odysz.transact.sql.parts;

import java.io.OutputStream;

import io.odysz.common.LangExt;
import io.odysz.semantics.ISemantext;
import io.odysz.transact.x.TransException;

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
	 * This method shouldn't been called. Use Statement#commit() to generate SQLs.
	 * @param context
	 * @return sql
	 * @throws TransException Something invalid while composing sql.
	 */
	public abstract String sql(ISemantext context) throws TransException;
	
	/**
	 * This should optimize performance. {@link #sql(ISemantext, OutputStream)} will be replaced by this.
	 * @since 1.6.0
	 * @param context
	 * @param os
	 * @throws TransException
	 */
	public void sql(ISemantext context, OutputStream os) throws TransException { }

	public static boolean isblank(Object obj, String... takeAsNull) {
		if (obj instanceof AbsPart)
			return obj instanceof Resulving ?
				obj == null : LangExt.isblank(obj.toString(), "null");
		else return LangExt.isblank(obj, takeAsNull);
	}
}
