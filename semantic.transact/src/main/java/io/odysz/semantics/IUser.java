package io.odysz.semantics;

import java.io.IOException;
import java.util.ArrayList;

import io.odysz.transact.x.TransException;

/**<p>Provide user e.g. servlet session information to modify some date in AST.</p>
 * <p>This is not necessary if using semantic-transact directly. But if the caller
 * want to set user information like fingerpirnt for modified records, this can be used
 * to let semantic-transact providing user identity to the semantics handler.</p>
 * @author ody
 *
 */
public interface IUser {

	/**The sqls is committed to database, do something for logging. 
	 * If there are some operation needing to update db, return those sql statements.
	 * @param sqls
	 * @return SQLs for logging
	 */
	ArrayList<String> dbLog(ArrayList<String> sqls);

	/**Check user log in (already has pswd, iv and user Id from db)
	 * @param request request object. In sematic.jserv, it's SessionReq object.
	 * @return
	 * @throws TransException Checking login information failed
	 */
	boolean login(Object request) throws TransException;

	String sessionId();

	/**Update last touched time stamp.*/
	void touch();

	/**user id */
	String uid();
	
	/**Get any property other than uid.
	 * @param prop
	 * @return porperty
	 */
	String get(String prop);

	SemanticObject logout();

	void writeJsonRespValue(Object writer) throws IOException;

}
