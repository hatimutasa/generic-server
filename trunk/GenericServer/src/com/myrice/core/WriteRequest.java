package com.myrice.core;

public interface WriteRequest extends Runnable {

	void destroy();

	void init(Session session);

	void flush();

	int getCount();

}
