package io.odysz.transact.sql.parts;

import io.odysz.common.LangExt;
import io.odysz.common.dbtype;
import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.x.TransException;

/**The full_col_name, a case of expression.
 * @author odys-z@github.com
 */
public class Colname extends ExprPart {

	protected String c;
	protected Alias tabl;

	public Colname(String n) {
		c = n;
	}

	@Override
	public String sql(ISemantext ctx) throws TransException {
		if (ctx != null && ctx.dbtype() == dbtype.oracle)
			return LangExt.isblank(tabl) ?
					"\"" + c + "\"" : tabl.sql(ctx) + ".\"" + c + "\"";
		else
			return LangExt.isblank(tabl) ?
					c : tabl.sql(ctx) + "." + c;
	}

	public static Colname parseFullname(String coln) {
		String[] colnss =  coln.split("\\.");
		Colname col = new Colname(colnss[colnss.length - 1]);
		if (colnss.length > 1)
			col.tabl(colnss[colnss.length - 2]);
		return col;
	}

	public Colname tabl(String tbl) {
		this.tabl = new Alias(tbl);
		return this;
	}

//	public String toUpperCase() {
//		return a == null ? "" : a.toUpperCase();
//	}
}
