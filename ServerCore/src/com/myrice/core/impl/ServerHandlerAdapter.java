package com.myrice.core.impl;

import java.nio.channels.ByteChannel;
import java.nio.channels.SelectableChannel;
import java.util.Collection;
import java.util.concurrent.ScheduledFuture;

import com.myrice.core.Connection;
import com.myrice.core.Connector;
import com.myrice.core.MessageQueue;
import com.myrice.core.Notifier;
import com.myrice.core.ServerHandler;
import com.myrice.core.Session;
import com.myrice.core.SessionFactory;
import com.myrice.core.WriteRequest;
import com.myrice.filter.IFilterChain;

public class ServerHandlerAdapter<R, S> extends DefaultContext implements
		ServerHandler<R, S> {

	public void init() {
	}

	public void destory() {
	}

	public void onAccept() throws Exception {
	}

	public R onAccepted(SelectableChannel sc, R prev) throws Exception {
		return prev;
	}

	public void onClosed(R request) {
	}

	public void onError(S request, Throwable e) {
		e.printStackTrace();
	}

	public boolean onRead(R request, boolean prev) throws Exception {
		return false;
	}

	public boolean onWrite(R request, boolean prev) throws Exception {
		return false;
	}

	public int getSessionCount() {

		return 0;
	}

	public String[] getSessionIds() {

		return null;
	}

	public Collection<Connection> getConnections() {

		return null;
	}

	public Connection getConnection(ByteChannel sc) {

		return null;
	}

	public Connector<Connection, Session> getConnector() {

		return null;
	}

	public Session createSession(Connection conn, Object sid) {

		return null;
	}

	public Session addSession(Session session) {

		return null;
	}

	public Session getSession(String sid) {

		return null;
	}

	public boolean hasSessionId(String sessionId) {

		return false;
	}

	public Session removeSession(String sid) {

		return null;
	}

	public MessageQueue createMessageQueue() {

		return null;
	}

	public ScheduledFuture<?> schedule(Runnable runnable, long delay) {

		return null;
	}

	public ScheduledFuture<?> schedule(Runnable runnable, long delay,
			long period) {

		return null;
	}

	public void execute(Runnable run, long delay) {

	}

	public void execute(Runnable runnable) {

	}

	public Notifier<Connection, Session> getNotifier() {

		return null;
	}

	public IFilterChain getFilterChain() {

		return null;
	}

	public WriteRequest createWriteRequest(Connection session) {

		return null;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {

	}

	public SessionFactory getSessionFactory() {

		return null;
	}

}
