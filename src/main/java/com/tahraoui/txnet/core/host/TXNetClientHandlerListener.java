package com.tahraoui.txnet.core.host;

import com.tahraoui.txnet.packet.request.TXNetRequestPacketWriter;
import com.tahraoui.txnet.packet.response.TXNetResponsePacketReader;

public interface TXNetClientHandlerListener extends TXNetRequestPacketWriter, TXNetResponsePacketReader {
	void onClientConnected(int id);
	void onClientDisconnected(int id);
}
