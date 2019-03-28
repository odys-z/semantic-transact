package io.odysz.transact.sql;

import io.odysz.semantics.ISemantext;
import io.odysz.semantics.IUser;

/**<p>Transaction Context, a Transaction / Batching SQL builder creator.</p>
 * <p>A Transcxt is typically pluged in with ISemantext, which is the handler of semantics.</p>
 * <p>When building sql, events like onInserting, etc. are fired to ISemantext.
 * @author odys-z@github.com
 */
public class Transcxt {

	protected ISemantext basictx;


	/**Get a basic Semantext that typicall don't handle many semantics, can only used for basic sql generation,
	 * except the transact builder is constructed with a very strong Semantext, like that in cheap engine.
	 * @return
	public ISemantext basictx() { return basictx; }
	 */

	public ISemantext instancontxt(IUser usr) {
		return basictx == null ? null : basictx.clone(usr);
	}

	/**Create a statements manager.
	 * @param staticSemantext A static semantic providing basic DB access, used to generate autoID etc.
	 */
	public Transcxt(ISemantext staticSemantext) {
		basictx = staticSemantext;
	}
	
	public Query select(String tabl, String ... alias) {
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

	/**
	 * @param insert
	 * @param tabl
	 * @param usr
	 * @return {@link #semantext}
	public <T extends Statement<T>> ISemantext ctx(T insert, String tabl, IUser... usr) {
		return semantext;
	}
	 */
}
