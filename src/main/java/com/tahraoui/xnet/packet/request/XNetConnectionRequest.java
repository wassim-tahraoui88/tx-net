package com.tahraoui.xnet.packet.request;

import java.security.PublicKey;

public final class XNetConnectionRequest extends XNetRequestPacket {

	private final String username;
	private final String password;
	private final PublicKey key;

	public XNetConnectionRequest(String username, String password, PublicKey key) {
		this.username = username;
		this.password = password;
		this.key = key;
	}

	public String getUsername() { return username; }
	public String getPassword() { return password; }
	public PublicKey getKey() { return key; }
}