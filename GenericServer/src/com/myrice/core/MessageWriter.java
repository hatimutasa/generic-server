package com.myrice.core;

import java.nio.channels.SelectionKey;

public interface MessageWriter<R> {

	void processRequest(SelectionKey key);

	void init(Connector<R> connector);

	void destory();

}
