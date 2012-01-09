package com.myrice.core.impl;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.myrice.core.Connector;
import com.myrice.core.MessageWriter;
import com.myrice.core.Notifier;
import com.myrice.core.ResponseFactory;

public class DefaultMessageWriter<R, W> implements MessageWriter<R, W> {
	private static final int CACHE_TASK_MAX = 50;
	private Connector<R, W> connector;
	private Notifier<R, W> notifier;
	private Executor executor;

	private ResponseFactory<W> responseFactory;

	public DefaultMessageWriter() {
	}

	public DefaultMessageWriter(int corePoolSize, int maximiumPoolSize,
			int keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
		this.executor = new ThreadPoolExecutor(corePoolSize, maximiumPoolSize,
				keepAliveTime, unit, workQueue);
	}

	public void destory() {
		if (this.executor == connector.getExecutor())
			this.executor = null;

		this.connector = null;
		this.notifier = null;
		this.responseFactory = null;
	}

	public void init(Connector<R, W> connector) {
		this.connector = connector;
		this.notifier = connector.getNotifier();
		this.responseFactory = connector.getResponseFactory();

		if (this.executor == null)
			this.executor = connector.getExecutor();
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
			if (notifier.fireOnWrite(request, responseFactory.create(key)))
				connector.processRead(key);// 报文完整写出，请求读取
			else
				connector.processWrite(key);// 报文未写完，继续请求写
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
