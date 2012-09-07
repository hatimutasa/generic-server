package com.myrice.filter;

import com.myrice.core.Connection;
import com.myrice.filter.IFilterChain.FilterChain;

public interface IClosedFilter extends IFilter {

	void sessionClosed(Connection session,
			FilterChain<IClosedFilter> filterChain);

}
