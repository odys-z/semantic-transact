package io.odysz.transact.sql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.common.Utils;
import io.odysz.semantics.ISemantext;
import io.odysz.semantics.SemanticObject;
import io.odysz.semantics.x.SemanticException;
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
	private Map<String,Integer> insertCols;
	
	private Query selectValues;
	/**[ list[Object[n, v], ... ], ... ] */
	private List<ArrayList<Object[]>> valuesNv;
	private ArrayList<Object[]> currentRowNv;

	Insert(Transcxt transc, String tabl) {
		super(transc, tabl, null);
	}

	public Insert nv(String n, Object v) {
		if (currentRowNv == null)
			currentRowNv = new ArrayList<Object[]>();
		currentRowNv.add(new String[] {n, String.valueOf(v)});
		
		// column names
		if (insertCols == null)
			insertCols = new HashMap<String, Integer>();
		if (!insertCols.containsKey(n))
			insertCols.put(n, insertCols.size());
		else Utils.warn("Column's (%s) value already exists, old value replaced by new value (%s)",
				n, v);
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

	/**Append values after cols been set.
	 * @param val
	 * @return this
	 * @throws TransException
	 */
	public Insert value(ArrayList<Object[]> val) throws TransException {
		if (val == null)
			return this;
		if (insertCols != null && insertCols.size() > 0
				&& selectValues != null && selectValues.size() > 0)
			throw new TransException("Semantic-Transact only support one of insert-select or insert-values.");

		if (insertCols != null && insertCols.size() != val.size())
			throw new TransException("columns' number didn't match rows field count.");

		if (valuesNv == null)
			valuesNv = new ArrayList<ArrayList<Object[]>>(val.size());
		
		if (currentRowNv != null && currentRowNv.size() > 0) {
			// append current row, then append new vals 
			valuesNv.add(currentRowNv);
			currentRowNv = null;
		}
		valuesNv.add(val);
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
	
//	/**sql: insert into tabl(...) values(...) / select ...
//	 * @see io.odysz.transact.sql.parts.AbsPart#sql(ISemantext)
//	 */
//	@Override
//	public String sql(ISemantext sctx) {
////		if (currentRowNv != null && currentRowNv.size() > 0) {
////			if (valuesNv == null) {
////				valuesNv = new ArrayList<ArrayList<Object[]>>(1);
////			}
////			valuesNv.add(currentRowNv);
////		}
//
//		boolean hasValuesNv = valuesNv != null && valuesNv.size() > 0;
//
////		if (sctx != null)
////			sctx.onInsert(this, mainTabl, valuesNv);
//		// FIXME
//		// insert into a_role_funcs  (funcId, roleId) values ('1A', '0101') ('03', '0101') ('0301', '0101') ('0302', '0101') ('04', '0101') ('0401', '0101')... ('0909', '0101')
//		// insert into tabl(...) values(...) / select ...
//		Stream<String> s = Stream.concat(
//			// insert into tabl(...)
//			Stream.of(new ExprPart("insert into"), new ExprPart(mainTabl), new ExprPart(mainAlias),
//					// (...)
//					new ColumnList(insertCols)
//			   // values(...) / select ...
//			), Stream.concat(
//				// FIXME how to join multiple values? (...), (...), ...
//				// values (...)
//				// whether 'values()' appears or not is the same as value valuesNv
//				Stream.concat(Stream.of(new ExprPart("values")),
//						// 'v1', 'v2', ...)
////						Stream.concat(
//								Optional.ofNullable(valuesNv).orElse(Collections.emptyList())
//									.stream().map(row -> getValue(sctx, row, insertCols))
////									, Stream.of(new ExprPart(")"))
////						)
//				).filter(w -> hasValuesNv),
//				// select ...
//				Stream.of(selectValues).filter(w -> selectValues != null))
//			).map(m -> {
//				try {
//					return m.sql(sctx);
//				} catch (TransException e) {
//					e.printStackTrace();
//					return "";
//				}
//			});
//
//		return s.collect(Collectors.joining(" "));
//	}
	
	/**sql: insert into tabl(...) values(...) / select ...
	 * @see io.odysz.transact.sql.parts.AbsPart#sql(ISemantext)
	 */
	@Override
	public String sql(ISemantext sctx) {
		boolean hasValuesNv = valuesNv != null && valuesNv.size() > 0;

		// FIXME
		// insert into a_role_funcs  (funcId, roleId) values ('1A', '0101') ('03', '0101') ('0301', '0101') ('0302', '0101') ('04', '0101') ('0401', '0101')... ('0909', '0101')
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
//						Optional.ofNullable(valuesNv).orElse(Collections.emptyList())
//								.stream().map(row -> getValue(sctx, row, insertCols))
					new InsertValues(insertCols, valuesNv)
				).filter(w -> hasValuesNv),
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

	/**Create ValueList from row. 
	 * @param row
	 * @param colIdx
	 * @return
	private ValueList getValue(ISemantext sctx, ArrayList<Object[]> row, Map<String, Integer> colIdx) {
		if (row == null)
			return null;

		ValueList vs = new ValueList(row.size());
		int idx = -1;
		for (Object[] nv : row) {
			if (nv == null) continue;

			if (colIdx == null)
				idx++;
			else if (colIdx.containsKey(nv[0]))
					idx = colIdx.get(nv[0]);
			else {
				Utils.warn("Can't find column index for col %s %s", nv[0], nv[1]);
				continue;
			}
			try {
				if (nv[1] instanceof String)
					// vs.constv(idx, (String) nv[1]);
					vs.constv(idx, sctx == null ? (String)nv[1]
												: (String) sctx.resulvedVal((String)nv[1]));
				else
					vs.v(idx, (AbsPart) nv[1]);
			} catch (TransException e) {
				e.printStackTrace();
			}
		}

		return vs;
	}
	 */

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
			return postOp.op(ctx, sqls);
		}
		return null;
	}
	
	@Override
	public Insert commit(ISemantext cxt, ArrayList<String> sqls) throws TransException {
		// prepare semantics like auto-pk
		prepare(cxt);

		// resolve semantics like fk-ins referring to auto-pk
		// FIXME should move ISemantext.onInsert() to Statement.resolveSemantics()?
		if (cxt != null) {
			cxt.onInsert(this, mainTabl, valuesNv);
			if (postate != null)
				for (Statement<?> pst : postate)
					if (pst instanceof Insert)
						cxt.onInsert((Insert)pst, pst.mainTabl, ((Insert)pst).valuesNv);
		}

		sqls.add(sql(cxt));
		if (postate != null)
			for (Statement<?> pst : postate)
				// pst.commit(cxt, sqls);
				sqls.add(pst.sql(cxt));
		return this;
	}		


	void prepare(ISemantext ctx) {
		if (currentRowNv != null && currentRowNv.size() > 0) {
			if (valuesNv == null) {
				valuesNv = new ArrayList<ArrayList<Object[]>>(1);
			}
			valuesNv.add(currentRowNv);
			currentRowNv = null;
		}

		if (ctx != null)
			ctx.onPrepare(this, mainTabl, valuesNv);
		
		if (postate != null)
			for (Statement<?> pst : postate)
				pst.prepare(ctx);
	}

	/**FIXME merge this to some where parsing JMessage<br>
	 * Add multi del insert update for children table<br>
	 * - a special frequently used case of CRUD, provided as a shortcut of API.
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
