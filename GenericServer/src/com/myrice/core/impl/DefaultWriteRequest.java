package com.myrice.core.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import com.myrice.core.Connection;
import com.myrice.core.MessageInput;
import com.myrice.core.WriteRequest;

public class DefaultWriteRequest implements WriteRequest, Runnable {
	// private static final Logger log = Logger
	// .getLogger(DefaultWriteRequest.class);

	private DefaultConnection conn;
	private int count;

	public DefaultWriteRequest(Connection session) {
		init(session);
	}

	public void destroy() {
		conn = null;
		count = 0;
	}

	public void init(Connection session) {
		this.conn = (DefaultConnection) session;
	}

	public void run() {
		MessageInput queue = conn.getSession().getMessageOutputQueue();
		WritableByteChannel sc = conn.getSocketChannel();
		ByteBuffer out = null;
		try {
			// ---------- Send begin ---------
			for (; queue.isEmpty() == false;) {
				out = (ByteBuffer) queue.message();

				int size = sc.write(out);
				count += size;

				if (out.remaining() == 0) {
					queue.popMessage();// 移除已发送数据包
				} else {
					try {
						Thread.sleep(10);// 网速不佳，延迟
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			// ----------- Send end ----------
		} catch (IOException e) {
			try {
				out.position(0);// 发送失败，复位，等待重连发送
				sc.close();
			} catch (IOException e1) {
			} finally {
				conn.getSession().getServerHandler().getConnector().wakeup();
				// if (e instanceof ClosedChannelException) {
				// if (session.isClosed() == false) {
				// getServerHandler().getNotifier().fireOnClosed(session);
				// }
				// }
			}
		} finally {
			synchronized (this) {
				conn.setBusy(false);// 全部发送完成，发送结束
			}
		}
	}

	public void flush() {
		if (conn.isClosed())
			return;
		synchronized (this) {
			if (conn.isBusy())
				return;
			conn.setBusy(true);
		}
		conn.getSession().getServerHandler().execute(this);
	}

	public int getCount() {
		return count;
	}
}
