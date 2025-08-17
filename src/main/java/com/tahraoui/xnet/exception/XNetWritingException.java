package com.tahraoui.xnet.exception;

public class XNetWritingException extends XNetException {
	public XNetWritingException() { super("Failed to write data to stream."); }
}
