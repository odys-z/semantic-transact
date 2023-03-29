package io.odysz.transact.sql.parts.insert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.odysz.common.LangExt;
import io.odysz.common.Utils;
import io.odysz.semantics.ISemantext;
import io.odysz.semantics.meta.TableMeta;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.sql.parts.antlr.ExprsVisitor;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.x.TransException;

public class InsertValues extends AbsPart {

	protected List<ArrayList<Object[]>> values;
	protected Map<String, Integer> cols;
	protected String tablName;

	public InsertValues(String tabl, Map<String, Integer> cols, List<ArrayList<Object[]>> values) {
		this.tablName = tabl;
		this.cols = cols;
		this.values = values;
	}

	@Override
	public String sql(ISemantext sctx) throws TransException {
		if (values == null)
			return "";

		return values.stream().map(row -> getValue(sctx, row, cols).sql(sctx))
				.collect(Collectors.joining(", ", "values ", ""));
	}

	/**Get row's value list used in sql values.
	 * @param sctx
	 * @param row
	 * @param colIdx
	 * @return value list
	 */
	protected ValueList getValue(ISemantext sctx, ArrayList<Object[]> row, Map<String, Integer> colIdx) {
		if (row == null)
			return null;

		ValueList vs = new ValueList(colIdx == null ? row.size() : colIdx.size());
		int idx = -1;
		for (Object[] nv : row) {
			if (nv == null || nv.length <= 1) continue;

			if (colIdx == null)
				idx++;
			else if (nv != null && nv.length >= 2 && colIdx.containsKey(nv[0]))
					idx = colIdx.get(nv[0]);
			else {
				try { Utils.warn("InsertValues#getValue(): Can't find column index for col %s (value = %s)", nv[0], nv[1]);
				} catch (Exception e) {}
				continue;
			}
			try {
				if (nv[1] instanceof AbsPart)
					vs.v(idx, (AbsPart) nv[1]);
				else {
					// Only value through java api know what's the type, the json massage handler don't.
					// So we figure it out through db meta data.
					String str = String.valueOf(nv[1]);
					if (sctx == null)
						// when testing, sctx is null
						if (nv[1] == null)
							vs.v(idx, new ExprPart("null"));
						else
							vs.v(idx, ExprsVisitor.parse(str));
					else {
						TableMeta cltyp = sctx.tablType(tablName);
						if (cltyp == null || cltyp.isQuoted((String)nv[0]))
							if (nv[1] == null)
								vs.v(idx, new ExprPart("null"));
							else
								vs.v(idx, ExprsVisitor.parse(str));
						else vs.v(idx, nv[1] == null
									 ? new ExprPart("null")
									 : LangExt.isblank(str) ? new ExprPart("0") : new ExprPart(str));
					}
				}
			} catch (TransException e) {
				e.printStackTrace();
			}
		}

		return vs;
	}


}
