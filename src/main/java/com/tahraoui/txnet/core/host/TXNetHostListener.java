package com.tahraoui.txnet.core.host;

import com.tahraoui.txnet.packet.request.TXNetRequestPacketWriter;
import com.tahraoui.txnet.packet.response.TXNetResponsePacketReader;

public interface TXNetHostListener extends TXNetRequestPacketWriter, TXNetResponsePacketReader {
	void onClientConnectionEstablished(int clientId);
	void onClientConnectionTerminated(int clientId);
}
