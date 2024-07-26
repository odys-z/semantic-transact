package io.odysz.transact.sql.parts;

import io.odysz.common.dbtype;
import io.odysz.semantics.ISemantext;

public class Alias extends AbsPart {

	String a;
	
	public Alias(String as) {
		a = as;
	}

	@Override
	public String sql(ISemantext ctx) {
		if (ctx != null && ctx.dbtype() == dbtype.oracle)
			return "\"" + a + "\"";
		return a == null ? "" : a;
	}

	public String toUpperCase() {
		return a == null ? "" : a.toUpperCase();
	}

	public String toString() {
		return a;
	}
}
