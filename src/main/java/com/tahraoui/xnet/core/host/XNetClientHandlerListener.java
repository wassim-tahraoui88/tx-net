package com.tahraoui.xnet.core.host;

import com.tahraoui.xnet.packet.request.XNetRequestPacketWriter;
import com.tahraoui.xnet.packet.response.XNetResponsePacketReader;

public interface XNetClientHandlerListener extends XNetRequestPacketWriter, XNetResponsePacketReader {
	void onClientConnected(int id);
	void onClientDisconnected(int id);
}
