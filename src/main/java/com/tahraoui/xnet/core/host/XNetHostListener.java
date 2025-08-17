package com.tahraoui.xnet.core.host;

import com.tahraoui.xnet.packet.request.XNetRequestPacketWriter;
import com.tahraoui.xnet.packet.response.XNetResponsePacketReader;

public interface XNetHostListener extends XNetRequestPacketWriter, XNetResponsePacketReader {
	void onClientConnectionEstablished(int clientId);
	void onClientConnectionTerminated(int clientId);
}
