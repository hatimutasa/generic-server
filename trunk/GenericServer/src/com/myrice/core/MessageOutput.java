package com.myrice.core;

import java.util.Collection;

public interface MessageOutput {

	void putMessage(Object message);

	void putMessageAll(Collection<Object> msg);

	boolean isFulled();
}
