package com.tahraoui.xnet.core;

import com.tahraoui.xnet.model.Connection;
import com.tahraoui.xnet.packet.request.XNetRequestPacketWriter;

import java.util.List;

public interface XNetConnection extends XNetRequestPacketWriter {
	int getId();
	String getUsername();
	void disconnect();
	void disconnect(int id);
	List<Connection> getConnections();
}
