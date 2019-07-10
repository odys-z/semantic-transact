//package io.odysz.transact.sql.orcl;
//
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//import io.odysz.semantics.ISemantext;
//import io.odysz.transact.sql.Insert;
//import io.odysz.transact.sql.Transcxt;
//import io.odysz.transact.sql.parts.AbsPart;
//import io.odysz.transact.sql.parts.condition.ExprPart;
//import io.odysz.transact.sql.parts.insert.ColumnListOrcl;
//import io.odysz.transact.sql.parts.insert.InsertValuesOrcl;
//import io.odysz.transact.x.TransException;
//
///**sql: insert into tabl(...) values(...) / select ...
// * @author ody
// *
// */
//public class InsertOrcl extends Insert {
//	InsertOrcl(Transcxt transc, String tabl) {
//		super(transc, tabl);
//	}
//
//	/**sql: insert into tabl(...) values(...) / select ...
//	 * @see io.odysz.transact.sql.parts.AbsPart#sql(ISemantext)
//	 */
//	@Override
//	public String sql(ISemantext sctx) {
//		boolean hasVals = valuesNv != null 
//				&& valuesNv.size() > 0
//				&& valuesNv.get(0) != null
//				&& valuesNv.get(0).size() > 0;
//		if (!hasVals && selectValues == null) return "";
//		
//		// insert into tabl(...) values(...) / select ...
//		Stream<String> s = Stream.concat(
//			// insert into tabl(...)
//			Stream.of("insert into ",
//					"\"", new ExprPart(mainTabl), "\"",
//					"\"", new ExprPart(mainAlias), "\"",
//					// (...)
//					new ColumnListOrcl(insertCols)
//			   // values(...) / select ...
//			), Stream.concat(
//
//				// values (...)
//				// whether 'values()' appears or not is the same as value valuesNv
//				Stream.of(" values ",
//						// 'v1', 'v2', ...)
//					new InsertValuesOrcl(mainTabl, insertCols, valuesNv)
//				).filter(w -> hasVals),
//
//				// select ...
//				Stream.of(selectValues).filter(w -> selectValues != null))
//			).map(m -> {
//				try {
//					return m instanceof AbsPart ? ((AbsPart) m).sql(sctx) : m.toString();
//				} catch (TransException e) {
//					e.printStackTrace();
//					return "";
//				}
//			});
//
//		return s.collect(Collectors.joining());
//	}
//
//}
