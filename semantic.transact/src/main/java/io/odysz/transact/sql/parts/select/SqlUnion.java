package io.odysz.transact.sql.parts.select;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.Query;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.x.TransException;

public class SqlUnion extends AbsPart {
	public static final int UNION = 1;
	public static final int EXCEPT = 2;
	public static final int INTERSECT = 3;

	private String utype;
	private Query unionWith;

	public SqlUnion (int type, Query with, boolean asExpression) {
		if (type == UNION)
			utype = "union";
		else if (type == EXCEPT)
			utype = "except";
		else if (type == INTERSECT)
			utype = "intersect";
		unionWith = with.asQueryExpr(asExpression);
	}

	@Override
	public String sql(ISemantext ctx) throws TransException {
		return Stream.of(new ExprPart(utype),
				unionWith)
				.map(m -> {
					try {
						return m.sql(ctx);
					} catch (TransException e) {
						e.printStackTrace();
						return "";
					}
				})
				.collect(Collectors.joining(" "));
	}

}
