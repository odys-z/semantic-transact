package io.odysz.transact.sql.parts.condition;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io_odysz.FilenameUtils;

import io.odysz.common.AESHelper;
import io.odysz.common.DateFormat;
import io.odysz.common.LangExt;
import io.odysz.common.Utils;
import io.odysz.common.dbtype;
import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.sql.parts.Alias;
import io.odysz.transact.sql.parts.Colname;
import io.odysz.transact.sql.parts.Sql;
import io.odysz.transact.x.TransException;

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
		datetime("datetime"),
		concat("concat"),
		/**Function extFile(uri):<br>
		 * Handle external file when reading - a post operation is added to the on-select-ok event,
		 * which will replace the uri with content of external file. 
		 */
		extFile("extfile"),
		dbSame("func");
		private final String fid;
		private Func(String fid) { this.fid = fid; }
		public String fid() { return fid; }

		public static Func parse(String funcName) {
			funcName = funcName.trim().toLowerCase();
			if (now.fid.equals(funcName))
				return now;
			else if (isnull.fid.equals(funcName))
				return isnull;
			else if (ifElse.fid.equals(funcName))
				return ifElse;
			else if (ifNullElse.fid.equals(funcName))
				return ifNullElse;
			else if (datetime.fid.equals(funcName) || "date".equals(funcName))
				return datetime;
			else if (extFile.fid.equals(funcName) || "ext".equals(funcName))
				return extFile;
			else if (concat.fid.equals(funcName))
				return concat;
			return dbSame; // max etc. are db same
		}
	}

	private Func func;
	private Object[] args;

	/**column name in resultset */
	private Alias resultAlias;;

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

	public Funcall(String funcName, ExprPart[] funcArgs) {
		super(funcName);
		this.func = Func.parse(funcName);
		args = funcArgs;
	}

	public Funcall args(String[] args) {
		this.args = args;
		return this;
	}
	
	/**Create a now() sql function.
	 * @return Funcall object
	 */
	public static Funcall now () {
		return new Funcall(Func.now);
	}
	
	/**@deprecated
	 * @param args
	 * @return Funcall object
	 */
	public static Funcall max(String... args) {
		Funcall f = new Funcall(Func.max);
		f.args = args;
		return f;
	}

	@Override
	public String sql(ISemantext context) throws TransException {
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
		else if (func == Func.datetime)
			return sql2Datetime(context, args);
		else if (func == Func.extFile)
			return sqlExtFile(context, args);
		else if (func == Func.concat)
			return sqlConcat(context, args);
		else
			try {
				return dbSame(context, args);
			} catch (TransException e) {
				e.printStackTrace();
				return null;
			}
	}

	/**return function string that database function used as the same style.
	 * @param ctx
	 * @return
	 * @throws TransException 
	 */
	private String dbSame(ISemantext ctx, String[] args) throws TransException {
		String f = super.sql(ctx) + "(";
		if (args != null && args.length > 0 && args[0] != null)
			f += args[0];

		for (int i = 1; args != null && i < args.length; i++)
			f += ", " + args[i];

		return f + ")";
	}

	/**
	 * What the handler is doing:<br>
	 * 1. read file from the file of args[0]<br>
	 * 2. set readed contents to the current row of contxt, with {@link ISemantext#setRs(String, String)}
	 * @param context semantext
	 * @param args <br>[0] relative filepath (replacing contents),
	 * <br>[1] select elem alias (to be replaced)
	 * @return sql for the SelectElem, a.k.a. args[0]
	 */
	private String sqlExtFile(ISemantext context, String[] args) {
		if (args == null || args.length != 1 || LangExt.isblank(args[0], "'\\s*'"))
			Utils.warn("Function extFile() only accept 1 arguments. (And do not confused with class ExtFile)");
		else {
			if (LangExt.isblank(resultAlias)) {
				String ss[] = LangExt.split(args[0], "\\.");
				resultAlias = new Alias(ss[ss.length - 1]);
			}

			// Add extFile() handler to handle selected value
			if (!context.hasOnSelectedHandler(Func.extFile.fid()))
				context.addOnSelectedHandler(Func.extFile.fid(),
					(stx, row, cols) -> {
						// replace path value in selected results with the content of file
						try {
							int c = (Integer) cols.get(resultAlias.toUpperCase())[0];
							c--; // in SResultset, column index start at 1
							String fn = (String) row.get(c);
							if (!LangExt.isblank(fn, "\\.", "\\*")) {
								fn = FilenameUtils.concat(stx.containerRoot(), fn);
								Path f = Paths.get(fn);
								if (Files.exists(f) && !Files.isDirectory(f)) {
									byte[] fi = Files.readAllBytes(f);
									row.set(c, AESHelper.encode64(fi));
								}
								else {
									Utils.warn("Funcal (extFile) onSelected postOP(): Can't find file:\n%s", fn);
									row.set(c, "File not Found: " + fn);
								}
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
		}
		return args[0];
	}

	private static String sqlConcat(ISemantext ctx, String[] args) {
		dbtype dt = ctx.dbtype();
//		if (dt == dbtype.oracle)
//			?;
//		else {
			if (dt != dbtype.mysql && dt != dbtype.oracle
					&& dt != dbtype.sqlite && dt != dbtype.ms2k)
				Utils.warn("Funcall#sql2datetime(): Using '%s' for unknown db type: %s", args[0], dt.name());
			return Stream.of(args).collect(Collectors.joining(" || "));
//		}
	}

	/**
	 * str_to_date(str-val, '%Y-%m-%d %H:%i:%s')<br>
	 * datetime('1866-12-12 14:12:12')<br>
	 * CONVERT(datetime, '2009/07/16 08:28:01', 120)<br>
	 * TO_DATE('2012-07-18 13:27:18', 'YYYY-MM-DD HH24:MI:SS')
	 * @param ctx
	 * @param args
	 * @return
	 */
	private String sql2Datetime(ISemantext ctx, String[] args) {
		dbtype dt = ctx.dbtype();
		if (dt == dbtype.mysql)
			return String.format("str_to_date('%s', '%Y-%m-%d %H:%i:%s')", args[0]);
		else  if (dt == dbtype.sqlite)
			return String.format("datetime('%s')", args[0]);
		else if (dt == dbtype.ms2k)
			return String.format("convert(datatime, '%s', 120)", args[0]);
		else if (dt == dbtype.oracle)
			return String.format("to_date('%s', 'YYYY-MM-DD HH24:MI:SS')", args[0]);
		else {
			Utils.warn("Funcall#sql2datetime(): Using '%s' for unknown db type: %s", args[0], dt.name());
			return "'" + args[0] + "'";
		}
	}

	private static String sqlIfNullElse(ISemantext context, String[] args) {
		dbtype dt = context.dbtype();
		if (dt == dbtype.mysql)
			return String.format("if(%s is null, %s, %s)",
					args[0], args[1], args[2]);
		else  if (dt == dbtype.sqlite)
			return String.format("case when %s is null then %s else %s end",
					args[0], args[1], args[2]);
		else if (dt == dbtype.ms2k)
			return String.format("case when %s is null then %s else %s end",
					args[0], args[1], args[2]);
		else if (dt == dbtype.oracle)
			return String.format("decode(%s, null, %s, %s)", args[0],
					Sql.bool2Int(args[1]), Sql.bool2Int(args[2]));
		else {
			Utils.warn("Funcall#sqlIfelse(): Using is(a is null, b, c) for unknown db type: %s",
					dt.name());
			return String.format("if(%s is null, %s, %s)",
					args[0], args[1], args[2]);
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

	private static String[] argsql(Object[] args, ISemantext context) throws TransException {
		if (args == null)
			return null;
		String argus[] = new String[args.length];
		for (int i = 0; i < args.length; i++) {
			Object a = args[i];
			if (a instanceof AbsPart)
				argus[i] = ((AbsPart)a).sql(context);
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

	/**Create a function decoding null value, e.g. for oracle: decode (colElem, null, ifTrue, orElse).
	 * @param colElem can only be a full column name.
	 * @param ifTrue
	 * @param orElse
	 * @return the Funcall object
	 */
	public static Funcall ifNullElse(String colElem, Object ifTrue, Object orElse) {
		Funcall f = new Funcall(Func.ifNullElse);
		f.args = new Object[] {Colname.parseFullname(colElem), ifTrue, orElse};
		return f;
	}

	public static Funcall ifElse(String logic, Object ifTrue, Object orElse) {
		Funcall f = new Funcall(Func.ifElse);
		f.args = new Object[] {logic, ifTrue, orElse};
		return f;
	}

	/**Create a funcall, generating sql for<br>
	 * <a href='https://dev.mysql.com/doc/refman/5.5/en/date-and-time-functions.html#function_str-to-date'>mysq:</a><br>
	 * str_to_date(str-val, '%Y-%m-%d %H:%i:%s')<br>
	 * <a href='https://www.sqlite.org/lang_datefunc.html'>sqlite</a><br>
	 * datetime('1866-12-12 14:12:12')<br>
	 * <a href='https://stackoverflow.com/questions/1135746/sql-server-convert-string-to-datetime'>ms sql 2k</a><br>
	 * CONVERT(datetime, '2009/07/16 08:28:01', 120)<br>
	 * <a href='www.sqlines.com/oracle-to-sql-server/to_date'>oracle</a><br>
	 * TO_DATE('2012-07-18 13:27:18', 'YYYY-MM-DD HH24:MI:SS')
	 * @param dt
	 * @param date
	 * @return create a Funcal
	 */
	public static Funcall toDate(dbtype dt, String date) {
		Funcall f = new Funcall(Func.datetime);
		f.args = new Object[] {date};
		return f;
	}

	public static AbsPart concat(String to, String... with) {
		Funcall f = new Funcall(Func.concat);
		
		Colname argTo = Colname.parseFullname(to);
		if (with == null)
			f.args = new Object[] {argTo == null ? to : argTo};
		else {
			f.args = new Object[with.length + 1];
			f.args[0] = argTo == null ? to : argTo;
			for (int ix = 0; ix < with.length; ix++) {
				// f.args[ix + 1] = with[ix];
				Colname argWith = Colname.parseFullname(with[ix]);
				f.args[ix + 1] = argWith == null ? with[ix] : argWith;
			}
		}
		return f;
	}

	public void selectElemAlias(Alias alias) {
		this.resultAlias = alias;
	}
}
