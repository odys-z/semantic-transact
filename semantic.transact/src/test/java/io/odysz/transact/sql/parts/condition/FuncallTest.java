package io.odysz.transact.sql.parts.condition;

import static org.junit.jupiter.api.Assertions.*;
import static io.odysz.semantics.SemanticsTest.fakeMetas;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.odysz.common.dbtype;
import io.odysz.semantics.Semantext2;
import io.odysz.semantics.Semantext3;
import io.odysz.semantics.Semantics2;
import io.odysz.transact.sql.Query;
import io.odysz.transact.sql.Transcxt;

class FuncallTest {
	private static Transcxt st;
	private static Semantext2 mysqlCxt;
	private static Semantext2 sqlitCxt;
	private static Semantext2 ms2kCxt;
	private static Semantext2 orclCxt;

	@BeforeAll
	public static void setUp() throws Exception {
		HashMap<String,Semantics2> semantics = Semantics2.init("src/test/resources/semantics.xml");
		st = new Transcxt(new Semantext2("root", semantics, fakeMetas()));
		mysqlCxt = new Semantext3("root", semantics, fakeMetas()).dbtype(dbtype.mysql);
		sqlitCxt = new Semantext3("root", semantics, fakeMetas()).dbtype(dbtype.sqlite);
		ms2kCxt = new Semantext3("root", semantics, fakeMetas()).dbtype(dbtype.ms2k);
		orclCxt = new Semantext3("root", semantics, fakeMetas()).dbtype(dbtype.oracle);
	}

	@Test
	void testSubstr() throws Exception {
		ArrayList<String> sqls = new ArrayList<String>();

		Query q = st.select("a_roles")
			.col(Funcall.subStr("roleName", 1, 4), "subname");


		q.commit(st.instancontxt(null, null), sqls);
		assertEquals("select substr(roleName, 1, 4) subname from a_roles ",
				sqls.get(0));	

		q.commit(mysqlCxt, sqls);
		assertEquals("select substring(roleName, 1, 4) subname from a_roles ",
				sqls.get(1));	

		q.commit(sqlitCxt, sqls);
		assertEquals("select substr(roleName, 1, 4) subname from a_roles ",
				sqls.get(2));	

		q.commit(ms2kCxt, sqls);
		assertEquals("select substring(roleName, 1, 4) subname from a_roles ",
				sqls.get(3));	

		q.commit(orclCxt, sqls);
		assertEquals("select substr(roleName, 1, 4) \"subname\" from \"a_roles\" ",
				sqls.get(4));	
	}

	@Test
	void testIfElse() throws Exception {
		ArrayList<String> sqls = new ArrayList<String>();

		Query q = st.select("a_roles")
			.col(Funcall.ifElse("roleName", "rolId", "orgId"), "ifname");

		q.commit(st.instancontxt(null, null), sqls);
		assertEquals("select case when roleName then rolId else orgId end ifname from a_roles ",
				sqls.get(0));	

		q.commit(mysqlCxt, sqls);
		assertEquals("select if(roleName, rolId, orgId) ifname from a_roles ",
				sqls.get(1));	

		q.commit(sqlitCxt, sqls);
		assertEquals("select case when roleName then rolId else orgId end ifname from a_roles ",
				sqls.get(2));	

		q.commit(ms2kCxt, sqls);
		assertEquals("select case when roleName then rolId else orgId end ifname from a_roles ",
				sqls.get(3));	

		q.commit(orclCxt, sqls);
		assertEquals("select case when roleName then rolId else orgId end \"ifname\" from \"a_roles\" ",
				sqls.get(4));	
	}

	@Test
	void testIsEnvelope() throws Exception {
		ArrayList<String> sqls = new ArrayList<String>();
		Query q = st.select("h_photos")
				.col(Funcall.isEnvelope("uri"), "isEnvelope");

			q.commit(st.instancontxt(null, null), sqls);
			assertEquals("select case when substr(uri, 1, 8) = '{\"type\":' then 1 else 0 end isEnvelope from h_photos ",
					sqls.get(0));	

	}
}
