package com.myrice.core;

public interface Context {

	Object setAttribute(String name, Object value);

	Object getAttribute(String name);

	Object getAttribute(String name, Object defaultValue);

	Object removeAttribute(String name);

	boolean contains(String name);

	String[] getAttributeNames();

	Object[] getAttributeValues();

	void clear();
}
