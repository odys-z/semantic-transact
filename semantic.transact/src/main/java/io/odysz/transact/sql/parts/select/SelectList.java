package io.odysz.transact.sql.parts.select;

import java.util.List;
import java.util.stream.Collectors;

import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.AbsPart;

public class SelectList extends AbsPart {

	private List<SelectElem> elems;

	public SelectList(List<SelectElem> elemList) {
		elems = elemList;
	}


	@Override
	public String sql(ISemantext sctx) {
		return elems == null ? "*"
				: elems.stream().map(e -> e.sql(sctx)).collect(Collectors.joining(", "));
	}
}
