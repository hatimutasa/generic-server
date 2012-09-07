package com.myrice.core;

import java.nio.channels.SelectableChannel;

public interface Notifier<R> {

	void addHandler(ServerHandler<R> listener);

	void removeHandler(ServerHandler<R> listener);

	void fireOnAccept();

	R fireOnAccepted(SelectableChannel sc, R prev) throws Exception;

	boolean fireOnRead(R request);

	boolean fireOnWrite(R request);

	void fireOnClosed(R request);

	void fireOnError(R request, Throwable e);

	boolean isEmpty();

	void init();

	void destory();

}
