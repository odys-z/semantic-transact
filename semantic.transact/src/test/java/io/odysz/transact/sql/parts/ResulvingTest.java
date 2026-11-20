package io.odysz.transact.sql.parts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.odysz.semantics.ISemantext;
import io.odysz.semantics.Semantext2;
import io.odysz.semantics.Semantics2;
import io.odysz.semantics.meta.TableMeta;
import io.odysz.semantics.meta.ColMeta.coltype;
import io.odysz.transact.sql.Transcxt;
import io.odysz.transact.x.TransException;

/**
 * Resulving with an index: an auto key at an index of those generated in the context.
 * For the semantics resolving with it, see semantic.DA's DASemantextTest.testResulvingTree().
 *
 * @since 1.5.81
 */
public class ResulvingTest {

	/** a context in which 4 keys have been generated for a_docnodes.nid, the first is the parent of the next two */
	static class Ctx extends Semantext2 {
		final List<Object> nids = new ArrayList<Object>(Arrays.asList("000001", "000002", "000003", "000004"));

		Ctx() { super("a_docnodes", new HashMap<String, Semantics2>(), metas()); }

		static HashMap<String, TableMeta> metas() {
			HashMap<String, TableMeta> m = new HashMap<String, TableMeta>();
			m.put("a_docnodes", new TableMeta("a_docnodes")
				.col("nid", coltype.text).col("parentId", coltype.text).col("ptext", coltype.text));
			return m;
		}

		@Override
		public List<Object> resulvedVals(String tabl, String col) {
			return "a_docnodes".equals(tabl) && "nid".equals(col) ? nids : null;
		}
	}

	@Test
	public void testIndex() throws TransException {
		ISemantext ctx = new Ctx();

		assertEquals("'000001'", new Resulving("a_docnodes", "nid", 0).sql(ctx));
		assertEquals("'000003'", new Resulving("a_docnodes", "nid", 2).sql(ctx));
		assertEquals("'000004'", new Resulving("a_docnodes", "nid", -1).sql(ctx));
		assertEquals("'000003'", new Resulving("a_docnodes", "nid", -2).sql(ctx));

		// without an index: the last one, -1
		assertEquals(new Resulving("a_docnodes", "nid").sql(ctx), new Resulving("a_docnodes", "nid", -1).sql(ctx));

		assertEquals("000002", new Resulving("a_docnodes", "nid", 1).resulved(ctx));
		assertEquals("'a_docnodes.nid'", new Resulving("a_docnodes", "nid", 1).resulved(null));
	}

	@Test
	public void testNotGenerated() {
		ISemantext ctx = new Ctx();
		assertThrows(TransException.class, () -> new Resulving("a_docnodes", "nid", 4).sql(ctx));
		assertThrows(TransException.class, () -> new Resulving("a_docnodes", "nid", -5).sql(ctx));
		assertThrows(TransException.class, () -> new Resulving("a_docs", "docId", 0).sql(ctx));
	}

	/** In a multi-row insert, each row refers to the key of its own parent, not the latest key. */
	@Test
	public void testMultiRowInsert() throws TransException {
		ISemantext ctx = new Ctx();
		Transcxt st = new Transcxt(ctx);

		ArrayList<ArrayList<Object[]>> vals = new ArrayList<ArrayList<Object[]>>();
		vals.add(row("000001", "root",                                 "section"));
		vals.add(row("000002", new Resulving("a_docnodes", "nid", 0), "p1"));
		vals.add(row("000003", new Resulving("a_docnodes", "nid", 0), "sub"));
		vals.add(row("000004", new Resulving("a_docnodes", "nid", 2), "sub.p1"));

		assertEquals("insert into a_docnodes (nid, parentId, ptext) values "
				+ "('000001', 'root', 'section'), "
				+ "('000002', '000001', 'p1'), "
				+ "('000003', '000001', 'sub'), "
				+ "('000004', '000003', 'sub.p1')",
			st.insert("a_docnodes")
				.cols(new String[] {"nid", "parentId", "ptext"})
				.values(vals)
				.sql(ctx));
	}

	static ArrayList<Object[]> row(String nid, Object parent, String text) {
		ArrayList<Object[]> r = new ArrayList<Object[]>();
		r.add(new Object[] {"nid", nid});
		r.add(new Object[] {"parentId", parent});
		r.add(new Object[] {"ptext", text});
		return r;
	}
}
