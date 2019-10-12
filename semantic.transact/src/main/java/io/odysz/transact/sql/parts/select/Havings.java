package io.odysz.transact.sql.parts.select;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.sql.parts.condition.Condit;
import io.odysz.transact.x.TransException;

public class Havings extends AbsPart {

	private Condit condits;

	public Havings(Condit havings) {
		condits = havings;
	}

	@Override
	public String sql(ISemantext sctx) throws TransException {
		return Stream.of("having", condits.sql(sctx))
				.collect(Collectors.joining(", ", "having ", ""));
	}

}
