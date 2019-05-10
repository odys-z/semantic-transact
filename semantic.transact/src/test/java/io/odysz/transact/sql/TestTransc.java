package io.odysz.transact.sql;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import io.odysz.common.Utils;
import io.odysz.transact.x.TransException;
import io.odysz.transact.sql.parts.Sql;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.sql.parts.Logic.op;

public class TestTransc {

	private User user;
	private Transcxt st;

	@Before
	public void setUp() throws Exception {
		Utils.printCaller(false);

		user = new User("admin", "123456");
		st = new Transcxt(null);
	}

	@Test
	public void testSelect() throws TransException {
		ArrayList<String> sqls = new ArrayList<String>();

		st.select("a_funcs", "f")
			.j("a_rolefunc", "rf", Sql.condt("f.funcId=rf.funcId and rf.roleId~%%'%s'", user.userId()))
			.col("f.funcName", "func")
			.col("f.funcId", "fid")
			.where("=", "f.isUsed", "'Y'")
			.where("%~", "f.funcName", "'bourgeoisie'")
			.commit(st.instancontxt(null), sqls);
		assertEquals("select f.funcName func, f.funcId fid from a_funcs f join a_rolefunc rf on f.funcId = rf.funcId AND rf.roleId like '123456%' where f.isUsed = 'Y' AND f.funcName like '%bourgeoisie'",
				sqls.get(0));

		st.select("a_log", "lg")
			.col("lg.stamp", "logtime")
			.col("lg.txt", "log")
			.where(">=", "lg.stamp", "'1776-07-04'")
			.where(Sql.condt("userId IN (%s)", "'ele1','ele2','ele3'"))
			// .where(Sql.condt("userId IN (%s)", "'ele1','ele2','ele3'"))
			.groupby("lg.stamp")
			.groupby("log")
			.commit(sqls);
		assertEquals("select lg.stamp logtime, lg.txt log from a_log lg where lg.stamp >= '1776-07-04' AND userId in ('ele1', 'ele2', 'ele3') group by lg.stamp, log",
				sqls.get(1));
		
		st.select("a_log", "lg")
			.page(0, 20)
			.col("count(*)", "cnt")
			.col("count", "cnt")
			.where("=", "userId", "funders")
			// (userId = 'user2' or userId = 'user3') and stamp <= '1911-10-10'
			.where(Sql.condt("userId = '%s'", "George").or("userId = '%s'", "Washington"),
					Sql.condt("<=", "stamp", "'1911-10-10'"),
					Sql.condt(op.eq, "userId", "'Sun Yat-sen'"))
			.orderby("cnt", "desc")
			.orderby("stamp")
			.commit(sqls);

		assertEquals("select count(*) cnt, count cnt from a_log lg where userId = funders AND userId = 'George' AND stamp <= '1911-10-10' AND userId = 'Sun Yat-sen' order by cnt desc, stamp asc",
				sqls.get(2));
	}

	@Test
	public void testFunc() throws TransException {
		ArrayList<String> sqls = new ArrayList<String>();
		st.select("a_funcs", "f")
			.j("a_rolefunc", "rf", Sql.condt("f.funcId=rf.funcId and rf.roleId='%s'", user.userId()))
			.col("f.funcName is not null", "checked")
			.col("f.funcId", "fid")
			.col("substring(notes, 1, 16)", "notes")
			.commit(st.instancontxt(null), sqls);

		assertEquals("select f.funcName is not null checked, f.funcId fid, substring(notes, 1, 16) notes " +
				"from a_funcs f join a_rolefunc rf on f.funcId = rf.funcId AND rf.roleId = '123456'",
				sqls.get(0));
	}
	
	@Test
	public void testInsert() throws TransException {
		ArrayList<String> sqls = new ArrayList<String>();
		st.insert("a_funcs")
			.nv("funcId", "a01")
			.commit(sqls);
		assertEquals(sqls.get(0), "insert into a_funcs  (funcId) values ('a01')");
		
		ArrayList<Object[]> vals = new ArrayList<Object[]>(2);
		vals.add(new String[]{ "logId", "b01"});
		vals.add(null);
		vals.add(new String[]{ "txt", "log .... 01"});

		st.insert("a_log")
			.cols("logId", "stamp", "txt")
			.value(vals)
			.commit(sqls);
		assertEquals(sqls.get(1),
				"insert into a_log  (logId, stamp, txt) values ('b01', null, 'log .... 01')");
	}

	@Test
	public void testUpdate() throws TransException {
		ArrayList<String> sqls = new ArrayList<String>();
		st.update("a_users")
			.nv("userName", "abc-x01")
			.where("=", "userId", "'admin'")
			.commit(sqls);

		// update a_users  set userName='abc-x01' where userId = 'admin'
		// Utils.logi(sqls);
		assertEquals("update a_users  set userName='abc-x01' where userId = 'admin'",
				sqls.get(0));
	}

	@Test
	public void testInsertSelectPostUpdate() throws TransException {
		ArrayList<String> sqls = new ArrayList<String>();
		st.insert("a_rolefunc")
			.select(st.select("a_functions", "f")
					// .col("f.funcId").col("'admin'").col("'c,r,u,d'")
					.cols("f.funcId", "'admin' roleId", "'c,r,u,d'")
					.j("a_roles", "r", "r.roleId='%s'", "admin"))
			.post(st.update("a_roles")
					.nv("funcount", st.select("a_rolefunc")
										.col("count(funcId)")
										.where("=", "roleId", "'admin'"))
					.nv("roleName", new ExprPart("roleName || 'abc'"))
					.where("=", "roleId", "'admin'"))
			.commit(sqls);

		// insert into a_rolefunc   select f.funcId, 'admin' roleId, 'c,r,u,d' from a_functions f join a_roles r on r.roleId = 'admin'
		// update a_roles  set funcount=(select count(funcId) from a_rolefunc  where roleId = 'admin'), roleName=roleName || 'abc' where roleId = 'admin'
		// Utils.logi(sqls);
		assertEquals(sqls.get(0),
				"insert into a_rolefunc   select f.funcId, 'admin' roleId, 'c,r,u,d' from a_functions f join a_roles r on r.roleId = 'admin'");
		assertEquals(sqls.get(1),
				"update a_roles  set funcount=(select count(funcId) from a_rolefunc  where roleId = 'admin'), roleName=roleName || 'abc' where roleId = 'admin'");
	}

	@Test
	public void testInsertAutoUpdate() throws TransException {
		ArrayList<String> sqls = new ArrayList<String>();
		st.insert("a_roles")
			.nv("roleId", "AUTO")
			.nv("roleName", "role-2")
			.nv("funcount", "0")
			.post(st.update("a_rolefunc")
					.nv("funcId", "f-01")
					.nv("roleId", "AUTO"))
			.commit(sqls);
	
		st.insert("a_roles")
			.nv("roleId", "AUTO")
			.nv("roleName", "role-2")
			.nv("funcount", "0")
			.post(st.insert("a_rolefunc")
					.nv("funcId", "f-01")
					.nv("roleId", "AUTO"))
			.commit(sqls);

		// insert into a_roles  (roleId, roleName, funcount) values ( 'AUTO #2018-12-02 10:02:23', 'role-2', '0' )
		// update a_rolefunc  set funcId='f-01', roleId='AUTO #2018-12-02 10:02:23'
		// insert into a_roles  (roleId, roleName, funcount) values ( 'AUTO #2018-12-02 10:02:30', 'role-2', '0' )
		// insert into a_rolefunc  (funcId, roleId) values ( 'f-01', 'AUTO #2018-12-02 10:02:30' )
		// Utils.logi(sqls);
		assertTrue(sqls.get(0).startsWith("insert into a_roles"));
		assertTrue(sqls.get(1).startsWith("update a_rolefunc"));
		assertTrue(sqls.get(2).startsWith("insert into a_roles"));
		assertTrue(sqls.get(3).startsWith("insert into a_rolefunc"));
	}
}
