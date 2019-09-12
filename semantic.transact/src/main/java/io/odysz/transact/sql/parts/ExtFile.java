package io.odysz.transact.sql.parts;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.odysz.FilenameUtils;

import io.odysz.common.AESHelper;
import io.odysz.common.LangExt;
import io.odysz.common.Utils;
import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.x.TransException;

/**External file representation.<br>
 * An ExtFile can only been used as a set value.
 * 
 * FIXME Why this is not a {@link SetValue}, as the ExtRef is a SelectElem?
 * @author odys-z@github.com
 *
 */
public class ExtFile extends AbsPart {
	private String b64;
	private ExprPart resulv_const_path;
	private String prefix;
	private String filename;
	private String absoluteroot;

	public ExtFile(ExprPart resulvingPath) {
		this.resulv_const_path = resulvingPath;
	}

	/**Set the absolute root path. This path is used to access file together with the relative path set by {@link ExtFile#prefixPath(String)}.<br>
	 * The argurment doesn't have to be absolute path if the runtime can access a file from a relative paht.<br>
	 * But the sevlet needing an absolute path to access a file, so this mus been set to the absolute path,
	 * like with the return of <a href='https://docs.oracle.com/javaee/6/api/javax/servlet/ServletContext.html'>
	 * javax.servlet.ServletContext#getRealPath(String)</a>.<br>
	 * If this is wrong, please feel free to correct the author at odysseusj@163.com.
	 * @param absRoot
	 * @return this
	 */
	public ExtFile absPath(String absRoot) {
		this.absoluteroot = absRoot;
		return this;
	}

	/**Set the relative path of the file.
	 * This part is saved in the replaced file path in database field.
	 * @param path
	 * @return this
	 */
	public ExtFile prefixPath(String path) {
		this.prefix = path;
		return this;
	}
	
	public ExtFile filename(String name) {
		this.filename = name;
		return this;
	}
	
	public ExtFile b64(String b64) {
		this.b64 = b64;
		return this;
	}

	@Override
	public String sql(ISemantext ctx) throws TransException {
		// save file to pathname
		String relatvFn;
		if (resulv_const_path instanceof Resulving) 
			relatvFn = ((Resulving)resulv_const_path).resulved(ctx);
		else
			relatvFn = resulv_const_path.sql(ctx);
		
		if (!LangExt.isblank(filename, "\\.", "\\*"))
				relatvFn += " " + filename;
		
		String dir = LangExt.isblank(prefix) ? "" : prefix;
		mkDir(dir);

		relatvFn = FilenameUtils.concat(dir, relatvFn);

		String absoluteFn = FilenameUtils.concat(absoluteroot, relatvFn);

		Path f = Paths.get(absoluteFn);
		try {
			byte[] b = AESHelper.decode64(b64);
			Files.write(f, b);
			// mysql doesn't like windows' path separator
			return "'" + relatvFn.replaceAll("\\\\", "/") + "'";
		} catch (IOException e) {
			e.printStackTrace();
			return "''";
		}
	}

	private void mkDir(String dir) {
		dir = FilenameUtils.concat(absoluteroot, dir);
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
