package io.odysz.transact.sql.parts;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io_odysz.FilenameUtils;

import io.odysz.common.AESHelper;
import io.odysz.common.DocLocks;
import io.odysz.common.EnvPath;
import io.odysz.common.LangExt;
import io.odysz.common.Utils;
import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.x.TransException;

/**External file representation - mapping URI and file path back and forth.<br>
 * An ExtFile can only been used as a setting value in update/insert statement.
 * <p>This class is only used for update and insert. For reading,
 * use {@link io.odysz.transact.sql.parts.condition.Funcall#extFile(String) Funcall.extFile(String)} </p>
 * <h4>Note</h4>
 * The {@link #sql(ISemantext)} method can be only used as insert mode. The DASemantics.ShExtFile only place this
 * class in insert statement.
 * 
 * @author odys-z@github.com
 */
public class ExtFileInsert extends AbsPart {
	private String b64;
	private ExprPart resulv_const_path;
	private String prefix;
	private String filename;
	private String configRoot;
	private String runtimePath;

	/**
	 * @param resulvingPath e.g. Id Resulve + client name.
	 * @param configRoot root path in config.xml
	 * @param runtimeRoot typically the return of {@link ISemantext#containerRoot()}
	 */
	public ExtFileInsert(ExprPart resulvingPath, String configRoot, String runtimeRoot) {
		this.resulv_const_path = resulvingPath;
		this.configRoot = configRoot;
		this.runtimePath = runtimeRoot;
	}

	/**
	 * Set the absolute root path. This path is used to access file together with the relative path set by {@link ExtFileInsert#prefixPath(String)}.<br>
	 * The argument doesn't have to be absolute path if the runtime can access a file from a relative path.<br>
	 * But servlet containers needing absolute paths to access file, so this must been set to the absolute path,
	 * such as the return of <a href='https://docs.oracle.com/javaee/6/api/javax/servlet/ServletContext.html'>
	 * javax.servlet.ServletContext#getRealPath(String)</a>.<br>
	 * 
	 * @param fn file name to be resolved
	 * @param configRoot root path in config.xml
	 * @param stx instance of run time context
	 */
	public ExtFileInsert(Resulving fn, String configRoot, ISemantext stx) {
		this(fn, configRoot, stx.containerRoot());
	}

	/**@see #ExtFileInsert(Resulving, String, ISemantext)
	 * @param resulvingPath
	 * @param configRoot root path in config.xml
	 * @param stx
	 */
	public ExtFileInsert(ExprPart resulvingPath, String configRoot, ISemantext stx) {
		this(resulvingPath, configRoot, stx.containerRoot());
	}

	/**Set the sub-path of the file - semantically sub-path of uploading.
	 * This part is saved in the replaced file path in database field.
	 * @param path
	 * @param subs
	 * @return this
	 */
	public ExtFileInsert prefixPath(String path, String...subs) {
		this.prefix = FilenameUtils.concat(path, subs);
		return this;
	}
	
	public ExtFileInsert appendSubFolder(Object sub) {
		if (sub != null)
			this.prefix = FilenameUtils
				.concat(this.prefix == null ? "" : this.prefix, sub.toString());
		return this;
	}
	
	public ExtFileInsert filename(String name) {
		this.filename = name;
		return this;
	}
	
	public ExtFileInsert b64(String b64) {
		this.b64 = b64;
		return this;
	}

	/**
	 * Save file to pathname, generate sql.
	 * 
	 * @see io.odysz.transact.sql.parts.AbsPart#sql(io.odysz.semantics.ISemantext)
	 */
	@Override
	public String sql(ISemantext ctx) throws TransException {
		String relatvFn = encodeUri(resulv_const_path, configRoot, prefix, filename, ctx);
		String absoluteFn = absolutePath(ctx);

		touchDir(FilenameUtils.getFullPath(absoluteFn));

		Path f = Paths.get(absoluteFn);
		byte[] b;// = AESHelper.decode64(b64);
		try {
			b = AESHelper.decode64(b64);
		} catch (Exception e) {
			b = b64.getBytes();
			Utils.warnT(new Object() {},
				"Cannot decode uri in base64.\npath: %s,\nuri: %s ...",
				f.toAbsolutePath(), b64 == null ? "" : b64.substring(0, 32) );
			e.printStackTrace();
		}

		try {
			DocLocks.writing(f);;
			Files.write(f, b);

			// mysql doesn't like windows' path separator
			return "'" + relatvFn.replaceAll("\\\\", "/") + "'";
		} catch (IOException e) {
			e.printStackTrace();
			return "''";
		}
		finally { DocLocks.writen(f); }
	}
	
	public String absolutePath(ISemantext ctx) throws TransException {
		String relatvFn = encodeUri(resulv_const_path, configRoot, prefix, filename, ctx);
		return decodeUri(runtimePath, relatvFn);
	}
	
	/**
	 * @param resulv
	 * @param cfgRoot
	 * @param prefix
	 * @param filename
	 * @param ctx
	 * @return cfgRoot[without-env]/prefix/resulved filename
	 * @throws TransException
	 */
	public static String encodeUri(ExprPart resulv, String cfgRoot, String prefix, String filename, ISemantext ctx) throws TransException {
		String relatvFn;
		if (resulv instanceof Resulving) 
			relatvFn = ((Resulving)resulv).resulved(ctx);
		else
			relatvFn = resulv.sql(ctx);
		
		return encodeUri(relatvFn, cfgRoot, prefix, filename);
	}
	
	/**
	 * @param nameId
	 * @param cfgRoot
	 * @param prefix
	 * @param filename
	 * @return cfgRoot(without-env)/prefix/nameId filename
	 */
	public static String encodeUri(String nameId, String cfgRoot, String prefix, String filename) {
		if (!LangExt.isblank(filename, "\\.", "\\*"))
			nameId += " " + filename;
		return EnvPath.encodeUri(cfgRoot, prefix, nameId);
	}
	
	public static String decodeUri(String runtimePath, String dbUri) {
		 return EnvPath.decodeUri(runtimePath, dbUri);
	}

	public static void touchDir(String dir) {
		File f = new File(dir);
		if (f.isDirectory())
			return;
		else if (!f.exists())
			// create dir
			f.mkdirs();
		else
			// must be a file
			Utils.warn("FATAL ExtFile can't create a folder, a same named file exists: ", dir);
	}
}
