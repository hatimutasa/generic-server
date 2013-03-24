package com.myrice.filter;

import com.myrice.core.Session;
import com.myrice.filter.IFilterChain.FilterChain;

public interface IErrFilter extends IFilter {

	void serverExcept(Session session, Throwable e,
			FilterChain<IErrFilter> filterChain);

}
