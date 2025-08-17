package com.tahraoui.txnet.core;

import com.tahraoui.txnet.model.Connection;
import com.tahraoui.txnet.packet.request.TXNetRequestPacketWriter;

import java.util.List;

public interface TXNetConnection extends TXNetRequestPacketWriter {
	int getId();
	String getUsername();
	void disconnect();
	void disconnect(int id);
	List<Connection> getConnections();
}
