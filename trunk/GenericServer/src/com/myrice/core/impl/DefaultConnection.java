package com.myrice.core.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;

import com.myrice.core.Connection;
import com.myrice.core.Session;
import com.myrice.core.WriteRequest;
import com.myrice.util.POJO;

public class DefaultConnection extends POJO implements Connection {

	protected ByteChannel channel;

	private String protocol;
	private String address;
	private int port;
	private String inetAddress;

	private Session session;

	private boolean closed;

	private ByteBuffer recvBuffer;

	private boolean writeBusy;
	private WriteRequest writer;

	public void init(ByteChannel sc) {
		this.channel = sc;
		this.protocol = getProtocol(sc);
		this.address = getAddress(sc);
		this.port = getPort(sc);
		this.closed = false;
	}

	public void destory() {
		this.recvBuffer = null;
		this.writeBusy = true;
	}

	public void onClosed() {
		closed = true;
		clearRecvByteBuffer();
	}

	public WriteRequest getWriteRequest() {
		if (writer == null)
			synchronized (this) {
				if (writer == null) {
					writer = session.getServerHandler()
							.createWriteRequest(this);
				}
			}
		return writer;
	}

	public ByteBuffer read() throws IOException {
		ByteBuffer buff = getInputBuffer();
		ReadableByteChannel sc = getSocketChannel();
		int size = 0;
		while ((size = sc.read(buff)) > 0) {
			if (buff.remaining() == 0) {
				ByteBuffer tmp = buff;
				buff = ByteBuffer.allocate((int) (tmp.capacity() * 1.75));
				buff.put((ByteBuffer) tmp.flip());
				setInputBuffer(buff);
			}
		}
		if (size == -1) {
			sc.close();// 到达文件尾
		}
		return buff;
	}

	public boolean isBusy() {
		return writeBusy;
	}

	public void setBusy(boolean value) {
		writeBusy = value;
	}

	private void setInputBuffer(ByteBuffer buff) {
		recvBuffer = buff;
	}

	public ByteBuffer getInputBuffer() {
		if (recvBuffer == null) {
			recvBuffer = ByteBuffer.allocate(getBufferCapacity());
		}
		return recvBuffer;
	}

	private int getBufferCapacity() {
		if (session.contains(Session.IO_BUFFER_CAPACITY)) {
			return (Integer) session.getAttribute(Session.IO_BUFFER_CAPACITY);
		}
		return Session.CAPACITY;
	}

	private String getProtocol(ByteChannel sc) {
		if (sc instanceof DatagramChannel) {
			return "UDP";
		}
		if (sc instanceof SocketChannel) {
			return "TCP";
		}
		return "Unkown";
	}

	private int getPort(ByteChannel sc) {
		if (sc instanceof DatagramChannel) {
			return ((DatagramChannel) sc).socket().getPort();
		}
		if (sc instanceof SocketChannel) {
			return ((SocketChannel) sc).socket().getPort();
		}
		return 0;
	}

	private String getAddress(ByteChannel sc) {
		if (sc instanceof DatagramChannel) {
			return ((DatagramChannel) sc).socket().getInetAddress()
					.getHostAddress();
		}
		if (sc instanceof SocketChannel) {
			return ((SocketChannel) sc).socket().getInetAddress()
					.getHostAddress();
		}
		return null;
	}

	public void close() {
		try {
			closed = true;
			channel.close();
		} catch (IOException e) {
		}
	}

	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public String getInetAddress() {
		if (inetAddress == null)
			inetAddress = getProtocol() + ":" + getRemoteAddress() + ":"
					+ getRemotePort();
		return inetAddress;
	}

	public String getProtocol() {
		return protocol;
	}

	public String getRemoteAddress() {
		return address;
	}

	public int getRemotePort() {
		return port;
	}

	public ByteChannel getSocketChannel() {
		return channel;
	}

	public void clearRecvByteBuffer() {
		if (recvBuffer != null)
			recvBuffer.clear();
	}

}
