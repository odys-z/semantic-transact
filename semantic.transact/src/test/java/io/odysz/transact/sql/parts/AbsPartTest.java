package io.odysz.transact.sql.parts;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.odysz.transact.sql.parts.condition.ExprPart;

public class AbsPartTest {

	@Test
	public void testIsblank() {
		ExprPart nexpr = new ExprPart((String)null);
		assertTrue(AbsPart.isblank(nexpr));

		nexpr = new ExprPart("null");
		assertTrue(AbsPart.isblank(nexpr));
	}

}
