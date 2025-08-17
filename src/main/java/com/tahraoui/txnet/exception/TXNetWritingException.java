package com.tahraoui.txnet.exception;

public class TXNetWritingException extends TXNetException {
	public TXNetWritingException() { super("Failed to write data to stream."); }
}
