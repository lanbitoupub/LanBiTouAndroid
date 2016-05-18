package com.lanbitou.thread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class ThreadPoolUtils {

	private ThreadPoolUtils() {
	}

	private static int CORE_POOL_SIZE = 3;

	private static int MAX_POOL_SIZE = 200;

	private static int KEEP_ALIVE_TIME = 5000;

	private static BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(10);
	private static ThreadFactory threadFactory = new ThreadFactory() {

		private final AtomicInteger ineger = new AtomicInteger();

		@Override
		public Thread newThread(Runnable arg0) {
			return new Thread(arg0, "MyThreadPool thread:"
					+ ineger.getAndIncrement());
		}
	};

	private static RejectedExecutionHandler handler = new RejectedExecutionHandler() {
		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		}

	};
	private static ThreadPoolExecutor threadpool;
	static {
		threadpool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE,
				KEEP_ALIVE_TIME, TimeUnit.SECONDS, workQueue, threadFactory,
				handler);
	}

	public static void execute(Runnable runnable) {
		threadpool.execute(runnable);
	}
}
