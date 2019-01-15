package io.odysz.transact.sql;

import io.odysz.semantics.ISemantext;
import io.odysz.semantics.IUser;

/**Transaction / Batching SQL builder creator.
 * @author ody
 */
public class Transcxt {

	ISemantext semantext;

	public Transcxt(ISemantext semantext) {
		this.semantext = semantext;
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

//	public <T extends Statement<T>> ISemantext insertCtx(Insert statement,
//			String tabl, List<ArrayList<Object[]>> valuesNv) {
//		return semantext.onInsert(statement, tabl, valuesNv);
//	}

	public <T extends Statement<T>> ISemantext ctx(T insert, String tabl, IUser... usr) {
//		return semantext == null ? null : semantext.insert((Insert) insert, tabl, usr);
		return semantext;
	}
}
