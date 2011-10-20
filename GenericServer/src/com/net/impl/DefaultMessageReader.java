package com.net.impl;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.net.Connector;
import com.net.MessageReader;
import com.net.Notifier;

public class DefaultMessageReader<R, W> implements MessageReader<R, W> {
	private static final int CACHE_TASK_MAX = 50;
	private Connector<R, W> connector;
	private Notifier<R, W> notifier;
	private Executor executor;

	public DefaultMessageReader() {
	}

	public DefaultMessageReader(int corePoolSize, int maximiumPoolSize,
			int keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
		executor = new ThreadPoolExecutor(corePoolSize, maximiumPoolSize,
				keepAliveTime, unit, workQueue);
	}

	public void destory() {
		if (executor == connector.getExecutor())
			executor = null;
	}

	public void init(Connector<R, W> connector) {
		this.connector = connector;
		notifier = connector.getNotifier();
		if (executor == null)
			executor = connector.getExecutor();
	}

	public void processRequest(SelectionKey key) {
		executor.execute(createTask(key));
	}

	protected Runnable createTask(SelectionKey key) {
		Task task = recycle.poll();
		if (task == null)
			task = new Task();
		task.key = key;
		return task;
	}

	private class Task implements Runnable {
		private SelectionKey key;

		public void run() {
			execute(key);
			destory();
		}

		void destory() {
			key = null;
			recycle.offer(this);
		}
	}

	private Queue<Task> recycle = new ArrayBlockingQueue<Task>(CACHE_TASK_MAX);

	@SuppressWarnings("unchecked")
	protected void execute(SelectionKey key) {
		R request = (R) key.attachment();
		try {
			if (notifier.fireOnRead(request))
				connector.processWrite(key);// 读到完整报文，请求写
			else
				connector.processRead(key);// 不完整报文，继续读取
		} catch (ClosedChannelException e) {
			notifier.fireOnClosed(request);
		} catch (Exception e) {
			try {
				notifier.fireOnError(e);
				key.channel().close();
			} catch (Exception e1) {
			} finally {
				notifier.fireOnClosed(request);
			}
		}
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	public Executor getExecutor() {
		return executor;
	}
}
