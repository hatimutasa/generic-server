package com.myrice.core;

public interface MessageInput {
	boolean isEmpty();

	Object message();

	Object popMessage();
}
