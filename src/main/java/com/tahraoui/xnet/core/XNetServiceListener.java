package com.tahraoui.xnet.core;

import com.tahraoui.xnet.packet.request.XNetRequestPacketWriter;
import com.tahraoui.xnet.packet.response.XNetResponsePacketReader;

public interface XNetServiceListener extends XNetRequestPacketWriter, XNetResponsePacketReader {
	void onConnected();
	void onDisconnected();
}
