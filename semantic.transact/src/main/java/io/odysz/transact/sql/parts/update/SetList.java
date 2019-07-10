package io.odysz.transact.sql.parts.update;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.sql.parts.Colname;
import io.odysz.transact.sql.parts.Tabl;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.x.TransException;

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

	private Tabl tabl;

	/**These values are set value to tabl.col. <br>
	 * Some times this information is used for handling expression,
	 * like {@link io.odysz.transact.sql.parts.condition.Funcall.Func#extFile extFile}
	 * will use this to handle external file at committing succeed.
	 * @param table
	 * @return this
	 */
	public SetList setVal2(Tabl table) {
		this.tabl = table;
		return this;
	}

	@Override
	public String sql(ISemantext context) {
		if (nvs == null)
			return "";
		else
			return nvs.stream().map(nv -> {
				String s = Stream.of(
							// new ExprPart((String) nv[0]), new ExprPart("="),
							Colname.parseFullname((String) nv[0]), new ExprPart("="),
							new SetValue(nv[1]).setVal2(tabl, (String) nv[0]))
						.map(m -> {
							try {
								return m.sql(context);
							} catch (TransException e) {
								e.printStackTrace();
								return "";
							}
						}).collect(Collectors.joining(""));
				return s;
			}).collect(Collectors.joining(", "));
	}

}
