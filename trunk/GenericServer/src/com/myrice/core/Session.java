package com.myrice.core;

public interface Session extends Context {

	String MESSAGE_QUEUE = "__MESSAGE_QUEUE__";
	String MESSAGE_QUEUE_OUT = "__MESSAGE_QUEUE_OUT__";

	String IO_BUFFER_CAPACITY = "__IO_BUFFER_CAPACITY__";
	String IO_PROTOCOL_ENCODE = "__IO_PROTOCOL_ENCODE__";
	String IO_PROTOCOL_DECODE = "__IO_PROTOCOL_DECODE__";

	int CAPACITY = 1024;

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

	String getInetAddress();

	String getRemoteAddress();

	int getRemotePort();

	Connection getConnection();

	MessageQueue getMessageInputQueue();

	MessageQueue getMessageOutputQueue();

	/**
	 * 获取用户覆盖属性，优先级 session -> application
	 * 
	 * @param name
	 *            属性名
	 * @param def
	 *            默认值
	 * @return
	 */
	Object getCoverAttributeOfUser(String name, Object def);

	/**
	 * 获取应用程序覆盖属性，优先级 application -> session
	 * 
	 * @param name
	 *            属性名
	 * @param def
	 *            默认值
	 * @return
	 */
	Object getCoverAttributeOfApp(String name, Object def);
}