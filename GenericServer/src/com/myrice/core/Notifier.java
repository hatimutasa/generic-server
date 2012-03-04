package com.myrice.core;

public interface Notifier<R, W> {

	void addHandler(ServerHandler<R, W> listener);

	void removeHandler(ServerHandler<R, W> listener);

	void fireOnAccept() throws Exception;

	void fireOnAccepted(R request) throws Exception;

	boolean fireOnRead(R request) throws Exception;

	boolean fireOnWrite(R request, W response) throws Exception;

	void fireOnClosed(R request);

	void fireOnError(Exception e);

	boolean isEmpty();

	void init();

	void destory();

}
