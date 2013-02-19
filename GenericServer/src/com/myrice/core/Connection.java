package com.myrice.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

public interface Connection {

	Session getSession();

	String getInetAddress();

	String getProtocol();

	/**
	 * 获取远程地址
	 * 
	 * @return
	 */
	String getRemoteAddress();

	/**
	 * 获取远程端口
	 * 
	 * @return
	 */
	int getRemotePort();

	/**
	 * 获取本地地址
	 * 
	 * @return
	 */
	String getLocalAddress();

	/**
	 * 获取本地端口
	 * 
	 * @return
	 */
	int getLocalPort();

	/**
	 * 关闭
	 */
	void close();

	/**
	 * 是否已经关闭
	 */
	boolean isClosed();

	ByteChannel getSocketChannel();

	ByteBuffer read() throws IOException;

	WriteRequest getWriteRequest();

	boolean isBusy();

	void clearRecvByteBuffer();

}
