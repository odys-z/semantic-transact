package io.odysz.transact.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import io.odysz.common.Utils;
import io.odysz.transact.sql.parts.Logic.op;
import io.odysz.transact.sql.parts.Sql;
import io.odysz.transact.sql.parts.antlr.ExprsVisitor;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.sql.parts.condition.Funcall;
import io.odysz.transact.x.TransException;

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
			.where("~%", "f.fullpath", "rf.funcId")
			.commit(st.instancontxt(null, null), sqls);
		assertEquals("select f.funcName func, f.funcId fid from a_funcs f join a_rolefunc rf on f.funcId = rf.funcId AND rf.roleId like '123456%' where f.isUsed = 'Y' AND f.funcName like '%bourgeoisie' AND f.fullpath like concat(rf.funcId, '%')",
				sqls.get(0));

		st.select("a_log", "lg")
			.col("lg.stamp", "logtime")
			.col("lg.txt", "log")
			.where(">=", "lg.stamp", "'1776-07-04'")
			.where(Sql.condt("userId IN (%s)", "'ele1','ele2','ele3'").escape(false))
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
			.where(Sql.condt("userId = '%s'", "George").or(Sql.condt("userId = '%s'", "Washington")),
					Sql.condt("<=", "stamp", "'1911-10-10'"),
					Sql.condt(op.eq, "userId", "'Sun Yat-sen'"))
			.orderby("cnt", "desc")
			.orderby("stamp")
			.commit(sqls);

		assertEquals("select count(*) cnt, count cnt from a_log lg where userId = funders AND (userId = 'George' OR userId = 'Washington') AND stamp <= '1911-10-10' AND userId = 'Sun Yat-sen' order by cnt desc, stamp asc",
				sqls.get(2));

			// 2019.10.12
		st.select("Orders")
			.col("Employees.LastName").col("COUNT(Orders.OrderID)", "NumberOfOrders")
			.j("Employees", null, "Orders.EmployeeID = Employees.EmployeeID)")
			.groupby("LastName")
			.having("COUNT(Orders.OrderID) > 10")
			.commit(sqls);
		assertEquals("select Employees.LastName, COUNT(Orders.OrderID) NumberOfOrders "
				+ "from Orders  join Employees null on Orders.EmployeeID = Employees.EmployeeID "
				+ "group by LastName "
				+ "having COUNT(Orders.OrderID) > 10",
				sqls.get(3));
		
		// 2021.9.12
		Query q = st.select("n_mykids", "u")
				.col("u.userId").col("userName").col("nebula").col("count(p.pid)", "todos")
				.l("polls", "p", "u.userId = p.userId and p.state in ('wait', 'poll')")
				.j("nebulae", "n", "n.nid = u.orgId")
				.groupby("p.userId");
	
		q.j("n_teaching", "o", String.format("o.class = n.nid and o.teacher = '%s'", "becky"));
		q.whereEq("n.nid", "ap01-22");

		q.whereLike("userName", "A")
			.commit(sqls);
		
		assertEquals("select u.userId, userName, nebula, count(p.pid) todos from n_mykids u left outer join polls p on u.userId = p.userId AND p.state in ('wait', 'poll') join nebulae n on n.nid = u.orgId join n_teaching o on o.class = n.nid AND o.teacher = 'becky' where n.nid = 'ap01-22' AND userName like '%A%' group by p.userId",
				sqls.get(4));

	}
	
	@Test
	public void testSelect_Union() throws TransException {
		ArrayList<String> sqls = new ArrayList<String>();
		Query q2 = st.select("a_users", "u")
			.j("a_roles", "r", Sql.condt("u.roleId=r.roleId and r.roleName='bourgeoisie'"))
			.col("u.userName")
			.col("r.roleName");
		Query q1 = st.select("a_users", "u")
			.j("a_roles", "r", Sql.condt("u.roleId=r.roleId and r.roleName='farmer'"))
			.col("u.userName")
			.col("r.roleName")
			.except(q2);

		st.select("a_users", "u")
			.j("a_roles", "r", Sql.condt("u.roleId=r.roleId and r.roleName='nationalist'"))
			.col("u.userName")
			.col("r.roleName")
			.asQueryExpr().union(q1, true)
			.commit(sqls);

		assertEquals("select u.userName, r.roleName from a_users u " +  
				"join a_roles r on u.roleId = r.roleId AND r.roleName = 'nationalist' " + 
				"union " + 
				"(select u.userName, r.roleName from a_users u " + 
				"join a_roles r on u.roleId = r.roleId AND r.roleName = 'farmer' " + 
				"except " + 
				"select u.userName, r.roleName from a_users u " + 
				"join a_roles r on u.roleId = r.roleId AND r.roleName = 'bourgeoisie')",
				sqls.get(0));
	}
	
	@Test
	public void testFunc() throws TransException {
		ArrayList<String> sqls = new ArrayList<String>();
		st.select("a_funcs", "f")
			.j("a_rolefunc", "rf", Sql.condt("f.funcId=rf.funcId and rf.roleId='%s'", user.userId()))
			.col("f.funcName is not null", "checked")
			.col("f.funcId", "fid")
			.col("substring(notes, 1, 16)", "notes")
			.where(">", "r.stamp", "dateDiff(day, r.stamp, sysdate)")
			.commit(st.instancontxt(null, null), sqls);

		assertEquals("select f.funcName is not null checked, f.funcId fid, substring(notes, 1, 16) notes " +
				"from a_funcs f join a_rolefunc rf on f.funcId = rf.funcId AND rf.roleId = '123456' where r.stamp > dateDiff(day, r.stamp, sysdate)",
				sqls.get(0));
	}
	
	/**<pre>aggregate_windowed_function
    : (AVG | MAX | MIN | SUM | STDEV | STDEVP | VAR | VARP)
      '(' full_column_name ')'
    | (COUNT | COUNT_BIG)
      '(' ('*' | full_column_name) ')'
    ; </pre>
	 * @throws TransException
	 */
	@Test
	public void testAggregateFunc() throws TransException {
		ArrayList<String> sqls = new ArrayList<String>();
		st.select("a_funcs", "f")
			.col("STDEV(notes)", "notes")
			.col("AVG(col1)", "avgcol")
			.j("a_rolefunc", "rf", Sql.condt("COUNT(Orders.OrderID) > 5"))
			.where(">", "r.stamp", "dateDiff(day, r.stamp, sysdate)")
			.groupby("max(col2) > 3")
			.having("sum(Orders.OrderID) > 10")
			.commit(st.instancontxt(null, null), sqls);

		// Utils.logi(sqls.get(0));
		assertEquals("select STDEV(notes) notes, AVG(col1) avgcol from a_funcs f "
				+ "join a_rolefunc rf on COUNT(Orders.OrderID) > 5 "
				+ "where r.stamp > dateDiff(day, r.stamp, sysdate) "
				+ "group by max(col2) > 3 "
				+ "having sum(Orders.OrderID) > 10",
				sqls.get(0));
	}
	
	@Test
	public void testInsert() throws TransException {
		ArrayList<String> sqls = new ArrayList<String>();
		st.insert("a_funcs")
			.nv("funcId", "a01")
			.nv("funcName", "")
			// because of no semantext, null is handled as constant string, resulting 'null'
			.nv("uri", ExprPart.constStr(null))
			.commit(sqls);
		assertEquals(sqls.get(0),
				"insert into a_funcs  (funcId, funcName, uri) values ('a01', '', null)");
		
		ArrayList<Object[]> vals = new ArrayList<Object[]>(3);
		vals.add(new String[]{ "logId", "b01"});
		vals.add(null);
		vals.add(new String[]{ "txt", "log .... 01"});

		st.insert("a_log")
			.cols("logId", "stamp", "txt")
			.value(vals)
			.commit(sqls);
		assertEquals("insert into a_log  (logId, stamp, txt) values ('b01', null, 'log .... 01')",
					sqls.get(1));
		
		
		ArrayList<Object[]> nullrow = new ArrayList<Object[]>(3);
		nullrow.add(new Object[] { null, null});
		nullrow.add(new Object[] { null, null});
		nullrow.add(new Object[] { null, null});

		sqls.clear();
		st.insert("a_log")
			.cols("logId", "stamp", "txt")
			.value(nullrow)
			.value(vals)
			.commit(sqls);
		assertEquals("insert into a_log  (logId, stamp, txt) values ('b01', null, 'log .... 01')",
					sqls.get(0));
	}

	@SuppressWarnings("serial")
	@Test
	public void testInsertPartialRow() throws TransException {
		String[] cols = new String[] {"funcId", "funcName", "uri", "css", "ext1", "ext2"};
		ArrayList<Object[]> row1 = new ArrayList<Object[]>() {
			{add(new Object[] {"funcId", "f01"});}
			{add(new String[] {"funcName", "f01-name"});}
			{add(new String[] {"uri", "/pp/aa"});}
			{add(new String[] {"css", null});}
			{add(new String[] {"ext1", "extra 1/f01"});}
			{add(new String[] {"ext2", "extra 2/f01"});}
		};
			
		ArrayList<String> sqls = new ArrayList<String>();
		st.insert("a_funcs")
			.nv("funcName", "pp")
			.nv("funcId", "a01")
			.nv("uri", ExprPart.constStr(null))
			.commit(sqls);
		assertEquals("insert into a_funcs  (funcName, funcId, uri) values ('pp', 'a01', null)",
				sqls.get(0));

		st.insert("a_funcs")
			.cols(cols)	// "funcId", "funcName", "uri", "css", "ext1", "ext2"
			.value(row1)
			.commit(sqls);
		assertEquals("insert into a_funcs  (funcId, funcName, uri, css, ext1, ext2) values "
				+ "('f01', 'f01-name', '/pp/aa', null, 'extra 1/f01', 'extra 2/f01')",
				sqls.get(1));

		// illegal row2
		ArrayList<Object[]> row2 = new ArrayList<Object[]>() {
			{add(new String[] {});}
			{add(new String[] {"funcId", "f02"});}
			{add(new String[] {"funcName", "f02-name"});}
			{add(new String[] {"css", ".cls {color: red;}"});}
			{add(null);}
			{add(new String[] {null, null, null});}
			{add(new String[] {"funcId"});}
			{add(new String[] {null, "f0x"});}
		};	

		try {
			st.insert("a_funcs")
			.cols(cols)	// "funcId", "funcName", "uri", "css", "ext1", "ext2"
			.value(row2);
			// too much columns
			fail("Column number checking failed.");
		} catch (TransException e) {
			assertEquals("columns' number is less than rows field count.",
				e.getMessage());
		}
		
		row2.remove(row2.size() -1);
		row2.remove(row2.size() -1);
		st.insert("a_funcs")
			.cols(cols)	// "funcId", "funcName", "uri", "css", "ext1", "ext2"
			.value(row2)
			.commit(sqls);
		assertEquals("insert into a_funcs  (funcId, funcName, uri, css, ext1, ext2) "
				+ "values ('f02', 'f02-name', null, '.cls {color: red;}', null, null)",
				sqls.get(2));

		row2.remove(row2.size() -1);
		row2.remove(row2.size() -1);
		st.insert("a_funcs")
			.cols(cols)	// "funcId", "funcName", "uri", "css", "ext1", "ext2"
			.value(row2)
			.commit(sqls);
		assertEquals("insert into a_funcs  (funcId, funcName, uri, css, ext1, ext2) "
				+ "values ('f02', 'f02-name', null, '.cls {color: red;}', null, null)",
				sqls.get(3));

		// value() has a side effect on row2, now row 2 has an extra nv can't pass checking
		row2.remove(0);
		row2.remove(row2.size() -1);
		row2.remove(row2.size() -1);
		row2.remove(row2.size() -1);
		ArrayList<ArrayList<Object[]>> rows = new ArrayList<ArrayList<Object[]>>() {
			{add(new ArrayList<Object[]>(){ {add(new String[] {});} });}
			{add(row1);}
			{add(null);}
			{add(new ArrayList<Object[]>() {
					{add(null);}
			});}
			{add(new ArrayList<Object[]>(){
							{add(null);}
							{add(new String[] {null, "a"});} // row 4, col 1
							});
			}
			{add(row2);}
			{add(row1);}
		};

		try {
			st.insert("a_funcs")
				.cols(cols)	// "funcId", "funcName", "uri", "css", "ext1", "ext2"
				.values(rows);
			fail("Checking nvs failed.");
		}catch (TransException e) {
			assertEquals("Invalid nv: [null, a]",
					e.getMessage());
		}
		
		rows.get(4).remove(1);
		rows.get(0).remove(0);
		rows.get(3).remove(0);

		st.insert("a_funcs")
			.cols(cols)	// "funcId", "funcName", "uri", "css", "ext1", "ext2"
			.value(row2)
			.values(rows)
			.value(row1)
			.commit(sqls);
		assertEquals("insert into a_funcs  "
				+ "(funcId, funcName, uri, css, ext1, ext2) values "
				+ "('f02', 'f02-name', null, '.cls {color: red;}', null, null), "
				+ "('f01', 'f01-name', '/pp/aa', null, 'extra 1/f01', 'extra 2/f01'), "
				+ "('f02', 'f02-name', null, '.cls {color: red;}', null, null), "
				+ "('f01', 'f01-name', '/pp/aa', null, 'extra 1/f01', 'extra 2/f01'), "
				+ "('f01', 'f01-name', '/pp/aa', null, 'extra 1/f01', 'extra 2/f01')",
				sqls.get(4));
		
		ArrayList<Object[]> nullrow = new ArrayList<Object[]>() {
			{add(new Object[] {"funcId", null});}
			{add(new String[] {"funcName", null});}
			{add(new String[] {"uri", null});}
			{add(new String[] {"css", null});}
			{add(new String[] {"ext1", null});}
			{add(new String[] {"ext2", null});}
		};
		st.insert("a_funcs")
			.cols(cols)
			.value(nullrow)
			.commit(sqls);
		assertEquals(5, sqls.size());
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
		assertEquals("update  a_users  set userName='abc-x01' where userId = 'admin' ",
				sqls.get(0));
		
		sqls.clear();
		st.update("a_users")
			.nv("userName", st.select("a_functions", "f")
								.col("count(funcId)", "c")
								.where_("=", "f.funcName", "admin"))
			.where("=", "userId", "'admin'")
			.commit(sqls);
		assertEquals("update  a_users  set userName=(select count(funcId) c from a_functions f where f.funcName = 'admin') where userId = 'admin' ",
				sqls.get(0));

	}
	
	/** Sqlite tested case
	 *<pre>with backtrace (indId, parent, fullpath) as (
	select indId indId, parent parent, fullpath fullpath from ind_emotion where indId = 'C' 
	union all 
	select me.indId, me.parent, p.fullpath || '.' || printf('%02d', sort) from backtrace p join ind_emotion me on me.fullpath = p.indId
	) update ind_emotion set fullpath = (
	select fullpath from backtrace t where ind_emotion.indId = t.indId) where indId in (select indId from backtrace)
	</pre>
	 * @throws TransException
	 */
	@Test
	public void testUpdateJoin() throws TransException {
		ArrayList<String> sqls = new ArrayList<String>();
		try {
			st.update("a_users")
				.nv("userName", Funcall.concat("userName", "o.orgName"))
				// TODO .j("a_org", "o", "o.orgId = a_users.orgId")
				.commit(sqls);
		} catch (Exception e) {
			Utils.warn("Call for features: with clause(recursive for sqlite 13.12.5, mysql v8, oracle 11gr2) & update from select...");
		}
	}

	@Test
	public void testCondtPrecedence() throws TransException {
		ArrayList<String> sqls = new ArrayList<String>();
		st.update("a_users")
			.nv("userName", "abc-x01")
			.where(Sql.condt("userId = 'admin'"))
			.where(Sql.condt("userName = 'Washington' or userName = 'Washinton'"))
			.commit(sqls);

		assertEquals("update  a_users  set userName='abc-x01' where userId = 'admin' AND (userName = 'Washington' OR userName = 'Washinton') ",
				sqls.get(0));

		st.select("a_users", "u")
			.j("a_org", "o", Sql.condt("u.orgId = o.orgId or (u.orgId = '%s' and (u.name <> '%s' or u.name <> '%s'))",
					"ChaoYang People", "James Bond", "007"))
			.commit(sqls);

		// select * from a_users u 
		// join a_org o on u.orgId = o.orgId OR
		// (u.orgId = 'ChaoYang People' AND (u.name <> 'admin' OR u.name <> '007'))
		assertEquals("select * from a_users u join a_org o on u.orgId = o.orgId OR (u.orgId = 'ChaoYang People' AND (u.name <> 'James Bond' OR u.name <> '007'))",
				sqls.get(1));
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
					.nv("roleName", new ExprPart("roleName || 'abc'").escape(false))
					.where("=", "roleId", "'admin'"))
			.commit(sqls);

		Utils.logi(sqls);
		assertEquals("insert into a_rolefunc   select f.funcId, 'admin' roleId, 'c,r,u,d' from a_functions f join a_roles r on r.roleId = 'admin'",
				sqls.get(0));

		assertEquals("update  a_roles  set funcount=(select count(funcId) from a_rolefunc  where roleId = 'admin'), roleName=roleName || 'abc' where roleId = 'admin' ",
				sqls.get(1));
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
					.nv("roleId", "AUTO")
					.where_("=", "roleId", "AUTO"))
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
		assertTrue(sqls.get(1).startsWith("update  a_rolefunc"));
		assertTrue(sqls.get(2).startsWith("insert into a_roles"));
		assertTrue(sqls.get(3).startsWith("insert into a_rolefunc"));
	}
	
	@Test
	public void testUpd_del_insts() throws TransException {
		ArrayList<String> sqls = new ArrayList<String>();
		st.update("a_roles")
			.nv("roleName", "role-21")
			.where_("=", "roleId", "role 01")
			.post(st.delete("a_rolefunc")
					.where_("=", "roleId", "role 01")
					.post(st.insert("a_rolefunc")
							.nv("funcId", "f 001")
							.nv("roleId", "role 01")))
			.commit(sqls);
		assertEquals("update  a_roles  set roleName='role-21' where roleId = 'role 01' ",
				sqls.get(0));
		assertEquals("delete from a_rolefunc where roleId = 'role 01'",
				sqls.get(1));
		assertEquals("insert into a_rolefunc  (funcId, roleId) values ('f 001', 'role 01')",
				sqls.get(2));
	}

	@Test
	public void testExprVals() throws TransException {
		ArrayList<String> sqls = new ArrayList<String>();
		st.insert("a_roles")
			.nv("roleName", "roleName'-new")
			.nv("roleId", ExprsVisitor.parse("roleName + 3"))
			.nv("s1", "'s - %'x")
			.nv("s2", "''")
			.nv("s3", "%%")
			.where_("=", "roleId", "role 01") // ignored safely
			.post(st.update("a_rolefunc")
					.nv("roleId", ExprsVisitor.parse("3 * 2"))

					// where*() function generate a Predicate, which will default ignore escaping,
					// so you should get this: roleName = 'roleName-old'' .
					// If you wanna this: roleName = 'roleName-old''',
					// you need create a condition with escaped like this:
					// .where(Sql.condt("roleName = '%s'", "roleName-old'''"))	// roleName = 'roleName-old'''
					// or this:
					// .whereEq("roleName", new ExprPart("'roleName-old'''"))	// roleName = 'roleName-old'''
					.where(Sql.condt(op.eq, "roleName", new ExprPart("'roleName-old''")).escape(true))	// roleName = 'roleName-old'''

					// .whereEq("roleName", new ExprPart("'roleName-old''"))	// roleName = 'roleName-old''
					
					// FIXME Predicate is designed to handle "'" when escape = true.
					// But this test shows that it parsed the Condt string "'roleName-old''" into "'roleName-old'".
					// Is this a grammar mistakes?
					// docs: notes/semantics/ref-transact.html#issue-sql-condt
					
					.where_("=", "roleId", "role 01"))
			.commit(sqls);
		
		assertEquals("insert into a_roles  (roleName, roleId, s1, s2, s3) values ('roleName''-new', roleName + 3, '''s - %''x', '''''', '%%')",
				sqls.get(0));
		assertEquals("update  a_rolefunc  set roleId=3 * 2 where roleName = 'roleName-old''' AND roleId = 'role 01' ",
				sqls.get(1));

		st.update("a_roles")
			.nvs(new ArrayList<Object[]>() {
				private static final long serialVersionUID = 1L;
				{add(new Object[] {"roleId", 1});} })
			.where_("=", "roleId", "role 01")
			.commit(sqls);
		assertEquals("update  a_roles  set roleId=1 where roleId = 'role 01' ",
				sqls.get(2));
	}
	
	@Test
	public void testWhereInArray() throws TransException {
		ArrayList<String> sqls = new ArrayList<String>();
		st.update("a_rolefunc")
			.nv("roleId", ExprsVisitor.parse("3 * 2"))
			.whereIn("roleId", new String[] {"01", "bb"})
			.commit(sqls);

		assertEquals("update  a_rolefunc  set roleId=3 * 2 where roleId in ('01', 'bb') ",
				sqls.get(0));
	}
}
