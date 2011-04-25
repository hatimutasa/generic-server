package com.net.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ThreadContext {

	public static final boolean contains(String name) {
		return getContext().containsKey(name);
	}

	public static final Object getAttribute(String name) {
		return getContext().get(name);
	}

	public static final String[] getAttributeNames() {
		Map<Object, Object> map = getContext();
		String[] names = new String[map.size()];
		return map.keySet().toArray(names);
	}

	public static final Object[] getAttributeValues() {
		return getContext().values().toArray();
	}

	public static final Object removeAttribute(String name) {
		return getContext().remove(name);
	}

	public static final Object setAttribute(String name, Object value) {
		return getContext().put(name, value);
	}

	public static final Map<Object, Object> getContext() {
		return context.get(Thread.currentThread());
	}

	public static final void init() {
		Thread current = Thread.currentThread();
		if (context.containsKey(current) == false)
			context.put(current, new HashMap<Object, Object>(4));
	}

	public static final void destory() {
		Thread current = Thread.currentThread();
		if (context.containsKey(current))
			context.remove(current).clear();
	}

	private static final Map<Thread, Map<Object, Object>> context = new ConcurrentHashMap<Thread, Map<Object, Object>>();

	private static final long serialVersionUID = -487361707658106598L;
}
