package com.myrice.filter;

import java.nio.channels.SelectableChannel;

import com.myrice.core.ServerContext;
import com.myrice.core.Session;
import com.myrice.filter.IFilterChain.FilterChain;

public interface IAcceptorFilter extends IFilter {

	Session sessionAccept(ServerContext serverHandler,
			SelectableChannel socket, FilterChain<IAcceptorFilter> filterChain)
			throws Exception;

	Session sessionOpened(ServerContext serverHandler, Session session,
			FilterChain<IAcceptorFilter> filterChain) throws Exception;

}
