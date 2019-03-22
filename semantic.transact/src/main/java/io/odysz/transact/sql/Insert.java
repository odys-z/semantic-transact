package io.odysz.transact.sql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.common.Utils;
import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.sql.parts.insert.ColumnList;
import io.odysz.transact.sql.parts.insert.ValueList;
import io.odysz.transact.x.TransException;

/**sql: insert into tabl(...) values(...) / select ...
 * @author ody
 *
 */
public class Insert extends Statement<Insert> {

	/**[col-name, col-index] */
	private Map<String,Integer> insertCols;
	
	/**@deprecated Insert know nothing about semantics. It's handled by Semantics2 and Semantext2*/
	@SuppressWarnings("unused")
	private String pk;

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
		currentRowNv.add(new Object[] {n, v});
		
		// column names
		if (insertCols == null)
			insertCols = new HashMap<String, Integer>();
		if (!insertCols.containsKey(n))
			insertCols.put(n, insertCols.size());
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

//		if (pk == null)
//			this.pk = col0;
		
		// initial columns size
		int size0 = insertCols.size();

		insertCols.put(col0, size0);
		if (cols != null)
			for (int c = size0; c < cols.length; c++)
				insertCols.put(cols[c], c + 1);
		return this;
	}

	public Insert values(ArrayList<Object[]> rowFields) throws TransException {
		if (rowFields == null)
			return this;
		if (insertCols.size() != rowFields.size())
			throw new TransException("columns' number didn't match rows field count.");

		if (selectValues != null)
			throw new TransException("Semantic-Transact only support one of insert-select or insert-values.");

		if (valuesNv == null)
			valuesNv = new ArrayList<ArrayList<Object[]>>(rowFields.size());
		
		if (currentRowNv != null && currentRowNv.size() > 0) {
			// append current row, then append new vals 
			valuesNv.add(currentRowNv);
			currentRowNv = null;
		}
		valuesNv.add(rowFields);
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
//		if (currentRowNv != null && currentRowNv.size() > 0) {
//			if (valuesNv == null) {
//				valuesNv = new ArrayList<ArrayList<Object[]>>(1);
//			}
//			valuesNv.add(currentRowNv);
//		}

		boolean hasValuesNv = valuesNv != null && valuesNv.size() > 0;

//		if (sctx != null)
//			sctx.onInsert(this, mainTabl, valuesNv);
		
		// insert into tabl(...) values(...) / select ...
		Stream<String> s = Stream.concat(
				// insert into tabl(...)
				Stream.of(new ExprPart("insert into"), new ExprPart(mainTabl), new ExprPart(mainAlias), new ColumnList(insertCols)
				// values(...) / select ...
				), Stream.concat(
						// FIXME how to join multiple values? (...), (...), ...
						// values (...)
						Stream.concat(Stream.of(new ExprPart("values (")), // whether 'values()' appears or not is the same as value nvs
									  // 'v1', 'v2', ...)
									  Stream.concat(Optional.ofNullable(valuesNv).orElse(Collections.emptyList())
											  		  		.stream().map(row -> getValue(row, insertCols)),
									  				Stream.of(new ExprPart(")")))
						).filter(w -> hasValuesNv),
						// select ...
						Stream.of(selectValues).filter(w -> selectValues != null))
			).map(m -> m.sql(sctx));

		return s.collect(Collectors.joining(" "));
	}

	/**Create ValueList from row. 
	 * @param row
	 * @param colIdx
	 * @return
	 */
	private ValueList getValue(ArrayList<Object[]> row, Map<String, Integer> colIdx) {
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
					vs.constv(idx, (String) nv[1]);
				else
					vs.v(idx, (AbsPart) nv[1]);
			} catch (TransException e) {
				e.printStackTrace();
			}
		}

		return vs;
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
	 * by {@link #commit(ArrayList)}, don't generate it and travels AST again in this method, 
	 * use it directly.
	 * @return results by resolving FK, etc.
	 * @throws TransException
	 * @throws SQLException
	 */
	public Object ins(ISemantext ctx) throws TransException, SQLException {
		// check referee
		beginSql(ctx);

		if (postOp != null) {
			ArrayList<String> sqls = new ArrayList<String>(); 
			commit(ctx, sqls);
			return postOp.op(sqls);
		}
		return null;
	}

	void beginSql(ISemantext ctx) {
		if (currentRowNv != null && currentRowNv.size() > 0) {
			if (valuesNv == null) {
				valuesNv = new ArrayList<ArrayList<Object[]>>(1);
			}
			valuesNv.add(currentRowNv);
			currentRowNv = null;
		}

//		@SuppressWarnings("unused")
//		boolean hasValuesNv = valuesNv != null && valuesNv.size() > 0;
		
		if (ctx != null)
			ctx.onInsert(this, mainTabl, valuesNv);
		
		if (postate != null)
			for (Statement<?> pst : postate)
				pst.beginSql(ctx);
	}
	
}
