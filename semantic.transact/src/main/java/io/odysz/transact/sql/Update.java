package io.odysz.transact.sql;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.semantics.Semantext;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.sql.parts.update.SetList;

public class Update extends Statement<Update> {
	private ArrayList<Object[]> nvs;

	Update(Transcxt transc, String tabl) {
		super(transc, tabl, null);
	}

	/**set n = v, where if v is constant, 'val', must have a '' pair.
	 * @param n
	 * @param v
	 * @return Update statement
	 */
	public Update nv(String n, Object v) {
		if (nvs == null)
			nvs = new ArrayList<Object[]>();
		nvs.add(new Object[] {n, v});
		return this;
	}

	@Override
	public String sql(Semantext sctx) {
		sctx.onUpdate(nvs);
		
		// update tabl t set col = 'val' where t.col = 'val'
		Stream<String> s = Stream.concat(
					Stream.of(new ExprPart("update"),
						new ExprPart(mainTabl), new ExprPart(mainAlias),
						new ExprPart("set"), new SetList(nvs)), 
					Stream.of(new ExprPart("where"), where).filter(w -> where != null))
				  .map(m -> m == null ? "" : m.sql(sctx));
//		// insert into tabl(...) values(...) / select ...
//		Stream<String> s2 = Stream.concat(
//				// insert into tabl(...)
//				/*
//				Stream.concat(
//						// insert into tabl(...)
//						Stream.of(new ExprPart("insert into"), new ExprPart(mainTabl), new ExprPart(mainAlias)),
//						Optional.ofNullable(insertCols).orElse((Map<String, Integer>)Collections.<String, Integer>emptyMap())
//											.keySet().stream().map(m -> new ExprPart(m)).filter(m -> hasValuesNv) */
//				Stream.of(new ExprPart("insert into"), new ExprPart(mainTabl), new ExprPart(mainAlias), new ColumnList(insertCols)
//				// values(...) / select ...
//				), Stream.concat(
//						// values (...)
//						Stream.concat(Stream.of(new ExprPart("values (")), // 'values()' appears or not being the same as value nvs
//									  // 'v1', 'v2', ...)
//									  Stream.concat(Optional.ofNullable(valuesNv).orElse(Collections.emptyList())
//											  		  		.stream().map(row -> getRow(row, insertCols)),
//									  				Stream.of(new ExprPart(")")))
//						).filter(w -> hasValuesNv),
//						// select ...
//						Stream.of(selectValues).filter(w -> selectValues != null))
//			).map(m -> m.sql(scxt));

		return s.collect(Collectors.joining(" "));
	}

}
