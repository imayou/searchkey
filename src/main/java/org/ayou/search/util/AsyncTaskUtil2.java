package org.ayou.search.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 */
public class AsyncTaskUtil2 {
	private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setDaemon(true);// 使用守护线程，这种方式不会阻止程序的关停
			return t;
		}
	});

	public static ExecutorService getExecutor(int t) {
		return executor;
	}
}
