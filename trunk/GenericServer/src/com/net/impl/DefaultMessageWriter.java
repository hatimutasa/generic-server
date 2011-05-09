package com.net.impl;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.net.Connector;
import com.net.MessageWriter;
import com.net.Notifier;
import com.net.ResponseFactory;

public class DefaultMessageWriter<R, W> implements MessageWriter<R, W> {
    private Connector<R, W> connector;
    private Notifier<R, W> notifier;
    private Executor executor;

    private ResponseFactory<W> respFactory;

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
	this.respFactory = null;
    }

    public void init(Connector<R, W> connector) {
	this.connector = connector;
	this.notifier = connector.getNotifier();
	this.respFactory = connector.getResponseFactory();

	if (this.executor == null)
	    this.executor = connector.getExecutor();
    }

    @SuppressWarnings("unchecked")
    public void processRequest(final SelectionKey task) {
	this.executor.execute(new Runnable() {
	    public void run() {
		R request = (R) task.attachment();
		try {
		    W response = respFactory.create(task);
		    notifier.fireOnWrite(request, response);
		    connector.processRead(task);
		} catch (ClosedChannelException e) {
		    // 主动关闭的忽略
		} catch (IOException e) {
		    notifier.fireOnError(e);
		    try {
			task.channel().close();
		    } catch (IOException e1) {
		    }
		    notifier.fireOnClosed(request);
		} catch (Exception e) {
		    notifier.fireOnError(e);
		    connector.processRead(task);
		}
	    }
	});
    }
}
