package com.net.impl;

import java.util.HashMap;
import java.util.Map;

import com.net.Context;
import com.net.Remote;
import com.net.util.POJO;

/**
 * иообнд
 * 
 * @author yiyongpeng
 * 
 */
public class ContextImpl extends POJO implements Remote, Context {
    private static final long serialVersionUID = -2520602227624923821L;
    
    private Map<String, Object> context = new HashMap<String, Object>(1);

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
