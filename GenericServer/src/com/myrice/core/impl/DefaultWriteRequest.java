package com.myrice.core.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;

import com.myrice.core.MessageInput;
import com.myrice.core.ServerContext;
import com.myrice.core.Session;
import com.myrice.core.WriteRequest;

public class DefaultWriteRequest implements WriteRequest, Runnable {
	// private static final Logger log = Logger
	// .getLogger(DefaultWriteRequest.class);

	private DefaultSession session;
	private int count;

	public void destroy() {
		session = null;
		count = 0;
	}

	public void init(Session session) {
		this.session = (DefaultSession) session;
	}

	public void run() {
		MessageInput queue = session.getMessageOutputQueue();
		SocketChannel sc = session.getSocketChannel();
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
				if (e instanceof ClosedChannelException) {
					if (session.isClosed() == false) {
						getServerHandler().getNotifier().fireOnClosed(session);
					}
				}
			}
		} finally {
			synchronized (this) {
				session.setBusy(false);// 全部发送完成，发送结束
			}
		}
	}

	private ServerContext getServerHandler() {
		return session.getServerHandler();
	}

	public void flush() {
		synchronized (this) {
			if (session.isBusy())
				return;
			session.setBusy(true);
		}
		session.getServerHandler().execute(this);
	}

	public int getCount() {
		return count;
	}
}
