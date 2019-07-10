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
				.collect(Collectors.joining("values (", "", ")"));

		else 
			return values.stream().map(row -> super.getValue(sctx, row, cols).sql(sctx))
				.collect(Collectors.joining(" from dual union select ", "select ", " from dual"));
	}

//	/**Get row's value list used in sql values.
//	 * @param sctx
//	 * @param row
//	 * @param colIdx
//	 * @return value list
//	 */
//	private ValueList getRow(ISemantext sctx, ArrayList<Object[]> row, Map<String, Integer> colIdx) {
//		if (row == null)
//			return null;
//
//		ValueList vs = new ValueList(colIdx == null ? row.size() : colIdx.size());
//		int idx = -1;
//		for (Object[] nv : row) {
//			if (nv == null || nv.length <= 1) continue;
//
//			if (colIdx == null)
//				idx++;
//			else if (nv != null && nv.length >= 2 && colIdx.containsKey(nv[0]))
//					idx = colIdx.get(nv[0]);
//			else {
//				try { Utils.warn("InsertValuesOrcl#getValue(): Can't find column index for col %s (value = %s)", nv[0], nv[1]);
//				} catch (Exception e) {}
//				continue;
//			}
//			try {
//				if (nv[1] instanceof AbsPart)
//					vs.v(idx, (AbsPart) nv[1]);
//				else {
//					// Only value through java api know what's the type, the json massage handler don't.
//					// So we figure it out through db meta data.
//					String str = String.valueOf(nv[1]);
//					if (sctx == null)
//						// when testing, sctx is null
//						if (nv[1] == null)
//							vs.v(idx, new ExprPart("null"));
//						else
//							vs.v(idx, ExprsVisitor.parse(str));
//					else {
//						TableMeta cltyp = sctx.colType(tabl);
//						if (cltyp == null || cltyp.isQuoted((String)nv[0]))
//							if (nv[1] == null)
//								vs.v(idx, new ExprPart("null"));
//							else
//								vs.v(idx, ExprsVisitor.parse(str));
//						else vs.v(idx, nv[1] == null
//									 ? new ExprPart("null")
//									 : LangExt.isblank(str) ? new ExprPart("0") : new ExprPart(str));
//					}
//				}
//			} catch (TransException e) {
//				e.printStackTrace();
//			}
//		}
//
//		return vs;
//	}


}
