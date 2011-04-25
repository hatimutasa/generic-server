package com.net.impl;

import com.net.ServerHandler;

public class ServerHandlerAdapter<R, W> implements ServerHandler<R, W> {

    public void onAccept() throws Exception {
    }

    public void onAccepted(R request) throws Exception {
    }

    public void onClosed(R request) {
    }

    public void onError(Exception e) {
	e.printStackTrace();
    }

    public void onRead(R request) throws Exception {
    }

    public void onWrite(R request, W response) throws Exception {
    }

}
