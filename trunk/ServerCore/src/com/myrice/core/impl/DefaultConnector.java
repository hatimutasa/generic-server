package com.myrice.core.impl;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.myrice.core.AccessException;
import com.myrice.core.Connection;
import com.myrice.core.Connector;
import com.myrice.core.MessageReader;
import com.myrice.core.MessageWriter;
import com.myrice.core.Notifier;

public class DefaultConnector<R, S> implements Connector<R, S>, Runnable {
	private static final int QUEUE_REQUEST_MAX = 2048;
	private Thread thread;
	private ExecutorService executer;

	protected Selector selector;

	private MessageReader<R, S> reader;
	private MessageWriter<R, S> writer;

	private BlockingQueue<SelectionKey> queue4read;// 读
	private BlockingQueue<SelectionKey> queue4write;// 写

	private BlockingQueue<ServerSocketChannel> queue4server;// 服务端
	private BlockingQueue<SocketChannel> queue4client;// 客户端
	private BlockingQueue<Object[]> queue4medley;// 混合请求

	protected Notifier<R, S> notifier;
	private String name = "SelectorHandler-" + nextId();

	public DefaultConnector(ExecutorService executer) throws IOException {
		this(executer, new DefaultNotifier<R, S>(),
				new DefaultMessageReader<R, S>(),
				new DefaultMessageWriter<R, S>());
	}

	public DefaultConnector() throws IOException {
		this(Executors.newCachedThreadPool());
	}

	public DefaultConnector(ExecutorService executer, Notifier<R, S> notifer,
			MessageReader<R, S> reader, MessageWriter<R, S> writer)
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

		this.notifier = notifer;

	}

	private static int nextId = 0;

	private static int nextId() {
		if (nextId < 0) {
			nextId = 0;
		}
		return nextId++;
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
							if (key.isReadable()) {
								key.cancel();
								reader.processRequest(key);
							} else if (key.isWritable()) {
								key.cancel();
								writer.processRequest(key);
							} else if (key.isAcceptable()) {
								notifier.fireOnAccept();
								ss = ((ServerSocketChannel) key.channel());
								if (ss.isOpen()) {
									accept4server(ss.accept());
								} else {
									key.cancel();
								}
							}
						} else {
							key.cancel();
							if (key.attachment() instanceof Connection) {
								notifier.fireOnClosed((R) key.attachment());
							}
						}
					} catch (CancelledKeyException e) {
						if (key.attachment() instanceof Connection) {
							notifier.fireOnClosed((R) key.attachment());
						}
						e.printStackTrace();
					} catch (Exception e) {
						notifier.fireOnError(null, e);
					}
			}
		} catch (Exception e) {
			notifier.fireOnError(null, e);
		} finally {
			thread = null;
			destory();
		}
	}

	protected void destory() {
		closeChannels();

		notifier.destory();
		reader.destory();
		writer.destory();

		try {
			executer.shutdown();
			executer.awaitTermination(10L, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		closeSelector();
	}

	protected void init() {
		if (executer == null || executer.isShutdown()) {
			executer = Executors.newCachedThreadPool();
		}
		reader.init(this);
		writer.init(this);
		notifier.init();
	}

	@SuppressWarnings("unchecked")
	protected void accept(SelectableChannel sc, R request) throws Exception {
		try {
			request = notifier.fireOnAccepted(sc, request);// 必须先通知再请求读数据
			addRegistor(sc, SelectionKey.OP_READ, request);
		} catch (Throwable e) {
			notifier.fireOnError(
					request != null ? (S) ((Connection) request).getSession()
							: null, e);
		}
	}

	protected void accept4server(SelectableChannel sc) throws Exception {
		accept(sc, null);
	}

	@SuppressWarnings("unchecked")
	protected void addRegistor() {
		// 添加读写请求
		addRegistors(queue4read, SelectionKey.OP_READ);
		addRegistors(queue4write, SelectionKey.OP_WRITE);

		// 处理自定义接收请求
		while (queue4medley.isEmpty() == false)
			try {
				Object[] aq = queue4medley.poll();
				SelectableChannel sc = (SelectableChannel) aq[0];
				R request = (R) aq[1];
				accept(sc, request);
			} catch (Exception e) {
				notifier.fireOnError(null, e);
			}

		// 处理接收套接字请求
		while (queue4client.isEmpty() == false)
			try {
				accept4server(queue4client.poll());
			} catch (Exception e) {
				e.printStackTrace();
			}

		// 处理接听套接字请求
		while (queue4server.isEmpty() == false)
			try {
				addRegistor(queue4server.poll(), SelectionKey.OP_ACCEPT, this);
			} catch (Exception e) {
				notifier.fireOnError(null, e);
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

	private SelectionKey addRegistor(SelectableChannel channel, int ops,
			Object attatch) throws IOException {
		if (channel != null) {
			channel.configureBlocking(false);
			return channel.register(selector, ops, attatch);
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
				if (executer == null)
					executer = Executors.newCachedThreadPool();
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
		onStop();
	}

	protected void onStop() {
		try {
			executer.shutdown();
			executer.awaitTermination(10L, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void closeChannels() {
		// 关闭所有注册的键
		Iterator<SelectionKey> keys = selector.keys().iterator();
		while (keys.hasNext())
			try {
				SelectionKey key = keys.next();
				key.cancel();
				try {
					key.channel().close();
				} catch (Throwable e) {
				} finally {
					Object attach = key.attachment();
					if (attach != null && attach instanceof Connection) {
						notifier.fireOnClosed((R) attach);
					}
				}
			} catch (Throwable e) {
				notifier.fireOnError(null, e);
			}
	}

	protected void closeSelector() {
		try {
			selector.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			selector = null;
		}
	}

	public Notifier<R, S> getNotifier() {
		return notifier;
	}

	@SuppressWarnings("unchecked")
	public void processRead(SelectionKey key) {
		try {
			queue4read.put(key);
			selector.wakeup();
		} catch (Exception e) {
			notifier.fireOnError(
					(S) ((Connection) key.attachment()).getSession(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public void processWrite(SelectionKey key) {
		try {
			queue4write.put(key);
			selector.wakeup();
		} catch (Exception e) {
			notifier.fireOnError(
					(S) ((Connection) key.attachment()).getSession(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public void registor(SelectableChannel sc, R request) {
		try {
			queue4medley.put(new Object[] { sc, request });
			selector.wakeup();
		} catch (InterruptedException e) {
			notifier.fireOnError((S) ((Connection) request).getSession(), e);
		}
	}

	public void registor(ServerSocketChannel... sscs) {
		try {
			for (ServerSocketChannel ssc : sscs)
				queue4server.put(ssc);
			selector.wakeup();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void registor(SocketChannel... scs) {
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

	public ExecutorService getExecutor() {
		return executer;
	}

	public void wakeup() {
		if (selector != null) {
			selector.wakeup();
		}
	}
}
