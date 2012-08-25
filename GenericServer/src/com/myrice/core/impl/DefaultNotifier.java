package com.myrice.core.impl;

import java.nio.channels.SelectableChannel;
import java.util.LinkedList;
import java.util.List;

import com.myrice.core.Notifier;
import com.myrice.core.ServerHandler;

public class DefaultNotifier<R> implements Notifier<R> {
	private List<ServerHandler<R>> listeners;

	public DefaultNotifier() {
		this.listeners = new LinkedList<ServerHandler<R>>();
	}

	public boolean isEmpty() {
		return listeners.isEmpty();
	}

	public void fireOnAccept() throws Exception {
		int length = listeners.size();
		for (int i = 0; i < length; i++)
			listeners.get(i).onAccept();
	}

	public R fireOnAccepted(SelectableChannel sc, R prev) throws Exception {
		int length = listeners.size();
		for (int i = 0; i < length; i++)
			prev = listeners.get(i).onAccepted(sc, prev);
		return prev;
	}

	public void fireOnClosed(R request) {
		int length = listeners.size();
		for (int i = 0; i < length; i++)
			listeners.get(i).onClosed(request);
	}

	public void fireOnError(R request, Exception e) {
		int length = listeners.size();
		for (int i = 0; i < length; i++)
			listeners.get(i).onError(request, e);
	}

	public boolean fireOnRead(R request) throws Exception {
		boolean suc = false;
		int length = listeners.size();
		for (int i = 0; i < length; i++)
			suc = listeners.get(i).onRead(request, suc);
		return suc;
	}

	public boolean fireOnWrite(R request) throws Exception {
		boolean bool = false;
		int length = listeners.size();
		for (int i = 0; i < length; i++)
			bool = listeners.get(i).onWrite(request, bool);
		return bool;
	}

	public void addHandler(ServerHandler<R> listener) {
		if (this.listeners.contains(listener) == false) {
			this.listeners.add(listener);
		}
	}

	public void removeHandler(ServerHandler<R> listener) {
		this.listeners.remove(listener);
	}

	public void init() {
		int length = listeners.size();
		for (int i = 0; i < length; i++)
			listeners.get(i).init();
	}

	public void destory() {
		int length = listeners.size();
		for (int i = 0; i < length; i++)
			listeners.get(i).destory();
	}
}
