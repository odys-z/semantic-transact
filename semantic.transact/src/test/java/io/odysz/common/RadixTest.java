package io.odysz.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;

import io.odysz.transact.x.TransException;

public class RadixTest {

	@Test
	public void test63() throws TransException {
		long v = 0;
		
		assertEquals("000000", Radix64.toString(v));
		assertEquals("000001", Radix64.toString(++v));
		
		v = 64 * 64 * 64;
		assertEquals("001001", Radix64.toString(++v));
		
		v *= 64 * 64;
		assertEquals("100100", Radix64.toString(v));
		
		v *= 63;
		assertEquals("-00-00", Radix64.toString(v));
		
		assertEquals("0-00-00", Radix64.toString(v, 7));
		
		v *= 64 * 64;
		assertEquals("-00-0000", Radix64.toString(v, 8));
		
		assertEquals(0, Radix64.toLong("0000"));
		assertEquals(1, Radix64.toLong("0001"));
		assertEquals(64, Radix64.toLong("10"));
		assertEquals(64 * 64, Radix64.toLong("100"));
		assertEquals(v, Radix64.toLong("-00-0000"));

		assertEquals(64 * 64 - 1, Radix64.toLong("--"));
		assertEquals(64 * 64 * 64 * 64 * 64 - 1, Radix64.toLong("-----"));

		assertEquals(64 * 64 * 64 - 1, Radix64.toLong("---"));

		// 2 ^ 63 -1 = 7fff ffffff ffffff
		//             7--   ----   ----
		assertEquals(Long.MAX_VALUE, Radix64.toLong("7----------"));
		assertEquals(Long.MIN_VALUE, Radix64.toLong("80000000000"));
	}

	@Test
	public void test32() throws TransException {
		long v = 0;
		
		assertEquals("000000", Radix32.toString(v));
		assertEquals("000001", Radix32.toString(++v));
		
		v = 32 * 32 * 32;
		assertEquals("001001", Radix32.toString(++v));
		
		v *= 32 * 32;
		assertEquals("100100", Radix32.toString(v));
		
		v *= 31;
		assertEquals("V00V00", Radix32.toString(v));
		
		assertEquals("0V00V00", Radix32.toString(v, 7));
		
		v *= 32 * 32;
		assertEquals("V00V0000", Radix32.toString(v, 8));
		
		assertEquals(0, Radix32.toLong("0000"));
		assertEquals(1, Radix32.toLong("0001"));
		assertEquals(32, Radix32.toLong("10"));
		assertEquals(32 * 32, Radix32.toLong("100"));
		assertEquals(v, Radix32.toLong("V00V0000"));

		assertEquals(32 * 32 * 32 - 1, Radix32.toLong("VVV"));

		assertEquals(31, Radix32.toLong("V"));
		// 2 ^ 63 -1 = 7 77777 77777 77777 77777
		//             7  VVV   VVV   VVV   VVV
		assertEquals(Long.MAX_VALUE, Radix32.toLong("7VVVVVVVVVVVV"));
		assertEquals(Long.MIN_VALUE, Radix32.toLong("8000000000000"));
	}
}
