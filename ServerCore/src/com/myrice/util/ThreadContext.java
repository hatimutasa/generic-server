package com.myrice.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public final class ThreadContext {
	public static final String SCOPE_APP = "__APP__";
	public static final String SCOPE_SESSION = "__SESSION__";
	public static final String SCOPE_REQUEST = "__REQUEST__";
	public static final String SCOPE_RESPONSE = "__RESPONSE__";

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

	public static final boolean contains() {
		return context.containsKey(Thread.currentThread());
	}

	public static final Map<Object, Object> init() {
		Thread t = Thread.currentThread();
		Map<Object, Object> map = context.get(t);
		if (map == null) {
			map = createContext();
			context.put(t, map);
		}
		return map;
	}

	private static Map<Object, Object> createContext() {
		Map<Object, Object> map = recycle.poll();
		if (map == null)
			map = new HashMap<Object, Object>();
		return map;
	}

	public static final void destory() {
		Map<Object, Object> map = context.remove(Thread.currentThread());
		if (map != null) {
			map.clear();
			recycle.offer(map);
		}
	}

	private static final int RECYCLE_MAX = 1024;

	private static final Map<Thread, Map<Object, Object>> context = new ConcurrentHashMap<Thread, Map<Object, Object>>();
	private static final BlockingQueue<Map<Object, Object>> recycle = new ArrayBlockingQueue<Map<Object, Object>>(
			RECYCLE_MAX);

}
