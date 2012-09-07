package com.myrice.core;

import java.util.concurrent.ScheduledFuture;

import com.myrice.filter.IFilterChain;

public interface ServerContext extends Context {

	Connector<Connection> getConnector();

	Session setSessionContext(String sid, Session session);

	Session getSessionContext(String sid);

	Session removeSessionContext(String sid);

	MessageQueue createMessageQueue();

	ScheduledFuture<?> schedule(Runnable runnable, long delay);

	ScheduledFuture<?> schedule(Runnable runnable, long delay, long period);

	void execute(Runnable runnable);

	Notifier<Connection> getNotifier();

	IFilterChain getFilterChain();

	WriteRequest createWriteRequest(Connection session);

}
