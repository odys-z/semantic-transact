package io.odysz.transact.sql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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
 * @since 1.4.40, this class support where clause, which can be helpful when insert or
 * update a record. (Not the same usage for different DB) 
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
	
	protected Query existsQuery;

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

	public Insert nv(String n, Iterable<String> lst) {
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
	
	/**
	 * @param nvs a row's n-v pairs, e.g. col-A, val-A, col-B, val-B, ...
	 * @return
	 * @throws TransException 
	 */
	public Insert value(Object ... nvs) throws TransException {
		if (nvs != null) {
			ArrayList<Object[]> row = new ArrayList<Object[]>(nvs.length / 2); // FIXME length?
			ArrayList<String> cols = new ArrayList<String>(nvs.length / 2);
			for (int i = 0; i < nvs.length; i += 2) {
				row.add(new Object[] {nvs[i], nvs[i + 1]});
				cols.add((String) nvs[i]);
			}
			cols(cols.toArray(new String[0]));
			return value(row);
		}
		return this;
	}
	
	/**
	 * Insert a row.
	 * {@code Example}
	 * <pre> trb
	 * .insert(entm.tbl, trb.synrobot())
	 * .cols(rs.getFlatColumns0())
	 * .row(rs.getColnames(), rs.index0(pk).getRowById(chuids))
	 * </pre>
	 * <p>Now {@code row} is ready to be inserted into {@code entm.tbl}.</p>
	 * @since 1.4.40
	 * @param colnames
	 * @param row
	 * @return this
	 * @throws TransException
	 */
	public Insert row(HashMap<String, Object[]> colnames, ArrayList<Object> row) throws TransException {
		if (insertCols == null)
			throw new TransException("Insert#row(): must call cols() first to set columns.");

		if (row != null) {
			if (valuesNv == null)
				valuesNv = new ArrayList<ArrayList<Object[]>>();
			
			if (currentRowNv != null && currentRowNv.size() > 0) {
				// append current row, then append new vals 
				valuesNv.add(currentRowNv);
				currentRowNv = null;
			}

			TableMeta mt = transc.tableMeta(mainTabl.name());
			ArrayList<Object[]> nvs = new ArrayList<Object[]>(Collections.nCopies(row.size(), null));
			for (String n : insertCols.keySet()) {
				int i = insertCols.get(n);
				// if (i + 1 != (int)colnames.get(n.toUpperCase())[0])
				if (i + 1 != TableMeta.colx(colnames, n))
					Utils.warnT(new Object() {},
						"Expecting column %s at index [%s], but got %s.",
						// n, i, (int)colnames.get(n.toUpperCase())[0]);
						n, i, TableMeta.colx(colnames, n));
				Object v = row.get(i);
				nvs.set(i, new Object[] {n, composeVal(v, mt, n)});
			}

			valuesNv.add(nvs);
		}
		return this;
	}

	/**
	 * 
	 * @param arrayList rows of array or n-v pairs, [[n, v], ...].
	 * @return this
	 * @throws TransException
	 */
	@SuppressWarnings("unchecked")
	public Insert values(ArrayList<ArrayList<Object[]>> arrayList) throws TransException {
		if (arrayList != null)
			for (ArrayList<?> val : arrayList)
				value((ArrayList<Object[]>)val);
		return this;
	}

	/**
	 * select clause in sql: insert into tabl() <b>select ...</b>
	 * <pre>e. g.
	st.insert("a_role_funcs")
	  .select(st.select("a_functions", "f")
	    .cols("f.funcId", "'admin' roleId", "'c,r,u,d'")
	    .j("a_roles", "r", "r.roleId='%s'", "admin"))
	// insert into a_role_funcs  
	// select f.funcId, 'admin' roleId, 'c,r,u,d' from a_functions f join a_roles r on r.roleId = 'admin'"
	 * </pre>
	 * @see {@link TestTransc#testInsertSelectPostUpdate()}
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
	 * <p>Sql for Oracle:</p>
	 * <pre> update maintbl set ... where exists (select * from maintbl where ...) and and-wheres;
	 * insert into maintbl (email, campaign_id)
	 *   select 'mom@cox.net',100 from dual where not exists
	 *   (select * from maintbl where ...);</pre>
	 * See <a href="https://stackoverflow.com/a/16639922/7362888">
	 * StackOverflow: Oracle insert if not exists statement</a>.
	 * 
	 * <p>Sql for MySql:</p>
	 * <pre> update maintbl set ... where exists (select * from maintbl where ...) and and-wheres;
	 * insert into maintbl (name, address, tele)
	 *   select * from (select 'john', 'doe', '022') as tmp
	 *   where not exists (select name from maintbl where ...);
	 * </pre>
	 * See <a href="https://stackoverflow.com/a/3164741/7362888">
	 * StackOverflow: MySQL: Insert record if not exists in table [duplicate]</a>.
	 * 
	 * <p>Sql for sqlite</p>
	 * <pre> update maintbl set ... where ...(and-wheres) and exists (select 1 from maintbl where ...);
	 * insert into memos(id,text) 
	 *   select 5, 'text to insert'
	 *   where not exists (select 1 from maintbl where ...);</pre>
	 * 
	 * <p>Sql for ms sql server</p>
	 * <pre> update maintbl ...
	 * insert  maintbl (SoftwareName, SoftwareSystemType)
	 *   select  @SoftwareName, @SoftwareType wherenot exists 
	 *   select  1 from    tblSoftwareTitles
	 *   where   softwarename = @softwarename
	 *   and     SoftwareSystemType = @Softwaretype);</pre>
	 *
	 * <h5>NOTE</h5>
	 * <ol>
	 * <li>The Update statement is not generated by this statement. It's user's responsibility to append this
	 * Insert to the Update statement.</li>  
	 * <li>It's users' responsibility to make sure the Select query for updating returns 1 row if exists.</li>
	 * <li>Open an issue if supporting {@code Merge} is necessary. But after tried Upsert by {@link InsertExp}</li>
	 * </ol>
	 * 
	 * <h5>Reference</h5>
	 * A performance experiment: <a href='https://cc.davelozinski.com/sql/fastest-way-to-insert-new-records-where-one-doesnt-already-exist'>
	 * SQL: Fastest way to insert new records where one doesn’t already exist</a>
	 * @param select
	 * @return this
	 */
	public Insert notExists(Query select) {
		existsQuery = select;
		return this;
	}

	/**
	 * sql: insert into tabl(...) values(...) / select ...
	 * 
	 * @see io.odysz.transact.sql.parts.AbsPart#sql(ISemantext)
	 */
	@Override
	public String sql(ISemantext sctx) {
		boolean hasVals = valuesNv != null 
				&& valuesNv.size() > 0
				&& valuesNv.get(0) != null
				&& valuesNv.get(0).size() > 0;
		if (!hasVals && selectValues == null) {
			Utils.warn("[Insert#sql()] Trying to stream a Insert statement without values, table %s, conn %s.",
					this.mainTabl.name(), sctx.connId());
			return "";
		}

		Stream<String> s = Stream.of(
			new ExprPart("insert into"),
			mainTabl, mainAlias,
			// (...)
			new ColumnList(insertCols),
			// values(...)
			hasVals
				? sctx != null && sctx.dbtype() == dbtype.oracle ?
					new InsertValuesOrcl(mainTabl.name(), insertCols, valuesNv) :
					new InsertValues(mainTabl.name(), insertCols, valuesNv)
				: null,
			selectValues,
			
			where == null ? null : new ExprPart("where"),
			where
		).filter(w -> w != null)
		.map(m -> {
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
		else
			Utils.warn("On operation for built sqls. Intend to call subclass' Insert(tbl, user)?");
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
