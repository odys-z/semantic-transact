package io.odysz.transact.sql.parts.insert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.odysz.common.Utils;
import io.odysz.semantics.ISemantext;
import io.odysz.semantics.meta.TableMeta;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.x.TransException;

public class InsertValues extends AbsPart {

	private List<ArrayList<Object[]>> values;
	private Map<String, Integer> cols;
	private String tabl;

	public InsertValues(String tabl, Map<String, Integer> cols, List<ArrayList<Object[]>> values) {
		this.tabl = tabl;
		this.cols = cols;
		this.values = values;
	}

	@Override
	public String sql(ISemantext sctx) throws TransException {
		if (values == null)
			return "";

		return values.stream().map(row -> getValue(sctx, row, cols).sql(sctx))
				.collect(Collectors.joining(", "));
	}

	/**Get row's value list used in sql values.
	 * @param sctx
	 * @param row
	 * @param colIdx
	 * @return value list
	 */
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
				Utils.warn("Can't find column index for col %s (value = %s)", nv[0], nv[1]);
				continue;
			}
			try {
				if (nv[1] instanceof String) {
					// Only value through java api know what's the type, the json massage handler don't.
					// So we figure it out throw db meta data.
					String str = sctx == null ? (String)nv[1]
							 : (String) sctx.resulvedVal((String)nv[1]);
					if (sctx == null)
						// when testing
						vs.constv(idx, str);
					else {
						TableMeta cltyp = sctx.colType(tabl);
						if (cltyp == null || cltyp.isText((String)nv[0]))
							vs.constv(idx, str);
						else vs.v(idx, new ExprPart(str));
					}
				}
				else
					vs.v(idx, (AbsPart) nv[1]);
			} catch (TransException e) {
				e.printStackTrace();
			}
		}

		return vs;
	}


}
