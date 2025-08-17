package com.tahraoui.txnet.exception;

public class TXNetEncryptionException extends TXNetException {

	public TXNetEncryptionException(String message) {
		super("Failed to encrypt data.\n%s".formatted(message));
	}
}
