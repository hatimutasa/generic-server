package com.net;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public interface ResponseFactory<W> {

    W create(SelectionKey key) throws IOException;

}
