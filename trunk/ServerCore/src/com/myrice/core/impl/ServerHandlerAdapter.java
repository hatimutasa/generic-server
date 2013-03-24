package com.myrice.core.impl;

import java.nio.channels.SelectableChannel;

import com.myrice.core.ServerHandler;

public class ServerHandlerAdapter<R, S> extends DefaultContext implements
		ServerHandler<R, S> {

	public void init() {
	}

	public void destory() {
	}

	public void onAccept() throws Exception {
	}

	public R onAccepted(SelectableChannel sc, R prev) throws Exception {
		return prev;
	}

	public void onClosed(R request) {
	}

	public void onError(S request, Throwable e) {
		e.printStackTrace();
	}

	public boolean onRead(R request, boolean prev) throws Exception {
		return false;
	}

	public boolean onWrite(R request, boolean prev) throws Exception {
		return false;
	}

}
