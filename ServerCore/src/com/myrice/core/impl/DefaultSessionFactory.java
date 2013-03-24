package com.myrice.core.impl;

import com.myrice.core.Connection;
import com.myrice.core.Session;
import com.myrice.core.SessionFactory;

public class DefaultSessionFactory implements SessionFactory {

	@Override
	public Session create(Connection conn, Object sessionId) {
		DefaultSession session = new DefaultSession(String.valueOf(sessionId));
		session.init(conn);
		return session;
	}

}
