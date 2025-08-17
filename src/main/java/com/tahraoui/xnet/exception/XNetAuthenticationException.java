package com.tahraoui.xnet.exception;

public class XNetAuthenticationException extends XNetException {
	public XNetAuthenticationException() { super("The password you entered is incorrect."); }
}
