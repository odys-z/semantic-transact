package io.odysz.transact.sql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.common.Utils;
import io.odysz.transact.x.StException;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.sql.parts.insert.ColumnList;
import io.odysz.transact.sql.parts.select.ConstList;

/**sql: insert into tabl(...) values(...) / select ...
 * @author ody
 *
 */
public class Insert extends Statement {

	private Map<String,Integer> insertCols;
	private String pk;
	private Query selectValues;
	private List<ArrayList<Object[]>> valuesNv;
	private ArrayList<Object[]> currentRowNv;

	Insert(Transcxt transc, String tabl) {
		super(transc, tabl, null);
	}

	public Statement nv(String n, Object v) {
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

	public Insert cols(String col0, String... cols) throws StException {
		if (valuesNv != null && valuesNv.size() > 0)
			throw new StException("cols() must been called before any rows' value been added (calling values())");
			
		if (insertCols == null)
			insertCols = new HashMap<String, Integer>();
		if (pk == null)
			this.pk = col0;
		
		// initial columns size
		int size0 = insertCols.size();

		insertCols.put(col0, size0);
		if (cols != null)
			for (int c = size0; c < cols.length; c++)
				insertCols.put(cols[c], c + 1);
		return this;
	}

	public Insert values(ArrayList<Object[]> rowFields) throws StException {
		if (rowFields == null)
			return this;
		if (insertCols.size() != rowFields.size())
			throw new StException("columns' number didn't match rows field count.");

		if (selectValues != null)
			throw new StException("Semantic-Transact only support one of insert-select or insert-values.");

		if (valuesNv == null)
			valuesNv = new ArrayList<ArrayList<Object[]>>(rowFields.size());
		
		if (currentRowNv != null && currentRowNv.size() > 0)
			// append current row, then append new vals 
			valuesNv.add(currentRowNv);
		valuesNv.add(rowFields);
		return this;
	}

	public Insert select(Query values) throws StException {
		if (valuesNv != null && valuesNv.size() > 0)
			throw new StException("Semantic-Transact only support one of insert-select or insert-values.");
		selectValues = values;
		return this;
	}

	/**sql: insert into tabl(...) values(...) / select ...
	 * @throws StException 
	 * @see io.odysz.transact.sql.Statement#sql()
	 */
	@Override
	public String sql() {
		if (currentRowNv != null && currentRowNv.size() > 0) {
			if (valuesNv == null) {
				valuesNv = new ArrayList<ArrayList<Object[]>>(1);
			}
			valuesNv.add(currentRowNv);
		}

		boolean hasValuesNv = valuesNv != null && valuesNv.size() > 0;

		// insert into tabl(...) values(...) / select ...
		Stream<String> s = Stream.concat(
				// insert into tabl(...)
				/*
				Stream.concat(
						// insert into tabl(...)
						Stream.of(new ExprPart("insert into"), new ExprPart(mainTabl), new ExprPart(mainAlias)),
						Optional.ofNullable(insertCols).orElse((Map<String, Integer>)Collections.<String, Integer>emptyMap())
											.keySet().stream().map(m -> new ExprPart(m)).filter(m -> hasValuesNv) */
				Stream.of(new ExprPart("insert into"), new ExprPart(mainTabl), new ExprPart(mainAlias), new ColumnList(insertCols)
				// values(...) / select ...
				), Stream.concat(
						// values (...)
						Stream.concat(Stream.of(new ExprPart("values (")), // 'values()' appears or not being the same as value nvs
									  // 'v1', 'v2', ...)
									  Stream.concat(Optional.ofNullable(valuesNv).orElse(Collections.emptyList())
											  		  		.stream().map(row -> getRow(row, insertCols)),
									  				Stream.of(new ExprPart(")")))
						).filter(w -> hasValuesNv),
						// select ...
						Stream.of(selectValues).filter(w -> selectValues != null))
			).map(m -> m.sql());

		return s.collect(Collectors.joining(" "));
	}

	/**Create ConstList from row. 
	 * @param row
	 * @param colIdx
	 * @return
	 */
	private ConstList getRow(ArrayList<Object[]> row, Map<String, Integer> colIdx) {
		if (row == null)
			return null;

		ConstList vs = new ConstList(row.size());
		int idx = -1;
		for (Object[] nv : row) {
			if (nv == null) continue;

			if (colIdx == null)
				idx++;
			else if (colIdx.containsKey(nv[0]))
					idx = colIdx.get(nv[0]);
			else {
				Utils.warn("Can't find column index for cole %s %s", nv[0], nv[1]);
				continue;
			}
			try {
				vs.constv(idx, (String) nv[1]);
			} catch (StException e) {
				e.printStackTrace();
			}
		}

		return vs;
	}

}
