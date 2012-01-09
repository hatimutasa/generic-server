package com.myrice.core;

import java.nio.channels.SelectionKey;
import java.util.concurrent.Executor;

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
public interface Connector<R, W> {

	void start();

	void stop();

	Notifier<R, W> getNotifier();

	void processRead(SelectionKey key);

	void processWrite(SelectionKey key);

	boolean isRuning();

	RequestFactory<R> getRequestFactory();

	ResponseFactory<W> getResponseFactory();

	Executor getExecutor();
}
