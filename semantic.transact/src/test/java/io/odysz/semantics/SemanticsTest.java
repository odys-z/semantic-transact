package io.odysz.semantics;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import io.odysz.common.Utils;
import io.odysz.transact.sql.Transcxt;
import io.odysz.transact.x.TransException;

public class SemanticsTest {

//	private Semantics2 semantics;
	private Transcxt st;

	@Before
	public void setUp() throws Exception {
		HashMap<String,Semantics2> semantics = Semantics2.init("src/test/resources/semantics.xml");
		st = new Transcxt(new Semantext2("root", semantics));
	}

	@Test
	public void test() throws TransException {
		ArrayList<String> sqls = new ArrayList<String>();
		st.insert("a_functions")
			.nv("funcId", "AUTO")
			.nv("funcName", "Test 001")
			.nv("sibling", "10")
			.nv("parentId", "0")
			.commit(sqls);
		
		Utils.logi(sqls);
		assertEquals(
			"insert into a_functions  (funcId, funcName, sibling, parentId, fullpath) values ( 'AUTO', 'Test 001', '10', '0', 'fullpath 0.0 AUTO' )",
			sqls.get(0));
	}

}
