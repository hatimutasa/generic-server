package com.myrice.core;

import java.util.concurrent.ScheduledFuture;

import com.myrice.filter.IFilterChain;

public interface ServerContext extends Context {

	Connector<Session> getConnector();

	/**
	 * 获取SessionContext
	 * 
	 * @param sid
	 *            SessionId
	 * @return 不可能返回null
	 */
	Context getSessionContext(String sid);

	void removeSessionContext(String sid);

	MessageQueue createMessageQueue();

	ScheduledFuture<?> schedule(Runnable runnable, long delay);

	ScheduledFuture<?> schedule(Runnable runnable, long delay, long period);

	void execute(Runnable runnable);

	Notifier<Session> getNotifier();

	IFilterChain getFilterChain();

	WriteRequest createWriteRequest();

}
