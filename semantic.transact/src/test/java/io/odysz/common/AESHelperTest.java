package io.odysz.common;


import static io.odysz.common.AESHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import io.odysz.transact.x.TransException;


public class AESHelperTest {

	static final String s219b64 = "iVBORw0KGgoAAAANSUhEUgAAALYAAAB5CAMAAACjkCtXAAAAFVBMVEX+1QABW7sAW7sAUcGWnmj/2wABXLkr7EQMAAAAgUlEQVR4nO3SyQ3DAAgAQZyr/5JTgy15I6IZvjzQijlWmuNxYu5bPrk+v+52jdoltUtql9QuqV1Su6R2Se2S2iW1S2qX1C6pXVK7pHZJ7ZLaJbVLapfWnu1JOmqX1C6pXVK7pHZJ7ZLapfmsNM+V5rXSvFcaAAAAAAAAAAAAAP7XFzzwP8UnEJ9SAAAAAElFTkSuQmCC";
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
			@SuppressWarnings("deprecation")
			String str64 = encode63(buf, ifs, index, readlen);
			b.append(str64);
			index += readlen;
		}
		ifs.close();
		
		return b.toString();
	}
	
	@Test
	public void testByteArrayOutputStream () throws IOException {
		int blocksize = 3 * 16;
		String fn = "decode-s219b64.png";
		Path p = Paths.get(fn);
		if (Files.exists(p))
				Files.delete(p);;

		byte[] b = s219b64.getBytes();
		try ( ByteArrayInputStream bais = new ByteArrayInputStream(b);
			  InputStream is = Base64.getDecoder().wrap(bais)) {

			byte[] buff = new byte[blocksize];
			int start = 0, len = blocksize;
			try (FileOutputStream fs = new FileOutputStream(fn)){
				while(start < b.length && len > 0) {
					len = is.read(buff, 0, blocksize);
					if (len > 0) {
						start += len;
						fs.write(buff, 0, len);
					}
				}
			}
			assertEquals(73 * 3, Files.size(p));
		}

		try ( ByteArrayOutputStream baos = new ByteArrayOutputStream();
			  OutputStream os = Base64.getEncoder().wrap(baos);
			  FileInputStream fs = new FileInputStream(fn)
			) {

			byte[] buff = new byte[blocksize];
			int start = 0, len = blocksize;

			while(start < b.length && len > 0) {
				len = fs.read(buff, 0, blocksize);
				if (len > 0) {
					start += len;
					os.write(buff, 0, len);
				}
			}

			String s = baos.toString();

			assertEquals(73 * 4, s.length());
			assertEquals(s219b64, s);
		}
		
		AESHelper.Range_Size = 32;
		
		File f219 = new File(fn);
		String
		s  = encodeRange(f219, 0, 3);
		s += encodeRange(f219, 3, 12);
		s += encodeRange(f219, 3+12, 219 - 3 - 12);
		assertEquals(s, s219b64);

		s  = encodeRange(f219, 0, 6);
		s += encodeRange(f219, 6, 24);
		s += encodeRange(f219, 6+24, 219 - 6 - 24);
		assertEquals(s, s219b64);

		s  = encodeRange(f219, 0, 9);
		s += encodeRange(f219, 9, 36);
		s += encodeRange(f219, 9+36, 219 - 9 - 36);
		assertEquals(s, s219b64);

		s  = encodeRange(f219, 0, 219);
		assertEquals(s, s219b64);

		s  = encodeRange(f219, 0, 555);
		assertEquals(s, s219b64);
		
		
		try {
			s = encodeRange(f219, 0, 32);
			fail("Expecting 3 * n length.");
		}
		catch (IOException e) {}
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
