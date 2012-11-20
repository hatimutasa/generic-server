package com.myrice.core.impl;

import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.myrice.core.Connection;
import com.myrice.core.Connector;
import com.myrice.core.MessageInput;
import com.myrice.core.MessageOutput;
import com.myrice.core.MessageQueue;
import com.myrice.core.Notifier;
import com.myrice.core.ServerContext;
import com.myrice.core.Session;
import com.myrice.core.WriteRequest;
import com.myrice.filter.IAcceptFilter;
import com.myrice.filter.IAcceptorFilter;
import com.myrice.filter.IClosedFilter;
import com.myrice.filter.IErrFilter;
import com.myrice.filter.IFilterChain;
import com.myrice.filter.IFilterChain.IChain;
import com.myrice.filter.IMessageFilter;
import com.myrice.filter.IProtocolDecodeFilter;
import com.myrice.filter.impl.DefaultFilterChain;
import com.myrice.util.TimerManager;

public class DefaultServerHandler extends ServerHandlerAdapter<Connection>
		implements ServerContext {
	protected final Logger log = Logger.getLogger(getClass());

	public static final String ATTR_MAX_MESSATE_QUEUE_CAPACITY = "__MAX_MESSATE_QUEUE_CAPACITY__";

	public static int MAX_MESSAGE_QUEUE_CAPACITY = 50;

	private IFilterChain filterChain;

	private Connector<Connection> connector;
	private Notifier<Connection> notifier;
	private ExecutorService executor;
	private TimerManager timerManager;

	public DefaultServerHandler(Connector<Connection> connector) {
		this.connector = connector;
		this.notifier = connector.getNotifier();
		this.executor = connector.getExecutor();
		this.timerManager = new TimerManager();
		this.filterChain = new DefaultFilterChain();
		this.notifier.addHandler(this);
	}

	@Override
	public void init() {
		timerManager.init();
	}

	@Override
	public void destory() {
		timerManager.destory();
	}

	public Executor getExecutor() {
		return executor;
	}

	public Notifier<Connection> getNotifier() {
		return notifier;
	}

	public void execute(Runnable run) {
		if (executor == null || executor.isShutdown()) {
			log.error("execute failed!  runable:" + run);
			return;
		}
		executor.execute(run);
	}

	/**
	 * 注册定时执行一次任务
	 * 
	 * @param run
	 * @param delay
	 *            延迟时长
	 * @return
	 */
	public ScheduledFuture<?> schedule(Runnable run, long delay) {
		return timerManager.schedule(run, delay, TimeUnit.MILLISECONDS);
	}

	/**
	 * 注册定时执行重复任务
	 * 
	 * @param run
	 * @param delay
	 *            初始延迟
	 * @param period
	 *            循环周期
	 * @return
	 */
	public ScheduledFuture<?> schedule(Runnable run, long delay, long period) {
		return timerManager.scheduleAtFixedRate(run, delay, period,
				TimeUnit.MILLISECONDS);
	}

	public Connector<Connection> getConnector() {
		return connector;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onAccept() throws Exception {
		IChain<IAcceptFilter> chain = (IChain<IAcceptFilter>) filterChain
				.getFirstChain(IFilterChain.FILTER_ACCEPT);
		if (chain != null) {
			chain.getFilter().serverAccept(this, chain);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Connection onAccepted(SelectableChannel request, Connection prev)
			throws Exception {
		IChain<IAcceptorFilter> chain = (IChain<IAcceptorFilter>) filterChain
				.getFirstChain(IFilterChain.FILTER_ACCEPTOR);
		if (chain != null) {
			if (prev == null) {
				prev = chain.getFilter().sessionAccept(this, request, chain);
			}
			if (prev != null) {
				prev = chain.getFilter().sessionOpened(this, prev, chain);
			}
		}
		return prev;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onClosed(Connection conn) {
		IChain<IClosedFilter> chain = (IChain<IClosedFilter>) filterChain
				.getFirstChain(IFilterChain.FILTER_CLOSED);
		if (chain != null) {
			chain.getFilter().sessionClosed(conn, chain);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onError(Connection conn, Throwable e) {
		IChain<IErrFilter> chain = (IChain<IErrFilter>) filterChain
				.getFirstChain(IFilterChain.FILTER_ERROR);
		if (chain != null) {
			chain.getFilter().serverExcept(conn, e, chain);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean onRead(Connection conn, boolean prev) throws Exception {
		IChain<IProtocolDecodeFilter> chain = (IChain<IProtocolDecodeFilter>) filterChain
				.getFirstChain(IFilterChain.FILTER_PROTOCOL_DECODE);
		if (chain != null) {
			ByteBuffer buff = conn.read();
			MessageOutput queue = conn.getSession().getMessageInputQueue();
			return chain.getFilter().messageDecode(conn, buff, queue, chain);
		}
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean onWrite(Connection conn, boolean prev) throws Exception {
		IChain<IMessageFilter> chain = (IChain<IMessageFilter>) filterChain
				.getFirstChain(IFilterChain.FILTER_MESSAGE);
		if (chain != null) {
			DefaultSession session = (DefaultSession) conn.getSession();
			MessageInput in = session.getMessageInputQueue();
			for (; in.isEmpty() == false;) {
				Object message = in.popMessage();
				executeMessageTask(newMessageTask(session, message, chain));
			}
		}
		return true;
	}

	protected void executeMessageTask(MessageTask messageTask) {
		execute(messageTask);
	}

	private MessageTask newMessageTask(Session session, Object message,
			IChain<IMessageFilter> chain) {
		MessageTask t = queue.poll();
		if (t == null)
			t = new MessageTask();

		t.session = session;
		t.message = message;
		t.chain = chain;

		return t;
	}

	protected class MessageTask implements Runnable {
		Session session;
		Object message;
		IChain<IMessageFilter> chain;

		public void run() {
			chain.getFilter().messageReceived(session, message, chain);

			// 自动回收复用
			session = null;
			message = null;
			chain = null;
			queue.offer(this);
		}

	}

	private static final Queue<MessageTask> queue = new ArrayBlockingQueue<MessageTask>(
			MAX_MESSAGE_QUEUE_CAPACITY);

	public IFilterChain getFilterChain() {
		return filterChain;
	}

	public Session setSessionContext(String sid, Session session) {
		return getSessionContextMap().put(sid, session);
	}

	public Session getSessionContext(String sid) {
		return getSessionContextMap().get(sid);
	}

	public Session removeSessionContext(String sid) {
		return getSessionContextMap().remove(sid);
	}

	private Map<String, Session> map;

	protected Map<String, Session> getSessionContextMap() {
		if (map == null)
			synchronized (this) {
				if (map == null) {
					map = createSessionContextMap();
				}
			}
		return map;
	}

	protected Map<String, Session> createSessionContextMap() {
		return new ConcurrentHashMap<String, Session>();
	}

	public MessageQueue createMessageQueue() {
		int capacity = MAX_MESSAGE_QUEUE_CAPACITY;
		if (contains(ATTR_MAX_MESSATE_QUEUE_CAPACITY)) {
			capacity = (Integer) getAttribute(ATTR_MAX_MESSATE_QUEUE_CAPACITY);
		}
		return new DefaultMessageQueue(capacity);
	}

	public WriteRequest createWriteRequest(Connection session) {
		return new DefaultWriteRequest(session);
	}

}
