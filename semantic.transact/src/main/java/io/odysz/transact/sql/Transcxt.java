package io.odysz.transact.sql;

import io.odysz.semantics.ISemantext;
import io.odysz.semantics.IUser;
import io.odysz.semantics.meta.TableMeta;
import io.odysz.transact.x.TransException;

/**<p>Transaction Context, a Transaction / Batching SQL builder creator.</p>
 * <p>A Transcxt is typically pluged in with ISemantext, which is the handler of semantics.</p>
 * <p>When building sql, events like onInserting, etc. are fired to ISemantext.
 * @author odys-z@github.com
 */
public class Transcxt {

	protected ISemantext basictx;
	public ISemantext basictx() { return basictx; }

	/**Get a {@link ISemantext} that typically handle configured semantics,
	 * @param conn not used
	 * @param usr the session user
	 * @return a new instance for building sql, resulving sql, etc.
	 * @throws TransException 
	 */
	public ISemantext instancontxt(String conn, IUser usr) throws TransException {
		return basictx == null ? null : basictx.clone(usr).connId(conn == null ? basictx.connId() : conn);
	}

	/**Create a statements manager.
	 * @param staticSemantext A static semantic providing basic DB access, used to generate autoID etc.
	 */
	public Transcxt(ISemantext staticSemantext) {
		basictx = staticSemantext;
	}
	
	public Query select(String tabl, String ... alias) {
//		if (basictx.dbtype() == dbtype.oracle)
//			return new QueryOrcl(this, tabl, alias);
		return new Query(this, tabl, alias);
	}
	
	public Insert insert(String tabl) {
		return new Insert(this, tabl);
	}
	
	public Update update(String tabl) {
		return new Update(this, tabl);
	}
	
	public Delete delete(String tabl) {
		return new Delete(this, tabl);
	}

	public TableMeta tableMeta(String tabl) throws TransException {
		return basictx == null ? null : basictx.colType(tabl);
	}
}
