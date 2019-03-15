package io.odysz.semantics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import io.odysz.common.DateFormat;
import io.odysz.common.dbtype;
import io.odysz.transact.sql.Insert;
import io.odysz.transact.sql.Statement;
import io.odysz.transact.sql.Update;
import io.odysz.transact.x.TransException;

/**Basic semantic context (semantics instance) for resolving "AUTO" when generating sql.
 * @author odys-z@github.com
 */
@SuppressWarnings("unused")
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
							nv[1] = nv[1] + " #" + DateFormat.getTimeStampYMDHms(dbtype.mysql);
							autoVals.put(nv[0], nv[1]);
						}
					}
			}
		return this;
	}

	/**Resolve AUTO generated by "insert"<br>
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
		return null;
	}

	@Override
	public SemanticObject results() { return null; }

	@Override
	public dbtype dbtype() { return dbtype.sqlite; }

	@Override
	public Stream<String> pagingStream(Stream<String> s, int pageIx, int pgSize) throws TransException {
		return s;
	}

	@Override
	public SemanticObject resolvedNewIds() { return null; }

	@Override
	public ISemantext addSemantics(String tabl, String pk, String smtcs, String args)
			throws TransException {
		return this;
	}
}
