package com.tahraoui.txnet.exception;

public class TXNetReadingException extends TXNetException {
	public TXNetReadingException() { super("Failed to read data from stream."); }
}
