package com.tahraoui.txnet.exception;

public class TXNetAuthenticationException extends TXNetException {
	public TXNetAuthenticationException() { super("The password you entered is incorrect."); }
}
