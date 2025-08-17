package com.tahraoui.txnet.packet.request;

import com.tahraoui.txnet.packet.TXNetPacket;
import com.tahraoui.txnet.util.TXNetEncryptionUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public abstract class TXNetRequestPacket extends TXNetPacket {
	public TXNetPacketEncrypted encrypt(SecretKey key, IvParameterSpec iv) {
		var encryptedBytes = TXNetEncryptionUtils.encrypt(this, key, iv);
		return new TXNetPacketEncrypted(encryptedBytes);
	}
}
