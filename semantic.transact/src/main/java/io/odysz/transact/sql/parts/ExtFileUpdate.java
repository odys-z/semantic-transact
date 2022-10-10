package io.odysz.transact.sql.parts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Random;

import org.apache.commons.io_odysz.FilenameUtils;

import io.odysz.common.Radix32;
import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.x.TransException;

/**
 * External file representation - mapping URI and file path back and forth.<br>
 * @see ExtFileInsert
 * 
 * @author odys-z@github.com
 */
public class ExtFileUpdate extends ExprPart {
	private String runtimePath;
	private String configRoot;
	private String prefix;
	private String nameId;
	private String filename;

	private String oldUri;

	/**
	 * javax.servlet.ServletContext#getRealPath(String)</a>.<br>
	 * @param recId
	 * @param configRoot, root path in config.xml
	 * @param stx instance of run time context
	 */
	public ExtFileUpdate(String recId, String configRoot, ISemantext stx) {
		// this.resulv_const_path = resulvingPath;
		this.nameId = recId;
		this.configRoot = configRoot;
		this.runtimePath = stx.containerRoot();
	}

	/**Set the sub-path of the file - semantically sub-path of uploading.
	 * This part is saved in the replaced file path in database field.
	 * @param path
	 * @param subs
	 * @return this
	 */
	public ExtFileUpdate prefixPath(String path, String...subs) {
		this.prefix = FilenameUtils.concat(path, subs);
		return this;
	}

	public ExtFileUpdate appendSubFolder(Object sub) {
		if (sub != null)
			this.prefix = FilenameUtils
				.concat(this.prefix == null ? "" : this.prefix, sub.toString());
		return this;
	}
	
	public ExtFileUpdate filename(String name) {
		this.filename = name;
		return this;
	}
	
	public ExtFileUpdate b64(String b64) throws TransException {
		throw new TransException("ExtFileUpdate can only used for moving file. To update file content, use insert then delete.");
	}
	
	/**
	 * Set uri of old file to be changed / updated.
	 * @param uri
	 * @return this
	 */
	public ExtFileUpdate oldUri(String uri) {
		this.oldUri = uri;
		return this;
	}

	@Override
	public String sql(ISemantext ctx) throws TransException {
		if (oldUri == null) throw new TransException("No uri (file) to move. Called oldUri() ?");
		
		String relatvFn = ExtFileInsert.encodeUri(nameId, configRoot, prefix, filename);
		String absoluteFn = ExtFileInsert.decodeUri(runtimePath, relatvFn);
		String absoluteOld = ExtFileInsert.decodeUri(runtimePath, oldUri);
		
		if (absoluteOld.equals(absoluteFn))
			return "'" + relatvFn.replaceAll("\\\\", "/") + "'";

		ExtFileInsert.touchDir(FilenameUtils.getFullPath(absoluteFn));
		
		Path f = Paths.get(absoluteFn);
		Path old = Paths.get(absoluteOld);

		if (!Files.exists(old))
			throw new TransException("Uri (file) doesn't exits - commit(sql) or update mulitple times?");

		try {
			while (Files.exists(f, LinkOption.NOFOLLOW_LINKS)) {
				Random random = new Random();
				String rand = Radix32.toString(random.nextInt(), 4);

				relatvFn = ExtFileInsert.encodeUri(nameId, configRoot, prefix, rand + " " + filename);
				absoluteFn = ExtFileInsert.decodeUri(runtimePath, relatvFn);
				f = Paths.get(absoluteFn);
			}

			Files.move(old, f, StandardCopyOption.ATOMIC_MOVE);

			// mysql doesn't like windows' path separator
			return "'" + relatvFn.replaceAll("\\\\", "/") + "'";
		} catch (IOException e) {
			e.printStackTrace();
			return "''";
		}
	}

//	protected void touchDir(String dir) {
//		File f = new File(dir);
//		if (f.isDirectory())
//			return;
//		else if (!f.exists())
//			// create dir
//			f.mkdirs();
//		else
//			// must be a file
//			Utils.warn("FATAL ExtFile can't create a folder, a same named file exists: ", dir);
//	}
}
