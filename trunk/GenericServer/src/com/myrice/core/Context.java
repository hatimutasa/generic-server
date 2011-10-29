package com.myrice.core;

public interface Context {

	Object setAttribute(String name, Object value);

	Object getAttribute(String name);

	Object removeAttribute(String name);

	boolean contains(String name);

	String[] getAttributeNames();

	Object[] getAttributeValues();

	void clear();
}
