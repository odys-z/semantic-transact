package io.odysz.common;

import static io.odysz.common.LangExt.eq;
import static io.odysz.common.LangExt.f;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.crypto.cipher.CryptoCipher;
import org.apache.commons.crypto.random.CryptoRandom;
import org.apache.commons.crypto.random.CryptoRandomFactory;
import org.apache.commons.crypto.utils.Utils;

/**Static helpers for encrypt/decipher string.
 * @author ody
 */
public class AESHelper {
    static Properties randomProperties = new Properties();
    /**
     * Deprecating static final String transform = "AES/CBC/PKCS5Padding";<br>
     * Apache Common Crypto only support PKCS#5 padding, but most js lib support PKCS#7 padding,
     * This makes trouble when negotiation with those API.
     * Solution: using no padding here, round the text to 16 or 32 ASCII bytes.
     */
    static final String transform = "AES/CBC/NoPadding";
    static CryptoCipher encipher;

    static ReentrantLock lock;

    static {
    	randomProperties.put(CryptoRandomFactory.CLASSES_KEY,
    			CryptoRandomFactory.RandomProvider.JAVA.getClassName());

        Properties cipherProperties = new Properties();
        // causing problem for different environment:
        // cipherProperties.setProperty(CryptoCipherFactory.CLASSES_KEY, CipherProvider.JCE.getClassName());
    	try {
			encipher = Utils.getCipherInstance(transform, cipherProperties);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	// experiment 10 Dec 2023
    	// solving crash happened outside the Java Virtual Machine in native code.
        lock = new ReentrantLock();
    }

	/**TODO move to test
	 * @param args
	 */
	public static void main(String[] args) {
		String s = "plain text 1";
		byte[] iv = getRandom();
		try {
			System.out.println("iv:\t\t" + Base64.getEncoder().encodeToString(iv));

			String cipher = encrypt(s, "infochange", iv);
			System.out.println("cipher:\t\t" + cipher);

			String plain = decrypt(cipher, "infochange", iv);
			System.out.println("plain-text:\t" + plain);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return 16 random bytes
	 */
	public static byte[] getRandom() {
		byte[] iv = new byte[16];
	    try (CryptoRandom random = CryptoRandomFactory.getCryptoRandom(randomProperties)) {
	        random.nextBytes(iv);
	        return iv;
	    }catch(IOException | GeneralSecurityException ex) {
	    	ex.printStackTrace();
	    	return null;
	    }
	}

	/**
	 * Decrypt then encrypt.
	 * @param cypher Base64
	 * @param decryptK plain key string
	 * @param decryptIv Base64
	 * @param encryptK plain key string
	 * @return [cipher-base64, new-iv-base64]
	 * @throws GeneralSecurityException
	 * @throws IOException
	 * @return string[b64(cipher), b64(iv)]
	 */
	public static String[] dencrypt(String cypher, String decryptK, String decryptIv,
			String encryptK) throws GeneralSecurityException, IOException {
		byte[] iv = AESHelper.decode64(decryptIv);
		byte[] input = AESHelper.decode64(cypher);
		byte[] dkb = getUTF8Bytes(pad16_32(decryptK)); // FIXME won't work for non ASCII
		byte[] plain = decryptEx(input, dkb, iv);
		byte[] eiv = getRandom();
		byte[] ekb = getUTF8Bytes(pad16_32(encryptK)); // FIXME won't work for non ASCII
		byte[] output = encryptEx(plain, ekb, eiv);
        String b64 = Base64.getEncoder().encodeToString(output);
        return new String[] {b64, AESHelper.encode64(eiv)};
	}

	/**
	 * Encrypt plain text to cipher of base 64.
	 * 
	 * @param plain
	 * @param key
	 * @param iv
	 * @return
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public static String encrypt(String plain, String key, byte[] iv)
			throws GeneralSecurityException, IOException {
		if (!plain.trim().equals(plain))
			throw new GeneralSecurityException("Plain text to be encrypted can not begin or end with space.");

		key = pad16_32(key);
		plain = pad16_32(plain);
		byte[] input = getUTF8Bytes(plain);
		byte[] kb = getUTF8Bytes(key);
		byte[] output = encryptEx(input, kb, iv);
        String b64 = Base64.getEncoder().encodeToString(output);
        return b64;
	}

	/**
	 * FIXME delete these comments by the future.
	 * 10 Dec 2024:<br>
	 * This line causes trouble in JDK 15, Open JDK x64 - fixed by adding a lock.
	 * 
	 * @param input
	 * @param key
	 * @param iv
	 * @return result bytes
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	static byte[] encryptEx(byte[] input, byte[] key, byte[]iv) throws GeneralSecurityException, IOException {
		final SecretKeySpec keyspec = new SecretKeySpec(key, "AES");
		final IvParameterSpec ivspec = new IvParameterSpec(iv);

        //Initializes the cipher with ENCRYPT_MODE, key and iv.
        try {
        	lock.lock(); // can't concurrently work in Open JDK 15 for x64 

			encipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);

			byte[] output = new byte[((input.length)/16 + 2) * 16];
			// int finalBytes = encipher.doFinal(input, 0, input.length, output, 0);
			// above code is incorrect (not working with PKCS#7 padding),
			// check Apache Common Crypto User Guide:
			// https://commons.apache.org/proper/commons-crypto/userguide.html
			// Usage of Byte Array Encryption/Decryption, CipherByteArrayExample.java
			int updateBytes = encipher.update(input, 0, input.length, output, 0);
			int finalBytes  = encipher.doFinal(input, 0, 0, output, updateBytes);

			output = Arrays.copyOf(output, updateBytes + finalBytes);
			encipher.close();
			return output;
		} catch (GeneralSecurityException e) {
			throw new GeneralSecurityException(e.getMessage());
		}
        finally {
        	lock.unlock();
        }
	}

	public static String decrypt(String cypher, String key, byte[] iv)
			throws GeneralSecurityException, IOException {
		byte[] input = Base64.getDecoder().decode(cypher);
		// FIXME should padding bytes, not string.
		byte[] kb = getUTF8Bytes(pad16_32(key)); // FIXME won't work for non ASCII
		byte[] output = decryptEx(input, kb, iv);
        String p = setUTF8Bytes(output);
        // return p.replace("-", "");
        return depad16_32(p);
	}

	static byte[] decryptEx(byte[] input, byte[] key, byte[]iv)
			throws GeneralSecurityException, IOException {

		final SecretKeySpec keyspec = new SecretKeySpec(key, "AES");
		final IvParameterSpec ivspec = new IvParameterSpec(iv);

        try {
        	lock.lock(); // can't concurrently work in Open JDK 15 for x64 
			encipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
			byte[] output = new byte[((input.length)/ 16 + 2) * 16];

			int finalBytes = encipher.doFinal(input, 0, input.length, output, 0);

			encipher.close();

			return Arrays.copyOf(output, finalBytes);
        } finally { lock.unlock(); }
	}

	/**
	 * @param s string of ASCII
	 * @return 16 / 32 byte string
	 * @throws GeneralSecurityException
	 */
	public static String pad16_32(String s) throws GeneralSecurityException {
		int l = s.length();
		if (l <= 16)
			return String.format("%1$16s", s).replaceAll(" ", "-");
		else if (l <= 32)
			return String.format("%1$32s", s).replaceAll(" ", "-");
		else
			throw new GeneralSecurityException("Not supported block length(16B/32B): " + s);
	}

	private static String depad16_32(String s) throws GeneralSecurityException {
		int l = s.length();
		if (l <= 16)
			return s.replaceAll("-", " ").trim();
		else if (l <= 32)
			return s.replaceAll("-", " ").trim();
		else
			throw new GeneralSecurityException("Not supported block length(16B/32B): " + s);
	}


    /**
     * Converts String to UTF8 bytes
     *
     * @param input the input string
     * @return UTF8 bytes
     */
    private static byte[] getUTF8Bytes(String input) {
        return input.getBytes(StandardCharsets.US_ASCII);
    }

    /**Converts UTF8 bytes to String
     * @param input
     * @return converted result
     */
    private static String setUTF8Bytes(byte[] input) {
    	return new String(input, StandardCharsets.US_ASCII);
    }

	public static String encode64(final byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
	}

	/**
	 * @param ifs
	 * @param blockSize default 3 * 1024 * 1024;
	 * @return encoded string
	 * @throws IOException
	 * ISSUE: we need a stream mode version
	 */
	public static String encode64(final InputStream ifs, int blockSize) throws IOException {
		blockSize = blockSize > 0 ? blockSize : 3 * 1024 * 1024;

		if ((blockSize % 12) != 0)
			throw new IOException ("Block size must be multple of 12.");

		byte[] chunk = new byte[blockSize];

		return encode63(chunk, ifs, 0, blockSize);
	}

	/**
	 * @deprecated bug fixed by {@link #encode64(byte[], InputStream, int, int)}.
	 * Usage example: <pre>
	 * byte[] buf = new byte[n * 3];
	 * int index = 0;
	 * while (index &lt; file_size) {
	 * 	int readlen  = Math.min(buf.length, size - index);
	 * 	String str64 = encode64(buf, ifs, index, readlen);
	 * 	index += readlen;
	 * 	// consumption of str64
	 * 	...
	 * }</pre>
	 * 
	 * @param buf
	 * @param ifs file input stream
	 * @param start
	 * @param len
	 * @return encoded string, length 0 if read nothing.
	 * @throws IOException
	 * @throws TransException buffer length is not multiple of 3.
	 */
	public static String encode63(byte[] buf, final InputStream ifs, int start, int len) throws IOException {
		BufferedInputStream in = new BufferedInputStream(ifs, buf.length);
		Base64.Encoder encoder = Base64.getEncoder();

		int readLen = in.read(buf);

		if (readLen <= 0)
			return null;
		else if (readLen == buf.length)
			return encoder.encodeToString(buf);
		else // (readLen < buf.length)
			return encoder.encodeToString(Arrays.copyOf(buf, readLen));
	}

	public static String encode64(byte[] buf, final InputStream ifs, int start, int len) throws IOException {
		BufferedInputStream in = new BufferedInputStream(ifs, buf.length);
		Base64.Encoder encoder = Base64.getEncoder();

		int readLen = in.read(buf, start, len);

		if (readLen <= 0)
			return null;
		else if (readLen == buf.length)
			return encoder.encodeToString(buf);
		else // (readLen < buf.length)
			return encoder.encodeToString(Arrays.copyOf(buf, readLen));
	}
	
	public static byte[] decode64(String str) {
        return Base64.getDecoder().decode(str);
	}

	/**
	 * Is encrypt(plain, k, v) == cipher?
	 * i.e. encrypt(uid:random, k, iv) == token ? where uid:random = clientoken
	 * 
	 * @return true: yes the same
	 * @throws Exception
	 */
	public static boolean verifyToken(String requestoken, String myKnowledge, String uid, String key)
			throws Exception {
		String[] sstoken = requestoken.split(":");
		String enciphered = encrypt(pad16_32(uid + ":" + myKnowledge), key, decode64(sstoken[1]));
		return eq(enciphered, sstoken[0]);
	}

	/**
	 * <pre>
	 * ssToken = cipher : iv, len(cipher) = 44
	 * plain = decrypt(cipher, key, iv)
	 * token = encrypt(pad(uid) : plain, key, iv2)
	 * return token : iv2, if cipher.length == 44, len(token : iv2) = 44 + 1 + 24 = 69
	 * </pre>
	 * 
	 * @return token for managed session requests, token:iv2, len = 69 for ssToken.len = 69
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public static String repackSessionToken(String ssToken, String key, String uid)
			throws GeneralSecurityException, IOException {
		String[] ss = ssToken.split(":");
		String plain = decrypt(ss[0], key, decode64(ss[1]));

		byte[] iv = getRandom();
		String cipher = encrypt(uid + ":" + plain, key, iv);
		return cipher + ":" + encode64(iv);
	}

	/**
	 * <pre>
	 * iv = random(16)
	 * token = encrypt(random, key, iv), len(random) = 24
	 * return [token : iv, token]
	 * where len(token) = [(24 + 15) / 16] * 16 * [4/3] = 32 * [4/3] = 44
	 * </pre>
	 * 
	 * @param key
	 * @return 0: string(token : iv), 1: knowledge in base 64 (random token)
	 * @throws GeneralSecurityException
	 * @throws IOException
	 * @since 1.4.37
	 * @see AESHelperTest#testSessionToken()
	 */
	public static String[] packSessionKey(String key) 
			throws GeneralSecurityException, IOException {
		byte[] iv = AESHelper.getRandom();
		byte[] knows = AESHelper.getRandom();
		String token = AESHelper.encode64(knows);
		return new String[] {AESHelper.encrypt(token, key, iv) + ":" + AESHelper.encode64(iv), token};
	}
	
	static int Block_Size = 1024 * 8;
	public static int blockSize() { return Block_Size; }
	
	public static String encodeRange(File file, long start, long length) throws IOException {
		try ( ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			stream64(file, baos, start, length);
			return baos.toString();
		}
	}

	public static long stream64(File file, OutputStream output, long start, long length) throws IOException {
		if (length % 3 != 0) throw new IOException(
			f("length %s | 3 != 0. Blocks to be encoded must be in length of 3*n - and you cannot start at a breakpoint index other than multiple of 3.",
			  length));
		return stream(file, Base64.getEncoder().wrap(output), start, length);
	}

	public static long stream(File file, OutputStream output, long start, long length) throws IOException {
		if (start == 0 && length >= file.length()) {
			try ( ReadableByteChannel inputChannel = Channels.newChannel(new FileInputStream(file));
				  WritableByteChannel outputChannel = Channels.newChannel(output)) {
				ByteBuffer buffer = ByteBuffer.allocateDirect(Block_Size);
				long size = 0;

				while (inputChannel.read(buffer) != -1) {
					buffer.flip();
					size += outputChannel.write(buffer);
					buffer.clear();
				}

				return size;
			}
		}
		else {
			try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(file.toPath(), StandardOpenOption.READ)) {
				WritableByteChannel outputChannel = Channels.newChannel(output);
				ByteBuffer buffer = ByteBuffer.allocateDirect(Block_Size);
				long size = 0;

				while (fileChannel.read(buffer, start + size) != -1) {
					buffer.flip();

					if (size + buffer.limit() > length) {
						buffer.limit((int) (length - size));
					}

					size += outputChannel.write(buffer);

					if (size >= length) break;

					buffer.clear();
				}

				return size;
			}
		}
	}
}
