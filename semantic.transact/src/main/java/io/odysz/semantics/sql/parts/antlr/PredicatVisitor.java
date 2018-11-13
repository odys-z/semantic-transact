package io.odysz.semantics.sql.parts.antlr;

import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import gen.antlr.sql.exprs.SearchExprsBaseVisitor;
import io.odysz.semantics.sql.parts.condition.Predicate;

public class PredicatVisitor extends SearchExprsBaseVisitor<Predicate> {

	@Override
	public Predicate visit(ParseTree tree) {
		return null;
	}


}
