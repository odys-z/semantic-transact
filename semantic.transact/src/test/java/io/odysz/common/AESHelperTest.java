package io.odysz.common;


import static io.odysz.common.AESHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

import org.junit.jupiter.api.Test;

import io.odysz.transact.x.TransException;


public class AESHelperTest {

	/**
	 * C# Debug Trace:<pre>
	Check this at server side:
	Cipher:
	4VGdDR9qJlq36bQGI+Sx3A==
	Key:
	io.github.odys-z
	IV:
	DITVJZA2mSDAw496hBz6BA==
	Expecting:
	Plain Text</pre>

	 * Case 2: user pswd (why c# AES padded an extra block in CBC?)<pre>
	 uid:  "-----------admin"
	 pswd: "----------123456"
	 iv64: "ZqlZsmoC3SNd2YeTTCkbVw=="
	 tk64: "3A0hfZiaozpwMeYs3nXdAb8mGtVc1KyGTyad7GZI8oM="
	 </pre>
	 * Case 3: AES-128/CBC/NoPadding<pre>
	 uid:  "-----------admin"
	 pswd: "----------123456"
	 iv64: "CTpAnB/jSRQTvelFwmJnlA=="
	 tk64: "WQiXlFCt5AGCabjSCkVh0Q=="
	 </pre>
	 * @throws IOException
	 * @throws GeneralSecurityException
	 *
	 */
	@Test
	public void testDecrypt() throws GeneralSecurityException, IOException {
		String cipher = "4VGdDR9qJlq36bQGI+Sx3A==";
		String key = "io.github.odys-z";
		String iv = "DITVJZA2mSDAw496hBz6BA==";
		String plain = AESHelper.decrypt(cipher, key,
							AESHelper.decode64(iv));
		assertEquals("Plain Text", plain.trim());

		plain = "-----------admin";
		key =   "----------123456";
		iv = "ZqlZsmoC3SNd2YeTTCkbVw==";
		// PCKS7 Padding results not suitable for here - AES-128/CBC/NoPadding
		assertNotEquals("3A0hfZiaozpwMeYs3nXdAb8mGtVc1KyGTyad7GZI8oM=",
				AESHelper.encrypt(plain, key, AESHelper.decode64(iv)));
		
		iv = "CTpAnB/jSRQTvelFwmJnlA==";
		assertEquals("WQiXlFCt5AGCabjSCkVh0Q==",
				AESHelper.encrypt(plain, key, AESHelper.decode64(iv)));
	}

	@Test
	public void testDecryptPad() throws GeneralSecurityException, IOException {
		String plain = "admin";
		String key =   "123456";
		String iv = "CTpAnB/jSRQTvelFwmJnlA==";

		// PCKS7 Padding results not suitable for here - AES-128/CBC/NoPadding
		String cipher = AESHelper.encrypt(plain, key, AESHelper.decode64(iv));
		assertNotEquals("3A0hfZiaozpwMeYs3nXdAb8mGtVc1KyGTyad7GZI8oM=", cipher);
		
		String decipher = AESHelper.decrypt(cipher, key, AESHelper.decode64(iv));
		assertEquals(plain, decipher);
	}
	
	@Test
	public void testEncodeFile() throws Exception {
		String s219b64 = "iVBORw0KGgoAAAANSUhEUgAAALYAAAB5CAMAAACjkCtXAAAAFVBMVEX+1QABW7sAW7sAUcGWnmj/2wABXLkr7EQMAAAAgUlEQVR4nO3SyQ3DAAgAQZyr/5JTgy15I6IZvjzQijlWmuNxYu5bPrk+v+52jdoltUtql9QuqV1Su6R2Se2S2iW1S2qX1C6pXVK7pHZJ7ZLaJbVLapfWnu1JOmqX1C6pXVK7pHZJ7ZLapfmsNM+V5rXSvFcaAAAAAAAAAAAAAP7XFzzwP8UnEJ9SAAAAAElFTkSuQmCC";
		int size = 219;
		
		byte[] buf = new byte[201];
		String s = buffer64(buf, size);
		assertEquals(73 * 4, s.length());
		assertEquals(s219b64, s);

		buf = new byte[219];
		s = buffer64(buf, size);
		assertEquals(73 * 4, s.length());
		assertEquals(s219b64, s);

		buf = new byte[73 * 4 - 1];
		s = buffer64(buf, size);
		assertEquals(73 * 4, s.length());
		assertEquals(s219b64, s);

		buf = new byte[73 * 4 + 2];
		s = buffer64(buf, size);
		assertEquals(73 * 4, s.length());
		assertEquals(s219b64, s);
	}

	private String buffer64(byte[] buf, int size) throws Exception {
		if (buf == null || buf.length % 3 != 0)
			throw new TransException("Buffer size must be multiple of 3.");
		FileInputStream ifs = Utils.input(this.getClass(), "219.png");
		StringBuffer b = new StringBuffer();
		int index = 0;
		while (index < size) {
			int readlen  = Math.min(buf.length, size - index);
			String str64 = encode64(buf, ifs, index, readlen);
			b.append(str64);
			index += readlen;
		}
		ifs.close();
		
		return b.toString();
	}
	
	@Test
	public void testSessionToken() throws Exception {
		String uid = "ody", pswd = "io.github.odys-z";
		String[] response = AESHelper.packSessionKey(pswd);
		String knowledge = response[1];
		assertEquals(24, knowledge.length());
		assertEquals(69, response[0].length()); // 44 + 1 + 24
		
		String request = AESHelper.repackSessionToken((String) response[0], pswd, uid);
		
		assertTrue(AESHelper.verifyToken(request, knowledge, uid, pswd));
	}
}
