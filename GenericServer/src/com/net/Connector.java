package com.net;

import java.nio.channels.SelectionKey;
import java.util.concurrent.Executor;

public interface Connector<R, W> {

    void start();

    void stop();

    Notifier<R, W> getNotifier();

    void processRead(SelectionKey key);

    void processWrite(SelectionKey key);

    boolean isRuning();

    RequestFactory<R> getRequestFactory();

    ResponseFactory<W> getResponseFactory();

    Executor getExecutor();
}
