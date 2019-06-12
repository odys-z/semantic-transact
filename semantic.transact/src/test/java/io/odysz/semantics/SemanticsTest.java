package io.odysz.semantics;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import io.odysz.common.Utils;
import io.odysz.semantics.meta.TableMeta;
import io.odysz.semantics.meta.ColMeta.coltype;
import io.odysz.transact.sql.Transcxt;
import io.odysz.transact.sql.parts.condition.Funcall;
import io.odysz.transact.x.TransException;

public class SemanticsTest {

	private Transcxt st;

	@Before
	public void setUp() throws Exception {
		HashMap<String,Semantics2> semantics = Semantics2.init("src/test/resources/semantics.xml");
		st = new Transcxt(new Semantext2("root", semantics, fakeMetas()));
	}

	@Test
	public void test() throws TransException {
		ArrayList<String> sqls = new ArrayList<String>();
		st.insert("a_functions")
			.nv("funcId", "AUTO")
			.nv("funcName", "Test 001")
			.nv("sibling", "10")
			.nv("parentId", "0")
			.commit(st.instancontxt(null), sqls);
		
		assertEquals(
			"insert into a_functions  (funcId, funcName, sibling, parentId, fullpath) values ('AUTO', 'Test 001', 10, '0', 'fullpath 0.0 AUTO')",
			sqls.get(0));
		
		ArrayList<ArrayList<?>> vals = new ArrayList<ArrayList<?>>();
		ArrayList<String[]> r1 = new ArrayList<String[]>();
		r1.add(new String[] {"roleId", "r01"});
		r1.add(new String[] {"funcId", "f01"});

		ArrayList<String[]> r2 = new ArrayList<String[]>();
		r2.add(new String[] {"roleId", "r02"});
		r2.add(new String[] {"funcId", "f02"});

		vals.add(r1);
		vals.add(r2);
		st.insert("a_role_funcs")
			.cols(new String[] {"roleId", "funcId"})
			.values(vals)
			.commit(st.instancontxt(null), sqls);

		assertEquals("insert into a_role_funcs  (roleId, funcId) values ('r01', 'f01'), ('r02', 'f02')",
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

		st.select("a_functions", "f")
			// .j("a_rolefunc", "rf", Sql.condt("f.funcId=rf.funcId and rf.roleId~%%'%s'", user.userId()))
			.l(st.select("a_role_func", "rf")
					.col(Funcall.ifNullElse("rf.funcId", Boolean.TRUE, Boolean.FALSE)),
					"tf", "tf.funcId = f.funcId")
			.col("f.funcName", "func")
			.commit(st.instancontxt(null), sqls);
		assertEquals("select f.funcName func from a_functions f left outer join ( select case when rf.funcId is null then true else false end from a_role_func rf ) tf on tf.funcId = f.funcId",
				sqls.get(0));
	}
	
	/**See <a href='https://odys-z.github.io/notes/semantics/ref-transact.html#ref-transact-empty-vals'>
	 * How empty values are handled</a>
	 * @throws TransException
	 */
	@Test
	public void testEmptyVals() throws TransException {
		ArrayList<String> sqls = new ArrayList<String>();
		st.insert("a_functions")
			.nv("funcId", "AUTO")		// no coltype,		not empty	=> 'AUTO'
			.nv("funcName", "")			// type text,		blank 		=> ''
			.nv("sibling", null)		// type number,		null 		=> null
			.nv("someDat", "")			// type datetime,	blank 		=> ''
			.nv("parentId", null)		// type text,		null		=> null
			.commit(st.instancontxt(null), sqls);
		
		assertEquals(
			"insert into a_functions  (funcId, funcName, sibling, someDat, parentId, fullpath) " +
							  "values ('AUTO', '', null, '', null, 'fullpath null.null AUTO')",
			sqls.get(0));

		st.insert("a_functions")
			.nv("funcId", "AUTO")		// no coltype,		not empty	=> 'AUTO'
			.nv("funcName", null)		// type text,		null 		=> null
			.nv("sibling", "")			// type number,		blank 		=> 0
			.nv("someDat", null)		// type datetime,	null 		=> null
			.nv("parentId", "")			// type text,		blank		=> ''
			.commit(st.instancontxt(null), sqls);
		
		assertEquals(
			"insert into a_functions  (funcId, funcName, sibling, someDat, parentId, fullpath) " +
								"values ('AUTO', null, 0, null, '', 'fullpath . AUTO')",
			sqls.get(1));
	}
	
	@SuppressWarnings("serial")
	static HashMap<String, TableMeta> fakeMetas() {
		return new HashMap<String, TableMeta>() {
			{put("a_functions", fakeFuncs());}
			{put("a_role_funcs", new TableMeta("a_role_funcs"));}
		};
	}

	private static TableMeta fakeFuncs() {
		return new TableMeta("a_functions")
				.col("sibling", coltype.number)
				.col("someDat", coltype.datetime)
				.col("parentId", coltype.text);
	}
}
