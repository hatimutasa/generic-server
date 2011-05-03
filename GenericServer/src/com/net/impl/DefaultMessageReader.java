package com.net.impl;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.net.Connector;
import com.net.MessageReader;
import com.net.Notifier;

public class DefaultMessageReader<R, W> implements MessageReader<R, W> {
    private Connector<R, W> connector;
    private Notifier<R, W> notifier;
    private Executor executor;

    public DefaultMessageReader() {

    }

    public DefaultMessageReader(int corePoolSize, int maximiumPoolSize, int keepAliveTime,
	    TimeUnit unit, BlockingQueue<Runnable> workQueue) {
	this.executor = new ThreadPoolExecutor(corePoolSize, maximiumPoolSize, keepAliveTime, unit,
		workQueue);
    }

    public void destory() {
	if (this.executor == this.connector.getExecutor())
	    this.executor = null;
    }

    public void init(Connector<R, W> connector) {
	this.connector = connector;
	this.notifier = connector.getNotifier();
	if (this.executor == null)
	    this.executor = connector.getExecutor();
    }

    @SuppressWarnings("unchecked")
    public void processRequest(final SelectionKey task) {
	this.executor.execute(new Runnable() {
	    public void run() {
		R request = (R) task.attachment();
		try {
		    notifier.fireOnRead(request);
		    connector.processWrite(task);
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
		    connector.processWrite(task);
		}
	    }
	});
    }

}
