package io.odysz.transact.sql.parts.insert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.odysz.semantics.ISemantext;
import io.odysz.transact.x.TransException;

/**Compose value list in sql values ('v1', 'v2', ...)
 * or select ... from dual uninon select ... from dual. 
 * @author odys-z@github.com
 */
public class InsertValuesOrcl extends InsertValues {

//	private List<ArrayList<Object[]>> values;
//	private Map<String, Integer> cols;
//	private String tabl;

	public InsertValuesOrcl(String tabl, Map<String, Integer> cols, List<ArrayList<Object[]>> values) {
		super(tabl, cols, values);
	}

	@Override
	public String sql(ISemantext sctx) throws TransException {
		if (values == null || values.size() == 0)
			return "";

		if (values.size() == 1)
			return values.stream().map(row -> super.getValue(sctx, row, cols).sql(sctx))
				.collect(Collectors.joining("", "values ", ""));

		else 
			return values.stream().map(row -> super
										.getValue(sctx, row, cols)
										.withParentheses(false)
										.sql(sctx))
				.collect(Collectors.joining(" from dual union select ", "select ", " from dual"));
	}
}
