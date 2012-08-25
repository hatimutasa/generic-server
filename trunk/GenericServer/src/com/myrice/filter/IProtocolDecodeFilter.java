package com.myrice.filter;

import java.nio.ByteBuffer;

import com.myrice.core.MessageOutput;
import com.myrice.core.Session;
import com.myrice.filter.IFilterChain.FilterChain;

public interface IProtocolDecodeFilter extends IFilter {

	/**
	 * 协议解码过滤
	 * 
	 * @param session
	 * @param in
	 * @param out
	 * @param chain
	 * @return 是否解码到一个完整消息
	 */
	boolean messageDecode(Session session, ByteBuffer in, MessageOutput out,
			FilterChain<IProtocolDecodeFilter> chain);

}
