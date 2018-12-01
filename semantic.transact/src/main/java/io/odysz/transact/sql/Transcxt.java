package io.odysz.transact.sql;

import io.odysz.semantics.ISemantics;
import io.odysz.semantics.Semantext;

/**Transaction / Batching SQL builder creator.
 * @author ody
 */
public class Transcxt {

	public Transcxt(ISemantics semantics) {
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

	public Semantext inert(String tabl) {
		return new Semantext(tabl);
	}

}
