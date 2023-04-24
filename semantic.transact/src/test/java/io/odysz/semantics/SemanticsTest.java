package io.odysz.semantics;

import static org.junit.Assert.assertEquals;
import static io.odysz.transact.sql.parts.condition.Funcall.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import io.odysz.common.Utils;
import io.odysz.common.dbtype;
import io.odysz.semantics.meta.ColMeta.coltype;
import io.odysz.semantics.meta.TableMeta;
import io.odysz.transact.sql.Query;
import io.odysz.transact.sql.Transcxt;
import io.odysz.transact.sql.Update;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.sql.parts.condition.Funcall;
import io.odysz.transact.x.TransException;

public class SemanticsTest {

	private Transcxt st;
	private Semantext2 mysqlCxt;
	private Semantext2 sqlitCxt;
	private Semantext2 ms2kCxt;
	private Semantext2 orclCxt;

	@Before
	public void setUp() throws Exception {
		HashMap<String,Semantics2> semantics = Semantics2.init("src/test/resources/semantics.xml");
		st = new Transcxt(new Semantext2("root", semantics, fakeMetas()));
		mysqlCxt = new Semantext3("root", semantics, fakeMetas()).dbtype(dbtype.mysql);
		sqlitCxt = new Semantext3("root", semantics, fakeMetas()).dbtype(dbtype.sqlite);
		ms2kCxt = new Semantext3("root", semantics, fakeMetas()).dbtype(dbtype.ms2k);
		orclCxt = new Semantext3("root", semantics, fakeMetas()).dbtype(dbtype.oracle);
	}

	@Test
	public void test() throws TransException {
		ArrayList<String> sqls = new ArrayList<String>();
		st.insert("a_functions")
			.nv("funcId", "AUTO")
			.nv("funcName", "Test 001")
			.nv("sibling", "10")
			.nv("parentId", "0")
			.commit(st.instancontxt(null, null), sqls);
		
		assertEquals(
			"insert into a_functions  (funcId, funcName, sibling, parentId, fullpath) values ('AUTO', 'Test 001', 10, '0', 'fullpath 0.0 AUTO')",
			sqls.get(0));
		
		ArrayList<ArrayList<Object[]>> vals = new ArrayList<ArrayList<Object[]>>();
		ArrayList<Object[]> r1 = new ArrayList<Object[]>();
		r1.add(new String[] {"roleId", "r01"});
		r1.add(new String[] {"funcId", "f01"});

		ArrayList<Object[]> r2 = new ArrayList<Object[]>();
		r2.add(new String[] {"roleId", "r02"});
		r2.add(new String[] {"funcId", "f02"});

		vals.add(r1);
		vals.add(r2);
		st.insert("a_role_funcs")
			.cols(new String[] {"roleId", "funcId"})
			.values(vals)
			.value(r2)
			.commit(st.instancontxt(null, null), sqls);

		assertEquals("insert into a_role_funcs  (roleId, funcId) values ('r01', 'f01'), ('r02', 'f02'), ('r02', 'f02')",
				sqls.get(1));
		Utils.logi(sqls);
	}

	/**Assert: 
	 * select f.funcName func from a_functions f join 
	 * ( select case rf.funcId when null then true else false from a_role_func rf ) tf 
	 * on rf.funcId = f.funcId",
	 * @throws TransException
	 */
	@Test
	public void testSelectJoin() throws TransException {
		ArrayList<String> sqls = new ArrayList<String>();

		Query q = st.select("a_functions", "f")
			.l(st.select("a_role_func", "rf")
					.col(Funcall.ifNullElse("rf.funcId", Boolean.TRUE, Boolean.FALSE)),
				"tf", "tf.funcId = f.funcId")
			.col("f.funcName", "func")
			.commit(st.instancontxt(null, null), sqls);
		assertEquals("select f.funcName func from a_functions f left outer join ( select case when rf.funcId is null then true else false end from a_role_func rf ) tf on tf.funcId = f.funcId",
				sqls.get(0));

		q.commit(orclCxt, sqls);
		assertEquals("select \"f\".\"funcName\" \"func\" from \"a_functions\" \"f\" left outer join ( select decode(\"rf\".\"funcId\", null, 1, 0) from \"a_role_func\" \"rf\" ) \"tf\" on \"tf\".\"funcId\" = \"f\".\"funcId\"",
				sqls.get(1));
	}
	
	@Test
	public void testSelectOracle() throws TransException {
		ArrayList<String> sqls = new ArrayList<String>();

		st.select("a_functions", "f")
			.l(st.select("a_role_func", "rf")
					.col(Funcall.ifNullElse("rf.funcId", Boolean.TRUE, Boolean.FALSE)),
					"tf", "tf.funcId = f.funcId")
			.col("f.funcName", "func")
			.commit(orclCxt, sqls);
		assertEquals("select \"f\".\"funcName\" \"func\" from \"a_functions\" \"f\" left outer join ( select decode(\"rf\".\"funcId\", null, 1, 0) from \"a_role_func\" \"rf\" ) \"tf\" on \"tf\".\"funcId\" = \"f\".\"funcId\"",
				sqls.get(0));
	}
	
	@Test
	public void testInsertOracle() throws TransException {
		ArrayList<String> sqls = new ArrayList<String>();

		ArrayList<Object[]> row0 = new ArrayList<Object[]>(2);
		row0.add(new Object[] { "funcName", "oracle 01" });
		row0.add(new Object[] { "funcId", "orcl-01" });
		ArrayList<Object[]> row1 = new ArrayList<Object[]>(2);
		row1.add(new Object[] { "funcName", "oracle 02" });
		row1.add(new Object[] { "funcId", "orcl-02" });

		st.insert("a_functions")
			.cols("funcName", "funcId")
			.value(row0)
			.value(row1)
			.commit(orclCxt, sqls);

		assertEquals("insert into \"a_functions\"  (\"funcName\", \"funcId\", \"fullpath\") select 'oracle 01', 'orcl-01', 'fullpath null.sibling orcl-01' from dual union select 'oracle 02', 'orcl-02', 'fullpath null.sibling orcl-02' from dual",
				sqls.get(0));
		
		ArrayList<Object[]> user = new ArrayList<Object[]>(2);
		user.add(new Object[] { "userName", "user 01" });
		user.add(new Object[] { "userId", "u-01" });

		st.insert("a_users")
			.cols("userName", "userId")
			.value(user)
			.commit(orclCxt, sqls);

		assertEquals("insert into \"a_users\"  (\"userName\", \"userId\") values ('user 01', 'u-01')",
				sqls.get(1));
	}

	/**Test function calls needing semantics.
	 * @throws TransException
	 */
	@Test
	public void testSmFuncall() throws TransException {
		ArrayList<String> sqls = new ArrayList<String>();
		st.update("a_roles")
			.nv("roleName", Funcall.concat("roleName", "'add 0 '", "'add 1'"))
			.where("=", "roleId", "'admin'")
			.commit(st.instancontxt(null, null), sqls);

		assertEquals("update  a_roles  set roleName=roleName || 'add 0 ' || 'add 1' where roleId = 'admin' ",
				sqls.get(0));
		
		st.select("a_roles")
			.col(Funcall.compound("roleName", "orgName"), "comp")
			.col("compound(col1, col2)", "rawsnippet")
			.commit(st.instancontxt(null, null), sqls);
		assertEquals("select roleName || '\n' || orgName comp, col1 || '\n' || col2 rawsnippet from a_roles ",
				sqls.get(1));
	}
	
	@Test
	public void testAvgCountSumMaxMinAlgorithm() throws TransException {
		ArrayList<String> sqls = new ArrayList<String>();
		st.select("a_roles")
			.col(avg("roleName"))
			.col(count("r"))
			.col(sum("r"))
			.col(max("r"))
			.col(min("r"))
			.col(add("l", "r"))
			.col(minus("l", "r"))
			.col(mul("l", "3"))
			.col(div("r", "0"), "err")
			.where("=", "roleId", "'admin'")
			.commit(st.instancontxt(null, null), sqls);

		assertEquals("select avg(roleName), count(r), sum(r), max(r), min(r), (l + r), (l - r), (l * 3), (r / 0) err from a_roles  where roleId = 'admin'",
				sqls.get(0));
		
	}
	
	@Test
	public void testFuncallOrcl() throws TransException {
		ArrayList<String> sqls = new ArrayList<String>();
		st.update("a_roles")
			.nv("roleName", Funcall.concat("roleName", "'add 0 '", "'add 1'"))
			.where("=", "roleId", "'admin'")
			.where(">", "r.stamp", "dateDiff(day, r.stamp, sysdate)")
			.commit(orclCxt, sqls);

		// update  "a_roles"  set "roleName"=roleName || 'add 0 ' || 'add 1' where "roleId" = 'admin' AND "r"."stamp" > dateDiff("day", "r"."stamp", "sysdate")
		assertEquals("update  \"a_roles\"  set \"roleName\"=\"roleName\" || 'add 0 ' || 'add 1' where \"roleId\" = 'admin' AND \"r\".\"stamp\" > dateDiff(day, \"r\".\"stamp\", sysdate) ",
				sqls.get(0));
		
		// WHERE decode("r"."stamp", NULL, sysdate, "r"."stamp") - sysdate > -0.1
		st.select("b_reports", "r")
			.j("b_repreocords", "rec", "r.repId = rec.repId")

			// bug fixed 2019.10.12
			// [recursive expression parse]
			// The correct call for oracle should be (no quotes):
			// .where(">", "decode(r.stamp, null, sysdate, r.stamp) - sysdate", "-0.1")
			// But the ExprsVisitor#parse() can only handle operands of string
			// This feature is only needed for oracle
			// FIX: ExprPart is the expression object, now can constructed from op, l-expr, r-expr.
			//      Then function argument list now can be parsed as ExprPart, which now can use column_name
			.where(">", "decode(r.stamp, null, sysdate, r.stamp) - sysdate", "-0.1")

			// ISSUE 2019.10.14 [Antlr4 visitor doesn't throw exception when parsing failed]
			// For a quoted full column name like "r"."stamp", in
			// .where(">", "decode(\"r\".\"stamp\", null, sysdate, r.stamp) - sysdate", "-0.1")
			// Antlr4.7.x/2 only report an error in console error output:
			// line 1:7 no viable alternative at input 'decode("r"'
			// This makes semantic-jserv won't report error until Oracle complain about sql error.
			.commit(orclCxt, sqls);

		// "sysdate" won't work
		// - all reserved words for oracle won't work
		assertEquals("select * from \"b_reports\" \"r\" join \"b_repreocords\" \"rec\" on \"r\".\"repId\" = \"rec\".\"repId\" "
				+ "where decode(\"r\".\"stamp\", null, sysdate, \"r\".\"stamp\") - sysdate > - 0.1",
				sqls.get(1));
	}
	
	/**See <a href='https://odys-z.github.io/notes/semantics/ref-transact.html#ref-transact-empty-vals'>
	 * How empty values are handled</a>
	 * @throws TransException
	 */
	@Test
	public void testEmptyVals() throws TransException {
		ArrayList<String> sqls = new ArrayList<String>();
		st.insert("a_functions")
			.nv("funcId", "AUTO")					// no coltype,		not empty	=> 'AUTO'
			.nv("funcName", "")						// type text,		blank 		=> ''
			.nv("sibling", ExprPart.constStr(null))	// type number,		null 		=> null
			.nv("someDat", "")						// type datetime,	blank 		=> ''
			.nv("parentId", ExprPart.constStr(null))// type text,		null		=> null
			.commit(st.instancontxt(null, null), sqls);
		
		assertEquals(
			"insert into a_functions  (funcId, funcName, sibling, someDat, parentId, fullpath) " +
			"values ('AUTO', '', null, '', null, 'fullpath null.null AUTO')",
			sqls.get(0));

		st.insert("a_functions")
			.nv("funcId", "AUTO")					// no coltype,		not empty	=> 'AUTO'
			.nv("funcName", ExprPart.constStr(null))// type text,		null 		=> null
			.nv("sibling", "")						// type number,		blank 		=> 0
			.nv("someDat", ExprPart.constStr(null))	// type datetime,	null 		=> null
			.nv("parentId", "")						// type text,		blank		=> ''
			.commit(st.instancontxt(null, null), sqls);
		
		assertEquals(
			"insert into a_functions  (funcId, funcName, sibling, someDat, parentId, fullpath) " +
			"values ('AUTO', null, 0, null, '', 'fullpath . AUTO')",
			sqls.get(1));
	}
	
	@Test
	public void testLimit() throws TransException {

		ArrayList<String> sqls = new ArrayList<String>();

		Update upd = st.update("a_users")
				.limit("3 + 1")
				.nv("userName", st.select("a_functions", "f")
								.limit("3 * 2", "5")
								.col("count(funcId)", "c")
								.where_("=", "f.funcName", "admin"))
				.where("=", "userId", "'admin'");
		upd.commit(mysqlCxt, sqls);
		upd.commit(sqlitCxt, sqls);
		upd.commit(ms2kCxt, sqls);
		upd.commit(orclCxt, sqls);
		Utils.logi(sqls);
		
		// mysql
		assertEquals("update  a_users  set userName=(select count(funcId) c from a_functions f where f.funcName = 'admin' limit 3 * 2, 5) where userId = 'admin' limit 3 + 1",
				sqls.get(0));
		// sqlite
		assertEquals("update  a_users  set userName=(select count(funcId) c from a_functions f where f.funcName = 'admin' limit 3 * 2, 5) where userId = 'admin' ",
				sqls.get(1));
		// ms2k
		assertEquals("update top(3 + 1) a_users  set userName=(select top(3 * 2) 5 count(funcId) c from a_functions f where f.funcName = 'admin') where userId = 'admin' ",
				sqls.get(2));
		// orcl
		assertEquals("update  \"a_users\"  set \"userName\"=(select count(\"funcId\") \"c\" from \"a_functions\" \"f\" where \"f\".\"funcName\" = 'admin') where \"userId\" = 'admin' ",
				sqls.get(3));
	}

	@SuppressWarnings("serial")
	public static HashMap<String, TableMeta> fakeMetas() {
		return new HashMap<String, TableMeta>() {
			{put("a_functions", fakeFuncsMeta());}
			{put("a_role_funcs", new TableMeta("a_role_funcs"));}
			{put("a_roles", new TableMeta("a_roles"));}
			{put("a_users", new TableMeta("a_users"));}
		};
	}

	private static TableMeta fakeFuncsMeta() {
		return new TableMeta("a_functions")
				.col("sibling", coltype.number)
				.col("someDat", coltype.datetime)
				.col("parentId", coltype.text);
	}
}
