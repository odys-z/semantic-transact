package io.odysz.transact.sql.parts.condition;

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

	public static Funcall now () {
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
		else return "TODO (Funcall)";
	}

	private String sqlNow(ISemantext context) {
		dbtype dt = context.dbtype();
		if (dbtype.sqlite == dt)
			return "datetime('now')"; 
		else if (dbtype.oracle == dt)
			return "sysdate";
		else
			// else if (mysql:
			return "now()";
	}

}
