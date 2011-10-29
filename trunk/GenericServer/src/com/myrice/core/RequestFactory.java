package com.myrice.core;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public interface RequestFactory<R> {

	R create(SocketChannel sc) throws IOException;

}
