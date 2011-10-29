package com.myrice.core.msg;

import java.io.Serializable;

public interface ObjectMessage extends Message {

	Serializable getObject();

	void setObject(Serializable value);
}
