package com.tahraoui.txnet.packet;

import com.tahraoui.txnet.core.TXNetResponsePacketDispatcher;
import com.tahraoui.txnet.packet.request.TXNetRequestPacket;
import com.tahraoui.txnet.packet.response.TXNetResponsePacket;

/**
 * The base class for handling {@link TXNetRequestPacket request packets}.
 */
public abstract class TXNetPacketRequestHandler {
	/**
	 * Handles an incoming {@link TXNetRequestPacket request packet}, processes it into a {@link TXNetResponsePacket response packet}
	 * and sends it back to the designated client(s) through the {@link TXNetResponsePacketDispatcher dispatcher}.
	 * @param packet The {@link TXNetRequestPacket request packet} to process.
	 * @param dispatcher The dispatcher delegate to unicast/broadcast packets to clients.
	 */
	public abstract void handle(TXNetRequestPacket packet, TXNetResponsePacketDispatcher dispatcher);
}
