package io.odysz.transact.sql.parts;

import io.odysz.common.dbtype;
import io.odysz.semantics.ISemantext;
import io.odysz.transact.x.TransException;

public class Tabl extends AbsPart {

	String tbl;
	
	public Tabl(String tabl) {
		tbl = tabl;
	}

	@Override
	public String sql(ISemantext ctx) throws TransException {
		if (ctx != null && ctx.dbtype() == dbtype.oracle)
			return "\"" + tbl + "\"";
		return tbl;
	}

	public String name() {
		return tbl;
	}

}
