package com.myrice.filter;

import com.myrice.core.Connection;
import com.myrice.filter.IFilterChain.FilterChain;

public interface IErrFilter extends IFilter {

	void serverExcept(Connection session, Throwable e,
			FilterChain<IErrFilter> filterChain);

}
