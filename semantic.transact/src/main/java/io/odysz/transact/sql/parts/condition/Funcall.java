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

	public enum Func {
		now("now()"),
		max("max(%s)"),
		isnull("ifnull"),
		ifElse("if"),
		ifNullElse("ifNullElse"),
		dbSame("func");
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
	private Object[] args;;

	public Funcall(Func func) {
		super(func.fid());
		this.func = func;
	}
	
	public Funcall(String funcName, String[] funcArgs) {
		super(funcName);
		this.func = Func.parse(funcName);
		args = funcArgs;
	}

	public Funcall(String funcName, String colName) {
		super(funcName);
		args = new String[] {colName};
		this.func = Func.dbSame;
	}

	public Funcall args(String[] args) {
		this.args = args;
		return this;
	}
	
	/**Create a now() sql function.
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
		// args are handled before this tree node handling, making ExprPart's sql available.
		String args[] = argsql(this.args, context);

		if (func == Func.now)
			return sqlNow(context, args);
		else if (func == Func.isnull)
			return sqlIfnull(context, args);
		else if (func == Func.ifNullElse)
			return sqlIfNullElse(context, args);
		else if (func == Func.ifElse)
			return sqlIfElse(context, args);
		else return dbSame(context, args);
	}

	/**return function string that database function used as the same style.
	 * @param ctx
	 * @return
	 */
	private String dbSame(ISemantext ctx, String[] args) {
		String f = super.sql(ctx) + "(";
		if (args != null && args.length > 0 && args[0] != null)
			f += args[0];

		for (int i = 1; args != null && i < args.length; i++)
			f += ", " + args[i];

		return f + ")";
	}

	private static String sqlIfNullElse(ISemantext context, String[] args) {
		dbtype dt = context.dbtype();
		if (dt == dbtype.mysql)
			return String.format("if(%s is null, %s, %s)", args[0], args[1], args[2]);
		else  if (dt == dbtype.sqlite)
			return String.format("case when %s is null then %s else %s end", args[0], args[1], args[2]);
		else if (dt == dbtype.ms2k)
			return String.format("case when %s is null then %s else %s end", args[0], args[1], args[2]);
		else if (dt == dbtype.oracle)
			return String.format("decode(%s, null, %s, %s)", args[0], args[1], args[2]);
		else {
			Utils.warn("Funcall#sqlIfelse(): Using is(a is null, b, c) for unknown db type: %s", dt.name());
			return String.format("if(%s is null, %s, %s)", args[0], args[1], args[2]);
		}
	}

	private static String sqlIfElse(ISemantext context, String[] args) {
		dbtype dt = context.dbtype();
		if (dt == dbtype.sqlite || dt == dbtype.ms2k || dt == dbtype.oracle)
			return String.format("case when %s then %s else %s end", args[0], args[1], args[2]);
		else if (dt == dbtype.mysql)
			return String.format("if(%s, %s, %s)", args[0], args[1], args[2]);
		else {
			Utils.warn("Funcall#sqlIfelse(): Using is(a is null, b, c) for unknown db type: %s", dt.name());
			return String.format("case when %s then %s else %s end", args[0], args[1], args[2]);
		}

	}

	private static String[] argsql(Object[] args, ISemantext context) {
		if (args == null)
			return null;
		String argus[] = new String[args.length];
		for (int i = 0; i < args.length; i++) {
			Object a = args[i];
			if (a instanceof ExprPart)
				argus[i] = ((ExprPart)a).sql(context);
			else argus[i] = a.toString(); 
		}
		return argus;
	}

	private static String sqlIfnull(ISemantext context, String[] args) {
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

	private static String sqlNow(ISemantext context, String[] args) {
		dbtype dt = context.dbtype();
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

	public static Funcall ifNullElse(String colElem, Object ifTrue, Object orElse) {
		Funcall f = new Funcall(Func.ifNullElse);
		f.args = new Object[] {colElem, ifTrue, orElse};
		return f;
	}

	public static Funcall ifElse(String logic, Object ifTrue, Object orElse) {
		Funcall f = new Funcall(Func.ifElse);
		f.args = new Object[] {logic, ifTrue, orElse};
		return f;
	}


}
