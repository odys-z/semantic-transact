package io.odysz.transact.sql.parts.condition;

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
	private String[] args;;

	public Funcall(Func func) {
		super(func.fid());
		this.func = func;
	}

	public static Funcall now () {
		return new Funcall(Func.now);
	}
	
	public static Funcall max(String... args) {
		Funcall f = new Funcall(Func.now);
		f.args = args;
		return f;
	}

	@Override
	public String sql(ISemantext context) {
		if (func == Func.now)
			return sqlNow(context);
		else return "TODO";
	}

	private String sqlNow(ISemantext context) {
		switch (context.dbtype()) {
		case sqlite:
			return "datetime('now')"; 
		case oracle:
			return "sysdate";
		case mysql:
		default:
			return "now()";
		}
	}

}
