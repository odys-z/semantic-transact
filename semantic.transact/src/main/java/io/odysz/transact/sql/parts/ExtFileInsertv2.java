package io.odysz.transact.sql.parts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

import io.odysz.common.AESHelper;
import io.odysz.common.DocLocks;
import io.odysz.common.FilenameUtils;
import io.odysz.common.Utils;
import io.odysz.semantics.ISemantext;
import io.odysz.transact.x.TransException;

/**
 * An external file representation - mapping URI and file path back and forth.<br>
 * 
 * An ExtFile can only been used as a setting value in update/insert statement.
 * 
 * <p>This class is only used for update and insert. For reading,
 * use {@link io.odysz.transact.sql.parts.condition.Funcall#extfile(String...) Funcall.extfile(String...)} </p>
 * 
 * <h4>Note</h4>
 * The {@link #sql(ISemantext)} method can be only used as insert mode. The DASemantics.ShExtFile only place this
 * class in insert statement.
 * 
 * @since 1.5.60 experimental for modularize file path encode / decode with environment variables,
 * and experimenting new b64 encode / decode in stream mode.
 * 
 * @author odys-z@github.com
 * 
 * @since 1.5.60
 */
public class ExtFileInsertv2 extends AbsPart {
	private String b64;

	/**
	 * @param resulvingId e.g. docId Resulve.
	 * @param volume e. g. args[0] in semantics.xml, $VOLUME_HOME
	 * @param ctx 
	 * @param runtimeRoot typically the return of {@link ISemantext#containerRoot()}
	 * @throws TransException 
	public ExtFileInsertv2(String volume, ExprPart resulvingId, ISemantext ctx) throws TransException {
		this.extpaths = new ExtFilePaths(volume, resulvingId.sql(ctx), null);
	}
	 */

	/**
	 * Create an exteranl file representation by setting the root path.
	 * This path is used to access file together with the relative path set
	 * by {@link ExtFileInsert#prefixPath(String, String...)}.<br>
	 * 
	 * The argument doesn't have to be absolute path if the runtime can access a file from a relative path.
	 * 
	 * @throws TransException 
	 */
	public ExtFileInsertv2(ExtFilePaths extpaths) throws TransException {
		this.extpaths = extpaths;
	}

	public ExtFileInsertv2 filename(String name) {
		this.extpaths.filename = name;
		return this;
	}
	
	/**
	 * set b64 string value, for write to file.
	 * 
	 * FIXME Performance problem: stream mode is needed
	 * 
	 * @param b64
	 * @return
	 */
	public ExtFileInsertv2 b64(Object b64) {
		this.b64 = b64.toString();
		return this;
	}

	ExtFilePaths extpaths;
	
	/**
	 * Decode this.b64, then save to file at pathname, and generate the sql snippet.
	 * 
	 * <p>FIXME b64 decoding has a performance problem - too much memory.</p>
	 * 
	 * @see io.odysz.transact.sql.parts.AbsPart#sql(io.odysz.semantics.ISemantext)
	 */
	@Override
	public String sql(ISemantext ctx) throws TransException {
		String absoluteFn = extpaths.decodeUriPath();

		Utils.touchDir(FilenameUtils.getFullPath(absoluteFn));

		Path f = Paths.get(absoluteFn);
		byte[] b;
		try {
			b = AESHelper.decode64(b64); // Performance problem: stream mode is needed
		} catch (Exception e) {
			b = b64.getBytes();
			Utils.warnT(new Object() {},
				"Cannot decode uri in base64.\npath: %s,\nuri: %s ...",
				f.toAbsolutePath(), b64 == null ? "" : b64.substring(0, 32) );
			e.printStackTrace();
		}

		try {
			DocLocks.writing(f);
			Files.write(f, b);

			// mysql doesn't like windows' path separator
			return "'" + extpaths.dburi(true) + "'";
		} catch (IOException e) {
			e.printStackTrace();
			return "''";
		}
		finally { DocLocks.writen(f); }
	}
	
	/**
	 * 
	 * @param ctx
	 * @return concat({@link #runtimePath} / {@link #volume} / {@link #prefix} / {@link #resulv_const_path}-{@link #filename}).replace-env()
	 * @throws TransException
	 */
	public String absolutePath(ISemantext ctx) throws TransException {
		return extpaths.decodeUriPath();
	}
	
	public ExtFileInsertv2 subpaths(String[] args, Map<String, Integer> cols, ArrayList<Object[]> row) throws TransException {
			extpaths.subpath(args, cols, row);
		return this;
	}
}
