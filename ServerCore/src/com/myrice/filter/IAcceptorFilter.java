package com.myrice.filter;

import java.nio.channels.SelectableChannel;

import com.myrice.core.Connection;
import com.myrice.core.ServerContext;
import com.myrice.filter.IFilterChain.FilterChain;

public interface IAcceptorFilter extends IFilter {

	Connection sessionAccept(ServerContext serverHandler,
			SelectableChannel socket, FilterChain<IAcceptorFilter> filterChain)
			throws Exception;

	Connection sessionOpened(ServerContext serverHandler, Connection session,
			FilterChain<IAcceptorFilter> filterChain) throws Exception;

}
