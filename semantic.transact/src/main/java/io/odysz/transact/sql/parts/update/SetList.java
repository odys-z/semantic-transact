package io.odysz.transact.sql.parts.update;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.sql.parts.condition.ExprPart;

public class SetList extends AbsPart {
	private ArrayList<Object[]> nvs;

	public SetList(ArrayList<Object[]> nvs) {
		this.nvs = nvs;
	}

	@Override
	public String sql(ISemantext context) {
		if (nvs == null)
			return "";
		else
			return nvs.stream().map(nv -> {
				String s = Stream.of(new ExprPart((String) nv[0]), new ExprPart("="), new SetValue(nv[1]))
						.map(m -> m.sql(context)).collect(Collectors.joining(""));
				return s;
			}).collect(Collectors.joining(", "));
	}

}
