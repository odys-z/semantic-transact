package io.odysz.common;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.crypto.cipher.CryptoCipher;
import org.apache.commons.crypto.random.CryptoRandom;
import org.apache.commons.crypto.random.CryptoRandomFactory;
import org.apache.commons.crypto.utils.Utils;

/**Static helpers for encrypt/decipher string.
 * <table>
 * <tr><td></td><td></td></tr>
 * </trable>
 * @author ody
 */
public class AESHelper {
    static Properties randomProperties = new Properties();
    /**Deprecating static final String transform = "AES/CBC/PKCS5Padding";<br>
     * Apache Common Crypto only support PKCS#5 padding, but most js lib support PKCS#7 padding,
     * This makes trouble when negotiation with those API.
     * Solution: using no padding here, round the text to 16 or 32 ASCII bytes.
     */
    static final String transform = "AES/CBC/NoPadding";
    static CryptoCipher encipher;

    static {
    	randomProperties.put(CryptoRandomFactory.CLASSES_KEY,
    			CryptoRandomFactory.RandomProvider.JAVA.getClassName());

        Properties cipherProperties = new Properties();
        // causing problem for different environment:
        // cipherProperties.setProperty(CryptoCipherFactory.CLASSES_KEY, CipherProvider.JCE.getClassName());
    	try {
			encipher = Utils.getCipherInstance(transform, cipherProperties);
	        //decipher = Utils.getCipherInstance(transform, properties);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

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

	/**Decrypt then encrypt.
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
		byte[] dkb = getUTF8Bytes(pad16_32(decryptK));
		byte[] plain = decryptEx(input, dkb, iv);
		byte[] eiv = getRandom();
		byte[] ekb = getUTF8Bytes(pad16_32(encryptK));
		byte[] output = encryptEx(plain, ekb, eiv);
        String b64 = Base64.getEncoder().encodeToString(output);
        return new String[] {b64, AESHelper.encode64(eiv)};
	}

	public static String encrypt(String plain, String key, byte[] iv)
			throws GeneralSecurityException, IOException {
		key = pad16_32(key);
		plain = pad16_32(plain);
		byte[] input = getUTF8Bytes(plain);
		byte[] kb = getUTF8Bytes(key);
		byte[] output = encryptEx(input, kb, iv);
        String b64 = Base64.getEncoder().encodeToString(output);
        return b64;
	}
	
	static byte[] encryptEx(byte[] input, byte[] key, byte[]iv) throws GeneralSecurityException, IOException {
		//System.out.println("txt: " + plain);
		//System.out.println("key: " + key);
		final SecretKeySpec keyspec = new SecretKeySpec(key, "AES");
		final IvParameterSpec ivspec = new IvParameterSpec(iv);

        //Initializes the cipher with ENCRYPT_MODE, key and iv.
        try {
			encipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);

        byte[] output = new byte[((input.length)/16 + 2) * 16]; // + 1 will throw exception

        // int finalBytes = encipher.doFinal(input, 0, input.length, output, 0);
        // above code is incorrect (not working with PKCS#7 padding),
        // check Apache Common Crypto User Guide:
        // https://commons.apache.org/proper/commons-crypto/userguide.html
        // Usage of Byte Array Encryption/Decryption, CipherByteArrayExample.java
        int updateBytes = encipher.update(input, 0, input.length, output, 0);
        //System.out.println("updateBytes " + updateBytes);
        int finalBytes = encipher.doFinal(input, 0, 0, output, updateBytes);
        //System.out.println("finalBytes " + finalBytes);
        output = Arrays.copyOf(output, updateBytes + finalBytes);
        encipher.close();
        return output;
		} catch (GeneralSecurityException e) {
			throw new GeneralSecurityException(e.getMessage());
		}
	}
	
	public static String decrypt(String cypher, String key, byte[] iv)
			throws Exception {
		byte[] input = Base64.getDecoder().decode(cypher);
		byte[] kb = getUTF8Bytes(pad16_32(key));
		byte[] output = decryptEx(input, kb, iv);
        String p = setUTF8Bytes(output);
        //return p.trim();
        return p.replace("-", "");
	}
	
	static byte[] decryptEx(byte[] input, byte[] key, byte[]iv) throws GeneralSecurityException, IOException {
		//key = pad16_32(key);
		//cypher = pad16_32(cypher);
		//byte[] input = Base64.getDecoder().decode(cypher);

		final SecretKeySpec keyspec = new SecretKeySpec(key, "AES");
		final IvParameterSpec ivspec = new IvParameterSpec(iv);

        encipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
        byte[] output = new byte[((input.length)/ 16 + 2) * 16];

        int finalBytes = encipher.doFinal(input, 0, input.length, output, 0);

        encipher.close();
        
        return Arrays.copyOf(output, finalBytes);
        //return setUTF8Bytes(Arrays.copyOf(output, finalBytes));
	}
	
	private static String pad16_32(String s) throws GeneralSecurityException {
		int l = s.length();
		if (l <= 16)
			return String.format("%1$16s", s).replace(' ', '-');
		else if (l <= 32)
			return String.format("%1$32s", s).replace(' ', '-');
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
        return input.getBytes(StandardCharsets.UTF_8);
    }
    
    /**Converts UTF8 bytes to String
     * @param input
     * @return
     */
    private static String setUTF8Bytes(byte[] input) {
    	return new String(input, StandardCharsets.UTF_8);
    }

	public static String encode64(final byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
	}

	public static byte[] decode64(String str) {
        return Base64.getDecoder().decode(str);
	}

	/**Is encrypt(plain, k, v) == cipher?
	 * @param plain
	 * @param cipher
	 * @param k
	 * @param iv
	 * @return true: yes the same
	 * @throws Exception
	 */
	public static boolean isSame(String cipher, String plain, String k, String iv) throws Exception {
		String enciphered = encrypt(plain, k, decode64(iv));
		return enciphered.equals(cipher);
	}

}
