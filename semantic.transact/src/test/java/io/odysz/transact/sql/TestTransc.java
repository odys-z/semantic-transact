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

	private String[] users() {
		return new String[] {"usr1", "user2"};
	}

	@Test
	public void testSelect() throws TransException {
		ArrayList<String> sqls = new ArrayList<String>();

		st.select("a_funcs", "f")
			.j("a_rolefunc", "rf", Sql.condt("f.funcId=rf.funcId and rf.roleId='%s'", user.userId()))
			.col("f.funcName", "func")
			.col("f.funcId", "fid")
			.where("=", "f.isUsed", "'Y'")
			.commit(st.instancontxt(null), sqls);

		st.select("a_log", "lg")
			.col("lg.stamp", "logtime")
			.col("lg.txt", "log")
			.where(">=", "lg.stamp", "'1776-07-04'")
			.where(Sql.condt("userId IN (%s)", Sql.str(users())))
			.groupby("lg.stamp")
			.groupby("log")
			.commit(sqls);
		
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
		// TODO assert OR

		/* Added case: ignored table / alias from client
select userId userId, userName userName, mobile mobile, dept.orgId orgId, a_reg_org.orgName orgName, 
dept.departName departName, dept.departId departId, r.roleId roleId, r.roleName roleName, notes notes 
from a_user  
join a_reg_org  on orgId = orgId 
left outer join a_org_depart dept on departId = dept.departId 
left outer join a_roles r on roleId = roleId 
		st.select("a_user")
			.col("userId", "userId").col("userName").col("dept.orgId", "orgId").col("a_org_depart.orgName")
			.j("a_reg_org", "orgId =orgId ")
			.l("a_org_depart", "dept", "departId = dept.departId")
			.l("a_roles", "r", "roleId=roleId")
			.where("like", "a_user.userName", "''")
			.where("=", "a_user.orgId", "''")
			.commit(sqls);

		Utils.logi(sqls);

		String last = sqls.get(sqls.size() - 1);
		int lastlen = last.length();
		assertEquals("a_user.roleId = r.roleId where a_user.userName like '' AND a_user.orgId = ''",
				last.substring(lastlen - 75, lastlen));
		 */
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
		// Utils.logi(sqls);
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
