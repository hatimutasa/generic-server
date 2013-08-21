package com.myrice.core.impl;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectableChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
import com.myrice.core.SessionFactory;
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

public class DefaultServerHandler extends
		ServerHandlerAdapter<Connection, Session> implements ServerContext {
	protected final Logger log = Logger.getLogger(getClass());

	public static final String ATTR_MAX_MESSATE_QUEUE_CAPACITY = "__MAX_MESSATE_QUEUE_CAPACITY__";

	public static int MAX_MESSAGE_QUEUE_CAPACITY = 50;

	private SessionFactory sessionFactory;

	private Map<ByteChannel, Connection> connections;
	private Map<String, Session> cachedSessionsMap;

	private IFilterChain filterChain;

	private Connector<Connection, Session> connector;
	private Notifier<Connection, Session> notifier;
	private ExecutorService executor;
	private TimerManager timerManager;

	public DefaultServerHandler(Connector<Connection, Session> connector) {
		this.connector = connector;
		this.notifier = connector.getNotifier();
		this.executor = connector.getExecutor();
		this.timerManager = new TimerManager();
		this.filterChain = new DefaultFilterChain();
		this.connections = new ConcurrentHashMap<ByteChannel, Connection>();

		this.notifier.addHandler(this);
	}

	@Override
	public void init() {
		sessionFactory = instanceSessionFactory();

		timerManager.init();

	}

	public SessionFactory instanceSessionFactory() {
		return new DefaultSessionFactory();
	}

	@Override
	public void destory() {
		timerManager.destory();
	}

	@Override
	public int getSessionCount() {
		return getSessionContextMap().size();
	}

	public List<Session> getSessionAll() {
		return new ArrayList<Session>(getSessionContextMap().values());
	}

	@Override
	public String[] getSessionIds() {
		List<Session> list = this.getSessionAll();
		String[] ids = new String[list.size()];
		for (int i = 0; i < ids.length; i++)
			ids[i] = list.get(i).getSessionId();
		return ids;
	}

	@Override
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	@Override
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public Executor getExecutor() {
		return executor;
	}

	@Override
	public Notifier<Connection, Session> getNotifier() {
		return notifier;
	}

	@Override
	public void execute(Runnable run) {
		if (executor == null || executor.isShutdown()) {
			log.error("execute failed!  runable:" + run);
			return;
		}
		executor.execute(run);
	}

	@Override
	public void execute(Runnable run, long delay) {
		schedule(run, delay);
	}

	/**
	 * 注册定时执行一次任务
	 * 
	 * @param run
	 * @param delay
	 *            延迟时长
	 * @return
	 */

	@Override
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

	@Override
	public ScheduledFuture<?> schedule(Runnable run, long delay, long period) {
		return timerManager.scheduleAtFixedRate(run, delay, period,
				TimeUnit.MILLISECONDS);
	}

	@Override
	public Connector<Connection, Session> getConnector() {
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
	public Connection onAccepted(SelectableChannel request, Connection conn)
			throws Exception {
		IChain<IAcceptorFilter> chain = (IChain<IAcceptorFilter>) filterChain
				.getFirstChain(IFilterChain.FILTER_ACCEPTOR);
		if (chain != null) {
			if (conn == null) {
				conn = chain.getFilter().sessionAccept(this, request, chain);
			}
			if (conn != null) {
				conn = chain.getFilter().sessionOpened(this, conn, chain);
				connections.put(conn.getSocketChannel(), conn);
			}
		}
		return conn;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onClosed(Connection conn) {
		connections.remove(conn.getSocketChannel());

		IChain<IClosedFilter> chain = (IChain<IClosedFilter>) filterChain
				.getFirstChain(IFilterChain.FILTER_CLOSED);
		if (chain != null) {
			chain.getFilter().sessionClosed(conn, chain);
		}
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public void onError(Session session, Throwable e) {
		IChain<IErrFilter> chain = (IChain<IErrFilter>) filterChain
				.getFirstChain(IFilterChain.FILTER_ERROR);
		if (chain != null) {
			chain.getFilter().serverExcept(session, e, chain);
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
				Object message = in.removeFirst();
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

	protected static class MessageTask implements Runnable {
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

	@Override
	public IFilterChain getFilterChain() {
		return filterChain;
	}

	@Override
	public Session addSession(Session session) {
		return getSessionContextMap().put(session.getSessionId(), session);
	}

	@Override
	public boolean hasSessionId(String sessionId) {
		if (sessionId == null)
			return false;
		return getSessionContextMap().containsKey(sessionId);
	}

	@Override
	public Session getSession(String sessionId) {
		if (sessionId == null)
			return null;
		return getSessionContextMap().get(sessionId);
	}

	@Override
	public Session removeSession(String sessionId) {
		if (sessionId == null)
			return null;
		return getSessionContextMap().remove(sessionId);
	}

	protected Map<String, Session> getSessionContextMap() {
		if (cachedSessionsMap == null)
			synchronized (this) {
				if (cachedSessionsMap == null) {
					cachedSessionsMap = createSessionContextMap();
				}
			}
		return cachedSessionsMap;
	}

	protected Map<String, Session> createSessionContextMap() {
		return new ConcurrentHashMap<String, Session>();
	}

	@Override
	public MessageQueue createMessageQueue() {
		int capacity = MAX_MESSAGE_QUEUE_CAPACITY;
		if (contains(ATTR_MAX_MESSATE_QUEUE_CAPACITY)) {
			capacity = (Integer) getAttribute(ATTR_MAX_MESSATE_QUEUE_CAPACITY);
		}
		return new DefaultMessageQueue(capacity);
	}

	@Override
	public WriteRequest createWriteRequest(Connection session) {
		return new DefaultWriteRequest(session);
	}

	@Override
	public Session createSession(Connection conn, Object sid) {
		if (hasSessionId(String.valueOf(sid))) {
			Session session = getSession(String.valueOf(sid));
			throw new RuntimeException("Already exists sessionId: "
					+ session.getSessionId() + "  Connection: "
					+ session.getInetAddress());
		}
		Session session = sessionFactory.create(conn, sid);
		if (session.isDefault() == false)
			onSessionOpened(session);
		return session;
	}

	protected void onSessionOpened(Session session) {
		if (session.getSessionId() != null) {
			addSession(session);// 暂存Sesssion
		}
	}

	@Override
	public Connection getConnection(ByteChannel sc) {
		return connections.get(sc);
	}

	@Override
	public Collection<Connection> getConnections() {
		return connections.values();
	}
}
