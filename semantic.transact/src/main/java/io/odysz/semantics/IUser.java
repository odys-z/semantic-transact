package io.odysz.semantics;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import io.odysz.anson.Anson;
import io.odysz.common.EnvPath;
import io.odysz.semantics.meta.TableMeta;
import io.odysz.transact.x.TransException;

/**<p>Provide user e.g. servlet session information to modify some data in AST.</p>
 * <h5>Usage</h5>
 * <p>
 * 1. Configure the implementations class name in config.xml.<br>
 * 2. If the client needing logging in and responsed with a user object, the class must extend {@link SemanticObject}. <br>
 * - For default implementation, see semantic.jserv/JUser. 
 * </p> 
 * <p>This is not necessary if using semantic-transact directly. But if the caller
 * want to set user information like fingerpirnt for modified records, this can be used
 * to let semantic-transact providing user identity to the semantics handler.</p>
 * 
 * <p>In v1.1.1, sessionId is read only. If a new password have been updated,
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

	/** If a user is allowed to change password, this is used to verify old and must be overriden to check the old password cipher.
	 * @param pswdCypher64 decrypted with my token id
	 * @param iv64
	 * @return yes or no
	 * @throws TransException
	 * @throws IOException 
	 * @throws GeneralSecurityException 
	 */
	default boolean guessPswd(String pswdCypher64, String iv64) throws TransException, GeneralSecurityException, IOException { return false; }

	default IUser sessionId(String rad64num) { return this; }

	/**A session Id can never be changed.
	 * If a new password been updated, just remove the session and re-login.
	 * @return the session token
	 */
	default String sessionId() { return null; }

	/**Note: science v1.3.5, this requires users implement a touch function, and return the instance.
	 * If the session object must been terminated when time out, this method must touch the current time.
	 * Update last touched time stamp.*/
	default IUser touch() { return this; };

	/**Last touched time in milliseconds, set by {@link #touch()}.<br>
	 */
	long touchedMs();

	/**user id */
	String uid() ;
	
	IUser logAct(String funcName, String funcId);

	default SemanticObject logout() { return null; }

	default void writeJsonRespValue(Object writer) throws IOException {}

	/**Add notifyings
	 * @param note
	 * @return this
	 * @throws TransException 
	 */
	public IUser notify(Object note) throws TransException;

	/**Get notified string list.
	 * @return notifyings
	 */
	public List<Object> notifies();

	/** @deprecated why this is needed if there is {@link #sessionId(String)} ?
	 * 
	 * @param string
	 * @return this
	 */
	public default IUser sessionKey(String string) { return this; }

	/** @deprecated why this is needed if there is {@link #sessionId(String)} ?
	 * 
	 * @return this
	 */
	public default String sessionKey() { return null; }

	/**
	 * Since v1.3.5, user object has a change to initialize with login request, e.g. set client device Id.
	 * @param sessionReqBody e.g. AnSessionReq
	 * @return
	 * @throws SsException 
	 */
	public default IUser onCreate(Anson sessionReqBody) throws GeneralSecurityException { return this; }

	/**
	 * @param folder
	 * @param uid
	 * @param folder
	 * @param ssid
	 * @return $root/folder/uid/folder/ssid;
	 */
	static String tempDir(String root, String uid, String folder, String ssid) {
		return EnvPath.decodeUri(root, uid, folder, ssid);
	}

	/**
	 * Validate user's password, e.g. is it changed since the initiall password been reset.
	 * 
	 * @return this
	 * @throws TransException 
	 * @throws SQLException 
	 */
	public default IUser validatePassword() throws GeneralSecurityException, SQLException, TransException {
		return this;
	}

	public default String orgId() { return null; }

	public default String roleId() { return null; }

}
