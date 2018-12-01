package io.odysz.semantics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.odysz.common.DateFormat;
import io.odysz.common.JDBCType;

/**Semantic context (semantics instance) for resolving semantics when generating sql.
 * @author ody
 *
 */
public class Semantext {

	private String tabl;
	private HashMap<Object, Object> autoVals;

	public Semantext(String tabl) {
		this.tabl = tabl;
	}

	/**When inserting, replace inserting values in 'AUTO' columns, e.g. generate auto PK for rec-id.
	 * @param valuesNv
	 */
	public void onInsert(List<ArrayList<Object[]>> valuesNv) {
		// TODO we need semantics here
		// This template implementation already showed the interface should be here
		if (valuesNv != null)
			for (ArrayList<Object[]> value : valuesNv) {
				for (Object[] nv : value)
					if (nv != null && nv.length > 0 && "AUTO".equals(nv[1])) {
						if (autoVals == null)
							autoVals = new HashMap<Object, Object>();
						if (autoVals.containsKey(nv[0]))
							nv[1] = autoVals.get(nv[0]);
						else {
							nv[1] = nv[1] + " #" + DateFormat.getTimeStampYMDHms(JDBCType.mysql);
							autoVals.put(nv[0], nv[1]);
						}
					}
			}
	}

	public void onUpdate(ArrayList<Object[]> nvs) {
		// TODO we need semantics here
		if (nvs != null)
			for (Object[] nv : nvs)
				if (nv != null && nv.length > 0 && "AUTO".equals(nv[1]))
					nv[1] = autoVals == null ? nv[1] : "'" + autoVals.get(nv[0]) + "'";
	}

}
