package io.odysz.semantics;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import io.odysz.anson.Anson;
import io.odysz.common.AESHelper;
import io.odysz.common.EnvPath;
import io.odysz.semantics.meta.TableMeta;
import io.odysz.transact.x.TransException;

/**<p>Provide a user instance, e.g. servlet session information, to operate data in database.</p>
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
 * @since 1.1.1, sessionId is read only. If a new password have been updated,
 * just remove then re-login</p>
 * 
 * @author ody
 */
public interface IUser {
	/**
	 * Provide user table's meta for managing session by AnSession.
	 * @return table meta
	 * @throws SQLException 
	 * @throws SemanticException 
	 */
	TableMeta meta(String... connId) throws TransException, SQLException;

	/**
	 * <p>The sqls is committed to database, do something for logging. 
	 * If there are some operation needing to update db, return those sql statements.</p>
	 * <p>If the parameter sqls is returned, it will be committed again because the semantic connection
	 * is think it's the logging sql.</p>
	 * <b>Make sure the committed sqls is not returned, only logging sqls are needed.</b><br>
	 * @param sqls
	 * @return SQLs for logging, null for nothing to do
	 * @throws TransException 
	 */
	default ArrayList<String> dbLog(final ArrayList<String> sqls) throws TransException { return null; }

	/**
	 * Check user log in (already has pswd, iv and user Id from db)
	 * @param request request object. In sematic.jserv, it's SessionReq object.
	 * @return true: ok; false: failed
	 * @throws TransException Checking login information failed
	 */
	default boolean login(Object request) throws TransException { return false; }

	/** If a user is allowed to change password, this is used to verify old
	 * and must be overridden to check the old password cipher.
	 * @param pswdCypher64 decrypted with my token id
	 * @param iv64
	 * @return yes or no the old password is working
	 * @throws TransException
	 * @throws IOException 
	 * @throws GeneralSecurityException 
	 */
	default boolean guessPswd(String pswdCypher64, String iv64) throws TransException, GeneralSecurityException, IOException { return false; }

	default IUser sessionId(String rad64num) { return this; }

	/**
	 * A session Id can never be changed.
	 * If a new password been updated, just remove the session and re-login.
	 * @return the session token
	 */
	default String sessionId() { return null; }

	/**
	 * <p>Update last touched time stamp.</p>
	 * <p>FYI, if the session object will be terminated when time is out,
	 * this method must touch the current time to keep session alive.</p>
	 * <p>Note: science v1.3.5, this requires users implement a touch function, and return the instance.<p>
	 * @since 1.3.5
	 * */
	default IUser touch() { return this; };

	/**Last touched time in milliseconds, set by {@link #touch()}.<br>
	 */
	long touchedMs();

	/**user id */
	String uid() ;
	
	IUser logAct(String funcName, String funcId);

	/**
	 * @return farewell message, default null
	 */
	default SemanticObject logout() { return null; }

	default void writeJsonRespValue(Object writer) throws IOException {}

	/**
	 * Add notifyings
	 * @deprecated since 1.5.0, this is not necessary for session management,
	 * and user's notifications can be implemented in the business layer.
	 * @param note
	 * @return this
	 * @throws TransException 
	 */
	default public IUser notify(Object note) throws TransException { return this; };

	/**
	 * Get notified string list.
	 * 
	 * Since 1.5.0, this is not necessary for session management,
	 * and user's notifications can be implemented in the business layer.
	 * 
	 * @return notifications. Default null.
	 */
	default public List<Object> notifies() { return null; }

	/**
	 * Set session key, not session-id, called by {@link #getClientSessionInf(IUser)}.
	 * @since 1.4.37
	 * @param token
	 * @return this
	 */
	public default IUser sessionKey(String token) { return this; }

	/**
	 * Get session key
	 * @return this
	 */
	public default String sessionKey() { return null; }

	/**
	 * <p>Since v1.4.11, user object has this chance to initialize with Semantic.DA AnResultset.
	 * e.g. setting client device Id which is essential to doc synchronizing.</p>
	 * <p>Since v1.3.5, user object has this chance to initialize with login request.</p>
	 * @param sessionReqBody e.g. AnSessionReq
	 * @return this
	 * @throws SsException 
	 */
	public default IUser onCreate(Anson sessionReqBody) throws GeneralSecurityException { return this; }

	/**
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

	public default String deviceId() { return null; }

	public default String orgId() { return null; }

	public default String roleId() { return null; }

	/**
	 * Get a session object for client. Implementation must not reveal server side knowledge in this object.
	 * 
	 * This method calls {@link #sessionKey()} for setting tokens.
	 * 
	 * @param usr
	 * @return the session information
	 * @throws Exception 
	 */
	public default SessionInf getClientSessionInf(IUser usr) throws Exception { 
		Object[] session = AESHelper.packSessionKey(usr.pswd());
		usr.sessionKey((String) session[1]);

		return new SessionInf(usr.sessionId(), usr.uid(), usr.roleId())
				.device(usr.deviceId())
				.userName(usr.userName())
				.ssToken((String)session[0]);
	}

	/**
	 * Sign and encrypt session-key for login reply. The sskey is used for
	 * verifying jserv requests' headers.
	 * 
	 * @since 1.4.37
	 * @param sskey
	 * @return encrypt(sskey, pswd)
	 * @throws Exception 
	default String signSessionKey(String sskey)
			throws GeneralSecurityException, IOException {
		return sskey;
	}
	 */

	/**
	 * @since 1.4.36
	 * @return
	 */
	public default String userName() { return uid(); }

	/**
	 * TODO release doc.
	 * @since 1.4.25
	 * @return profiles
	 */
	public default Anson profile() { return null; }

	/**
	 * @since 1.4.38, verifying token by AnSession needs this.
	 * @return pswd
	 * @throws TransException 
	 */
	public default String pswd() throws TransException { throw new TransException("Must be overriden by subclass."); }

	/**
	 * @since 1.5.0
	 * @param string
	 * @return this
	 */
	default IUser orgId(String string) { return this; }

	/**
	 * @since 1.5.0
	 * @param string
	 * @return this
	 */
	default IUser roleId(String string) { return this; }

}
