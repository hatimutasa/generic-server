package com.myrice.util;

import java.nio.ByteBuffer;

public class ByteBufferUtils {

	/**
	 * 平移字节内容
	 * 
	 * @param msg
	 * @param offset
	 */
	public static void offset(ByteBuffer msg, int offset) {
		if (offset == 0)
			return;
		int length = msg.remaining();
		byte[] rawBytes = msg.array();
		// 向后平移4个字节
		for (int i = 0; i < length; i++) {
			int p = offset > 0 ? msg.limit() - 1 - i : msg.position() + i;
			rawBytes[p + offset] = rawBytes[p];
		}
	}

}
