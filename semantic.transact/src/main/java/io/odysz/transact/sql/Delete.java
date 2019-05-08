package io.odysz.transact.sql;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.semantics.ISemantext;
import io.odysz.semantics.SemanticObject;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.x.TransException;

public class Delete extends Statement<Delete>  {
	private ArrayList<Object[]> nvs;

	Delete(Transcxt transc, String tabl) {
		super(transc, tabl, null);
	}

	@Override
	public String sql(ISemantext sctx) throws TransException {
		if (sctx != null)
			sctx.onUpdate(this, mainTabl, nvs);
		
		if (where == null)
			throw new TransException("semantic.transact doesn't allow any delete statement without conditions. table: %s", mainTabl);
		
		// update tabl t set col = 'val' where t.col = 'val'
		Stream<String> s = Stream.concat(
					Stream.of(  new ExprPart("delete from"),
								new ExprPart(mainTabl)), 
					Stream.of(new ExprPart("where"), where).filter(w -> where != null))
				  .map(m -> {
					try {
						return m == null ? "" : m.sql(sctx);
					} catch (TransException e) {
						e.printStackTrace();
						return "";
					}
				});
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

	/**Add multi del insert update for children table
	 * - a special frequently used case of CRUD, should be abstracted into a more general way.
	 * @param multireq
	 * @throws TransException 
	 */
	public void postChildren(SemanticObject multireq) throws TransException {
		throw new TransException("Not working yet ...");
	}
	
	public Object del(ISemantext ctx) throws TransException {
		throw new TransException("Not working yet ...");
	}
}
