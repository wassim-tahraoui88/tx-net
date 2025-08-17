package com.tahraoui.xnet.packet.request;

import com.tahraoui.xnet.packet.XNetPacket;
import com.tahraoui.xnet.util.EncryptionUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public abstract class XNetRequestPacket extends XNetPacket {
	public XNetPacketEncrypted encrypt(SecretKey key, IvParameterSpec iv) {
		var encryptedBytes = EncryptionUtils.encrypt(this, key, iv);
		return new XNetPacketEncrypted(encryptedBytes);
	}
}
