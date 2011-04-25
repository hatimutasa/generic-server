package com.net.impl;

import java.util.LinkedList;
import java.util.List;

import com.net.Notifier;
import com.net.ServerHandler;

public class DefaultNotifier<R, W> implements Notifier<R, W> {
	private List<ServerHandler<R, W>> listeners;

	public DefaultNotifier() {
		this.listeners = new LinkedList<ServerHandler<R, W>>();
	}

	public boolean isEmpty() {
		return listeners.isEmpty();
	}

	public void fireOnAccept() throws Exception {
		for (ServerHandler<R, W> listener : this.listeners)
			listener.onAccept();
	}

	public void fireOnAccepted(R request) throws Exception {
		for (ServerHandler<R, W> listener : this.listeners)
			listener.onAccepted(request);
	}

	public void fireOnClosed(R request) {
		for (ServerHandler<R, W> listener : this.listeners)
			listener.onClosed(request);
	}

	public void fireOnError(Exception e) {
		for (ServerHandler<R, W> listener : this.listeners)
			listener.onError(e);
	}

	public void fireOnRead(R request) throws Exception {
		for (ServerHandler<R, W> listener : this.listeners)
			listener.onRead(request);
	}

	public void fireOnWrite(R request, W response) throws Exception {
		for (ServerHandler<R, W> listener : this.listeners)
			listener.onWrite(request, response);
	}

	public void addHandler(ServerHandler<R, W> listener) {
		this.listeners.add(listener);
	}

	public void removeHandler(ServerHandler<R, W> listener) {
		this.listeners.remove(listener);
	}
}
