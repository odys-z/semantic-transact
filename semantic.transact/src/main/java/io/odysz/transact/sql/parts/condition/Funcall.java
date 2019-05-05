package io.odysz.transact.sql.parts.condition;

import java.util.Date;
import java.util.List;

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

	public enum Func { now("now()"), max("max(%s)"), isnull("ifnull"), dbSame("func");
		private final String fid;
		private Func(String fid) { this.fid = fid; }
		public String fid() { return fid; }

		public static Func parse(String funcName) {
			if (now.fid.equals(funcName))
				return now;
			if (isnull.fid.equals(funcName))
				return isnull;
			return dbSame; // max is db same
		}
	}

	private Func func;
	private String[] args;;

	public Funcall(Func func) {
		super(func.fid());
		this.func = func;
	}
	
	public Funcall(String funcName, List<ExprPart> funcArgs) {
		// TODO Auto-generated constructor stub
		super(funcName);
		this.func = Func.parse(funcName);
	}

	public Funcall args(String[] args) {
		this.args = args;
		return this;
	}
	
//	public Funcall(String funcExpr) {
//		super(funcExpr);
//		Func f = parse(funcExpr);
//		this.func = f;
//	}
//
//	private Func parse(String funcExpr) {
//		return null;
//	}

	/**@deprecated
	 * @param dtype
	 * @return Funcall object
	 */
	public static Funcall now (dbtype dtype) {
		return new Funcall(Func.now);
	}
	
	/**@deprecated
	 * @param dtype
	 * @return Funcall object
	 */
	public static Funcall max(String... args) {
		Funcall f = new Funcall(Func.max);
		f.args = args;
		return f;
	}

	@Override
	public String sql(ISemantext context) {
		if (func == Func.now)
			return sqlNow(context);
		else if (func == Func.isnull)
			return sqlIfnull(context);
		// else return func.fid();
		else return dbSame();
	}

	private String dbSame() {
		String f = func.fid + "(" +
				args != null && args.length > 0 && args[0] != null ? args[0] : "";

		for (int i = 1; args != null && i < args.length; i++)
			f += ", " + args[i];

		return f + ")";
	}

	private String sqlIfnull(ISemantext context) {
		dbtype dt = context.dbtype();
		if (dt == dbtype.mysql)
			return String.format("ifnull(%s, %s)", args[0], args[1]);
		else  if (dt == dbtype.sqlite)
			return String.format("ifnull(%s, %s)", args[0], args[1]);
		else if (dt == dbtype.ms2k)
			return String.format("isnull(%s, %s)", args[0], args[1]);
		else if (dt == dbtype.oracle)
			return String.format("nvl(%s, %s)", args[0], args[1]);
		else {
			Utils.warn("Using isnull() for unknown db type: %s", dt.name());
			return String.format("isnull(%s, %s)", args[0], args[1]);
		}
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
