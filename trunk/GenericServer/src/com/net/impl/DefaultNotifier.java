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
		int length = listeners.size();
		for (int i = 0; i < length; i++)
			listeners.get(i).onAccept();
	}

	public void fireOnAccepted(R request) throws Exception {
		int length = listeners.size();
		for (int i = 0; i < length; i++)
			listeners.get(i).onAccepted(request);
	}

	public void fireOnClosed(R request) {
		int length = listeners.size();
		for (int i = 0; i < length; i++)
			listeners.get(i).onClosed(request);
	}

	public void fireOnError(Exception e) {
		int length = listeners.size();
		for (int i = 0; i < length; i++)
			listeners.get(i).onError(e);
	}

	public void fireOnRead(R request) throws Exception {
		int length = listeners.size();
		for (int i = 0; i < length; i++)
			listeners.get(i).onRead(request);
	}

	public void fireOnWrite(R request, W response) throws Exception {
		int length = listeners.size();
		for (int i = 0; i < length; i++)
			listeners.get(i).onWrite(request, response);
	}

	public void addHandler(ServerHandler<R, W> listener) {
		this.listeners.add(listener);
	}

	public void removeHandler(ServerHandler<R, W> listener) {
		this.listeners.remove(listener);
	}
}
