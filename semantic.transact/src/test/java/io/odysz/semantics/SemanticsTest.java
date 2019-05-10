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
import io.odysz.transact.x.TransException;

public class SemanticsTest {

//	private Semantics2 semantics;
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


	@SuppressWarnings("serial")
	static HashMap<String, TableMeta> fakeMetas() {
		return new HashMap<String, TableMeta>() {
			{put("a_functions", fackFuncs());}
			{put("a_role_funcs", new TableMeta("a_role_funcs"));}
		};
	}

	private static TableMeta fackFuncs() {
		return new TableMeta("a_functions")
				.col("sibling", coltype.number)
				.col("parentId", coltype.text);
	}
}
