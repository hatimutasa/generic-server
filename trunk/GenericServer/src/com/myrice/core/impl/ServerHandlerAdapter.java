package com.myrice.core.impl;

import com.myrice.core.ServerHandler;

public class ServerHandlerAdapter<R, W> implements ServerHandler<R, W> {

	public void destory() {
	}

	public void init() {
	}

	public void onAccept() throws Exception {
	}

	public void onAccepted(R request) throws Exception {
	}

	public void onClosed(R request) {
	}

	public void onError(Exception e) {
		e.printStackTrace();
	}

	public boolean onRead(R request) throws Exception {
		return false;
	}

	public boolean onWrite(R request, W response) throws Exception {
		return false;
	}

}