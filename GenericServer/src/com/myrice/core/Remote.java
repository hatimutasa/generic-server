package com.myrice.core;

/**
 * Զ�̵��ýӿڱ��
 * 
 * @author yiyongpeng
 * 
 */
public interface Remote extends java.rmi.Remote{

	/** Session scope */
	String APPLICATION = "__APPLICATION__", SESSION = "__SESSION__";

	/** Login info, class:com.remote.service.LoginParameter */
	String LOGIN_USER = "__LOGIN_USER__";

}