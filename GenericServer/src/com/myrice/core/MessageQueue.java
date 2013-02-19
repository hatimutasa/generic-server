package com.myrice.core;

import java.util.Collection;

public interface MessageQueue extends MessageInput, MessageOutput {
	Collection<Object> removeAll();
	int size();
}
