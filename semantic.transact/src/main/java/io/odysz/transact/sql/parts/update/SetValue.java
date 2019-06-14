package io.odysz.transact.sql.parts.update;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.Query;
import io.odysz.transact.sql.parts.antlr.ExprsVisitor;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.sql.parts.condition.Funcall;
import io.odysz.transact.x.TransException;

/**Support value list in update({@link SetList})
 * set value elem and insert values list
 * ({@link io.odysz.transact.sql.parts.insert.ValueList insert's-ValueList}).<br>
 * Value can only be:<br>
 * sub-query ({@link Query} statement),<br>
 * expression ({@link ExprPart}),<br>
 * function_call: method_name '(' expression_list ')'
 * where {@link Funcall} can handle 'now' for different db, like
 * <pre>datetime('now'), strftime('%Y-%m-%d %H-%M-%f','now')</pre>
 * @see {@link io.odysz.transact.sql.parts.insert.ValueList}
 * 
 * @author odys-z@github.com
 * */
public class SetValue extends ExprPart {

	private Query selectValue;
//	private String constValue;
	private ExprPart expr;

	public SetValue(Object rexp) {
		super(rexp instanceof String ? (String) rexp : "");
		if (rexp instanceof Query)
			selectValue = (Query) rexp;
		else if (rexp instanceof ExprPart)
			expr = (ExprPart) rexp;
		else if (rexp instanceof String)
			// rexp can be an expression here
			// constValue = (String) rexp;
			expr = ExprsVisitor.parse((String)rexp);
	}
	
	@SuppressWarnings("unused")
	private String tabl;
	@SuppressWarnings("unused")
	private String col;

	/**This value is set value to tabl.col. 
	 * @param tabl
	 * @param col
	 * @return this
	 */
	public SetValue setVal2(String tabl, String col) {
		this.tabl = tabl;
		this.col = col;
		return this;
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
		else
			return expr.sql(sctx);
//		else if (expr != null)
//			return expr.sql(sctx);
//		else if (constValue != null) {
//			// String v = sctx == null ? constValue : (String) sctx.resulvedVal(constValue);
//			// return "'" + v + "'";
//
//			// return "'" + constValue + "'";
//			if (col == null || sctx.colType(tabl) == null)
//				return "'" + constValue + "'";
//			else {
//				TableMeta mt = sctx.colType(tabl);
//				if (mt.isQuoted(col))
//					return "'" + constValue + "'";
//				else 
//					return constValue;
//			}
//		}
//		else
//			return super.sql(sctx);
	}
}
