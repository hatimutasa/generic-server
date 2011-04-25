package com.net;

import java.nio.channels.SelectionKey;

public interface MessageReader<R, W> {

	void processRequest(SelectionKey key);

	void init(Connector<R, W> connector);

	void destory();

}
