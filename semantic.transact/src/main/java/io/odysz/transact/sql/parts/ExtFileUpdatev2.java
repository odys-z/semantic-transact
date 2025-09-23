package io.odysz.transact.sql.parts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Map;

import io.odysz.common.DocLocks;
import io.odysz.common.FilenameUtils;
import io.odysz.common.Utils;
import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.x.TransException;

/**
 * External file representation - mapping URI and file paths back and forth.<br>
 * @see ExtFileInsert
 * 
 * @author odys-z@github.com
 * @since 1.5.60
 */
public class ExtFileUpdatev2 extends ExprPart {

	private String oldUri;

	/**
	 * javax.servlet.ServletContext#getRealPath(String)</a>.<br>
	 * @param volume, i.e. config-root, root path in config.xml
	 * @param recId
	 */
	public ExtFileUpdatev2(String volume, String recId, String filename) {
		this.extpaths = new ExtFilePaths(volume, recId, filename);
	}

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
		
		String absoluteFn = decodeUriPath();
		// String absoluteOld = ExtFilePaths.decodeUri(ctx.containerRoot(), oldUri);
		String absoluteOld = ExtFilePaths.decodeUriPath(oldUri);

		if (absoluteOld.equals(absoluteFn))
			return "'" + extpaths.dburi(true) + "'";

		Utils.touchDir(FilenameUtils.getFullPath(absoluteFn));
		
//		Path f = Paths.get(absoluteFn);
		Path f = null;
		Path old = Paths.get(absoluteOld);

		if (!Files.exists(old))
			throw new TransException("Uri (file) doesn't exits - committing sql or updating mulitple times?");

		try {
			// FIXME shouldn't call this before creating 'f'?
			// System.err.println("FIXME shouldn't call this before creating 'f'?");
			absoluteFn = extpaths.avoidConflict(absoluteFn);
			f = Paths.get(absoluteFn);

			DocLocks.writing(f);
			Files.move(old, f, StandardCopyOption.ATOMIC_MOVE);

			return "'" + extpaths.dburi(true) + "'";
		} catch (IOException e) {
			// TODO FIXME
			// This is a data integrate breach. Must be fixed by moving this, moving the file, to postOps.
			// See ShExtFile.onDelete()
			e.printStackTrace();
			return "''";
		}
		finally { if (f != null) DocLocks.writen(f); }
	}

	/**
	 * For insert, get ext-path
	 * @return
	 */
	public String decodeUriPath() {
		return extpaths.decodeUriPath();
	}
	
	/**
	 * For update/delete, get ext-path
	 * @return
	 */
	public static String decodeUriPath(String dburi) {
		return ExtFilePaths.decodeUriPath(dburi);
	}

	public void subpaths(String[] subcols, Map<String, Integer> colnames,
			ArrayList<Object[]> row) throws TransException {
		extpaths.subpath(subcols, colnames, row);
	}
}
