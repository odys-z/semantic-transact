package io.odysz.transact.sql.parts.update;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.sql.parts.condition.ExprPart;

/**The SET clause equetions' list:<br>
 * col= v1, c2 = 'v2', ...<br>
 * This class use {@link SetValue} as element type.
 * @author odys-z@github.com
 */
public class SetList extends AbsPart {
	private ArrayList<Object[]> nvs;

	public SetList(ArrayList<Object[]> nvs) {
		this.nvs = nvs;
	}

	private String tabl;
	private String col;
	/**These values are set value to tabl.col. <br>
	 * Some times this information is used for handling expression,
	 * like {@link io.odysz.transact.sql.parts.condition.Funcall.Func#extFile extFile} will use to handle external file at committing succeed.
	 * @param tabl
	 * @param col
	 * @return this
	 */
	public SetList setVal2(String tabl, String col) {
		this.tabl = tabl;
		this.col = col;
		return this;
	}

	@Override
	public String sql(ISemantext context) {
		if (nvs == null)
			return "";
		else
			return nvs.stream().map(nv -> {
				String s = Stream.of(new ExprPart((String) nv[0]), new ExprPart("="), new SetValue(nv[1]).setVal2(tabl, col))
						.map(m -> m.sql(context)).collect(Collectors.joining(""));
				return s;
			}).collect(Collectors.joining(", "));
	}

}
