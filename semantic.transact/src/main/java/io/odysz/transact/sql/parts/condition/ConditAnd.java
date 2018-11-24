package io.odysz.transact.sql.parts.condition;
//package io.odysz.transact.sql.parts.condition;
//
//import java.util.ArrayList;
//
//import io.odysz.transact.sql.parts.Logic;
//import io.odysz.transact.sql.parts.Logic.op;
//
//public class ConditAnd extends Condit {
//
//	public ConditAnd(op op, String lop, String rop) {
//		super(op, lop, rop);
//	}
//
//	ArrayList<AbsPart> predicts;
//	
//	@Override
//	public Condit and(Condit condt) {
//		predicts.add(condt);
//		return this;
//	}
//	
//	public Condit and(String logic, String from, String... to) {
//		// ExprBuilder expr = new ExprBuilder(logic).left(from).right(to == null || to.length == 0 ? null : to[0]);
//
//		Predicate condt = new Predicate(Logic.op(logic), from, to == null || to.length == 0 ? null : to[0]);
//
//		if (predicts == null) {
//			predicts = new ArrayList<AbsPart>();
//		}
//		// predicts.add(condt);
//		return and((Condit) condt);
//	}
//
//
//
//}
