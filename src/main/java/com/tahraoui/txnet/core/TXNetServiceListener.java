package com.tahraoui.txnet.core;

import com.tahraoui.txnet.packet.request.TXNetRequestPacketWriter;
import com.tahraoui.txnet.packet.response.TXNetResponsePacketReader;

public interface TXNetServiceListener extends TXNetRequestPacketWriter, TXNetResponsePacketReader {
	void onConnected();
	void onDisconnected();
}
