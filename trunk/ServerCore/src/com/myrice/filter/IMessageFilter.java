package com.myrice.filter;

import com.myrice.core.Session;
import com.myrice.filter.IFilterChain.FilterChain;

public interface IMessageFilter extends IFilter {

	void messageReceived(Session session, Object message,
			FilterChain<IMessageFilter> chain);

}
