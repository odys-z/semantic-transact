package io.odysz.semantics.sql;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import io.odysz.common.Utils;

public class TestTransc {

	private User user;
	private Transc st;

	@Before
	public void setUp() throws Exception {
		Utils.printCaller(true);

		user = new User("admin", "123456");

		DataSource ds0 = new DataSource();
		st = new Transc(ds0);
		assertFalse(ds0 == null);
	}

	@Test
	public void test() {
		ArrayList<String> sqls = new ArrayList<String>();

		st.select("a_funcs", "f")
			.j("a_rolefunc rf", Sql.condt("f.funcId=rf.funcId rf.roleId='%s'", user.userId()))
			.column("f.funcName", "func")
			.column("f.funcId", "fid")
			.where("=", "f.isUsed", "'Y'")
			.commit(sqls);

		Utils.logi(sqls);
	}

}
