package io.odysz.transact.sql;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.semantics.ISemantext;
import io.odysz.semantics.SemanticObject;
import io.odysz.semantics.x.SemanticException;
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
	public String sql(ISemantext sctx) {
		if (sctx != null)
			sctx.onUpdate(this, mainTabl, nvs);
		
		// update tabl t set col = 'val' where t.col = 'val'
		Stream<String> s = Stream.concat(
					Stream.of(new ExprPart("update"),
						new ExprPart(mainTabl), new ExprPart(mainAlias),
						new ExprPart("set"), new SetList(nvs)), 
					Stream.of(new ExprPart("where"), where).filter(w -> where != null))
				  .map(m -> m == null ? "" : m.sql(sctx));

		return s.collect(Collectors.joining(" "));
	}

	/**FIXME merge this to some where parsing JMessage<br>
	 * Add multi del insert update for children table<br>
	 * - a special frequently used case of CRUD, provided as a shortcut of API.
	 * @param multireq {dels: [condition-strings[]], ins: [nvs[]]}
	 * @param stcx 
	 * @throws SemanticException 
	 */
	public void postChildren(SemanticObject multireq, Transcxt stcx) throws SemanticException {
		// throw new SemanticException("TODO...");
		Delete del = (Delete) multireq.get("dels");
		if (del != null) {
			if (postate == null)
				postate = new ArrayList<Statement<?>>();
			postate.add(del);
		}

		Insert ins = (Insert) multireq.get("insert");
		if (ins != null) {
			if (postate == null)
				postate = new ArrayList<Statement<?>>();
			postate.add(ins);
		}
	}
}
