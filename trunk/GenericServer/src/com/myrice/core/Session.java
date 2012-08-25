package com.myrice.core;

import com.myrice.core.Context;

public interface Session extends Context {
	String IO_BUFFER = "__IO_BUFFER__";
	String IO_BUFFER_CAPACITY = "__IO_BUFFER_CAPACITY__";
	String IO_BUSY = "__IO_BUSY__";
	String IO_WRITE_REQUEST = "__IO_WRITE_REQUEST__";

	String MESSAGE_QUEUE = "__MESSAGE_QUEUE__";
	String MESSAGE_QUEUE_OUT = "__MESSAGE_QUEUE_OUT__";

	String IO_PROTOCOL_ENCODE = "__IO_PROTOCOL_ENCODE__";
	String IO_PROTOCOL_DECODE = "__IO_PROTOCOL_DECODE__";

	int CAPACITY = 1024;

	String getInetAddress();

	/**
	 * 获取核心处理器
	 * 
	 * @return
	 */
	ServerContext getServerHandler();

	/**
	 * 发送非阻塞消息
	 * 
	 * @param msg
	 */
	void send(Object message);

	/**
	 * 刷新发送消息
	 */
	void flush();

	String getSessionId();

	void setSessionId(String sid);

	boolean isBusy();

	String getRemoteAddress();

	int getRemotePort();

}
