package com.tahraoui.xnet.packet.request;

import com.tahraoui.xnet.packet.XNetPacket;
import com.tahraoui.xnet.util.EncryptionUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.Serializable;

public class XNetPacketEncrypted extends XNetPacket implements Serializable {

	private final byte[] data;

	public XNetPacketEncrypted(byte[] data) {
		this.data = data;

	}

	public XNetPacket decrypt(SecretKey key, IvParameterSpec iv) {
		var decryptedData = EncryptionUtils.decrypt(data, key, iv);
		return (XNetPacket) decryptedData;
	}
}
