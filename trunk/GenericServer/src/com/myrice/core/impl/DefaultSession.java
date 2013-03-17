package com.myrice.core.impl;

import java.nio.channels.ByteChannel;

import org.apache.log4j.Logger;

import com.myrice.core.AccessException;
import com.myrice.core.Connection;
import com.myrice.core.MessageOutput;
import com.myrice.core.MessageQueue;
import com.myrice.core.ServerContext;
import com.myrice.core.Session;
import com.myrice.filter.IFilterChain;
import com.myrice.filter.IFilterChain.IChain;
import com.myrice.filter.IProtocolEncodeFilter;

public class DefaultSession extends DefaultContext implements Session {

	private static final Logger log = Logger.getLogger(DefaultSession.class);

	private boolean closed;

	private ServerContext server;
	protected Connection conn;

	protected String sessionId;

	public DefaultSession(String sessionId) {
		this.sessionId = sessionId;
	}

	@Override
	public void close() {
		if (conn != null)
			conn.close();
	}

	public void flush() {
		conn.getWriteRequest().flush(this);
	}

	public void init(ServerContext server) {
		this.server = server;
	}

	public void init(Connection conn) {
		this.conn = conn;
	}

	@SuppressWarnings("unchecked")
	public void send(Object message) {
		if (server.getConnector().isRuning() == false) {
			log.warn(this.getInetAddress()
					+ " send msg failure,  Server is stoped!");
			return;
		}
		if (server == null)
			throw new AccessException("session is destory!");

		IChain<IProtocolEncodeFilter> chain = (IChain<IProtocolEncodeFilter>) server
				.getFilterChain().getFirstChain(
						IFilterChain.FILTER_PROTOCOL_ENCODE);

		if (chain == null) {
			throw new IllegalStateException(
					"No configuration protocol encode filter.");
		}
		try {
			MessageOutput output = getMessageOutputQueue();

			chain.getFilter().messageEncode(conn, message, output, chain);

		} catch (Throwable e) {
			server.getNotifier().fireOnError(conn, e);
		}

	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		if (sessionId.equals(this.sessionId))
			return;

		// 移除旧Session
		getServerHandler().removeSession(getSessionId());

		// 设置新Session
		this.sessionId = sessionId;
		getServerHandler().addSession(this);
	}

	public MessageQueue getMessageOutputQueue() {
		MessageQueue output = (MessageQueue) getAttribute(MESSAGE_QUEUE_OUT);
		if (output == null) {
			output = server.createMessageQueue();
			setAttribute(MESSAGE_QUEUE_OUT, output);
		}
		return output;
	}

	public MessageQueue getMessageInputQueue() {
		MessageQueue queue = (MessageQueue) getAttribute(MESSAGE_QUEUE);
		if (queue == null) {
			queue = server.createMessageQueue();
			setAttribute(MESSAGE_QUEUE, queue);
		}
		return queue;
	}

	public Object getCoverAttributeOfUser(Object key, Object def) {
		// Session
		if (contains(key))
			return getAttribute(key);
		// App
		if (getServerHandler().contains(key))
			return getServerHandler().getAttribute(key);
		return def;
	}

	public Object getCoverAttributeOfApp(Object key, Object def) {
		// App
		if (getServerHandler().contains(key))
			return getServerHandler().getAttribute(key);
		// Session
		if (contains(key))
			return getAttribute(key);
		return def;
	}

	public ByteChannel getSocketChannel() {
		return conn.getSocketChannel();
	}

	public ServerContext getServerHandler() {
		return server;
	}

	public Connection getConnection() {
		return conn;
	}

	public String getInetAddress() {
		return conn.getInetAddress();
	}

	public String getRemoteAddress() {
		return conn.getRemoteAddress();
	}

	public int getRemotePort() {
		return conn.getRemotePort();
	}

	@Override
	public String getLocalAddress() {
		return conn.getLocalAddress();
	}

	@Override
	public int getLocalPort() {
		return conn.getLocalPort();
	}

	public boolean isClosed() {
		return closed;
	}

	protected void setClosed(boolean bool) {
		this.closed = bool;
	}

}
