package com.tahraoui.xnet.exception;

public class XNetEncryptionException extends XNetException {

	public XNetEncryptionException(String message) {
		super("Failed to encrypt data.\n%s".formatted(message));
	}
}
