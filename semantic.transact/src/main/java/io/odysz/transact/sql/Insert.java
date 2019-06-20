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
import io.odysz.semantics.ISemantext;
import io.odysz.semantics.SemanticObject;
import io.odysz.semantics.meta.TableMeta;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.sql.parts.insert.ColumnList;
import io.odysz.transact.sql.parts.insert.InsertValues;
import io.odysz.transact.x.TransException;

/**sql: insert into tabl(...) values(...) / select ...
 * @author ody
 *
 */
public class Insert extends Statement<Insert> {

	/**[col-name, col-index] */
	private Map<String, Integer> insertCols;
	
	private Query selectValues;
	/**[ list[Object[n, v], ... ], ... ] */
	private List<ArrayList<Object[]>> valuesNv;
	
	/**current row's nv.<br>
	 * TODO let's depcate this - all new nv are appended to last of valuesNv */
	private ArrayList<Object[]> currentRowNv;

	Insert(Transcxt transc, String tabl) {
		super(transc, tabl, null);
	}

	@Override
	public Insert nv(String n, AbsPart v) throws TransException {
		if (currentRowNv == null)
			currentRowNv = new ArrayList<Object[]>();
		//  currentRowNv.add(new Object[] {n, v});
		
		// column names
		if (insertCols == null)
			insertCols = new HashMap<String, Integer>();
		if (!insertCols.containsKey(n)) {
			insertCols.put(n, insertCols.size());
			currentRowNv.add(new Object[] {n, v});
		}
		else {
			// replace the old one
//			currentRowNv.get(insertCols.get(n))[1] = v;
//			if (verbose) Utils.warn(
//				"Insert.nv(%1$s, %2$s): Column's value already exists, old value replaced by new value (%1$s = %2$s)",
//				n, v);
			throw new TransException("If using nv(), don't use cols() and value(); If using cols(), don't use nv().");
		}
		return this;
	}

	/**Instead of using {@link #nv(String, Object)} to setup columns, sometimes we use insert tabl(col) select ...<br>
	 * This method is used to setup cols in the latter case.
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

	/**Append values (a row) after cols been set:<br>
	 * [[col1, val1], [col2, val2], ...]
	 * @param val
	 * @return this
	 * @throws TransException
	 */
	public Insert value(ArrayList<Object[]> val) throws TransException {
		if (val == null)
			return this;
		if (insertCols == null)
			throw new TransException("value() or values() can't been used befor cols() been called.");
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

		// remember cols should be appended
		Set<String> appendings = null;
		if (val.size() < insertCols.size())
			appendings = new HashSet<String>(insertCols.keySet());

		TableMeta mt = transc.tableMeta(mainTabl);
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

//			if (nv[0] == null)
//				if (!LangExt.isblank(nv[1], "''"))
//					Utils.warn("Insert#values(): Ignoring value for empty column name: %s", nv[1]);
//				else if (nv[1] == null) continue;

			notNull = true;
			String v = (String) nv[1];
			String n = (String) nv[0];

			// v must be String constant or number, etc.
			val.set(i, new Object[] {n, composeVal(v, mt, mainTabl, n)});
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
	public Insert values(ArrayList<ArrayList<?>> arrayList) throws TransException {
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
			Stream.of(new ExprPart("insert into"), new ExprPart(mainTabl), new ExprPart(mainAlias),
					// (...)
					new ColumnList(insertCols)
			   // values(...) / select ...
			), Stream.concat(
				// values (...)
				// whether 'values()' appears or not is the same as value valuesNv
				Stream.of(new ExprPart("values"),
						// 'v1', 'v2', ...)
					new InsertValues(mainTabl, insertCols, valuesNv)
				).filter(w -> hasVals),
				// select ...
				Stream.of(selectValues).filter(w -> selectValues != null))
			).map(m -> {
				try {
					return m.sql(sctx);
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
	 * <p><b>Node:</b>This method shouldn't been used the same time with {@link #commit(ArrayList)}
	 * because the inserting values will be handled / smirred in both methods.</p>
	 * <p>If you can make sure the ISemantext instance provided to Transcxt is clean of data
	 * invention, you can safely use both of these methods. But it's not guaranteed in the
	 * future version.</p>
	 * Also it's not recommended for the performance reason. The sql string is already generated
	 * by {@link #commit(ArrayList, int...)} , don't generate it and travels AST again in this method, 
	 * use it directly.
	 * @param ctx
	 * @return results by resolving FK, etc.
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
	
//	@Override
//	public Insert commit(ISemantext cxt, ArrayList<String> sqls) throws TransException {
//		// prepare semantics like auto-pk
//		prepare(cxt);
//
//		// resolve semantics like fk-ins referring to auto-pk
//		if (cxt != null) {
//			cxt.onInsert(this, mainTabl, valuesNv);
//			if (postate != null)
//				for (Statement<?> pst : postate)
//					if (pst instanceof Insert)
//						cxt.onInsert((Insert)pst, pst.mainTabl, ((Insert)pst).valuesNv);
//		}
//
//		// sqls.add(sql(cxt));
//		// sql() calling onDelete (generating before sentences), must called before "before"
//		String itself = sql(cxt);
//
//		if (before != null)
//			for (Statement<?> bf : before)
//				bf.commit(cxt, sqls);
//
//		sqls.add(itself);
//
//
//		if (postate != null)
//			for (Statement<?> pst : postate)
//				// sqls.add(pst.sql(cxt));
//				pst.commit(cxt, sqls);
//		return this;
//	}		

//	void prepare(ISemantext ctx) {
//		if (currentRowNv != null && currentRowNv.size() > 0) {
//			if (valuesNv == null) {
//				valuesNv = new ArrayList<ArrayList<Object[]>>(1);
//			}
//			valuesNv.add(currentRowNv);
//			currentRowNv = null;
//		}
//
//		if (ctx != null)
//			ctx.onPrepare(this, mainTabl, valuesNv);
//		
//		if (postate != null)
//			for (Statement<?> pst : postate)
//				pst.prepare(ctx);
//	}

	@Override
	public Insert commit(ISemantext cxt, ArrayList<String> sqls) throws TransException {
		List<ArrayList<Object[]>> values = prepareNv(cxt);

		if (cxt != null)
			cxt.onInsert(this, mainTabl, values);

		Insert ins = super.commit(cxt, sqls);
		
		if (cxt != null && values != null)
			for (ArrayList<Object[]> row : values)
				cxt.onPost(this, mainTabl, row, sqls);

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

//		if (ctx != null)
//			ctx.onPrepare(this, mainTabl, valuesNv);
//		
//		if (postate != null)
//			for (Statement<?> pst : postate)
//				pst.prepare(ctx);
	}

	/**Add multi del insert update for children table<br>
	 * - a special frequently used case of CRUD, provided as a shortcut of API.
	 * FIXME merge this to some where parsing JMessage<br>
	 * @param multireq {dels: [condition-strings[]], ins: [nvs[]]}
	 * @param stcx 
	 * @throws SemanticException 
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
