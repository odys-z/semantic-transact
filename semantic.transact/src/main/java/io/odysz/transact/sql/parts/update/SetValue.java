package io.odysz.transact.sql.parts.update;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.Query;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.sql.parts.condition.Funcall;
import io.odysz.transact.x.TransException;

/**Support value list in update set value elem and insert values list:<br>
 * value can only be:<br>
 * subquery,<br>
 * expression,<br>
 * function_call: method_name '(' expression_list ')'
 * where {@link Funcall} can handle 'now' for different db, like
 * <pre>datetime('now'), strftime('%Y-%m-%d %H-%M-%f','now')</pre>
 * @see {@link io.odysz.transact.sql.parts.insert.ValueList}
 * 
 * @author odys-z@github.com
 * */
public class SetValue extends ExprPart {

	private Query selectValue;
	private String constValue;
	private ExprPart expr;

	public SetValue(Object rexp) {
		super(rexp instanceof String ? (String) rexp : "");
		if (rexp instanceof Query)
			selectValue = (Query) rexp;
		else if (rexp instanceof ExprPart)
			expr = (ExprPart) rexp;
		else if (rexp instanceof String)
			constValue = (String) rexp;
	}

	@Override
	public String sql(ISemantext sctx) {
		if (selectValue != null)
			return Stream.of(new ExprPart("("), selectValue, new ExprPart(")"))
					.map(p -> {
						try {
							return p.sql(sctx);
						} catch (TransException e) {
							e.printStackTrace();
							return "";
						}
					})
					.collect(Collectors.joining(""));
		else if (expr != null)
			return expr.sql(sctx);
		else if (constValue != null) {
			// String v = sctx == null ? constValue : (String) sctx.resulvedVal(constValue);
			// return "'" + v + "'";
			return "'" + constValue + "'";
		}
		else
			return super.sql(sctx);
	}
}
