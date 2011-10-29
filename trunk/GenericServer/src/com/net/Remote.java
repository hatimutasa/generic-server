package com.net;

/**
 * 远程调用接口标记
 * 
 * @author yiyongpeng
 * 
 */
public interface Remote {

	/** Session scope */
	String APPLICATION = "__APPLICATION__", SESSION = "__SESSION__";

	/** Login info, class:com.remote.service.LoginParameter */
	String LOGIN_USER = "__LOGIN_USER__";

}
