package com.myrice.core;

import java.nio.channels.SelectionKey;

public interface MessageWriter<R, W> {

	void processRequest(SelectionKey key);

	void init(Connector<R, W> connector);

	void destory();

}
