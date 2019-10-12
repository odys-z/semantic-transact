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

	/**<p>Regex for reserved key words may presented like a column name in select element list.</p>
	 * <p>Matched words like "SYSDATE" won't be quoted with "" if database is oracle.</p>
	 * <p>This regex is case insensitive.</p>
	 * <p>For oracle documents, see <a href='https://docs.oracle.com/cd/B19306_01/em.102/b40103/app_oracle_reserved_words.htm'>
	 * Oracle Reserved Words</a>.</p>
	 */
	public static String regCantColumnOrcl = "(SYSDATE)|(OID)|(TIME)|(TIMESTAMP)|(UID)|(ZONE)|(INTERVAL)|(YEAR)|(MONTH)|(DAY)|(HOURE)|(MINUTE)|(SECOND)";

	protected String c;
	protected Alias tabl;

	public Colname(String n) {
		c = n;
	}

	@Override
	public String sql(ISemantext ctx) throws TransException {
		if (ctx != null && ctx.dbtype() == dbtype.oracle)
			return LangExt.isblank(tabl) ?
					cantOrclColname(c) ? c : "\"" + c + "\""
					: tabl.sql(ctx) + ".\"" + c + "\"";
		else
			return LangExt.isblank(tabl) ?
					c : tabl.sql(ctx) + "." + c;
	}

	private static boolean cantOrclColname(String col) {
		return col == null ? true :
			col.toUpperCase().matches(regCantColumnOrcl);
	}

	/**Parse the full column name, can distinguish constant like ".*"
	 * @param coln
	 * @return null for constant, otherwise always a colname, with or without table name.
	 */
	public static Colname parseFullname(String coln) {
		// Some times both full column name and constants can be used as arguments.
		// This is shortcut, not fully parsed 
		if (coln == null || coln.matches("^\\s*'.*'\\s*$"))
			return null;

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
}
