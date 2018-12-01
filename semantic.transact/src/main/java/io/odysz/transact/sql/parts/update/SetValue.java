package io.odysz.transact.sql.parts.update;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.semantics.Semantext;
import io.odysz.transact.sql.Query;
import io.odysz.transact.sql.parts.condition.ExprPart;

public class SetValue extends ExprPart {

	private Query selectValue;

	public SetValue(Object rexp) {
		super(rexp instanceof String ? (String) rexp : "");
		if (rexp instanceof Query)
			selectValue = (Query) rexp;
	}

	@Override
	public String sql(Semantext sctx) {
		if (selectValue != null)
			return Stream.of(new ExprPart("("), selectValue, new ExprPart(")"))
					.map(p -> p.sql(sctx))
					.collect(Collectors.joining(""));
		else
			return super.sql(sctx);
	}
}
