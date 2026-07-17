package io.odysz.common;

import static io.odysz.common.LangExt._0;
import static io.odysz.common.LangExt.ifnull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CheapIO {

	public static String readB64(String filename) throws IOException {
		Path p = Paths.get(filename);
		byte[] f = Files.readAllBytes(p);
		return AESHelper2.encode64(f);
	}
	
	/**
     * Copies a source file to a new file in the same directory, appending a formatted 
     * timestamp and a custom suffix (extension) to the filename.
     * 
     * @param src    The absolute or relative path to the source file.
     * @param suffix The new file extension (e.g., "txt" or ".txt").
     * @return       The absolute path of the newly created copy as a String.
     * @throws IOException If the source file doesn't exist or the copy operation fails.
     */
    public static String genTestFile(String src, String... suffix) throws IOException {
        Path sourcePath = Paths.get(src);
        if (!Files.exists(sourcePath)) {
            throw new IOException("Source file does not exist: " + src);
        }

        // Clean up the suffix (remove leading dot if present)
        String cleanSuffix = null;

        // Get the parent directory and the raw filename
        Path parentDir = sourcePath.getParent();
        String originalFileName = sourcePath.getFileName().toString();

        // Strip the original extension to get just the base name
        String baseName = originalFileName;
        int lastDotIndex = originalFileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            baseName = originalFileName.substring(0, lastDotIndex);
            String _suffix = originalFileName.substring(lastDotIndex + 1);
            if (cleanSuffix == null)
            	cleanSuffix = _suffix;

            cleanSuffix = ifnull(_0(suffix), _suffix);
        }

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        String timestamp = now.format(formatter);

        // Construct the new filename: baseName-timestamp.suffix
        String newFileName = String.format("%s-%s.%s", baseName, timestamp, cleanSuffix);

        // Resolve the destination path relative to the source file's parent directory
        Path destPath = parentDir != null ? parentDir.resolve(newFileName) : Paths.get(newFileName);

        // Copy the file (overwriting if a file with the exact same timestamped name already exists)
        Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);

        return destPath.toAbsolutePath().toString();
    }
}
