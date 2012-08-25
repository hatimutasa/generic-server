package com.myrice.filter;

import com.myrice.core.ServerContext;
import com.myrice.filter.IFilterChain.FilterChain;

public interface IAcceptFilter extends IFilter {

	void serverAccept(ServerContext serverHandler,
			FilterChain<IAcceptFilter> filterChain) throws Exception;

}
