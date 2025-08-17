package com.tahraoui.xnet.packet;

import com.tahraoui.xnet.core.XNetResponsePacketDispatcher;
import com.tahraoui.xnet.packet.request.XNetRequestPacket;
import com.tahraoui.xnet.packet.response.XNetResponsePacket;

/**
 * The base class for handling {@link XNetRequestPacket request packets}.
 */
public abstract class XNetPacketRequestHandler {
	/**
	 * Handles an incoming {@link XNetRequestPacket request packet}, processes it into a {@link XNetResponsePacket response packet}
	 * and sends it back to the designated client(s) through the {@link XNetResponsePacketDispatcher dispatcher}.
	 * @param packet The {@link XNetRequestPacket request packet} to process.
	 * @param dispatcher The dispatcher delegate to unicast/broadcast packets to clients.
	 */
	public abstract void handle(XNetRequestPacket packet, XNetResponsePacketDispatcher dispatcher);
}
