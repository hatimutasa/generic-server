package com.myrice.filter;

import com.myrice.core.Connection;
import com.myrice.core.MessageOutput;
import com.myrice.filter.IFilterChain.FilterChain;

public interface IProtocolEncodeFilter extends IFilter {

	void messageEncode(Connection session, Object message,
			MessageOutput output, FilterChain<IProtocolEncodeFilter> filterChain)
			throws Exception;

}
