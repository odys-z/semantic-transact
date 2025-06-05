package io.odysz.transact.sql.parts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import io.odysz.common.DocLocks;
import io.odysz.common.FilenameUtils;
import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.Transcxt;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.x.TransException;

/**
 * External file representation - mapping URI and file path back and forth.<br>
 * @see ExtFileInsert
 * 
 * @author odys-z@github.com
 */
public class ExtFileUpdatev2 extends ExprPart {
//	private String runtimePath;
//	String configRoot;
//	private String prefix;
//	private String nameId;
//	private String filename;

	private String oldUri;

	/**
	 * javax.servlet.ServletContext#getRealPath(String)</a>.<br>
	 * @param recId
	 * @param configRoot, root path in config.xml
	 * @param stx instance of run time context
	 */
	public ExtFileUpdatev2(String recId, String filename) {
		this.extpaths = new ExtFilePaths(recId, filename);
		// this.resulv_const_path = resulvingPath;
//		this.nameId = recId;
//		this.configRoot = configRoot;
//		this.runtimePath = stx.containerRoot();
	}

	/**Set the sub-path of the file - semantically sub-path of uploading.
	 * This part is saved in the replaced file path in database field.
	 * @param path
	 * @param subs
	 * @return this
	public ExtFileUpdatev2 prefixPath(String path, String...subs) {
		this.prefix = FilenameUtils.concat(path, subs);
		return this;
	}

	public ExtFileUpdatev2 appendSubFolder(Object sub) {
		if (sub != null)
			this.prefix = FilenameUtils
				.concat(this.prefix == null ? "" : this.prefix, sub.toString());
		return this;
	}
	 */
	
//	public ExtFileUpdatev2 filename(String name) {
//		this.filename = name;
//		return this;
//	}
	
	public ExtFileUpdatev2 b64(String b64) throws TransException {
		throw new TransException("ExtFileUpdate can only be used for moving file. To update file content, delete then insert the record.");
	}
	
	/**
	 * Set uri of old file to be changed / updated.
	 * @param uri
	 * @return this
	 */
	public ExtFileUpdatev2 oldUri(String uri) {
		this.oldUri = uri;
		return this;
	}
	
	ExtFilePaths extpaths;

	@Override
	public String sql(ISemantext ctx) throws TransException {
		if (oldUri == null) throw new TransException("No uri (file) to move. Called oldUri() ?");
		
//		String relatvFn = ExtFileInsertv2.encodeUri(nameId, configRoot, prefix, filename);
//		String absoluteFn = ExtFileInsertv2.decodeUri(runtimePath, relatvFn);
//		String absoluteOld = ExtFileInsertv2.decodeUri(runtimePath, oldUri);

		String absoluteFn = extpaths.abspath();
		String absoluteOld = ExtFilePaths.decodeUri(Transcxt.runtimeRoot(), oldUri);

		if (absoluteOld.equals(absoluteFn))
			return "'" + extpaths.dburi(true) + "'";

		ExtFileInsertv2.touchDir(FilenameUtils.getFullPath(absoluteFn));
		
		Path f = Paths.get(absoluteFn);
		Path old = Paths.get(absoluteOld);

		if (!Files.exists(old))
			throw new TransException("Uri (file) doesn't exits - committing sql or updating mulitple times?");

		try {
			// Resolve name conflict
//			while (Files.exists(f, LinkOption.NOFOLLOW_LINKS)) {
//				Random random = new Random();
//				// String rand = Radix32.toString(random.nextInt(), 4);
//
//
//				extpaths = new ExtFilePaths(nameId, random,  filename).subpath(args);
//				f = Paths.get(absoluteFn);
//			}
			absoluteFn = extpaths.avoidConflict(absoluteFn);

			DocLocks.writing(f);
			Files.move(old, f, StandardCopyOption.ATOMIC_MOVE);

			// mysql doesn't like windows' path separator
			return "'" + extpaths.dburi(true) + "'";
		} catch (IOException e) {
			e.printStackTrace();
			return "''";
		}
		finally { DocLocks.writen(f); }
	}

	public void subpaths(int i, String[] args) {
		extpaths.subpath(Arrays.copyOfRange(args, i, args.length));
	}
}
