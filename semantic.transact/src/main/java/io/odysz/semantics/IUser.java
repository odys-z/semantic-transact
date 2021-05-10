package io.odysz.semantics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.odysz.semantics.meta.TableMeta;
import io.odysz.transact.x.TransException;

/**<p>Provide user e.g. servlet session information to modify some date in AST.</p>
 * <p>This is not necessary if using semantic-transact directly. But if the caller
 * want to set user information like fingerpirnt for modified records, this can be used
 * to let semantic-transact providing user identity to the semantics handler.</p>
 * 
 * <p>In v1.1.1, sessionId is read only. If a new password bee updated,
 * just remove then re-login</p>
 * 
 * @author ody
 */
public interface IUser {
	TableMeta meta();

	/**The sqls is committed to database, do something for logging. 
	 * If there are some operation needing to update db, return those sql statements.
	 * <p><b>Make sure the committed sqls is not returned, only logging sqls are needed.</b><br>
	 * If the parameter sqls is returned, it will be committed again because the semantic connection
	 * is think it's the logging sql.</p>
	 * @param sqls
	 * @return SQLs for logging
	 */
	default ArrayList<String> dbLog(ArrayList<String> sqls) { return null; }

	/**Check user log in (already has pswd, iv and user Id from db)
	 * @param request request object. In sematic.jserv, it's SessionReq object.
	 * @return true: ok; false: failed
	 * @throws TransException Checking login information failed
	 */
	default boolean login(Object request) throws TransException { return false; }

	/**A session Id can never be changed.
	 * If a new password been updated, just remove the session and re-login.
	 * @return
	 */
	default String sessionId() { return null; }

	/**Update last touched time stamp.*/
	default void touch() {}

	/**Last touched time in milliseconds, set by {@link #touch()}.<br>
	 */
	long touchedMs();

	/**user id */
	String uid() ;
	
	IUser logAct(String funcName, String funcId);

	default SemanticObject logout() { return null; }

	default void writeJsonRespValue(Object writer) throws IOException {}

	/**Add notifyings
	 * @param n
	 * @return this
	 * @throws TransException 
	 */
	public IUser notify(Object note) throws TransException;

	/**Get notified string list.
	 * @return notifyings
	 */
	public List<Object> notifies();

	public IUser sessionKey(String string);

	public String sessionKey();

	default IUser sessionId(String rad64num) { return this; }
}
