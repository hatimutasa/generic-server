package com.myrice.core.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.myrice.core.AccessException;
import com.myrice.core.MessageQueue;
import com.myrice.core.MessageOutput;
import com.myrice.core.ServerContext;
import com.myrice.core.Session;
import com.myrice.core.WriteRequest;
import com.myrice.filter.IFilterChain;
import com.myrice.filter.IFilterChain.IChain;
import com.myrice.filter.IProtocolEncodeFilter;
import com.myrice.util.POJO;

public class DefaultSession extends POJO implements Session {

	ServerContext server;
	protected SocketChannel sc;

	String sessionId = getClass().getCanonicalName() + "@" + hashCode();
	private String address;
	private int port;
	private boolean closed;

	public String getInetAddress() {
		return getRemoteAddress() + ":" + getRemotePort();
	}

	public String getRemoteAddress() {
		return address;
	}

	public int getRemotePort() {
		return port;
	}

	public void flush() {
		getWriteRequest().flush();
	}

	public boolean isBusy() {
		if (contains(IO_BUSY)) {
			return (Boolean) getAttribute(IO_BUSY);
		}
		return false;
	}

	public boolean isClosed() {
		return closed;
	}

	protected void setClosed(boolean bool) {
		this.closed = bool;
	}

	public SocketChannel getSocketChannel() {
		return sc;
	}

	public void setBusy(boolean value) {
		setAttribute(IO_BUSY, value);
	}

	public void init(ServerContext server) {
		this.server = server;
	}

	protected void init(SocketChannel sc) {
		this.sc = sc;
		this.address = sc.socket().getInetAddress().getHostAddress();
		this.port = sc.socket().getPort();
	}

	public ServerContext getServerHandler() {
		return server;
	}

	public WriteRequest getWriteRequest() {
		WriteRequest output = (WriteRequest) getAttribute(IO_WRITE_REQUEST);
		if (output == null) {
			output = server.createWriteRequest();
			setAttribute(IO_WRITE_REQUEST, output);
		}
		output.init(this);
		return output;
	}

	@SuppressWarnings("unchecked")
	public void send(Object message) {
		if (server == null)
			throw new AccessException("session is closed!");

		IChain<IProtocolEncodeFilter> chain = (IChain<IProtocolEncodeFilter>) server
				.getFilterChain().getFirstChain(
						IFilterChain.FILTER_PROTOCOL_ENCODE);

		if (chain == null) {
			throw new IllegalStateException(
					"No configuration protocol encode filter.");
		}
		try {
			MessageOutput output = getMessageOutputQueue();
			chain.getFilter().messageEncode(this, message, output, chain);
		} catch (Exception e) {
			server.getNotifier().fireOnError(this, e);
		}
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sid) {
		if (server != null) {
			server.removeSessionContext(sessionId);
		}
		this.sessionId = sid;
	}

	/* Session Scope begin */

	public void clear() {
		server.getSessionContext(sessionId).clear();
	}

	public boolean contains(String name) {
		return server.getSessionContext(sessionId).contains(name);
	}

	public Object getAttribute(String name) {
		return server.getSessionContext(sessionId).getAttribute(name);
	}

	public String[] getAttributeNames() {
		return server.getSessionContext(sessionId).getAttributeNames();
	}

	public Object[] getAttributeValues() {
		return server.getSessionContext(sessionId).getAttributeValues();
	}

	public Object removeAttribute(String name) {
		return server.getSessionContext(sessionId).removeAttribute(name);
	}

	public Object setAttribute(String name, Object value) {
		return server.getSessionContext(sessionId).setAttribute(name, value);
	}

	/* Session Scope end */

	public ByteBuffer onRead() throws IOException {
		ByteBuffer buff = getInputBuffer();
		SocketChannel sc = getSocketChannel();
		int size = 0;
		while ((size = sc.read(buff)) > 0) {
			if (buff.remaining() == 0) {
				ByteBuffer tmp = buff;
				buff = ByteBuffer.allocate(tmp.capacity() * 2);
				buff.put((ByteBuffer) tmp.flip());
				setInputBuffer(buff);
			}
		}
		if (size == -1) {
			sc.close();// 到达文件尾
		}
		return buff;
	}

	private void setInputBuffer(ByteBuffer buff) {
		setAttribute(IO_BUFFER, buff);
	}

	public ByteBuffer getInputBuffer() {
		ByteBuffer buff = (ByteBuffer) getAttribute(IO_BUFFER);
		if (buff == null) {
			buff = ByteBuffer.allocate(getBufferCapacity());
			setAttribute(IO_BUFFER, buff);
		}
		return buff;
	}

	private int getBufferCapacity() {
		if (contains(IO_BUFFER_CAPACITY)) {
			return (Integer) getAttribute(IO_BUFFER_CAPACITY);
		}
		return CAPACITY;
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

	public void destory() {
		this.server = null;
		this.sc = null;
		this.sessionId = null;
		this.address = null;
	}

}
