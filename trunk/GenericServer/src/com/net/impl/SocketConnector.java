package com.net.impl;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.net.Connector;
import com.net.MessageReader;
import com.net.MessageWriter;
import com.net.Notifier;
import com.net.RequestFactory;
import com.net.ResponseFactory;

public class SocketConnector<R, W> implements Connector<R, W>, Runnable {
    private Thread thread;
    private ExecutorService executer;

    protected Selector selector;
    
    private MessageReader<R, W> reader;
    private MessageWriter<R, W> writer;

    private BlockingQueue<SelectionKey> rpPool;
    private BlockingQueue<SelectionKey> wpPool;

    private BlockingQueue<ServerSocketChannel> sspPool;
    private BlockingQueue<SocketChannel> spPool;
    private BlockingQueue<Object[]> apPool;

    private RequestFactory<R> requestFactory;
    private ResponseFactory<W> responseFactory;

    protected Notifier<R, W> notifier;

    public SocketConnector(ExecutorService executer, Notifier<R, W> notifer,
	    MessageReader<R, W> reader, MessageWriter<R, W> writer,
	    RequestFactory<R> requestFactory, ResponseFactory<W> responseFactory)
	    throws IOException {

	this.executer = executer;
	this.selector = Selector.open();

	this.reader = reader;
	this.writer = writer;

	this.rpPool = new LinkedBlockingQueue<SelectionKey>();
	this.wpPool = new LinkedBlockingQueue<SelectionKey>();

	this.sspPool = new LinkedBlockingQueue<ServerSocketChannel>();
	this.spPool = new LinkedBlockingQueue<SocketChannel>();
	this.apPool = new LinkedBlockingQueue<Object[]>();

	this.requestFactory = requestFactory;
	this.responseFactory = responseFactory;

	this.notifier = notifer;

    }

    public SocketConnector(ExecutorService executer,
	    RequestFactory<R> requestFactory, ResponseFactory<W> responseFactory)
	    throws IOException {
	this(executer, new DefaultNotifier<R, W>(),
		new DefaultMessageReader<R, W>(),
		new DefaultMessageWriter<R, W>(), requestFactory,
		responseFactory);
    }

    public SocketConnector(RequestFactory<R> requestFactory,
	    ResponseFactory<W> responseFactory) throws IOException {
	this(new ThreadPoolExecutor(20, 256, 1, TimeUnit.HOURS,
		new LinkedBlockingQueue<Runnable>()), requestFactory,
		responseFactory);
    }

    public void run() {
	Thread current = Thread.currentThread();
	ServerSocketChannel ss;
	Iterator<SelectionKey> keys;
	SelectionKey key = null;
	int size;
	try {
	    synchronized (this) {
		init();
		this.notify();
	    }
	    while (current == thread) {
		size = selector.select();
		if (size == 0) {
		    addRegistor();
		    continue;
		}
		keys = selector.selectedKeys().iterator();
		while (keys.hasNext())
		    try {
			key = keys.next();
			keys.remove();
			if (key.isValid())
			    if (key.isAcceptable()) {
				notifier.fireOnAccept();
				ss = ((ServerSocketChannel) key.channel());
				accept(ss.accept());
			    } else if (key.isReadable()) {
				key.cancel();
				reader.processRequest(key);
			    } else if (key.isWritable()) {
				key.cancel();
				writer.processRequest(key);
			    }
		    } catch (Exception e) {
			notifier.fireOnError(e);
		    }
	    }
	} catch (Exception e) {
	    notifier.fireOnError(e);
	} finally {
	    thread = null;
	}
    }

    protected void destory() {
	reader.destory();
	writer.destory();
    }

    protected void init() {
	reader.init(this);
	writer.init(this);
    }

    protected void accept(SocketChannel sc) throws Exception {
	R request = requestFactory.create(sc);
	addRegistor(sc, SelectionKey.OP_READ, request);
	notifier.fireOnAccepted(request);
    }

    protected void accept(SocketChannel sc, R request) throws Exception {
	addRegistor(sc, SelectionKey.OP_READ, request);
	notifier.fireOnAccepted(request);
    }

    @SuppressWarnings("unchecked")
    protected void addRegistor() {
	addRegistors(wpPool, SelectionKey.OP_WRITE);
	addRegistors(rpPool, SelectionKey.OP_READ);

	while (this.sspPool.isEmpty() == false)
	    try {
		this.addRegistor(this.sspPool.poll(), SelectionKey.OP_ACCEPT,
			null);
	    } catch (Exception e) {
		this.notifier.fireOnError(e);
	    }
	while (this.spPool.isEmpty() == false)
	    try {
		this.accept(this.spPool.poll());
	    } catch (Exception e) {
		this.notifier.fireOnError(e);
	    }
	while (this.apPool.isEmpty() == false)
	    try {
		Object[] aq = this.apPool.poll();
		this.accept((SocketChannel) aq[0], (R) aq[1]);
	    } catch (Exception e) {
		this.notifier.fireOnError(e);
	    }
    }

    @SuppressWarnings("unchecked")
    private void addRegistors(Queue<SelectionKey> queue, int ops) {
	SelectionKey key = null;
	R request = null;
	while ((key = queue.poll()) != null)
	    try {
		request = (R) key.attachment();
		addRegistor(key.channel(), ops, request);
	    } catch (Exception e) {
		notifier.fireOnClosed(request);
	    }
    }

    protected SelectionKey addRegistor(SelectableChannel channel, int ops, R req)
	    throws IOException {
	if (channel != null) {
	    channel.configureBlocking(false);
	    return channel.register(selector, ops, req);
	}
	return null;
    }

    public void start() {
	if (thread != null)
	    return;

	if (this.notifier.isEmpty())
	    throw new NullPointerException("没有注册任何处理器。");

	synchronized (this) {
	    thread = new Thread(this);
	    thread.start();
	    try {
		this.wait();
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	}
    }

    public void stop() {
	if (thread == null)
	    return;

	Thread tmp = thread;
	thread = null;
	selector.wakeup();
	try {
	    tmp.join();
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
	try {
	    this.executer.shutdown();
	    this.executer.awaitTermination(30L, TimeUnit.SECONDS);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
	destory();
    }

    public Notifier<R, W> getNotifier() {
	return this.notifier;
    }

    public void processRead(SelectionKey key) {
	try {
	    this.rpPool.put(key);
	    this.selector.wakeup();
	} catch (InterruptedException e) {
	    this.notifier.fireOnError(e);
	}
    }

    public void processWrite(SelectionKey key) {
	try {
	    this.wpPool.put(key);
	    this.selector.wakeup();
	} catch (InterruptedException e) {
	    this.notifier.fireOnError(e);
	}
    }

    public void registor(SocketChannel sc, R request) throws Exception {
	try {
	    this.apPool.put(new Object[] { sc, request });
	    this.selector.wakeup();
	} catch (InterruptedException e) {
	    this.notifier.fireOnError(e);
	}
    }

    public void registor(ServerSocketChannel... sscs) throws IOException {
	try {
	    for (ServerSocketChannel ssc : sscs)
		this.sspPool.put(ssc);
	    this.selector.wakeup();
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
    }

    public void registor(SocketChannel... scs) throws IOException {
	try {
	    for (SocketChannel sc : scs)
		this.spPool.put(sc);
	    this.selector.wakeup();
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
    }

    public boolean isRuning() {
	return this.thread != null;
    }

    public ResponseFactory<W> getResponseFactory() {
	return this.responseFactory;
    }

    public RequestFactory<R> getRequestFactory() {
	return requestFactory;
    }

    public Selector getSelector() {
	return this.selector;
    }

    public Executor getExecutor() {
	return this.executer;
    }
}
