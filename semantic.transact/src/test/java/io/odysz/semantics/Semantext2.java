package io.odysz.semantics;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.odysz.common.DateFormat;
import io.odysz.semantics.Semantics2.smtype;
import io.odysz.transact.sql.Insert;
import io.odysz.transact.sql.Statement;
import io.odysz.transact.sql.Update;

/**Basic semantic context (semantics instance) for resolving semantics when generating sql.
 * @author ody
 *
 */
public class Semantext2 implements ISemantext {

	private String tabl;
	private HashMap<Object, Object> autoVals;
	private HashMap<String, Semantics2> semantics;
	private Statement<?> callerStatement;

	public Semantext2(String tabl, HashMap<String,Semantics2> semantics) {
		this.tabl = tabl;
		this.semantics = semantics;
	}

	/**When inserting, replace inserting values in 'AUTO' columns, e.g. generate auto PK for rec-id.
	 * @see io.odysz.semantics.ISemantext#onInsert(io.odysz.transact.sql.Insert, java.lang.String, java.util.List)
	 */
	@Override
	public ISemantext onInsert(Insert insert, String tabl, List<ArrayList<Object[]>> valuesNv) {
		if (valuesNv != null)
			for (ArrayList<Object[]> value : valuesNv) {
				Semantics2 s = semantics.get(tabl);
				if (s == null)
					continue;
				Map<String, Integer> cols = insert.getColumns();
				if (s.is(smtype.autoPk)) {
					String pk = s.autoPk();
					String n = (String) value.get(cols.get(pk))[0];
					if (n.equals(pk))
						value.get(cols.get(pk))[1] = "TEST-" + DateFormat.format(new Date());
				}
				if (s.is(smtype.fullpath)) {
					String n = s.getFullpathField();
					String fp = s.genFullpath(value, cols);
					Object[] nv = null;
					if (!cols.containsKey(n)) {
						// append fullpath nv
						int c = cols.size();
						nv = new Object[] {n, fp};
						value.add(nv);
						cols.put(n, c);
					}
					else {
						nv = value.get(cols.get(n));
						nv[1] = fp;
					}
				}
			}
		callerStatement = insert;
		return this;
	}

	@Override
	public ISemantext onUpdate(Update update, String tabl, ArrayList<Object[]> nvs) {
		// TODO we need semantics here
		if (nvs != null)
			for (Object[] nv : nvs)
				if (nv != null && nv.length > 0 && "AUTO".equals(nv[1]))
					nv[1] = autoVals == null ? nv[1] : autoVals.get(nv[0]);
		return this;
	}


	@Override
	public ISemantext insert(Insert insert, String tabl) {
		return new Semantext2(tabl, semantics);
	}

}
