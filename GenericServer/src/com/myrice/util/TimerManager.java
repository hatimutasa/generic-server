package com.myrice.util;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TimerManager {

	public void init() {
		if (executorService == null) {
			executorService = new ScheduledThreadPoolExecutor(corePoolSize);
			executorService.setMaximumPoolSize(maxPoolSize);
			executorService.setKeepAliveTime(keepAliveTime, TimeUnit.SECONDS);
		}
	}

	public void destory() {
		if (executorService != null) {
			executorService.shutdown();
			try {
				executorService.awaitTermination(keepAliveTime,
						TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				executorService = null;
			}
		}
	}

	private ScheduledThreadPoolExecutor executorService;
	private int corePoolSize = 10;
	private int maxPoolSize = 50;
	private long keepAliveTime = 1200;

	public int getCorePoolSize() {
		return corePoolSize;
	}

	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}

	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public static TimerManager getInstance() {
		if (instance == null)
			instance = new TimerManager();
		return instance;
	}

	private static TimerManager instance;

	public void execute(Runnable run) {
		executorService.execute(run);
	}

	public ScheduledFuture<?> schedule(Runnable run, long delay,
			TimeUnit timeUnit) {
		return executorService.schedule(run, delay, timeUnit);
	}

	public ScheduledFuture<?> scheduleAtFixedRate(Runnable run, long delay,
			long period, TimeUnit timeUnit) {
		return executorService
				.scheduleAtFixedRate(run, delay, period, timeUnit);
	}
}
