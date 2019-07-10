package io.odysz.transact.sql.parts.update;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.Query;
import io.odysz.transact.sql.parts.Colname;
import io.odysz.transact.sql.parts.Tabl;
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
	private Tabl tabl;
	@SuppressWarnings("unused")
	private Colname col;

	/**This value is set value to tabl.col. <br>
	 * Set this information is initially designed for generating data according to meta,
	 * but seems unused now?
	 * @param tabl
	 * @param colname
	 * @return this
	 */
	public SetValue setVal2(Tabl tabl, String colname) {
		this.tabl = tabl;
		this.col = new Colname(colname);
		return this;
	}

	@Override
	public String sql(ISemantext sctx) throws TransException {
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
	}
}
