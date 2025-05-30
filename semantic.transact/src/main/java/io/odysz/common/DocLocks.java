/**
 * 
 */
package io.odysz.common;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @since 1.4.12
 * 
 * @author odys-z@github.com
 *
 */
public class DocLocks {

	private static HashMap<String, ReentrantReadWriteLock> locks;

	static {
		locks = new HashMap<String, ReentrantReadWriteLock>();
	}
	
	public static void reading(String fullpath) {
		synchronized(locks) {
			if (!locks.containsKey(fullpath))
				locks.put(fullpath, new ReentrantReadWriteLock());
			locks.get(fullpath).readLock().lock();
		}
	}

	public static void reading(Path p) {
		reading(p.toAbsolutePath().toString());
	}

	public static void readed(String fullpath) {
		synchronized(locks) {
			try {
					if (locks.containsKey(fullpath))
						locks.get(fullpath).readLock().unlock();
			} catch (Throwable t) {
				Utils.warn("Unlock error: %s", fullpath);
				t.printStackTrace();
			}
			try { locks.remove(fullpath); }
			catch (Throwable t) {}
		}
	}

	public static void readed(Path p) {
		readed(p.toAbsolutePath().toString());
	}

	public static void writing(String fullpath) {
		synchronized(locks) {
			if (!locks.containsKey(fullpath))
				locks.put(fullpath, new ReentrantReadWriteLock());
			locks.get(fullpath).writeLock().lock();
		}
	}

	public static void writing(Path p) {
		writing(p.toAbsolutePath().toString());
	}

	public static void writen(String fullpath) {
		try {
			synchronized(locks) {
				locks.get(fullpath).writeLock().unlock();
			}
		} catch (Throwable t) {
			Utils.warn("Unlock error: %s", fullpath);
			t.printStackTrace();
		}
	}

	public static void writen(Path p) {
		writen(p.toAbsolutePath().toString());
	}
}
