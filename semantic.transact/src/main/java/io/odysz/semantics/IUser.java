package io.odysz.semantics;

import java.util.ArrayList;

/**<p>Provide user e.g. servlet session information to modify some date in AST.</p>
 * <p>This is not necessary if using semantic-transact directly. But if the caller
 * want to set user information like fingerpirnt for modified records, this can be used
 * to let semantic-transact providing user identity to the semantics handler.</p>
 * @author ody
 *
 */
public interface IUser {

	String getUserId();

	/**The sqls is committed to database, do something for logging. 
	 * If there are some operation needing to update db, return those sql statements.
	 * @param sqls
	 * @return SQLs for logging
	 */
	ArrayList<String> dbLog(ArrayList<String> sqls);


}
