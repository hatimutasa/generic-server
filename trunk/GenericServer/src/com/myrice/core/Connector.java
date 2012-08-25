package com.myrice.core;

import java.nio.channels.SelectionKey;
import java.util.concurrent.ExecutorService;

/**
 * 连接器
 * 
 * @author yiyongpeng
 * 
 * @param <R>
 *            请求类型
 * @param <W>
 *            响应类型
 */
public interface Connector<R> {

	void start();

	void stop();

	Notifier<R> getNotifier();

	void processRead(SelectionKey key);

	void processWrite(SelectionKey key);

	boolean isRuning();

	ExecutorService getExecutor();
}
