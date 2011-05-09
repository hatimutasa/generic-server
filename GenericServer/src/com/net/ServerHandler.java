package com.net;

public interface ServerHandler<R, W> {

    void onClosed(R request);

    void onError(Exception e);

    void onRead(R request) throws Exception;

    void onWrite(R request, W response) throws Exception;

    void onAccepted(R request) throws Exception;

    void onAccept() throws Exception;

}
