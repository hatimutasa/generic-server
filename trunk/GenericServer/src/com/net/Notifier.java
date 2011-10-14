package com.net;

public interface Notifier<R, W> {

	void addHandler(ServerHandler<R, W> listener);

	void removeHandler(ServerHandler<R, W> listener);

	void fireOnAccept() throws Exception;

	void fireOnAccepted(R request) throws Exception;

	void fireOnRead(R request) throws Exception;

	void fireOnWrite(R request, W response) throws Exception;

	void fireOnClosed(R request);

	void fireOnError(Exception e);

	boolean isEmpty();

}
