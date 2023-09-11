package io.odysz.transact.sql.parts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.odysz.semantics.ISemantext;
import io.odysz.semantics.Semantext2;
import io.odysz.semantics.Semantics2;
import io.odysz.semantics.SemanticsTest;
import io.odysz.semantics.meta.TableMeta;
import io.odysz.semantics.meta.ColMeta.coltype;
import io.odysz.transact.sql.Transcxt;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.x.TransException;

public class AnsonFieldTest {
	private static Transcxt st;

	@BeforeAll
	public static void setUp() throws Exception {
		HashMap<String, Semantics2> semantics = Semantics2.init("src/test/resources/semantics.xml");
		st = new Transcxt((ISemantext) new Semantext2("root", semantics, fakeMetas()));
	}

	public static HashMap<String, TableMeta> fakeMetas() {
		HashMap<String, TableMeta> m = SemanticsTest.fakeMetas();
		m.put("a_funcs", new TableMeta("a_funcs").col("funcId", coltype.text));
		m.put("a_log", new TableMeta("a_log").col("logId", coltype.text));
		return m;
	}
	
	@Test
	public void testSql() throws TransException {
		ArrayList<String> sqls = new ArrayList<String>();
		T_PhotoCSS anson = new T_PhotoCSS(4, 3);
		st.insert("a_funcs")
			.nv("funcId", "a01")
			.nv("funcName", anson)
			.nv("uri", ExprPart.constStr(null))
			.commit(sqls);

		assertEquals(
		"insert into a_funcs  (funcId, funcName, uri) values ('a01', '{\"type\": \"io.odysz.transact.sql.parts.T_PhotoCSS\", \"size\": [4, 3]}', null)",
		sqls.get(0));

		st.update("a_funcs")
			.nv("funcName", anson)
			.nv("uri", ExprPart.constStr(null))
			.whereEq("funcId", "a01")
			.commit(sqls);

		assertEquals(
		"update  a_funcs  set funcName='{\"type\": \"io.odysz.transact.sql.parts.T_PhotoCSS\", \"size\": [4, 3]}', uri=null where funcId = 'a01' ",
		sqls.get(1));
	
	}

}
