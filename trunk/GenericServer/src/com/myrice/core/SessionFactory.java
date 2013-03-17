package com.myrice.core;

public interface SessionFactory {

	Session create(Connection conn, Object sid);

}
