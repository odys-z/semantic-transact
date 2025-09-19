package io.odysz.transact.sql.parts.condition;

import static io.odysz.common.LangExt.f_funcall;
import static io.odysz.common.LangExt.ifnull;
import static io.odysz.common.LangExt.join;
import static io.odysz.common.LangExt.split;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.common.AESHelper;
import io.odysz.common.DateFormat;
import io.odysz.common.DocLocks;
import io.odysz.common.Utils;
import io.odysz.common.dbtype;
import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.sql.parts.Alias;
import io.odysz.transact.sql.parts.AnDbField;
import io.odysz.transact.sql.parts.Colname;
import io.odysz.transact.sql.parts.ExtFilePaths;
import io.odysz.transact.sql.parts.Resulving;
import io.odysz.transact.sql.parts.Sql;
import io.odysz.transact.sql.parts.select.SelectElem;
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

	public static enum Func {
		now("now()"),
		max("max"),
		min("min"),
		count("count"),
		sum("sum"), avg("avg"),
		isnull("ifnull"),
		ifElse("if"),
		ifNullElse("ifNullElse"),
		datetime("datetime"),
		concat("concat"),
		subStr("substr"),
		/**
		 * Concatenate multiple (string) columns in a "\n" separated string.
		 * The result can be splited by {@link io.odysz.common.LangExt#uncombine(String)}
		 */
		compound("compound"),

		div("div"), add("add"), minus("minus"), mul("mul"),

		/**
		 * Function extFile(uri):<br>
		 * Handle external file when reading - a post operation is added to the on-select-ok event,
		 * which will replace the uri with content of external file. 
		 */
		extFile("extfile"),
		
		/**
		 * Function file reference(uri):<br>
		 * Handle external file when reading - a deserializable object for user,
		 * e.g. file reference for asynchronous synchronization,
		 * which will replace the uri with string of the object for the uri content. 
		 * @since 1.5.60
		 */
		refile("refile"),

		/** such as max, probably are the same for various DB */
		dbSame("func");

		private final String fid;
		private Func(String fid) { this.fid = fid; }
		public String fid() { return fid; }

		public static Func parse(String funcName) {
			funcName = funcName.trim().toLowerCase();
			if (now.fid.equals(funcName)) return now;

			else if (max.fid.equals(funcName)) return max;
			else if (min.fid.equals(funcName)) return min;
			else if (count.fid.equals(funcName)) return count;
			else if (sum.fid.equals(funcName)) return sum;
			else if (avg.fid.equals(funcName)) return avg;

			else if (add.fid.equals(funcName))   return add;
			else if (minus.fid.equals(funcName)) return minus;
			else if (mul.fid.equals(funcName))   return mul;
			else if (div.fid.equals(funcName))   return div;

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
			else if (refile.fid.equals(funcName))
				return refile;
			else if (concat.fid.equals(funcName))
				return concat;
			
			else if (compound.fid.equals(funcName))
				return compound;

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
	
	/**
	 * @param args
	 * @return Funcall object
	 */
	public static Funcall max(String... args) {
		Funcall f = new Funcall(Func.max);
		f.args = args;
		return f;
	}

	public static Funcall max(ExprPart... exp) {
		Funcall f = new Funcall(Func.max);
		f.args = exp == null ? new String[]{"*"} : new Object[] {exp};
		return f;
	}

	public static Funcall min(String... args) {
		Funcall f = new Funcall(Func.min);
		f.args = args;
		return f;
	}

	public static Funcall min(ExprPart... exp) {
		Funcall f = new Funcall(Func.min);
		f.args = exp == null ? new String[]{"*"} : new Object[] {exp};
		return f;
	}

	public static Funcall avg(String... args) {
		Funcall f = new Funcall(Func.avg);
		f.args = args;
		return f;
	}
	
	public static Funcall avg(ExprPart... exp) {
		Funcall f = new Funcall(Func.avg);
		f.args = exp == null ? new String[]{"*"} : new Object[] {exp};
		return f;
	}
	
	public static Funcall add(Object l, Object r) {
		Funcall f = new Funcall(Func.add);
		f.args = new Object[] {l, r};
		return f;
	}
	
	public static Funcall minus(Object l, Object r) {
		Funcall f = new Funcall(Func.minus);
		f.args = new Object[] {l, r};
		return f;
	}
	
	public static Funcall mul(Object l, Object r) {
		Funcall f = new Funcall(Func.mul);
		f.args = new Object[] {l, r};
		return f;
	}
	
	public static Funcall div(Object l, Object r) {
		Funcall f = new Funcall(Func.div);
		f.args = new Object[] {l, r};
		return f;
	}

	/**
	 * @param col
	 * @return Funcall object
	 */
	public static Funcall count(String... col) {
		Funcall f = new Funcall(Func.count);
		f.args = col == null ? new String[]{"*"} : col;
		return f;
	}

	public static Funcall count(ExprPart exp) {
		Funcall f = new Funcall(Func.count);
		f.args = exp == null ? new String[]{"*"} : new Object[] {exp};
		return f;
	}
	
	public static Funcall sum(ExprPart exp) {
		Funcall f = new Funcall(Func.sum);
		f.args = new Object[] {exp};
		return f;
	}

	public static Funcall sum(String col) {
		Funcall f = new Funcall(Func.sum);
		f.args = new String[] {col};
		return f;
	}
	
	public static Funcall isnull(Object col, Object ifnull) {
		Funcall f = new Funcall(Func.isnull);
		f.args = new Object[] {col, ifnull};
		return f;
	}

	public static Funcall extfile(String... args) {
		Funcall f = new Funcall(Func.extFile);
		f.args = args;
		return f;
	}
	
	/**
	 * File reference. Got a serialized Anson object, ReourceRef for resolving later.
	 * @param ansonObj
	 * @return the function
	 * @since 1.5.60
	 */
	public static Funcall refile(AnDbField ansonObj) {
		Funcall f = new Funcall(Func.refile);
		f.args = new Object[] {ansonObj};
		return f;
	}
	
	public static SelectElem refile(AnDbField ansonObj, String as) {
		return new SelectElem(refile(ansonObj), as);
	}

	public static Funcall subStr(Object col, int start, int end) {
		Funcall f = new Funcall(Func.subStr);
		f.args = new Object[] {col, start, end};
		return f;
	}

	/**
	 * This function doesn't has a func type id as it is a wrapping of
	 * {@link #ifElse(Predicate, Object, Object)}.
	 * 
	 * @param col
	 * @return the function for be used in a Query or other statements. The sql function returns 1 : 0. 
	 */
	public static ExprPart isEnvelope(Object col) {
		return ifElse(Predicate.eq(Funcall.subStr(col, 1, 8), "{\"type\":"), 1, 0);
	}

	/**
	 * Concatenate col values into a split-able text value
	 * @param col
	 * @param withs
	 * @return
	 */
	public static Funcall compound(String col, String... withs) {
		Funcall f = new Funcall(Func.compound);
		f.args = Stream.concat(Stream.of(col), Stream.of(withs)).toArray();
		return f;
	}

	public static Funcall compound(String[] cols) {
		Funcall f = new Funcall(Func.compound);
		f.args = cols;
		return f;
	}
	
	public static String compoundVal(String ... vi) {
		return join("\n", "\\\\n", vi);
	}
	
	@Override
	public String sql(ISemantext context) throws TransException {
		// 2025-09-19
		// This previous comments is not understandable.
		// function parameters are handled before this AST node handling, making ExprPart's sql available.
		// Test case will fail:
		// select().where(op.ge, Funcall.toDate(Funcall.isnull(synm.jserv_utc, Funcall.toDate(DateFormat.jour0))), Funcall.toDate(ifnull(utc, DateFormat.jour0)))
		// The way of call args preparations is not correct. The final sql is:
		// select ... where datetime('ifnull(optime, datetime('1911-10-10'))') >= datetime('1911-10-10')
		// Should iterate the AST while serializing the statement.

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
			return sqlDatetime(context, args);
		else if (func == Func.extFile)
			return sqlExtFile(context, args);
		else if (func == Func.refile)
			return sqlRefile(context, args);
		else if (func == Func.concat)
			return sqlConcat(context, args);
		else if (func == Func.add)
			return sqlAdd(context, args);
		else if (func == Func.minus)
			return sqlMinus(context, args);
		else if (func == Func.mul)
			return sqlMul(context, args);
		else if (func == Func.div)
			return sqlDiv(context, args);
		
		else if (func == Func.subStr)
			return sqlSubstr(context, args);
		else if (func == Func.compound)
			return sqlCompound(context, args);
		else
			try {
				return dbSame(context, args);
			} catch (TransException e) {
				e.printStackTrace();
				return null;
			}
	}

	protected String sqlAdd(ISemantext context, String[] args) {
		return Stream.of(args).collect(Collectors.joining(" + ", "(", ")"));
	}

	protected String sqlMinus(ISemantext context, String[] args) {
		return Stream.of(args).collect(Collectors.joining(" - ", "(", ")"));
	}

	protected String sqlMul(ISemantext context, String[] args) {
		return Stream.of(args).collect(Collectors.joining(" * ", "(", ")"));
	}

	protected String sqlDiv(ISemantext context, String[] args) {
		return Stream.of(args).collect(Collectors.joining(" / ", "(", ")"));
	}
	
	protected String sqlSubstr(ISemantext context, String[] args) throws TransException {
		dbtype dt = context.dbtype();
		if (dt == dbtype.sqlite || dt == dbtype.oracle)
			return f_funcall("substr", args);
		else if (dt == dbtype.mysql || dt == dbtype.ms2k)
			return f_funcall("substring", args);
		else
			throw new TransException ("Funcall#subStr(): substr() are not implemented for db type: %s", dt.name());
	}

	protected String sqlCompound(ISemantext context, String[] args) throws TransException {
		return sqlConcat(context, Stream.concat(
				Stream.of(args[0]),
				Stream.of(args).skip(1)
				.filter(v -> !isblank(v))
				.map(c -> {
					Colname colname = Colname.parseFullname(c);
					return new Object[] { "'\n'", ifnull(colname, c) };
				})
				.flatMap(e -> Stream.of(e)))
				.toArray());
	}

	/**Get function string that the database can understand, e.g. ["f," "arg1", "arg2"] =&gt; "f(arg1, arg2)".
	 * @param ctx
	 * @return formatted function call
	 * @throws TransException 
	 */
	protected String dbSame(ISemantext ctx, String[] args) throws TransException {
		String f = super.sql(ctx) + "(";
		if (args != null && args.length > 0 && args[0] != null)
			f += args[0];

		for (int i = 1; args != null && i < args.length; i++)
			f += ", " + args[i];

		return f + ")";
	}
	
	/**
	 * Get sql for select the serialized string of the field.
	 * 
	 * <p>ISSUE, without using param args is a sign of wrong design the switch in {@link #sql(ISemantext)}?</p>
	 * @param context
	 * @param args
	 * @return
	 * @throws TransException 
	 */
	private String sqlRefile(ISemantext context, Object[] args) throws TransException {
		return ((AnDbField) this.args[0]).sql(context);
	}

	/**
	 * What the handler is doing:<br>
	 * 1. read file from the file named by args[0]<br>
	 * 2. set readed contents to the current row of contxt, with {@link ISemantext#setRs(String, String)}
	 * @param context semantext
	 * @param args <br>[0] relative filepath (replacing contents),
	 * <br>[1] select elem alias (to be replaced)
	 * @return sql for the SelectElem, a.k.a. args[0]
	 */
	private String sqlExtFile(ISemantext context, String[] args) {
		if (args == null || args.length != 1 || isblank(args[0], "'\\s*'"))
			Utils.warn("Function extFile() only accept 1 arguments. (And do not confused with class ExtFile)");
		else {
			if (isblank(resultAlias)) {
				String ss[] = split(args[0], "\\.");
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
							if (!isblank(fn, "\\.", "\\*")) {
								// 2025-07-05
								// fn = EnvPath.decodeUri(stx.containerRoot(), fn);
								// fn = ExtFilePaths.decodeUri(stx.containerRoot(), fn);
								fn = ExtFilePaths.decodeUriPath(fn);

								Path f = Paths.get(fn);
								if (Files.exists(f) && !Files.isDirectory(f)) {
									try {
										DocLocks.reading(f);
										byte[] fi = Files.readAllBytes(f);
										row.set(c, AESHelper.encode64(fi));
									} finally { DocLocks.readed(f); }
								}
								else {
									Utils.warn("Funcal (extFile) onSelected postOP(): Can't find file:\n%s", fn);
									row.set(c, "File not Found: " + row.get(c)); // no absolute path - error message for client
								}
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
		}
		return args[0];
	}
	
	public void selectElemAlias(Alias alias) {
		this.resultAlias = alias;
	}

	protected static String sqlConcat(ISemantext ctx, Object[] args) throws TransException {
		dbtype dt = ctx.dbtype();
		if (dt == dbtype.sqlite || dt == dbtype.oracle)
			return Stream.of(args).map(v -> v.toString()).collect(Collectors.joining(" || "));
		else if (dt == dbtype.mysql || dt == dbtype.ms2k)
			return Stream.of(args).map(v -> v.toString()).collect(Collectors.joining("concat(", ", ", ")"));
		else
			throw new TransException ("Funcall#sqlConcat(): concat() are not implemented for db type: %s", dt.name());
	}

	/**<p>Convert string value to datatiem.</p>
	 * str_to_date(str-val, '%Y-%m-%d %H:%i:%s')<br>
	 * datetime('1866-12-12 14:12:12')<br>
	 * CONVERT(datetime, '2009/07/16 08:28:01', 120)<br>
	 * TO_DATE('2012-07-18 13:27:18', 'YYYY-MM-DD HH24:MI:SS')
	 * @param ctx
	 * @param args
	 * @return see {@link #sqlDatetime(ISemantext, String)}
	 */
	protected static String sqlDatetime(ISemantext ctx, String[] args) {
		return sqlDatetime(ctx, args[0]);
	}

	/**<p>Convert string value to datatiem.</p>
	 * str_to_date(str-val, '%Y-%m-%d %H:%i:%s')<br>
	 * datetime('1866-12-12 14:12:12')<br>
	 * CONVERT(datetime, '2009/07/16 08:28:01', 120)<br>
	 * TO_DATE('2012-07-18 13:27:18', 'YYYY-MM-DD HH24:MI:SS')
	 * @param ctx
	 * @param str the date string
	 * @return the correct sql snippet
	 */
	protected static String sqlDatetime(ISemantext ctx, String str) {
		dbtype dt = ctx.dbtype();
		if (dt == dbtype.mysql)
			return String.format("str_to_date('%s', '%Y-%m-%d %H:%i:%s')", str);
		else  if (dt == dbtype.sqlite)
			return String.format("datetime('%s')", str);
		else if (dt == dbtype.ms2k)
			return String.format("convert(datatime, '%s', 120)", str);
		else if (dt == dbtype.oracle)
			return String.format("to_date('%s', 'YYYY-MM-DD HH24:MI:SS')", str);
		else {
			Utils.warn("Funcall#sql2datetime(): Using '%s' for unknown db type: %s", str, dt.name());
			return "'" + str + "'";
		}
	}

	protected static String sqlIfNullElse(ISemantext context, String[] args) throws TransException {
		dbtype dt = context.dbtype();
		if (dt == dbtype.mysql)
			return String.format("if(%s is null, %s, %s)",
					args[0], args[1], args[2]);
		else  if (dt == dbtype.sqlite)
			return String.format("case when %s is null then %s else %s end",
					args[0], args[1], args[2]);
			// return sqls(context, "case when",  args[0], "is null then", args[1], "else", args[2], "end");
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

//	private static String sqls(ISemantext ctx, Object... exprs) {
//		return LangExt.isNull(exprs) ? "" :
//			Stream.of(exprs)
//				.filter(v -> v != null)
//				.map((Object v) -> {
//					try {
//						return v instanceof AbsPart ? ((AbsPart) v).sql(ctx) : v instanceof String ? (String)v : v.toString();
//					} catch (TransException e) {
//						e.printStackTrace();
//						return v.toString();
//					}})
//				.collect(Collectors.joining(" "));	
//	}

	protected static String sqlIfElse(ISemantext context, String exp, String then, String otherwise) {
		return sqlIfElse(context, new String[] {exp, then, otherwise});
	}

	protected static String sqlIfElse(ISemantext context, Object[] args) {
		dbtype dt = context.dbtype();
		if (dt == dbtype.sqlite || dt == dbtype.ms2k || dt == dbtype.oracle)
			return String.format("case when %s then %s else %s end", args[0], args[1], args[2]);
			// return sqls(context, "case when", args[0], "then", args[1], "else", args[2], "end");
		else if (dt == dbtype.mysql)
			return String.format("if(%s, %s, %s)", args[0], args[1], args[2]);
		else {
			Utils.warn("Funcall#sqlIfelse(): Using is(a is null, b, c) for unknown db type: %s", dt.name());
			return String.format("case when %s then %s else %s end", args[0], args[1], args[2]);
		}
	}

	protected static String[] argsql(Object[] args, ISemantext context) throws TransException {
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

	protected static String sqlIfnull(ISemantext context, String... args) throws TransException {
		if (args == null || args.length != 2)
			throw new TransException("Arugments are invalid.");
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

	protected static String sqlNow(ISemantext context, String[] args) {
		dbtype dt = context == null ? null : context.dbtype();
		if (dt == dbtype.mysql)
			return "now()";
		else  if (dt == dbtype.sqlite)
			return sqliteUseLocaltime ? "datetime('now', 'localtime')" : "datetime('now')";
		else if (dt == dbtype.ms2k)
			return ms2kUseUTCtime ? "getutcdate()" : "getdate()";
		else if (dt == dbtype.oracle)
			return "sysdate";
		else {
			String s = DateFormat.formatime_utc(new Date());
			Utils.warn("Formating now() for unknown db type: %s as %s", dt.name(), s);
			return "'" + s + "'";
		}
	}

	/**
	 * Create a function decoding null value,
	 * e.g. for oracle: decode (colElem, null, ifTrue, orElse).
	 * 
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

	/**
	 * Create a function decoding value, e.g. for oracle: decode (colElem, logic, ifTrue, orElse).
	 * @param logic the boolean expression
	 * @param ifTrue
	 * @param orElse
	 * @return Funcall instance will generating sql if-else
	 */
	public static Funcall ifElse(Predicate logic, Object ifTrue, Object orElse) {
		Funcall f = new Funcall(Func.ifElse);
		f.args = new Object[] {logic, ifTrue, orElse};
		return f;
	}

	/**
	 * @see #ifElse(Predicate, Object, Object)
	 * @param logic
	 * @param ifTrue
	 * @param orElse
	 * @return Funcall instance will generating sql if-else
	 */
	public static Funcall ifElse(String logic, Object ifTrue, Object orElse) {
		Funcall f = new Funcall(Func.ifElse);
		f.args = new Object[] {logic, ifTrue, orElse};
		return f;
	}

	/**
	 * Create a funcall, generating sql for<br>
	 * <a href='https://dev.mysql.com/doc/refman/5.5/en/date-and-time-functions.html#function_str-to-date'>mysq:</a><br>
	 * str_to_date(str-val, '%Y-%m-%d %H:%i:%s')<br>
	 * <a href='https://www.sqlite.org/lang_datefunc.html'>sqlite</a><br>
	 * datetime('1866-12-12 14:12:12')<br>
	 * <a href='https://stackoverflow.com/questions/1135746/sql-server-convert-string-to-datetime'>ms sql 2k</a><br>
	 * CONVERT(datetime, '2009/07/16 08:28:01', 120)<br>
	 * <a href='www.sqlines.com/oracle-to-sql-server/to_date'>oracle</a><br>
	 * TO_DATE('2012-07-18 13:27:18', 'YYYY-MM-DD HH24:MI:SS')
	 * 
	 * <p>How to use</p><pre>
	 	// req: parsed jmsg body, e.g. AnsonQueryReq.
		String conn = Connects.uri2conn(req.uri());
		Query q = st
			.select("polls", "p")
			.col(Funcall.toDate(Connects.driverType(conn), "p.optime"), "pdate")
	 * </pre>
	 * 
	 * @param date
	 * @return create a Funcal
	 */
	public static Funcall toDate(String date) {
		Funcall f = new Funcall(Func.datetime);
		f.args = new Object[] {date};
		return f;
	}

	public static Funcall toDate(ExprPart expr) {
		Funcall f = new Funcall(Func.datetime);
		f.args = new Object[] {expr};
		return f;
	}
	
	/**
	 * Concatenate column names.
	 * @param to
	 * @param with
	 * @return function expression, e.g. in sqlite, to || with[0] || with[1] ... 
	 */
	public static Funcall concat(String to, String... with) {
		Funcall f = new Funcall(Func.concat);
		
		f.args = Stream
				.concat(Stream.of(to), Stream.of(with))
				.map(c -> {
					Colname colname = Colname.parseFullname(c);
					return ifnull(colname, c);  
				})
				.toArray();
		return f;
	}

	/**
	 * Concatenate constant strings.
	 * @param v
	 * @param with
	 * @return function expression, e.g. in sqlite, 'v' || 'with[0]' || 'with[1]' ... 
	 * @since 1.4.40
	 */
	public static Funcall concatstr(String v, Object... with) {
		Funcall f = new Funcall(Func.concat);
		
		f.args = Stream
				.concat(Stream.of(v), Stream.of(with))
				.filter(c -> !isblank(c))
				.map(c -> {
					return c instanceof Resulving ? ((Resulving) c).asConstr()
						: c instanceof AbsPart ? c
						: constr(c.toString());
				})
				.toArray();
		return f;
	}
}
