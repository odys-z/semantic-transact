package io.odysz.semantics;

import io.odysz.anson.Anson;

public class SessionInf extends Anson {
	String ssid;
	String uid;
	String roleId;
	String userName;
	String roleName;
	
	/**
	 * Session Token
	 * @since 1.4.37
	 */
	public String ssToken;

	/**
	 * Last Sequence
	 * @since 1.4.37
	 */
	int seq;

	public String device;
	public SessionInf device(String dev) { this.device = dev; return this; }
	
	public SessionInf () {
	}
	
	public SessionInf (String ssid, String uid, String... roleId) {
		this.ssid = ssid;
		this.uid = uid;
		this.roleId = roleId == null || roleId.length == 0 ? null : roleId[0];
	}
	
	public String ssid() { return ssid; }
	public SessionInf ssid(String id) {
		this.ssid = id;
		return this;
	}

	public String uid() { return uid; }
	public SessionInf uid(String id) {
		this.uid = id;
		return this;
	}

	public String roleId() { return roleId; }
	public SessionInf roleId(String id) {
		this.roleId = id;
		return this;
	}

	public String userName() { return userName; }
	public SessionInf userName(String name) {
		this.userName = name;
		return this;
	}

	public String roleName() { return roleName; }
	public SessionInf roleName(String name) {
		this.roleName = name;
		return this;
	}

	public SessionInf ssToken(String sessionKey) {
		this.ssToken = sessionKey;
		return this;
	}
}
