package io.odysz.transact.sql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.common.LangExt;
import io.odysz.common.Utils;
import io.odysz.common.dbtype;
import io.odysz.semantics.ISemantext;
import io.odysz.semantics.SemanticObject;
import io.odysz.semantics.meta.TableMeta;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.sql.parts.insert.ColumnList;
import io.odysz.transact.sql.parts.insert.InsertValues;
import io.odysz.transact.sql.parts.insert.InsertValuesOrcl;
import io.odysz.transact.x.TransException;

/**
 * sql: insert into tabl(...) values(...) / select ...
 * 
 * @author ody
 *
 */
public class Insert extends Statement<Insert> {

	/**[col-name, col-index] */
	protected Map<String, Integer> insertCols;
	
	protected Query selectValues;
	/**list[ list[Object[n, v], ... ], ... ] */
	protected List<ArrayList<Object[]>> valuesNv;
	
	/**
	 * Current row's nv.<br>
	 * 
	 * TODO let's deprecate this - all new nv are appended to last of valuesNv.
	 */
	private ArrayList<Object[]> currentRowNv;

	/**
	 * Insert statement intialization, not come with a post operation for committing SQL.
	 * So don't confused with DATranscxt#Insert(String tabl, IUser usr), which will instert into 
	 * DB when calling ins().
	 * 
	 * @param transc
	 * @param tabl
	 */
	protected Insert(Transcxt transc, String tabl) {
		super(transc, tabl, null);
	}

	@Override
	public Insert nv(String n, AbsPart v) {
		if (currentRowNv == null)
			currentRowNv = new ArrayList<Object[]>();
		
		// column names
		if (insertCols == null)
			insertCols = new HashMap<String, Integer>();
		if (!insertCols.containsKey(n)) {
			insertCols.put(n, insertCols.size());
			currentRowNv.add(new Object[] {n, v});
		}
		else {
			Utils.warn("Insert.nv(): n-v (%s - %s) already exists. Duplicated rows? If using nv(), don't use cols() and value(); If using cols(), don't use nv().",
					n, v);
		}
		return this;
	}

	public Insert nv(String n, ArrayList<String> lst) {
		nv(n, lst == null ? "null" : String.join(",", lst));
		return this;
	}

	/**
	 * Instead of using {@link #nv(String, AbsPart)} to setup columns, sometimes we use insert tabl(col) select ...<br>
	 * This method is used to setup cols in the latter case.
	 * 
	 * @param col0
	 * @param cols
	 * @return this
	 * @throws TransException
	 */
	public Insert cols(String col0, String... cols) throws TransException {
		if (valuesNv != null && valuesNv.size() > 0)
			throw new TransException("cols() must been called before any rows' value been added (calling values())");
			
		if (insertCols == null)
			insertCols = new HashMap<String, Integer>();

		// initial columns size
		int size0 = insertCols.size();

		insertCols.put(col0, size0);
		if (cols != null)
			for (int c = size0; c < cols.length; c++)
				insertCols.put(cols[c], c + 1);
		return this;
	}

	public Insert cols(String[] cols) throws TransException {
		if (cols != null) {
			for (String c : cols)
				cols(c);
		}
		return this;
	}

	/**
	 * Append values (a row) after cols been set (call {@link Insert#cols(String, String...) cols(...)} first):<br>
	 * [[col1, val1], [col2, val2], ...]
	 * 
	 * @param val pairs of col-val
	 * @return this
	 * @throws TransException
	 */
	public Insert value(ArrayList<Object[]> val) throws TransException {
		if (val == null)
			return this;
		if (insertCols == null)
			throw new TransException("Insert#value(): value() or values() can't been used before cols() has been called.");
		if (insertCols != null && insertCols.size() > 0
				&& selectValues != null && selectValues.size() > 0)
			throw new TransException("Semantic-Transact only support one of insert-select or insert-values.");

		if (insertCols != null && insertCols.size() < val.size())
			throw new TransException("columns' number is less than rows field count.");

		if (valuesNv == null)
			valuesNv = new ArrayList<ArrayList<Object[]>>();
		
		if (currentRowNv != null && currentRowNv.size() > 0) {
			// append current row, then append new vals 
			valuesNv.add(currentRowNv);
			currentRowNv = null;
		}
		
		boolean notNull = false;

		// remember cols to be appended
		Set<String> appendings = null;
		if (val.size() < insertCols.size())
			appendings = new HashSet<String>(insertCols.keySet());

		// TableMeta mt = transc.tableMeta(transc.basictx.connId(), mainTabl.name());
		TableMeta mt = transc.tableMeta(mainTabl.name());
		for (int i = 0; i < val.size(); i++) {
			Object[] nv = val.get(i);
			
			if (nv == null || nv.length == 0)
				continue;
			else if (nv.length >= 2 && LangExt.isblank(nv[0]) && nv[1] == null) {
				val.set(i, null);
				continue;
			}
			else if (nv != null && (nv.length != 2 || LangExt.isblank(nv[0]) && nv[1] != null))
				throw new TransException("Invalid nv: [%s, %s]",
						nv != null ? nv[0] : "",
						nv != null && nv.length > 0 ? nv[1] : "");

			// now col already known, only care about value

			if (nv != null && nv[1] instanceof AbsPart) {
				if (appendings != null)
					appendings.remove(nv[0]);
				notNull = true;
				continue;
			}

			if (nv != null && nv[1] != null)
				notNull = true;

			String n = nv[0].toString();
			String v = nv[1] == null ? null : nv[1].toString();

			// v must be String constant or number, etc.
			val.set(i, new Object[] {n, composeVal(v, mt, n)});
			if (appendings != null)
				appendings.remove(n);
		}

		// append null value to know cols
		if (notNull) {
			if (val.size() < insertCols.size()) 
				for (String appcol : appendings) {
					val.add(new Object[] {appcol, null});
				}
			valuesNv.add(val);
		}

		return this;
	}

	@SuppressWarnings("unchecked")
	public Insert values(ArrayList<ArrayList<Object[]>> arrayList) throws TransException {
		if (arrayList != null)
			for (ArrayList<?> val : arrayList)
				value((ArrayList<Object[]>)val);
		return this;
	}

	/**select clause in sql: insert into tabl() <b>select ...</b>
	 * @param values
	 * @return this
	 * @throws TransException
	 */
	public Insert select(Query values) throws TransException {
		if (valuesNv != null && valuesNv.size() > 0)
			throw new TransException("Semantic-Transact only support one of insert-select or insert-values.");
		selectValues = values;
		return this;
	}

	/**
	 * <p>Solution for UPSERT.</p>
	 * <h5>NOTE:</h5>
	 * <p>UPSERT is not a standard SQL syntax (April 2024), and currently only sql for
	 *  Sqlite3 are verified. For MS Sql Server, use * {@link #onDuplicate(Query)},
	 *  and for Oracle, not implemented yet. For MySql, not verified. Open an issue at
	 *  <a href="https://github.com/odys-z/semantic-transact/issues">Github</a>
	 * if the features are needed.</p>
	 * <h5>References:</h5>
	 * <ul>
	 * <li><a href="https://sqlite.org/lang_upsert.html">Sqlite UPSERT</a><br>
	 * INSERT INTO ... VALUES('jovial') ON CONFLICT(word) DO UPDATE SET count=count+1</li>
	 * <li><a href="https://dev.mysql.com/doc/refman/8.0/en/insert-on-duplicate.html">
	 * 15.2.7.2 INSERT ... ON DUPLICATE KEY UPDATE Statement, MySql Documentation</a> and
	 * <a href="https://blog.devart.com/mysql-upsert.html">
	 * MySQL UPSERT: Comprehensive Examples and Use Cases</a><br>
	 * INSERT INTO table_name (column1, column2, ...) VALUES (value1, value2, ...)
	 * ON DUPLICATE KEY UPDATE column1 = value1, column2 = value2, ...;
	 * </li>
	 * <li><a href="https://stackoverflow.com/a/27989832/7362888">
	 * StackOverflow: How to upsert (update or insert) in SQL Server 2005</a><br>
	 * IF NOT EXISTS (SELECT * FROM dbo.Employee WHERE ID = @SomeID)
	 * INSERT INTO dbo.Employee(Col1, ..., ColN)
	 * VALUES(Val1, .., ValN)
	 * ELSE UPDATE dbo.Employee
	 * SET Col1 = Val1, Col2 = Val2, ...., ColN = ValN
	 * WHERE ID = @SomeID
	 * </li>
	 * <li><a href="https://docs.oracle.com/en/database/other-databases/nosql-database/23.3/sqlreferencefornosql/upsert-statement.html">
	 * Upsert statement, Oracle Help Center</a><br>
	 * <pre>upsert_statement ::=
	 * [variable_declaration]
	 * UPSERT INTO table_name 
	 * [AS] table_alias]
	 * ["(" id ("," id)* ")"]
	 * VALUES "(" insert_clause ("," insert_clause)* ")"
	 * [SET TTL ttl_clause ]
	 * [returning_clause]</pre>
	 * </li>
	 * </ul>
	 * @param unvs updating name-value pairs, if null, use all values set by
	 * {@link #nv(String, AbsPart) nv()} or {@link Update#nvs(ArrayList) nvs()} for insert statement.
	 * @return this
	 */
	public Insert onDuplicate(ArrayList<Object[]> unvs) {
		return this;
	}

	/**
	 * For MS Sql Server only. Not implemented.
	 * @see #onDuplicate(ArrayList)
	 * @param select
	 * @return this
	 */
	public Insert onDuplicate(Query select) {
		return this;
	}

	/**sql: insert into tabl(...) values(...) / select ...
	 * @see io.odysz.transact.sql.parts.AbsPart#sql(ISemantext)
	 */
	@Override
	public String sql(ISemantext sctx) {
		boolean hasVals = valuesNv != null 
				&& valuesNv.size() > 0
				&& valuesNv.get(0) != null
				&& valuesNv.get(0).size() > 0;
		if (!hasVals && selectValues == null) return "";

		// insert into tabl(...) values(...) / select ...
		Stream<String> s = Stream.concat(
			// insert into tabl(...)
			Stream.of(new ExprPart("insert into"), mainTabl, mainAlias,
					// (...)
					new ColumnList(insertCols)
			   // values(...) / select ...
			), Stream.concat(
				// values (...)
				// whether 'values()' appears or not is the same as value valuesNv
				Stream.of(// new ExprPart("values"),
						// 'v1', 'v2', ...)
					sctx != null && sctx.dbtype() == dbtype.oracle ?
							new InsertValuesOrcl(mainTabl.name(), insertCols, valuesNv) :
							new InsertValues(mainTabl.name(), insertCols, valuesNv)
				).filter(w -> hasVals),
				// select ...
				Stream.of(selectValues).filter(w -> selectValues != null))
			).map(m -> {
				try {
					return m == null ? "" : m.sql(sctx);
				} catch (TransException e) {
					e.printStackTrace();
					return "";
				}
			});

		return s.collect(Collectors.joining(" "));
	}

	public Map<String, Integer> getColumns() { return insertCols; }

	/**<p>Use this method to do post operation, a. k. a. for {@link Insert} to get inserted new Ids.</p>
	 * <p>This method must called after the post operation (lambda expression) been initialized.</p>
	 * <h3>Why rs() must been used after setting lambda expression?</h3>
	 * <p>As Query generated sql, it should be used to get result set - execute the SQL
	 * select statement.</p>
	 * <p>But semantic-transact is designed not to depend on any database accessing layer,
	 * so Query shouldn't access database, although in many case it's very convenient to
	 * get result set directly.</p>
	 * <p>This requirements is handled in JDK1.8 lambda expression style:<br>
	 * User override method {@link Transcxt#select(String, String...)}, which add a lambda
	 * expression as the post operation for handling execution of the generated SQL statement,
	 * rs() will call the lambda and return the result set returned by this operation.</p>
	 * <h3>Where is the sample code?</h3>
	 * <p>To see how to extend {@link Transcxt}, see DATranscxt in project semantic-DA.<br>
	 * To see how to use this method, see io.odysz.semantic.DASemantextTest in project sematic-DA.</p>
	 * <p><b>Note:</b><br>This method shouldn't been used the same time with {@link #commit(ArrayList, io.odysz.semantics.IUser...)}
	 * because the inserting values will be handled / smirred in both methods.</p>
	 * @param ctx
	 * @return results for resolving FK, etc.
	 * @throws TransException
	 * @throws SQLException
	 */
	public Object ins(ISemantext ctx) throws TransException, SQLException {
		if (postOp != null) {
			ArrayList<String> sqls = new ArrayList<String>(); 
			commit(ctx, sqls);
			return postOp.onCommitOk(ctx, sqls);
		}
		return null;
	}
	
	@Override
	public Insert commit(ISemantext cxt, ArrayList<String> sqls) throws TransException {
		List<ArrayList<Object[]>> values = prepareNv(cxt);

		if (cxt != null)
			cxt.onInsert(this, mainTabl.name(), values);

		Insert ins = super.commit(cxt, sqls);
		
		if (cxt != null && values != null)
			for (ArrayList<Object[]> row : values)
				cxt.onPost(this, mainTabl.name(), row, sqls);

		return ins;
	}

	List<ArrayList<Object[]>> prepareNv(ISemantext ctx) {
		if (currentRowNv != null && currentRowNv.size() > 0) {
			if (valuesNv == null) {
				valuesNv = new ArrayList<ArrayList<Object[]>>(1);
			}
			valuesNv.add(currentRowNv);
			currentRowNv = null;
		}
		return valuesNv;
	}

	/**Add multi del insert update for children table<br>
	 * - a special frequently used case of CRUD, provided as a shortcut of API.
	 * @param multireq {dels: [condition-strings[]], ins: [nvs[]]}
	 * @param stcx 
	 * @throws TransException 
	 */
	public void postChildren(SemanticObject multireq, Transcxt stcx) throws TransException {
		Delete del = (Delete) multireq.get("dels");
		if (del != null) {
			if (postate == null)
				postate = new ArrayList<Statement<?>>();
			postate.add(del);
		}

		@SuppressWarnings("unchecked")
		ArrayList<Insert> ins = (ArrayList<Insert>) multireq.get("insert");
		if (ins != null) {
			if (postate == null)
				postate = new ArrayList<Statement<?>>();
			for (Insert i : ins)
				postate.add(i);
		}
	}
}
