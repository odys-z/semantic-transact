package io.odysz.transact.sql.parts.insert;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.AbsPart;

public class ColumnList extends AbsPart {
	String[] cols;

	public ColumnList(Map<String, Integer> colIdx) {
		if (colIdx == null)
			return;

		cols = new String[colIdx.size()];
		for (String n : colIdx.keySet()) {
			Integer ix = colIdx.get(n);
			if (ix == null)
				continue;
			cols[ix] = n;
		}
	}

	@Override
	public String sql(ISemantext context) {
		if (cols == null)
			return "";
		else
			return "(" + Arrays.stream(cols).collect(Collectors.joining(", ")) + ")";
	}


}
