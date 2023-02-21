package io.odysz.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CheapIO {

	public static String readB64(String filename) throws IOException {
		Path p = Paths.get(filename);
		byte[] f = Files.readAllBytes(p);
		return AESHelper.encode64(f);
	}
}
