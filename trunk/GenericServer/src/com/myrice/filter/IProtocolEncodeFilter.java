package com.myrice.filter;

import com.myrice.core.MessageOutput;
import com.myrice.core.Session;
import com.myrice.filter.IFilterChain.FilterChain;

public interface IProtocolEncodeFilter extends IFilter {

	void messageEncode(Session session, Object message, MessageOutput output,
			FilterChain<IProtocolEncodeFilter> filterChain) throws Exception;

}
