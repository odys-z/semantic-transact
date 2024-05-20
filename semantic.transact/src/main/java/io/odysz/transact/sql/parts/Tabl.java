package io.odysz.transact.sql.parts;

import io.odysz.common.dbtype;
import io.odysz.semantics.ISemantext;

public class Tabl extends AbsPart {

	String tbl;
	
	public Tabl(String tabl) {
		tbl = tabl;
	}

	@Override
	public String sql(ISemantext ctx) {
		if (ctx != null && ctx.dbtype() == dbtype.oracle)
			return "\"" + tbl + "\"";
		return tbl == null ? "" : tbl;
	}

	public String name() {
		return tbl;
	}
	
	public boolean isblank() {
		return isblank(tbl);
	}

}
