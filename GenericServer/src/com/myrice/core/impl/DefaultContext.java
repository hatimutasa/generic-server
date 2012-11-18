package com.myrice.core.impl;

import java.util.HashMap;
import java.util.Map;

import com.myrice.core.Context;
import com.myrice.core.Remote;
import com.myrice.util.POJO;

/**
 * 上下文容器默认实现类
 * 
 * @author yiyongpeng
 * 
 */
public class DefaultContext extends POJO implements Remote, Context {
	private Map<Object, Object> context = new HashMap<Object, Object>(1);

	@Override
	public boolean contains(Object key) {
		return context.containsKey(key);
	}

	@Override
	public Object getAttribute(Object key) {
		return context.get(key);
	}

	@Override
	public Object removeAttribute(Object key) {
		return context.remove(key);
	}

	@Override
	public Object setAttribute(Object key, Object value) {
		return context.put(key, value);
	}

	public Object getAttribute(String name, Object defaultValue) {
		synchronized (context) {
			if (context.containsKey(name))
				return context.get(name);
		}
		return defaultValue;
	}

	public Object getAttribute(String name) {
		synchronized (context) {
			return context.get(name);
		}
	}

	public String[] getAttributeNames() {
		synchronized (context) {
			String[] names = new String[context.size()];
			return context.keySet().toArray(names);
		}
	}

	public Object[] getAttributeValues() {
		synchronized (context) {
			return context.values().toArray();
		}
	}

	public Object removeAttribute(String name) {
		synchronized (context) {
			return context.remove(name);
		}
	}

	public Object setAttribute(String name, Object value) {
		synchronized (context) {
			return context.put(name, value);
		}
	}

	public boolean contains(String name) {
		synchronized (context) {
			return context.containsKey(name);
		}
	}

	public void clear() {
		synchronized (context) {
			context.clear();
		}
	}
}
