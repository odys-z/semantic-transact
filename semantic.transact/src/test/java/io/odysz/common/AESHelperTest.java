package io.odysz.common;


import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.junit.Test;


public class AESHelperTest {

	/**C# Debug Trace:<pre>
	Check this at server side:
	Cypher:
	4VGdDR9qJlq36bQGI+Sx3A==
	Key:
	io.github.odys-z
	IV:
	DITVJZA2mSDAw496hBz6BA==
	Expacting:
	Plain Text</pre>

	 * Case 2: user pswd (why c# AES padded an extra block in CBC?)<pre>
	 uid:  "-----------admin"
	 pswd: "----------123456"
	 iv64: "ZqlZsmoC3SNd2YeTTCkbVw=="
	 tk64: "3A0hfZiaozpwMeYs3nXdAb8mGtVc1KyGTyad7GZI8oM="
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
		
		key = "----------123456";
		plain = "-----------admin";
		assertEquals("NLy7ldimKuNgOA8IlWqloA==",
				AESHelper.encrypt(plain, key, AESHelper.decode64(iv)));
	}

}
