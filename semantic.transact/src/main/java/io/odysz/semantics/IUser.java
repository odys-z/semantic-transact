package io.odysz.semantics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.odysz.transact.x.TransException;

/**<p>Provide user e.g. servlet session information to modify some date in AST.</p>
 * <p>This is not necessary if using semantic-transact directly. But if the caller
 * want to set user information like fingerpirnt for modified records, this can be used
 * to let semantic-transact providing user identity to the semantics handler.</p>
 * @author ody
 *
 */
public interface IUser {

	HashMap<String, Object> props = new HashMap<String, Object>();

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

	default String sessionId() { return null; }

	/**Update last touched time stamp.*/
	default void touch() {}

	/**Last touched time in milliseconds, set by {@link #touch()}.<br>
	 * Default: 20 minutes */
	default long touchedMs() { return 20 * 60 * 1000; }

	/**user id */
	String uid() ;
	
	/**Get any property other than uid.
	 * @param prop
	 * @return property value
	 */
	default Object get(String prop) { return props.get(prop); };

	default IUser set(String prop, Object v) {
		props.put(prop, v);
		return this;
	};

	IUser logAct(String funcName, String funcId);

	default SemanticObject logout() { return null; }

	default void writeJsonRespValue(Object writer) throws IOException {}

	default String sessionKey() { return (String) get("s-key"); }
	default IUser sessionKey(String skey) { return set("s-key", skey); }

	/**Add notifyings
	 * @param n
	 * @return this
	 */
	@SuppressWarnings("unchecked")
	default IUser notify(Object note) {
		if (!props.containsKey("_notifies_"))
			props.put("_notifies_", new ArrayList<Object>());
		((ArrayList<Object>)props.get("_notifies_")).add(note);
		return this;
	}

	/**Get notified string list.
	 * @return notifyings
	 */
	@SuppressWarnings("unchecked")
	default List<Object> notifies() {
		return (List<Object>) props.get("_notifies_");
	}
}
