package com.myrice.core;

import java.nio.channels.ByteChannel;
import java.util.Collection;
import java.util.concurrent.ScheduledFuture;

import com.myrice.filter.IFilterChain;

public interface ServerContext extends Context {

	Collection<Connection> getConnections();

	Connection getConnection(ByteChannel sc);

	Connector<Connection> getConnector();

	Session createSession(Connection conn, Object sid);

	Session addSession(Session session);

	Session getSession(String sid);

	Session removeSession(String sid);

	MessageQueue createMessageQueue();

	ScheduledFuture<?> schedule(Runnable runnable, long delay);

	ScheduledFuture<?> schedule(Runnable runnable, long delay, long period);

	void execute(Runnable runnable);

	Notifier<Connection> getNotifier();

	IFilterChain getFilterChain();

	WriteRequest createWriteRequest(Connection session);

}
