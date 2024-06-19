package io.odysz.transact.sql.parts;

import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.Logic.op;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.x.TransException;

/**Value to be resulved.
 * For what's resulved value, see <a href=''>RESolved resULt Value</a>.
 * <p>Note: This class can not been used to resolve post fk value.</p> 
 * <p>Design Memo:<br>
 * If leting Resulving can resolve post fk value, all statement tree nodes
 * must traveled by a pre-inserting event handler, that's not possible to
 * remember all generated key if there are two nodes inserting the same table.<br>
 * So statement tree events do not travel across sub-trees is a rule of the design.
 * Then it's impossible to post resolve children pk using Resulving.</p>
 * @author odys-z@github.com
 */
public class Resulving extends ExprPart {

	private String autok;
	private String tabl;
	
	private boolean asConstr = false;

	public Resulving(String tabl, String autok) {
		// super(op.eq, null, null);
		super(op.eq, "", "");
		this.tabl = tabl;
		this.autok = autok;
	}

	@Override
	public String sql(ISemantext context) throws TransException {
		Object o = context.resulvedVal(tabl, autok);
		if (o == null)
			throw new TransException("Can't resolve auto Id - %s.%s. Possible error: wrong configure; empty row (no insertion triggered)", tabl, autok);
		if (o instanceof AbsPart)
			try {
				// return ((AbsPart) o).sql(context);
				return asConstr ? "'" + ((AbsPart) o).sql(context) + "'" : ((AbsPart) o).sql(context);
			} catch (TransException e) {
				e.printStackTrace();
				return "'" + tabl + "." + autok + "'";
			}
		else return "'" + o.toString() + "'";
	}

	/**Get the resulved value
	 * @param smtx
	 * @return the resulved value or 'tabl.pk' (for null semantext)
	 */
	public String resulved(ISemantext smtx) {
		if (smtx == null)
			return "'" + tabl + "." + autok + "'";
		return (String) smtx.resulvedVal(tabl, autok);
	}

	public Resulving asConstr() {
		asConstr = true;
		return this;
	}

}
