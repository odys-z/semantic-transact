package io.odysz.transact.sql.parts.insert;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import io.odysz.common.Utils;
import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.sql.parts.Colname;
import io.odysz.transact.x.TransException;

public class ColumnList extends AbsPart {
	Colname[] cols;

	public ColumnList(Map<String, Integer> colIdx) {
		if (colIdx == null)
			return;

		cols = new Colname[colIdx.size()];
		for (String n : colIdx.keySet()) {
			Integer ix = colIdx.get(n);
			if (ix == null)
				continue;
			// ix--; // because colindext set early is starting from 1! disgusting
			if (ix >= cols.length)
				Utils.warn("Column ignored, possibly results from duplicate name (%s) in column list.", n);
			else
			cols[ix] = Colname.parseFullname(n);
		}
	}

	@Override
	public String sql(ISemantext context) {
		if (cols == null)
			return "";
		else
			return "(" + Arrays.stream(cols)
				.map(c -> {
					try {
						return c.sql(context);
					} catch (TransException e) {
						e.printStackTrace();
						return e.getMessage();
					}
				})
				.collect(Collectors.joining(", ")) + ")";
	}
}
