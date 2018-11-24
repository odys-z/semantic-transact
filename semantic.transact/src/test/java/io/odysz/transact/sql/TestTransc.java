package io.odysz.transact.sql;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import io.odysz.common.Utils;
import io.odysz.transact.x.TransException;
import io.odysz.transact.sql.DataSource;
import io.odysz.transact.sql.Transcxt;
import io.odysz.transact.sql.parts.Sql;
import io.odysz.transact.sql.parts.Logic.op;

public class TestTransc {

	private User user;
	private Transcxt st;

	@Before
	public void setUp() throws Exception {
		Utils.printCaller(true);

		user = new User("admin", "123456");

		DataSource ds0 = new DataSource("stub");
//		ds0.addTabl("a_funcs");
//		ds0.addColumn("a_funcs", "funcName", "varchar", 50);
//		ds0.addColumn("a_funcs", "funcId", "varchar", 50);
//		ds0.addColumn("a_funcs", "isUsed", "varchar", 2);
//
//		ds0.addTabl("a_rolefunc");
//		ds0.addColumn("a_rolefunc", "funcId", "varchar", 50);
//		ds0.addColumn("a_rolefunc", "roleId", "varchar", 50);
//
//		ds0.addTabl("a_log");
//		ds0.addColumn("a_log", "stamp", "datetime", 50);
//		ds0.addColumn("a_log", "userId", "varchar", 50);
//		ds0.addColumn("a_log", "txt", "text", 0);
		
		st = new Transcxt(ds0);
	}

	@Test
	public void testSelect() throws TransException {
		ArrayList<String> sqls = new ArrayList<String>();

		st.select("a_funcs", "f")
			.j("a_rolefunc", "rf", Sql.condt("f.funcId=rf.funcId and rf.roleId='%s'", user.userId()))
			.col("f.funcName", "func")
			.col("f.funcId", "fid")
			.where("=", "f.isUsed", "'Y'")
			.commit(sqls);

		st.select("a_log", "lg")
			.col("lg.stamp", "logtime")
			.col("lg.txt", "log")
			.where(">=", "lg.stamp", "'1776-07-04'")
			.where(Sql.condt("userId IN (%s)", Sql.str(users())))
			.commit(sqls);
		
		st.select("a_log", "lg")
			.col("count(*)", "cnt")
			 .col("count", "cnt")
			.where("=", "userId", "funders")
			// (userId = 'user2' or userId = 'user3') and stamp <= '1911-10-10'
			.where(Sql.condt("userId = '%s'", "George").or("userId = '%s'", "Washinton"),
					Sql.condt("<=", "stamp", "'1911-10-10'"),
					Sql.condt(op.eq, "userId", "'Sun Yat-sen'"))
			.commit(sqls);

		Utils.logi(sqls);
		
		// .. .. ..
		// nothing 
	}
	
	@Test
	public void testInsert() throws TransException {
		ArrayList<String> sqls = new ArrayList<String>();
		st.insert("a_funcs")
			.nv("funcId", "a01")
			.commit(sqls);
		assertEquals(sqls.get(0), "insert into a_funcs  (funcId) values ( 'a01' )");
		
		ArrayList<Object[]> vals = new ArrayList<Object[]>(2);
		vals.add(new String[]{ "logId", "b01"});
		vals.add(null);
		vals.add(new String[]{ "txt", "log .... 01"});

		st.insert("a_log")
			.cols("logId", "stamp", "txt")
			.values(vals)
			.commit(sqls);
		assertEquals(sqls.get(1),
				"insert into a_log  (logId, stamp, txt) values ( 'b01', null, 'log .... 01' )");
		
		st.insert("a_rolefunc")
			.select(st.select("a_functions", "f")
						.col("f.funcId").col("'admin'")
						.j("a_roles", "r", "r.roleId='%s'", "admin"))
			.commit(sqls);

		Utils.logi(sqls);
	}
	
	@Test
	public void testUpdate() {
		ArrayList<String> sqls = new ArrayList<String>();
		st.update("a_users")
			.nv("userName", "'abc-x01'")
			.where("=", "userId", "admin");

		Utils.logi(sqls);
	}

	private String[] users() {
		return new String[] {"usr1", "user2"};
	}

}