package io.odysz.semantics.sql;

public class User {
	private String user;
	private String id;

	public User(String uname, String uid) {
		user = uname;
		id = uid;
	}

	public String userId() { return id;}
	public String userName() { return user; }
}
