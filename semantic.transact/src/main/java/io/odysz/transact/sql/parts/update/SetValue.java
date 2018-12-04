package io.odysz.transact.sql.parts.update;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.Query;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.sql.parts.condition.Funcall;
import io.odysz.transact.sql.parts.insert.ValueList;
import io.odysz.transact.x.TransException;

/**Support value list in update set value elem and insert values list:<br>
 * value can only be:<br>
 * subquery,<br>
 * expression,<br>
 * function_call: method_name '(' expression_list ')'
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * TODO handle select date('now'), time('now'), DATETIME('now'), strftime('%Y-%m-%d %H-%M-%f','now')
 * @see {@link ValueList}
 * @author ody */
public class SetValue extends ExprPart {

	private Query selectValue;
	private String constValue;
	private ExprPart expr;
	private Funcall function;

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
					.map(p -> p.sql(sctx))
					.collect(Collectors.joining(""));
		else if (expr != null)
			return expr.sql(sctx);
		else if (function != null)
			return function.sql(sctx);
		else if (constValue != null) {
			ValueList constList = new ValueList(1);
			try { constList.constv(0, constValue); }
			catch (TransException e) {
				e.printStackTrace();
				return "";
			}
			return constList.sql(sctx);
		}
		else
			return super.sql(sctx);
	}
}
