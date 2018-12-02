package io.odysz.transact.sql.parts.update;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.Query;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.sql.parts.select.ConstList;
import io.odysz.transact.x.TransException;

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
					.map(p -> p.sql(sctx))
					.collect(Collectors.joining(""));
		else if (expr != null)
			return expr.sql(sctx);
		else if (constValue != null) {
			ConstList constList = new ConstList(1);
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
