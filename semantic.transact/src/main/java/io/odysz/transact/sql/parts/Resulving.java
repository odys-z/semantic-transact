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
 * remember all generated key if there are to nodes inserting the same table.<br>
 * So statement tree events do not travel across sub-trees is a rule of the design.
 * Then it's impossible to post resolve children pk using Resulving.</p>
 * @author odys-z@github.com
 */
public class Resulving extends ExprPart {

	private String autok;
	private String tabl;

	public Resulving(String tabl, String autok) {
		super(op.eq, null, null);
		this.tabl = tabl;
		this.autok = autok;
	}

	@Override
	public String sql(ISemantext context) {
		Object o = context.resulvedVal(tabl, autok);
		if (o instanceof AbsPart)
			try {
				return ((AbsPart) o).sql(context);
			} catch (TransException e) {
				e.printStackTrace();
				return "'" + tabl + "." + autok + "'";
			}
		else return "'" + o.toString() + "'";
	}

	public String resulved(ISemantext smtx) {
		if (smtx == null)
			return "'" + tabl + "." + autok + "'";
		return (String) smtx.resulvedVal(tabl, autok);
	}

}