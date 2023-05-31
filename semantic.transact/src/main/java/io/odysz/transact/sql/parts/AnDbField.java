package io.odysz.transact.sql.parts;

import java.io.IOException;
import java.io.OutputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.anson.Anson;
import io.odysz.anson.IJsonable;
import io.odysz.anson.JsonOpt;
import io.odysz.anson.x.AnsonException;
import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.x.TransException;

/**
 * @since 1.5.0
 * @author Ody Z
 */
public abstract class AnDbField extends ExprPart implements IJsonable {
	
	// public abstract <T> T get(String col);

	@Override
	public IJsonable toBlock(OutputStream stream, JsonOpt... opts) throws AnsonException, IOException {
		return Anson.toEnvelope(this, stream, opts);
	}

	@Override
	public IJsonable toJson(StringBuffer buf) throws IOException, AnsonException {
		return Anson.toJson(buf, this);
	}

	@Override
	public String sql(ISemantext context) throws TransException {
		try {
			// TODO new interface sql(stream, context) should optimize this performance.
			return Stream.of(toBlock(new JsonOpt().escape4DB(true)))
						.collect(Collectors.joining(" ", "'", "'"));
		} catch (AnsonException | IOException e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
}
