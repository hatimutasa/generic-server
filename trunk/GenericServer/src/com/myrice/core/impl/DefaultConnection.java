package com.myrice.core.impl;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
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
	private Session session;

	protected ByteChannel channel;

	private String localAddress;
	private int localPort;

	private String protocol;
	private String address;
	private int port;

	private String inetAddress;

	private boolean closed;

	private ByteBuffer recvBuffer;

	private boolean writeBusy;
	private WriteRequest writer;

	public void init(ByteChannel sc) {
		this.channel = sc;
		this.localAddress = getAddress(sc, false);
		this.localPort = getPort(sc, false);
		this.protocol = getProtocol(sc);
		this.address = getAddress(sc, true);
		this.port = getPort(sc, true);
		this.inetAddress = null;
		this.closed = false;
		recvBuffer = null;
		writeBusy = false;
	}

	public void destory() {
		this.recvBuffer = null;
		this.writeBusy = false;
		this.writer = null;
	}

	public void onClosed() {
		closed = true;
		clearRecvBuffer();
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
				buff = ByteBuffer.allocate((int) (tmp.capacity() * 1.75));// 1.75倍自增
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
		return (Integer) session.getCoverAttributeOfUser(
				Session.IO_BUFFER_CAPACITY, Session.DEFAULT_IO_BUFFER_CAPACITY);
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

	private int getPort(ByteChannel sc, boolean remote) {
		if (sc instanceof DatagramChannel) {
			DatagramSocket sock = ((DatagramChannel) sc).socket();
			return remote ? sock.getPort() : sock.getLocalPort();
		}
		if (sc instanceof SocketChannel) {
			Socket sock = ((SocketChannel) sc).socket();
			return remote ? sock.getPort() : sock.getLocalPort();
		}
		return 0;
	}

	private String getAddress(ByteChannel sc, boolean remote) {
		if (sc instanceof DatagramChannel) {
			DatagramSocket sock = ((DatagramChannel) sc).socket();
			return remote ? sock.getInetAddress().getHostAddress() : sock
					.getLocalAddress().getHostAddress();
		}
		if (sc instanceof SocketChannel) {
			Socket sock = ((SocketChannel) sc).socket();
			return remote ? sock.getInetAddress().getHostAddress() : sock
					.getLocalAddress().getHostAddress();
		}
		return null;
	}

	public void close() {
		if (closed)
			return;
		try {
			closed = true;
			channel.close();
		} catch (IOException e) {
		} finally {
			Thread.dumpStack();
		}
	}

	public boolean isClosed() {
		SocketChannel sc = ((SocketChannel) channel);
		if (!this.channel.isOpen() || !sc.isConnected()
				|| sc.socket().isClosed() || !sc.socket().isConnected())
			return true;
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

	@Override
	public String getLocalAddress() {
		return localAddress;
	}

	@Override
	public int getLocalPort() {
		return localPort;
	}

	public ByteChannel getSocketChannel() {
		return channel;
	}

	public void clearRecvBuffer() {
		recvBuffer = null;
	}

	@Override
	public Session createSession(String sessionId) {
		return getSession().getServerHandler().createSession(this, sessionId);
	}

	@Override
	public String toString() {
		return new StringBuilder("[").append(getLocalAddress()).append(":")
				.append(getLocalPort()).append("  =>  ")
				.append(getInetAddress()).append("]").toString();
	}
}
