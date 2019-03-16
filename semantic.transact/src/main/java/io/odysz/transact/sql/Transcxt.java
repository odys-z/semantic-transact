package io.odysz.transact.sql;

import io.odysz.semantics.ISemantext;

/**<p>Transaction Context, a Transaction / Batching SQL builder creator.</p>
 * <p>A Transcxt is typically pluged in with ISemantext, which is the handler of semantics.</p>
 * <p>When building sql, events like onInserting, etc. are fired to ISemantext.
 * @author odys-z@github.com
 */
public class Transcxt {

	protected static ISemantext statiCtx;
	public ISemantext staticContext() { return statiCtx; }

	/**Create a statements manager.
	 * @param staticSemantext A static semantic providing basic DB access, used to generate autoID etc.
	 */
	public Transcxt(ISemantext staticSemantext) {
		statiCtx = staticSemantext;
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

	/**
	 * @param insert
	 * @param tabl
	 * @param usr
	 * @return {@link #semantext}
	public <T extends Statement<T>> ISemantext ctx(T insert, String tabl, IUser... usr) {
		return semantext;
	}
	 */

	/**Add Semantics to {@link #semantext}
	 * @param tabl
	 * @param pk
	 * @param smtcs
	 * @param args
	 * @throws TransException
	public Transcxt addSemantics(String tabl, String pk, String smtcs, String args) throws TransException {
		semantext.addSemantics(tabl, pk, smtcs, args);
		return this;
	}

	public Object resolvedVal(String tabl, String col) {
		return semantext.results().has(tabl) ?
				((SemanticObject)semantext.results().get(tabl)).get(col) : null;
	}
	 */
}
