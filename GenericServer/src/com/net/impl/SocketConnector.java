package com.net.impl;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.myrice.core.AccessException;
import com.myrice.core.Connector;
import com.myrice.core.MessageReader;
import com.myrice.core.MessageWriter;
import com.myrice.core.Notifier;
import com.myrice.core.RequestFactory;
import com.myrice.core.ResponseFactory;

public class SocketConnector<R, W> implements Connector<R, W>, Runnable {
	private static final int QUEUE_REQUEST_MAX = 1024;
	private Thread thread;
	private ExecutorService executer;

	protected Selector selector;

	private MessageReader<R, W> reader;
	private MessageWriter<R, W> writer;

	private BlockingQueue<SelectionKey> queue4read;// 读
	private BlockingQueue<SelectionKey> queue4write;// 写

	private BlockingQueue<ServerSocketChannel> queue4server;// 服务端
	private BlockingQueue<SocketChannel> queue4client;// 客户端
	private BlockingQueue<Object[]> queue4medley;// 混合请求

	private RequestFactory<R> requestFactory;
	private ResponseFactory<W> responseFactory;

	protected Notifier<R, W> notifier;
	private String name = "SelectorHandler-" + nextId();

	public SocketConnector(ExecutorService executer, Notifier<R, W> notifer,
			MessageReader<R, W> reader, MessageWriter<R, W> writer,
			RequestFactory<R> requestFactory, ResponseFactory<W> responseFactory)
			throws IOException {

		this.executer = executer;

		this.reader = reader;
		this.writer = writer;

		this.queue4read = new ArrayBlockingQueue<SelectionKey>(
				QUEUE_REQUEST_MAX);
		this.queue4write = new ArrayBlockingQueue<SelectionKey>(
				QUEUE_REQUEST_MAX);

		this.queue4server = new ArrayBlockingQueue<ServerSocketChannel>(
				QUEUE_REQUEST_MAX);
		this.queue4client = new ArrayBlockingQueue<SocketChannel>(
				QUEUE_REQUEST_MAX);
		this.queue4medley = new ArrayBlockingQueue<Object[]>(QUEUE_REQUEST_MAX);

		this.requestFactory = requestFactory;
		this.responseFactory = responseFactory;

		this.notifier = notifer;

	}

	private static int nextId = 0;

	private static int nextId() {
		if (nextId < 0) {
			nextId = 0;
		}
		return nextId++;
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
		this(new ThreadPoolExecutor(20, QUEUE_REQUEST_MAX, 60,
				TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(
						QUEUE_REQUEST_MAX)), requestFactory, responseFactory);
	}

	@SuppressWarnings("unchecked")
	public void run() {
		try {
			Thread current = Thread.currentThread();
			ServerSocketChannel ss;
			Iterator<SelectionKey> keys;
			SelectionKey key = null;
			int size;
			synchronized (this) {
				try {
					init();
				} finally {
					notify();
				}
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
						if (key.isValid()) {
							if (key.isAcceptable()) {
								notifier.fireOnAccept();
								ss = ((ServerSocketChannel) key.channel());
								accept4server(ss.accept());
							} else if (key.isReadable()) {
								key.cancel();
								reader.processRequest(key);
							} else if (key.isWritable()) {
								key.cancel();
								writer.processRequest(key);
							}
						} else {
							notifier.fireOnClosed((R) key.attachment());
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

	protected void accept(SelectableChannel sc, R request) throws Exception {
		addRegistor(sc, SelectionKey.OP_READ, request);
		notifier.fireOnAccepted(request);
	}

	protected void accept4server(SocketChannel sc) throws Exception {
		accept(sc, requestFactory.create(sc));
	}

	@SuppressWarnings("unchecked")
	protected void addRegistor() {
		// 添加读写请求注册器
		addRegistors(queue4write, SelectionKey.OP_WRITE);
		addRegistors(queue4read, SelectionKey.OP_READ);

		// 添加混合请求注册器
		while (queue4medley.isEmpty() == false)
			try {
				Object[] aq = queue4medley.poll();
				accept((SelectableChannel) aq[0], (R) aq[1]);
			} catch (Exception e) {
				notifier.fireOnError(e);
			}

		// 添加套接字请求注册器
		while (queue4client.isEmpty() == false)
			try {
				accept4server(queue4client.poll());
			} catch (Exception e) {
				notifier.fireOnError(e);
			}

		// 添加服务器请求注册器
		while (queue4server.isEmpty() == false)
			try {
				addRegistor(queue4server.poll(), SelectionKey.OP_ACCEPT, null);
			} catch (Exception e) {
				notifier.fireOnError(e);
			}
	}

	@SuppressWarnings("unchecked")
	private void addRegistors(Queue<SelectionKey> queue, int ops) {
		SelectionKey key;
		while ((key = queue.poll()) != null) {
			R request = null;
			try {
				request = (R) key.attachment();
				addRegistor(key.channel(), ops, request);
			} catch (Exception e) {
				notifier.fireOnClosed(request);
			}
		}
	}

	private SelectionKey addRegistor(SelectableChannel channel, int ops, R req)
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

		if (notifier.isEmpty())
			throw new AccessException("没有注册任何处理器。");

		synchronized (this) {
			try {
				selector = Selector.open();
				thread = new Thread(this, name);
				thread.setDaemon(true);
				thread.start();
				wait();
			} catch (Exception e) {
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
			executer.shutdown();
			executer.awaitTermination(30L, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			selector.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			selector = null;
		}
		destory();
	}

	public Notifier<R, W> getNotifier() {
		return notifier;
	}

	public void processRead(SelectionKey key) {
		try {
			queue4read.put(key);
			selector.wakeup();
		} catch (InterruptedException e) {
			notifier.fireOnError(e);
		}
	}

	public void processWrite(SelectionKey key) {
		try {
			queue4write.put(key);
			selector.wakeup();
		} catch (InterruptedException e) {
			notifier.fireOnError(e);
		}
	}

	public void registor(SelectableChannel sc, R request) throws Exception {
		try {
			queue4medley.put(new Object[] { sc, request });
			selector.wakeup();
		} catch (InterruptedException e) {
			notifier.fireOnError(e);
		}
	}

	public void registor(ServerSocketChannel... sscs) throws IOException {
		try {
			for (ServerSocketChannel ssc : sscs)
				queue4server.put(ssc);
			selector.wakeup();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void registor(SocketChannel... scs) throws IOException {
		try {
			for (SocketChannel sc : scs)
				queue4client.put(sc);
			selector.wakeup();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public boolean isRuning() {
		return thread != null;
	}

	public ResponseFactory<W> getResponseFactory() {
		return responseFactory;
	}

	public RequestFactory<R> getRequestFactory() {
		return requestFactory;
	}

	public Executor getExecutor() {
		return executer;
	}
}
