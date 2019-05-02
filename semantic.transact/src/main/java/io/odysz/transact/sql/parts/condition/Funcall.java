package io.odysz.transact.sql.parts.condition;

import java.util.Date;

import io.odysz.common.DateFormat;
import io.odysz.common.Utils;
import io.odysz.common.dbtype;
import io.odysz.semantics.ISemantext;

/**
 * <pre>
function_call
    : aggregate_windowed_function
    | func_proc_name '(' expression_list? ')'
    ;</pre>
 * @author ody
 *
 */
public class Funcall extends ExprPart {
	/**Use sqlite localtime or not */
	public static boolean sqliteUseLocaltime = false;
	/**Use ms 2k sql server getutcdate() or getDate() */
	public static boolean ms2kUseUTCtime = false;

	public enum Func { now("now()"), max("max(%s)");
		private final String fid;
		private Func(String fid) { this.fid = fid; }
		public String fid() { return fid; }
	}

	private Func func;
	@SuppressWarnings("unused")
	private String[] args;;

	public Funcall(Func func) {
		super(func.fid());
		this.func = func;
	}

	public static Funcall now (dbtype dtype) {
		return new Funcall(Func.now);
	}
	
	public static Funcall max(String... args) {
		Funcall f = new Funcall(Func.max);
		f.args = args;
		return f;
	}

	@Override
	public String sql(ISemantext context) {
		if (func == Func.now)
			return sqlNow(context);
		else if (func == Func.ifnull)
			return sqlIfnull(context);
		else return "TODO (Funcall)";
	}

	private String sqlNow(ISemantext context) {
		dbtype dt = context.dbtype();
		// return new Funcall(Func.now);
		if (dt == dbtype.mysql)
			return "now()";
		else  if (dt == dbtype.sqlite)
			return sqliteUseLocaltime ? "datetime('now', 'localtime')" : "datetime('now')";
		else if (dt == dbtype.ms2k)
			return ms2kUseUTCtime ? "getutcdate()" : "getdate()";
		else if (dt == dbtype.oracle)
			return "SYSDATE()";
		else {
			String s = DateFormat.formatime(new Date());
			Utils.warn("Formating now() for unknown db type: %s as %s", dt.name(), s);
			return "'" + s + "'";
		}
	}

}
