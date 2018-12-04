package io.odysz.semantics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.odysz.common.DateFormat;
import io.odysz.common.JDBCType;
import io.odysz.transact.sql.Insert;
import io.odysz.transact.sql.Statement;
import io.odysz.transact.sql.Update;

/**Basic semantic context (semantics instance) for resolving "AUTO" when generating sql.
 * @author ody
 *
 */
class Semantext1 implements ISemantext {

	private String tabl;
	private HashMap<Object, Object> autoVals;
	private HashMap<String, Semantics2> semantics;
	private Statement<?> callerStatement;

	public Semantext1(String tabl, HashMap<String,Semantics2> semantics) {
		this.tabl = tabl;
		this.semantics = semantics;
	}

	/**When inserting, replace inserting values in 'AUTO' columns, e.g. generate auto PK for rec-id.<br>
	 * TODO we need semantics here
	 * @param valuesNv
	 */
	@Override
	public ISemantext onInsert(Insert insert, String tabl, List<ArrayList<Object[]>> valuesNv) {
		// This template implementation already showed the interface should be here
		callerStatement = (Insert) insert;
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
		return this;
	}

	/**Resolve AUTO generated by "insert"<br>
	 * TODO we need semantics here
	 * @see io.odysz.semantics.ISemantext#onUpdate(java.util.ArrayList)
	 */
	@Override
	public ISemantext onUpdate(Update update, String tabl, ArrayList<Object[]> nvs) {
		if (autoVals != null && nvs != null)
			for (Object[] nv : nvs)
				if (nv != null && nv.length > 0 && "AUTO".equals(nv[1]))
					nv[1] = autoVals == null ? nv[1] : autoVals.get(nv[0]);
		return this;
	}

	@Override
	public ISemantext insert(Insert insert, String tabl, IUser... usr) {
		return new Semantext1(tabl, semantics);
	}

	@Override
	public ISemantext update(Update update, String mainTabl, IUser... usr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<String, SemanticObject> results() {
		return null;
	}
}
