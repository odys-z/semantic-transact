package io.odysz.transact.sql.parts.select;

import static io.odysz.common.LangExt.isNull;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.Query;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.x.TransException;

public class WithClause extends AbsPart {

	ArrayList<Query> withs;
	
	public WithClause(ArrayList<Query> withs) {
		this.withs = withs;
	}
	
	@Override
	public String sql(ISemantext sctx) throws TransException {
		
		return isNull(withs) ? "" : 
				withs.stream()
					.filter(e -> e != null)
					.map(q -> { return
							Stream.of(
								q.alias().sql(sctx),
								" as (", 
								((Query) q).sql(sctx),
								")").collect(Collectors.joining());
							})
							.collect(Collectors.joining(", ", "with ", "")); 
	}

}
