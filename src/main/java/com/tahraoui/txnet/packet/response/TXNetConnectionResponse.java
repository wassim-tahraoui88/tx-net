package com.tahraoui.txnet.packet.response;

import java.security.PublicKey;

public final class TXNetConnectionResponse extends TXNetResponsePacket {

	private final int id;
	private final PublicKey key;
	private final byte[] secret;
	private final byte[] iv;

	public TXNetConnectionResponse(int id, PublicKey key, byte[] secret, byte[] iv) {
		this.id = id;
		this.key = key;
		this.secret = secret;
		this.iv = iv;
	}

	public int getId() { return id; }
	public PublicKey getKey() { return key; }
	public byte[] getSecret() { return secret; }
	public byte[] getIv() { return iv; }
	public boolean isSuccessful() { return id > -1; }
}