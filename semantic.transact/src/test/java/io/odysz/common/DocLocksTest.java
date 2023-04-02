package io.odysz.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.junit.Test;

import io.odysz.common.DocLocks;

public class DocLocksTest {

	@Test
	public void test() throws InterruptedException {
		String src = ".";
		
		final ArrayList<String> seq = new ArrayList<String>();
		final ArrayList<String> res = new ArrayList<String>();

		Thread r1 = new Thread(() -> {
			seq.add("r1");
			try { Thread.sleep(200); }
			catch (InterruptedException e) { fail(e.getMessage()); }

			DocLocks.reading(src);
			res.add("r1");
			DocLocks.readed(src);
		});

		Thread r2 = new Thread(() -> {
			seq.add("r2");
			try { Thread.sleep(200); }
			catch (InterruptedException e) { fail(e.getMessage()); }

			DocLocks.reading(src);
			res.add("r2");
			DocLocks.readed(src);
		});
		
		Thread w1 = new Thread(() ->  {
			seq.add("w1");
			DocLocks.writing(src);

			try { Thread.sleep(400); }
			catch (InterruptedException e) { fail(e.getMessage()); }
			res.add("w1");
			DocLocks.writen(src);
		});
		
		r1.start();
		r2.start();
		w1.start();
		
		Thread.sleep(1000);
		
		assertEquals("w1,r1,r2", // not w1,r2,r1?
					 res.stream().collect(Collectors.joining(",")));
		assertEquals("r1,r2,w1",
					 seq.stream().collect(Collectors.joining(",")));
	} 

}