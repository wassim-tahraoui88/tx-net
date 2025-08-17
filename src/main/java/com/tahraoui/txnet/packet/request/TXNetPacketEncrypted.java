package com.tahraoui.txnet.packet.request;

import com.tahraoui.txnet.packet.TXNetPacket;
import com.tahraoui.txnet.util.TXNetEncryptionUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.Serializable;

public class TXNetPacketEncrypted extends TXNetPacket implements Serializable {

	private final byte[] data;

	public TXNetPacketEncrypted(byte[] data) {
		this.data = data;

	}

	public TXNetPacket decrypt(SecretKey key, IvParameterSpec iv) {
		var decryptedData = TXNetEncryptionUtils.decrypt(data, key, iv);
		return (TXNetPacket) decryptedData;
	}
}
