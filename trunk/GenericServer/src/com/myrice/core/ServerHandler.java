package com.myrice.core;

import java.nio.channels.SelectableChannel;

public interface ServerHandler<R> extends Context {

	void onError(R request, Exception e);

	void onClosed(R request);

	boolean onWrite(R request, boolean prev) throws Exception;

	boolean onRead(R request, boolean prev) throws Exception;

	R onAccepted(SelectableChannel sc, R prev) throws Exception;

	void onAccept() throws Exception;

	void destory();

	void init();

}
