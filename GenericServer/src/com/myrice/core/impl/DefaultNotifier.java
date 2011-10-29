package com.myrice.core.impl;

import java.util.LinkedList;
import java.util.List;

import com.myrice.core.Notifier;
import com.myrice.core.ServerHandler;

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

	public boolean fireOnRead(R request) throws Exception {
		boolean suc = false;
		int length = listeners.size();
		for (int i = 0; i < length; i++)
			suc = listeners.get(i).onRead(request);
		return suc;
	}

	public boolean fireOnWrite(R request, W response) throws Exception {
		boolean suc = false;
		int length = listeners.size();
		for (int i = 0; i < length; i++)
			suc = listeners.get(i).onWrite(request, response);
		return suc;
	}

	public void addHandler(ServerHandler<R, W> listener) {
		this.listeners.add(listener);
	}

	public void removeHandler(ServerHandler<R, W> listener) {
		this.listeners.remove(listener);
	}
}
