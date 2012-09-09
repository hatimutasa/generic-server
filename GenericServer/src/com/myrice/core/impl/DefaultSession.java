package com.myrice.core.impl;

import java.nio.channels.ByteChannel;
import java.util.UUID;

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
	// private static final Logger log = Logger.getLogger(DefaultSession.class);

	private ServerContext server;
	protected Connection conn;

	private boolean closed;

	String sessionId;
	{
		sessionId = UUID.randomUUID().toString();
	}

	public void flush() {
		conn.getWriteRequest().flush();
	}

	public void init(ServerContext server) {
		this.server = server;
	}

	public void init(Connection conn) {
		this.conn = conn;
		if (conn instanceof DefaultConnection) {
			((DefaultConnection) conn).setSession(this);
		}
	}

	@SuppressWarnings("unchecked")
	public void send(Object message) {
		if (server == null)
			throw new AccessException("session is destory!");

		IChain<IProtocolEncodeFilter> chain = (IChain<IProtocolEncodeFilter>) server
				.getFilterChain().getFirstChain(
						IFilterChain.FILTER_PROTOCOL_ENCODE);

		if (chain == null)
			throw new IllegalStateException(
					"No configuration protocol encode filter.");

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
		getServerHandler().setSessionContext(sessionId, this);
		getServerHandler().removeSessionContext(getSessionId());

		this.sessionId = sessionId;
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

	public Object getCoverAttributeOfUser(String name, Object def) {
		if (contains(name))
			return getAttribute(name);
		if (getServerHandler().contains(name))
			return getServerHandler().getAttribute(name);
		return def;
	}

	public Object getCoverAttributeOfApp(String name, Object def) {
		if (getServerHandler().contains(name))
			return getServerHandler().getAttribute(name);
		if (contains(name))
			return getAttribute(name);
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

	public boolean isClosed() {
		return closed;
	}

	protected void setClosed(boolean bool) {
		this.closed = bool;
	}

}
