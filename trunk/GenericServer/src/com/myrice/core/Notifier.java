package com.myrice.core;

import java.nio.channels.SelectableChannel;

public interface Notifier<R> {

	void addHandler(ServerHandler<R> listener);

	void removeHandler(ServerHandler<R> listener);

	void fireOnAccept() throws Exception;

	R fireOnAccepted(SelectableChannel sc, R prev) throws Exception;

	boolean fireOnRead(R request) throws Exception;

	boolean fireOnWrite(R request) throws Exception;

	void fireOnClosed(R request);

	void fireOnError(R request, Exception e);

	boolean isEmpty();

	void init();

	void destory();

}
