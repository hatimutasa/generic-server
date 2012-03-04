package com.myrice.core;

public interface ServerHandler<R, W> {

	void onClosed(R request);

	void onError(Exception e);

	boolean onRead(R request) throws Exception;

	boolean onWrite(R request, W response) throws Exception;

	void onAccepted(R request) throws Exception;

	void onAccept() throws Exception;

	void init();

	void destory();

}
