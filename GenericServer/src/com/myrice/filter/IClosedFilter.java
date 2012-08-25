package com.myrice.filter;

import com.myrice.core.Session;
import com.myrice.filter.IFilterChain.FilterChain;

public interface IClosedFilter extends IFilter {

	void sessionClosed(Session session, FilterChain<IClosedFilter> filterChain);

}
